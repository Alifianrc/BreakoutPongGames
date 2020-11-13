package com.example.breakout;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;


public class MainActivity extends Activity {

    // This will be the view of the game
    // It will also hold logic of the game
    // And respond to screen touches as well
    BreakoutView breakoutView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize gameView and set it as the view
        breakoutView = new BreakoutView(this);
        setContentView(breakoutView);
    }

    class BreakoutView extends SurfaceView implements Runnable{
        // This will be threat
        Thread gameThread = null;

        // The size of the screen in pixels
        int screenX;
        int screenY;

        // The player's paddle
        Paddle paddle;

        // The Ball
        Ball ball;

        // The Bricks
        // Up to 200 :v
        Brick[] bricks = new Brick[200];
        int numBricks = 0;
        int BrickXValue = 8;
        int BrickYValue = 10;
        int BrickRowCount = 4;

        // For sound FX
        SoundPool soundPool;
        int beep1ID = -1;
        int beep2ID = -1;
        int beep3ID = -1;
        int loseLifeID = -1;
        int explodeID = -1;

        // The score
        int score = 0;
        int addScore = 10;

        // Lives
        int lives = 3;

        // We need surface holder
        // When we use Paint and Canvas in a thread
        // We will see it in action in the draw method soon.
        SurfaceHolder ourHolder;

        // A boolean which we will set and unset
        // when the game is running- or not.
        volatile boolean playing;

        // Game is paused at the start
        boolean paused = true;

        // A Canvas and a Paint object
        Canvas canvas;
        Paint paint;

        // This variable tracks the game frame rate
        long fps;

        // This is used to help calculate the fps
        private long timeThisFrame;

        // Some UI
        String won = "YOU HAVE WON!";
        String lose = "YOU HAVE LOST!";
        String again = "Touch to Play Again";

        // When the we initialize (call new()) on gameView
        // This special constructor method runs
        public BreakoutView(Context context) {
            // The next line of code asks the
            // SurfaceView class to set up our object.
            // How kind.
            super(context);

            // Initialize ourHolder and paint objects
            ourHolder = getHolder();
            paint = new Paint();

            // Get a Display object to access screen details
            Display display = getWindowManager().getDefaultDisplay();
            // Load the resolution into a Point object
            Point size = new Point();
            display.getSize(size);
            // Save the screen size
            screenX = size.x;
            screenY = size.y;

            // Initiate Paddle class
            paddle = new Paddle(screenX, screenY);

            // Create a ball
            ball = new Ball(screenX, screenY);

            // Load the sounds
            // This SoundPool is deprecated but don't worry
            soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC,0);

            try{
                // Create objects of the 2 required classes
                AssetManager assetManager = context.getAssets();
                AssetFileDescriptor descriptor;

                // Load our fx in memory ready for use
                descriptor = assetManager.openFd("beep1.ogg");
                beep1ID = soundPool.load(descriptor, 0);

                descriptor = assetManager.openFd("beep2.ogg");
                beep2ID = soundPool.load(descriptor, 0);

                descriptor = assetManager.openFd("beep3.ogg");
                beep3ID = soundPool.load(descriptor, 0);

                descriptor = assetManager.openFd("loseLife.ogg");
                loseLifeID = soundPool.load(descriptor, 0);

                descriptor = assetManager.openFd("explode.ogg");
                explodeID = soundPool.load(descriptor, 0);

            }catch(IOException e){
                // Print an error message to the console
                Log.e("error", "failed to load sound files");
            }

            // Setting all object
            createBricksAndRestart();
        }

        public void createBricksAndRestart(){

            // Put the ball back to the start
            ball.reset(screenX, screenY, paddle.GetPaddleHeiht());

            // Put the Paddle Back to the start
            paddle.RepositioningPaddle(screenX, screenY);

            // Brick size based on Screen size
            int brickWidth = screenX / BrickXValue;
            int brickHeight = screenY / BrickYValue;

            // Build a wall of bricks
            numBricks = 0;

            for(int column = 0; column < BrickXValue; column ++ ){
                for(int row = 0; row < BrickRowCount; row ++ ){
                    bricks[numBricks] = new Brick(row, column, brickWidth, brickHeight);
                    numBricks ++;
                }
            }

            // Reset scores and lives
            score = 0;
            lives = 3;
        }

        @Override
        public void run() {
            while (playing) {

                // Capture the current time in milliseconds in startFrameTime
                long startFrameTime = System.currentTimeMillis();

                // Update the frame
                // Update the frame
                if(!paused){
                    update();
                }

                // Draw the frame
                draw();

                // Calculate the fps this frame
                // We can then use the result to
                // time animations and more.
                timeThisFrame = System.currentTimeMillis() - startFrameTime;
                if (timeThisFrame >= 1) {
                    fps = 1000 / timeThisFrame;
                }

            }

        }

        // Everything that needs to be updated goes in here
        // Movement, collision detection etc.
        public void update() {
            // Move the paddle if required
            paddle.update(fps);

            // Update the Ball
            ball.update(fps);

            // Check for ball colliding with a brick
            for(int i = 0; i < numBricks; i++){
                // Check which Brick is still active
                if (bricks[i].getVisibility()){
                    // Check for collision
                    if(RectF.intersects(bricks[i].getRect(),ball.getRect())) {
                        bricks[i].setInvisible();
                        ball.reverseYVelocity();
                        score = score + addScore;
                        soundPool.play(explodeID, 1, 1, 0, 0, 1);
                    }
                }
            }

            // Check for ball colliding with paddle
            if(RectF.intersects(paddle.getRect(),ball.getRect())) {
                ball.setRandomXVelocity();
                ball.reverseYVelocity();
                ball.clearObstacleY(paddle.getRect().top - 2);
                soundPool.play(beep1ID, 1, 1, 0, 0, 1);
            }

            // Bounce the ball back when it hits the bottom of screen
            // And deduct a life
            if(ball.getRect().bottom > screenY){
                // Turning Ball direction in y axis and clear Path
                ball.reverseYVelocity();
                ball.clearObstacleY(screenY - 2);

                // Lose a life
                lives --;
                soundPool.play(loseLifeID, 1, 1, 0, 0, 1);

                // Game over
                if(lives == 0){
                    paused = true;
                    //createBricksAndRestart();
                }
            }

            // Bounce the ball back when it hits the top of screen
            if(ball.getRect().top < 0){
                ball.reverseYVelocity();
                // We need to add Ball size because
                // This function works on the bottom of the Ball
                ball.clearObstacleY(ball.GetBallSizeY() + 2);
                soundPool.play(beep2ID, 1, 1, 0, 0, 1);
            }

            // If the ball hits left wall bounce
            if(ball.getRect().left < 0){
                ball.reverseXVelocity();
                ball.clearObstacleX(2);
                soundPool.play(beep3ID, 1, 1, 0, 0, 1);
            }

            // If the ball hits right wall bounce
            if(ball.getRect().right > screenX - ball.GetBallSizeX()){
                ball.reverseXVelocity();
                ball.clearObstacleX(screenX - ((ball.GetBallSizeX() * 2) + 2));
                soundPool.play(beep3ID, 1, 1, 0, 0, 1);
            }

            // Pause if cleared screen
            if(score == numBricks * addScore){
                paused = true;
                //createBricksAndRestart();
            }
        }

        // Draw the newly updated scene
        public void draw() {

            // Make sure our drawing surface is valid or we crash
            if (ourHolder.getSurface().isValid()) {
                // Lock the canvas ready to draw
                canvas = ourHolder.lockCanvas();

                // Draw the background color
                canvas.drawColor(Color.argb(255,  230, 145, 145));

                // Choose the brush color for drawing Paddle and Ball
                paint.setColor(Color.argb(255,  182, 218, 242));
                // Draw the paddle
                canvas.drawRect(paddle.getRect(), paint);
                // Draw the ball
                canvas.drawRect(ball.getRect(), paint);

                // Draw the bricks
                // Change the brush color for drawing Brick
                paint.setColor(Color.argb(255,  66, 199, 110));
                // Draw the bricks if visible
                for(int i = 0; i < numBricks; i++){
                    if(bricks[i].getVisibility()) {
                        canvas.drawRect(bricks[i].getRect(), paint);
                    }
                }

                // Draw the HUD
                // Choose the brush color for drawing Score
                paint.setColor(Color.argb(255,  255, 255, 255));

                // Draw the score
                paint.setTextSize(60);
                canvas.drawText("Score: " + score + "   Lives: " + lives, 10,50, paint);

                // Has the player cleared the screen?
                if(score == numBricks * addScore){
                    paint.setTextSize(100);
                    canvas.drawText(won, screenX / 4,screenY / 2, paint);
                    paint.setTextSize(60);
                    canvas.drawText(again, (screenX / 4) + 50, (screenY / 2) + 90, paint);
                }

                // Has the player lost?
                if(lives <= 0){
                    paint.setTextSize(100);
                    canvas.drawText(lose, screenX / 4,screenY / 2, paint);
                    paint.setTextSize(60);
                    canvas.drawText(again, (screenX / 4) + 50, (screenY / 2) + 90, paint);
                }

                // Draw everything to the screen
                ourHolder.unlockCanvasAndPost(canvas);
            }

        }


        // If SimpleGameEngine Activity is paused/stopped
        // shutdown our thread.
        public void pause() {
            playing = false;
            try {
                gameThread.join();
            } catch (InterruptedException e) {
                Log.e("Error:", "joining thread");
            }

        }

        // If SimpleGameEngine Activity is started theb
        // start our thread.
        public void resume() {
            playing = true;
            gameThread = new Thread(this);
            gameThread.start();
        }

        // The SurfaceView class implements onTouchListener
        // So we can override this method and detect screen touches.
        @Override
        public boolean onTouchEvent(MotionEvent motionEvent) {

            switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {

                // Player has touched the screen
                case MotionEvent.ACTION_DOWN:

                    if(paused){
                        paused = false;
                        createBricksAndRestart();
                    }

                    // This will detect where user touch the screen
                    // Right or Left
                    if(motionEvent.getX() > screenX / 2){
                        paddle.setMovementState(paddle.RIGHT);
                    }
                    else{
                        paddle.setMovementState(paddle.LEFT);
                    }

                    break;

                // Player has removed finger from screen
                case MotionEvent.ACTION_UP:
                    // This will detect if user unTouch the screen
                    paddle.setMovementState(paddle.STOPPED);

                    break;
            }
            return true;
        }
    }
    //This is the end of our BreakoutView inner class

    // This method executes when the player starts the game
    @Override
    protected void onResume() {
        super.onResume();

        // Tell the gameView resume method to execute
        breakoutView.resume();
    }

    // This method executes when the player quits the game
    @Override
    protected void onPause() {
        super.onPause();

        // Tell the gameView pause method to execute
        breakoutView.pause();
    }

} // This is the end of the BreakoutGame class
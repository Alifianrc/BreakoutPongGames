package com.example.breakout;

import android.graphics.RectF;

import java.util.Random;

public class Ball {
    // Ball Position
    RectF rect;

    // Ball speed
    float xVelocity;
    float yVelocity;

    // Ball size
    float ballWidth = 15;
    float ballHeight = 15;

    // The Constructor
    public Ball(int screenX, int screenY){

        // Start the ball travelling straight up at 100 pixels per second
        xVelocity = 200;
        yVelocity = -400;

        // Place the ball in the centre of the screen at the bottom
        // Make it a 10 pixel x 10 pixel square
        rect = new RectF();
    }

    // Getter Method
    public RectF getRect(){
        return rect;
    }

    // Update Method
    public void update(long fps){
        rect.left = rect.left + (xVelocity / fps);
        rect.top = rect.top + (yVelocity / fps);
        rect.right = rect.left + ballWidth;
        rect.bottom = rect.top - ballHeight;
    }

    // Bounce Ball in y direction
    public void reverseYVelocity(){
        yVelocity = -yVelocity;
    }

    // Bounce Ball in x direction
    public void reverseXVelocity(){
        xVelocity = - xVelocity;
    }

    // Random bounce after collision with Paddle
    public void setRandomXVelocity(){
        Random generator = new Random();
        int answer = generator.nextInt(2);

        if(answer == 0){
            reverseXVelocity();
        }
    }

    // Clear obstacle to not get Stuck in y Direction
    public void clearObstacleY(float y){
        rect.bottom = y;
        rect.top = y - ballHeight;
    }
    // Clear obstacle to not get Stuck in x Direction
    public void clearObstacleX(float x){
        rect.left = x;
        rect.right = x + ballWidth;
    }

    // Reset Ball Position
    public void reset(int x, int y, float paddleHeight){
        rect.left = x / 2;
        rect.top = y - (paddleHeight + 2);
        rect.right = x / 2 + ballWidth;
        rect.bottom = y - (paddleHeight + 2) - ballHeight;
    }

    // Get Ball Size
    public float GetBallSizeY() {
        return ballHeight;
    }

    // Get Ball Size
    public float GetBallSizeX() {
        return ballWidth;
    }
}

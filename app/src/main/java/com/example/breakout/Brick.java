package com.example.breakout;

import android.graphics.RectF;

public class Brick {
    // Brick Position
    private RectF rect;

    // Brick visibility (Destroyed or not)
    private boolean isVisible;

    // The Constructor
    public Brick(int row, int column, int width, int height){
        // Set Brick to Visible
        isVisible = true;

        // Give a linear
        int padding = 1;

        // Set Brick Position by row and column
        rect = new RectF(column * width + padding,
                        row * height + padding,
                        column * width + width - padding,
                        row * height + height - padding);
    }

    // Brick Getter
    public RectF getRect(){
        return this.rect;
    }

    // Set Brick to Invisible
    public void setInvisible(){
        isVisible = false;
    }

    // Get visibility status of the Brick
    public boolean getVisibility(){
        return isVisible;
    }

}

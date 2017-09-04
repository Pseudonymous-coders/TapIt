package org.pseudonymous.tapit.components;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import org.pseudonymous.tapit.engine.Engine;

/**
 * Created by smerkous on 9/4/17.
 */

public class Circle {
    private int x, y, r, c, pW, pH;
    private float sX, sY, sR;

    public Circle(int c) {
        this.c = c;
    }

    public void setParentDims(int width, int height) {
        this.pW = width;
        this.pH = height;
    }

    public void inheritParentAttributes(Engine engine) {
        this.setParentDims(engine.getWidth(), engine.getHeight());
    }

    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void setScaledPosition(float x, float y) {
        this.sX = x;
        this.sY = y;
        this.x = (int) ((float) this.pW * x);
        this.y = (int) ((float) this.pH * y);
    }

    public void setRadius(int r) {
        this.r = r;
    }

    public void setScaledRadius(float r) {
        this.sR = r;
        this.r = (int) ((float) ((this.pH > this.pW) ? this.pW : this.pH) * (r / 2)); //Make sure we stick to the bounds of our screen
    }

    public int getXPosition() {
        return this.x;
    }

    public int getYPosition() {
        return this.y;
    }

    public int getRadius() {
        return this.r;
    }

    public float getScaledXPosition() {
        return this.sX;
    }

    public float getScaledYPosition() {
        return this.sY;
    }

    public float getScaledRadius() {
        return this.sR;
    }

    public void setColor(int c) {
        this.c = c;
    }

    public void drawToCanvas(Canvas canvas) {
        Paint p = new Paint();
        p.setStyle(Paint.Style.FILL);
        p.setColor(this.c);
        canvas.drawCircle(this.x, this.y, this.r, p);
    }
}

package org.pseudonymous.tapit.configs;

public class CircleProps {
    private int c, w;
    private float r;

    public CircleProps(int c, float r, int w) {
        this.c = c;
        this.r = r;
    }

    public void setColor(int c) {
        this.c = c;
    }

    public void setRadius(float r) {
       this.r = r; 
    }

    public void setWait(int w) {
        this.w = w;
    }

    public int getColor() {
        return this.c;
    }

    public float getRadius() {
        return this.r;
    }

    public int getWait() {
        return this.w;
    }
}

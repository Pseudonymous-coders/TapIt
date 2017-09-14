package org.pseudonymous.tapit.components;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Looper;
import android.view.animation.AccelerateInterpolator;

import org.pseudonymous.tapit.configs.Logger;
import org.pseudonymous.tapit.engine.Engine;

/**
 * Created by smerkous on 9/4/17.
 *
 */

public class Circle {
    private int x, y, r, c, pW, pH;
    private long circleId = 0;
    private float sX, sY, sR;
    private ValueAnimator out, in;
    private boolean clickedOn = false;

    public interface CircleEvents {
        void onClick(Circle circle);
        void onDestroyed(Circle circle);
    }

    private CircleEvents circleEvents;

    public Circle(int c) {
        this.c = c;
    }

    public Circle(Engine engine, int c) {
        this.inheritParentAttributes(engine);
        this.c = c;
    }

    public void setParentDims(int width, int height) {
        this.pW = width;
        this.pH = height;
    }

    public void setCircleId(long id) {
        this.circleId = id;
    }

    public long getCircleId() {
        return this.circleId;
    }

    public void inheritParentAttributes(Engine engine) {
        this.setParentDims(engine.getWidth(), engine.getHeight());
    }

    public void setCircleEvents(CircleEvents events) {
        this.circleEvents = events;
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

    public float distanceFrom(Circle otherCircle) {
        int dX = Math.abs(otherCircle.getXPosition() - this.getXPosition());
        int dY = Math.abs(otherCircle.getYPosition() - this.getYPosition());
        return Math.sqrt(Math.pow(dX, 2) + Math.pow(dY, 2));
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

    public void emitTouchEvent(float xTouch, float yTouch) {
        double distanceFromCenter = Math.sqrt(Math.pow(Math.abs(this.x - xTouch), 2) + Math.pow(Math.abs(this.y - yTouch), 2));
        if((int) distanceFromCenter < this.r && this.circleEvents != null) {
            this.circleEvents.onClick(this);
            clickedOn = true;
        }
    }

    public void startAnimation(float maxScaledRadius, int waitDuration) {
        this.startAnimation(maxScaledRadius, 300, waitDuration, 300);
    }

    public void startAnimation(float maxScaledRadius, int outDuration, final int waitDuration, int inDuration) {
        out = ValueAnimator.ofFloat(this.getScaledRadius(), maxScaledRadius);
        in = ValueAnimator.ofFloat(maxScaledRadius, 0);

        ValueAnimator.AnimatorUpdateListener updateListener = new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float value = (float) valueAnimator.getAnimatedValue();
                setScaledRadius(value); //Update the scaled radius to hit the max value
            }
        };

        out.addUpdateListener(updateListener);
        out.setInterpolator(new AccelerateInterpolator());
        out.setDuration(outDuration);

        in.addUpdateListener(updateListener);
        in.setInterpolator(new AccelerateInterpolator());
        in.setDuration(inDuration);

        out.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                Thread waitThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(waitDuration);
                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    in.start();
                                }
                            });
                        } catch (InterruptedException ignored) {}
                    }
                });
                waitThread.setDaemon(true);
                waitThread.setName("AnimationWaitThread");
                waitThread.start();
            }
        });

        final Circle _circle = this;

        in.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                if(circleEvents != null && !clickedOn) circleEvents.onDestroyed(_circle);
            }
        });

        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Logger.Log("STARTING THE ANIMATION");
                out.start();
            }
        });
    }

    public void drawToCanvas(Canvas canvas) {
        Paint p = new Paint();
        p.setStyle(Paint.Style.FILL);
        p.setColor(this.c);
        canvas.drawCircle(this.x, this.y, this.r, p);
    }
}

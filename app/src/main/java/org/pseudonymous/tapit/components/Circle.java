package org.pseudonymous.tapit.components;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Looper;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.LinearInterpolator;

import org.pseudonymous.tapit.configs.Logger;
import org.pseudonymous.tapit.engine.Engine;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by smerkous on 9/4/17.
 *
 */

public class Circle {
    private int x, y, r, c, pW, pH, mR;
    private long circleId = 0, startTime = -1, lifeSpan = -1;
    private float sX, sY, sR, sMR;
    private ValueAnimator out, in;
    private boolean
            clickedOn = false,
            render = true,
            animating = false;

    public interface CircleEvents {
        void onClick(Circle circle);
        void onDestroyed(Circle circle);
        void onCleanup(Circle circle);
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

    public float getPaddingWidth(float sidePadding) {
        return (this.pW * sidePadding) + (float) this.mR;
    }

    public float getPaddingHeight(float sidePadding) {
        return (this.pH * sidePadding) + (float) this.mR;
    }

    public float getAvailableWidth(float paddingWidth) {
        return (this.pW - (2 * paddingWidth));
    }

    public float getAvailableHeight(float paddingHeight) {
        return (this.pH - (2 * paddingHeight));
    }

    public void setRandomLocation(float sidePadding) {
        float rX = ThreadLocalRandom.current().nextFloat();
        float rY = ThreadLocalRandom.current().nextFloat();
        float paddingWidth = this.getPaddingWidth(sidePadding);
        float paddingHeight = this.getPaddingHeight(sidePadding);
        float maxWidth = this.getAvailableWidth(paddingWidth);
        float maxHeight = this.getAvailableHeight(paddingHeight);
        this.sX = rX;
        this.sY = rY;
        this.x = (int) (maxWidth * rX) + (int) paddingWidth;
        this.y = (int) (maxHeight * rY) + (int) paddingHeight;
    }

    public void setRadius(int r) {
        this.r = r;
    }

    public void setScaledRadius(float r) {
        this.sR = r;
        if(r == 0.0f) {
            render = false;
            return;
        }
        render = true;
        this.r = (int) ((float) ((this.pH > this.pW) ? this.pW : this.pH) * (r / 2)); //Make sure we stick to the bounds of our screen
    }

    public void setMaxScaledRadius(float r) {
        this.sMR = r;
        this.mR = (int) ((float) ((this.pH > this.pW) ? this.pW : this.pH) * (r / 2)); //Make sure we stick to the bounds of our screen
    }

    public float distanceFrom(Circle otherCircle) {
        int dX = Math.abs(otherCircle.getXPosition() - this.getXPosition());
        int dY = Math.abs(otherCircle.getYPosition() - this.getYPosition());
        return (float) Math.sqrt(Math.pow(dX, 2) + Math.pow(dY, 2));
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

    public int getMaxRadius() {
        return this.mR;
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
        if(clickedOn) return; //Skip the touch event if the circle has already been clicked on
        double distanceFromCenter = Math.sqrt(Math.pow(Math.abs(this.x - xTouch), 2) + Math.pow(Math.abs(this.y - yTouch), 2));
        if((int) distanceFromCenter < this.r && this.circleEvents != null) {
            //Start the in animation if it hasn't already been started
            if(!in.isStarted() && !in.isRunning()) {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        in.start();
                    }
                });
            }
            this.circleEvents.onClick(this);
            clickedOn = true;
        }
    }

    public void startAnimation(float maxScaledRadius, int waitDuration) {
        this.startAnimation(maxScaledRadius, 300, waitDuration, 300);
    }

    public void startAnimation(float maxScaledRadius, int outDuration, final int waitDuration, int inDuration) {
        startTime = System.currentTimeMillis();
        lifeSpan = outDuration + waitDuration + inDuration + 200;
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
        out.setInterpolator(new LinearInterpolator()); //new AccelerateInterpolator());
        out.setDuration(outDuration);

        in.addUpdateListener(updateListener);
        in.setInterpolator(new LinearInterpolator()); //new AccelerateInterpolator());
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
                            if(!in.isStarted() && !in.isRunning() && !clickedOn) {
                                new Handler(Looper.getMainLooper()).post(new Runnable() {
                                    @Override
                                    public void run() {
                                        in.start();
                                    }
                                });
                            }
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
                animating = false;
                if(circleEvents != null && !clickedOn) circleEvents.onDestroyed(_circle);
                else if(circleEvents != null) circleEvents.onCleanup(_circle);
            }
        });

        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                animating = true;
                out.start();
            }
        });
    }

    public boolean isAnimating() {
        return this.animating;
    }

    public boolean isDead() {
        if(startTime == -1 || lifeSpan == -1) return false;
        return (System.currentTimeMillis() - startTime) <= lifeSpan;
    }

    public void drawToCanvas(Canvas canvas) {
        if(!render) return; //Don't render the circle if the radius is zero
        Paint p = new Paint();
        p.setStyle(Paint.Style.FILL);
        p.setColor(this.c);
        canvas.drawCircle(this.x, this.y, this.r, p);
    }
}

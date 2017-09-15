package org.pseudonymous.tapit.components;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Looper;
import android.view.animation.LinearInterpolator;

import org.pseudonymous.tapit.engine.Engine;

import java.util.concurrent.ThreadLocalRandom;

/**
 *
 * Created by smerkous on 9/15/17.
 */

public class SplashText {
    private int x, y, s, c, tW, tH, pW, pH;
    private float sX, sY, sS, tS = 48f;
    private ValueAnimator out, in;
    private boolean render = true;
    private Paint paint = new Paint();
    private String t = "";

    public SplashText(String t, int c) {
        this.t = t;
        this.c = c;
        this.paint.setStyle(Paint.Style.FILL);
        this.paint.setColor(this.c);
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

    public void setScaledSize(float s) {
        this.sS = s;
        if(s == 0.0f) {
            render = false;
            return;
        }
        render = true;
        this.s = (int) (this.pW * s); //Make sure we stick to the bounds of our screen

        paint.setTextSize(this.tS);
        Rect bounds = new Rect();
        paint.getTextBounds(this.t, 0, this.t.length(), bounds);

        this.tW = bounds.width();
        this.tH = bounds.height();

        this.tS = this.tS * this.s  / this.tW;

        paint.setTextSize(this.tS);
    }

    public int getXPosition() {
        return this.x;
    }

    public int getYPosition() {
        return this.y;
    }

    public int getSize() {
        return this.s;
    }

    public float getScaledXPosition() {
        return this.sX;
    }

    public float getScaledYPosition() {
        return this.sY;
    }

    public float getScaledSize() {
        return this.sS;
    }

    public void setColor(int c) {
        this.c = c;
    }

    public void startAnimation(float maxScaledRadius, int waitDuration) {
        this.startAnimation(maxScaledRadius, 300, waitDuration, 300);
    }

    public void startAnimation(float maxScaledSize, int outDuration, final int waitDuration, int inDuration) {
        out = ValueAnimator.ofFloat(this.getScaledSize(), maxScaledSize);
        in = ValueAnimator.ofFloat(maxScaledSize, 0);

        ValueAnimator.AnimatorUpdateListener updateListener = new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float value = (float) valueAnimator.getAnimatedValue();
                setScaledSize(value); //Update the scaled radius to hit the max value
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
                            if(!in.isStarted() && !in.isRunning()) {
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

        in.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
            }
        });

        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                out.start();
            }
        });
    }

    public void drawToCanvas(Canvas canvas) {
        if(!render) return; //Don't render the circle if the radius is zero
        canvas.drawText(this.t, 0, this.t.length(), this.x - (this.tW / 2), this.y + (this.tH / 2), this.paint);
    }
}

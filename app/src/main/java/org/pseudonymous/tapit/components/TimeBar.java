package org.pseudonymous.tapit.components;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

import org.pseudonymous.tapit.R;
import org.pseudonymous.tapit.configs.Configs;

/**
 *
 * Created by smerkous on 9/17/17.
 */

public class TimeBar extends View {
    private boolean constructed = false;
    private int color, slide_to;
    private Paint barPaint;
    private float percent = 100.0f;
    private float drawWidth;
    private ValueAnimator animator;

    public TimeBar(Context context) {
        super(context);
    }

    public TimeBar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.pseudoConstructor(context, attrs, 0, 0);
    }

    public TimeBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.pseudoConstructor(context, attrs, defStyleAttr, 0);
    }

    public TimeBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.pseudoConstructor(context, attrs, defStyleAttr, defStyleRes);
    }

    private void pseudoConstructor(Context context, @Nullable  AttributeSet attrs, int defStyle, int defStyleRes) {
        if(this.constructed) return;
        TypedArray arr = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.TimeBar,
                defStyle, defStyleRes
        );

        try {
            this.color = arr.getColor(R.styleable.TimeBar_bar_color, Configs.getColor(R.color.green, this.getContext()));
            this.slide_to= arr.getInteger(R.styleable.TimeBar_slide_to, 0);
        } finally {
            arr.recycle();
        }
        this.barPaint = new Paint();
        this.barPaint.setStyle(Paint.Style.FILL);
        this.barPaint.setColor(this.color);
        this.constructed = true;
    }

    public void setColor(int color) {
        this.color = color;
        this.barPaint.setColor(this.color);
    }

    public int getColor() {
        return this.color;
    }

    public void setPercent(float percent) {
        this.percent = percent;
        this.drawWidth = this.percent * this.getWidth();
    }

    private void attemptToStopAnimation() {
        if(this.animator != null && (this.animator.isRunning() || this.animator.isStarted())) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    animator.cancel();
                }
            });
        }
    }

    public void startAnimation(long duration) {
        attemptToStopAnimation();

        switch (this.slide_to) {
            case 0: //Slide to the left
                this.animator = ValueAnimator.ofFloat(this.getWidth(), 0);
                break;
            case 1: //Slide to the right
                this.animator = ValueAnimator.ofFloat(0, this.getWidth());
                break;
        }

        this.animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                drawWidth = (float) valueAnimator.getAnimatedValue();
                invalidate();
            }
        });

        this.animator.setInterpolator(new LinearInterpolator());
        this.animator.setDuration(duration);

        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                animator.start();
            }
        });
    }

    @Override
    protected void onDraw(Canvas c) {
        c.drawRect(0.0f, 0.0f, this.drawWidth, this.getHeight(), this.barPaint);
    }
}

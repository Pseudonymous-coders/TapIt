package org.pseudonymous.tapit.components;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.PaintDrawable;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;

import org.pseudonymous.tapit.configs.Logger;

public class SettingsButton extends android.support.v7.widget.AppCompatButton {
    private volatile int buttonState = 0;
    private int color;
    private Paint buttonPaint = new Paint();

    public interface AnimationCallbacks {
        void onAnimationEnd();
    }

    private AnimationCallbacks animationCallbacks = null;

    public SettingsButton(Context context) {
        super(context);
        pseudoConstructor();
    }

    public SettingsButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        pseudoConstructor();
    }

    public SettingsButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        pseudoConstructor();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawRect(0, 0, getWidth() * 1.2f, getHeight(), buttonPaint);
        super.onDraw(canvas);
    }

    public void pseudoConstructor() {
        buttonPaint = new Paint();
        buttonPaint.setStyle(Paint.Style.FILL);
    }

    public void setAnimationCallbacks(AnimationCallbacks animationCallbacks) {
        this.animationCallbacks = animationCallbacks;
    }

    public void setBackgroundColor(int color) {
        this.color = color;
        this.buttonPaint.setColor(this.color);
    }

    public void downAnimation() {
        if(buttonState != 0) return;
        final int buttonHeight = this.getHeight();
        final ValueAnimator downAnimator = ValueAnimator.ofFloat(0, buttonHeight);

        downAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float value = (float) valueAnimator.getAnimatedValue();
                setTranslationY(value);
            }
        });
        downAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                if(animationCallbacks != null) animationCallbacks.onAnimationEnd();
            }
        });

        downAnimator.start();
        downAnimator.setInterpolator(new LinearInterpolator());
        buttonState = 1;
    }

    public void upAnimation(){
        if(buttonState != 1) return;
        final int buttonHeight = this.getHeight();
        final ValueAnimator upAnimator = ValueAnimator.ofFloat(buttonHeight, 0);

        upAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float value = (float) valueAnimator.getAnimatedValue();
                setTranslationY(value);
            }
        });
        upAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
            }
        });

        upAnimator.start();
        upAnimator.setInterpolator(new LinearInterpolator());
        buttonState = 0;
    }

}

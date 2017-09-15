package org.pseudonymous.tapit.components;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.animation.LinearInterpolator;

public class StartButton extends android.support.v7.widget.AppCompatButton {
    public interface AnimationCallbacks {
        void onAnimationEnd();
    }

    private AnimationCallbacks animationCallbacks = null;

    public StartButton(Context context) {
        super(context);
    }

    public StartButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public StartButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setAnimationCallbacks(AnimationCallbacks animationCallbacks) {
        this.animationCallbacks = animationCallbacks;
    }

    public void startAnimation() {
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
    }

}

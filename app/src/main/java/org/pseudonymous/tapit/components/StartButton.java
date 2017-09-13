package org.pseudonymous.tapit.components;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.animation.LinearInterpolator;

/**
 *
 * Created by smerkous on 9/12/17.
 */

public class StartButton extends android.support.v7.widget.AppCompatButton {
    public StartButton(Context context) {
        super(context);
    }
    public StartButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public StartButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void startAnimation() {
        final ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, -300f);
        final ValueAnimator downAnimator = ValueAnimator.ofFloat(-300f, 600f);

        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float value = (float) valueAnimator.getAnimatedValue();
                setTranslationY(value);
            }
        });

        downAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float value = (float) valueAnimator.getAnimatedValue();
                setTranslationY(value);
            }
        });

        valueAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                downAnimator.start();
            }
        });

        valueAnimator.setInterpolator(new LinearInterpolator());
        valueAnimator.setDuration(300);

        downAnimator.setInterpolator(new LinearInterpolator());
        valueAnimator.setDuration(1500);

        valueAnimator.start();
    }
}

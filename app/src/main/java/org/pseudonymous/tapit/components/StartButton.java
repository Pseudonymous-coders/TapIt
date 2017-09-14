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
    public int activityWidth;
    public int activityHeight;
    public StartButton(Context context) {
        super(context);
    }

    public StartButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public StartButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void inherit(Activity activity) {
        Point size = new Point();
        activity.getWindowManager().getDefaultDisplay().getSize(size);
        this.activityHeight = size.y;
        this.activityWidth = size.x;
    }

    public void startAnimation() {
        final int buttonWidth = this.getWidth();
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
                // This is where we will start GameEngine
            }
        });

        downAnimator.start();
        downAnimator.setInterpolator(new LinearInterpolator());
    }

}

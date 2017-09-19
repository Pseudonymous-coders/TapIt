package org.pseudonymous.tapit.components;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Interpolator;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RotateDrawable;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.LinearInterpolator;

import org.pseudonymous.tapit.configs.Logger;

/**
 *
 * Created by smerkous on 9/17/17.
 */

public class DraggableImageButton extends android.support.v7.widget.AppCompatImageButton implements View.OnTouchListener{
    private float curY;
    private ValueAnimator animator;
    private float minAutoPullDown = 0.6f, bottomLocation = 0.90f;
    private int dragState = 0, windowHeight, downTint, upTint;
    private boolean downDirection = true, stayedAtHome = false;
    private Matrix rotateMatrix = new Matrix();
    private Bitmap buttonImage;

    public interface PullDownEvents {
        void returned(boolean downDirection);
        int started(boolean downDirection);
        void moved(float displacement, boolean downDirection);
        void opened(boolean downDirection);
    }

    private PullDownEvents pullDownEvents = null;

    public DraggableImageButton(Context context) {
        super(context);
        pseudoConstructor();
    }

    public DraggableImageButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        pseudoConstructor();
    }

    public DraggableImageButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        pseudoConstructor();
    }

    public void pseudoConstructor() {
        this.setFocusable(true);
        this.setFocusableInTouchMode(true);
        this.bringToFront();
        this.setOnTouchListener(this);
        this.setScaleType(ScaleType.MATRIX);
    }

    public void setPullDownEvents(PullDownEvents pullDownEvents) {
        this.pullDownEvents = pullDownEvents;
    }

    public void setMinAutoPullDown(float minAutoPullDown) {
        this.minAutoPullDown = minAutoPullDown;
    }

    public void setBottomLocation(float bottomLocation) {
        this.bottomLocation = bottomLocation;
    }

    public void setButtonImage(Bitmap buttonImage) {
        this.buttonImage = buttonImage;
    }

    public void setTints(int downTint, int upTint) {
        this.downTint = downTint;
        this.upTint = upTint;
    }

    private void setDownTint() {
        this.setColorFilter(this.downTint);
    }

    private void setUpTint() {
        this.setColorFilter(this.upTint);
    }

    private Bitmap rotateImage(Bitmap source, float angle) {
        if(source == null) {
            Logger.Log("Failed to rotate image! (Reason: Source image is null)");
            return null;
        }
        rotateMatrix.setRotate(angle, source.getWidth() / 2, source.getHeight() / 2);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), rotateMatrix, true);
    }

    private void imageDown() {
        this.setImageBitmap(this.rotateImage(this.buttonImage, 0f));
    }

    private void imageUp() {
        this.setImageBitmap(this.rotateImage(this.buttonImage, 180f));
    }

    @Override
    public boolean performClick() {
        super.performClick();
        return false;
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        if(dragState == 1) return true;

        int action = motionEvent.getActionMasked();
        float rawY = motionEvent.getRawY();

        switch (action) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN: {
                curY = rawY;
                this.stayedAtHome = true;
                if (pullDownEvents != null) windowHeight = pullDownEvents.started(downDirection);
                break;
            }

            case MotionEvent.ACTION_MOVE: {
                float disp = (rawY - curY);

                if(disp > (this.windowHeight * (this.minAutoPullDown / 2))) {
                    this.stayedAtHome = false;
                }

                if(downDirection) {
                    setTranslationY(disp);
                } else {
                    setTranslationY((this.windowHeight * this.bottomLocation) + disp);
                    disp = windowHeight + disp;
                }
                if(pullDownEvents != null) pullDownEvents.moved(disp, downDirection);
                break;
            }

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP: {
                if(stayedAtHome) {
                    Logger.Log("The menu button was clicked on!");
                    if(downDirection) {
                        this.slideDown();
                    } else {
                        this.slideUp();
                    }
                    return true;
                }

                if(downDirection) {
                    if (rawY > (windowHeight * minAutoPullDown)) {
                        this.slideDown();
                    } else {
                        this.slideUp();
                    }
                } else {
                    if (rawY < windowHeight - (windowHeight * minAutoPullDown)) {
                        this.slideUp();
                    } else {
                        this.slideDown();
                    }
                }
                break;
            }
        }
        return true;
    }

    private void stopAnimator() {
        if(this.animator != null && (this.animator.isRunning() || this.animator.isStarted())) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    animator.cancel();
                }
            });
        }
    }

    private void startAnimator() {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                animator.start();
            }
        });
    }

    private void setSlideAnimationProps() {
        this.setAnimtorProps(1500, new AccelerateDecelerateInterpolator());
    }

    private void setButtonAnimationProps() {
        this.setAnimtorProps(800, new LinearInterpolator());
    }

    private void setAnimtorProps(long duration, TimeInterpolator interpolator) {
        this.animator.setInterpolator(interpolator);
        this.animator.setDuration(duration);
    }

    public void slideUp() {
        this.stopAnimator();
        final float smallDiscrepancyFix = windowHeight * (1 - this.bottomLocation);
        this.animator = ValueAnimator.ofFloat(this.getTranslationY() , -smallDiscrepancyFix);
        this.setSlideAnimationProps();

        this.animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float disp = (float) valueAnimator.getAnimatedValue();
                setTranslationY(disp);
                if(pullDownEvents != null) pullDownEvents.moved(disp + smallDiscrepancyFix, downDirection);
            }
        });

        this.animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                dragState = 0;
                setDownTint();
                imageDown();

                boolean tempDirection = downDirection;

                downDirection = true;

                if(!tempDirection) {
                    slideToFirstHome();
                }
                if(pullDownEvents != null) pullDownEvents.returned(downDirection);
            }
        });

        this.startAnimator();
        dragState = 1;
    }

    public void slideDown() {
        this.stopAnimator();
        this.animator = ValueAnimator.ofFloat(this.getTranslationY(), (windowHeight == 0) ? curY : windowHeight);
        this.setSlideAnimationProps();

        this.animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float disp = (float) valueAnimator.getAnimatedValue();
                setTranslationY(disp);
                if(pullDownEvents != null) pullDownEvents.moved(disp, downDirection);
            }
        });

        this.animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                dragState = 1;
                if(downDirection) {
                    slideToSecondHome();
                } else {
                    slideToFirstHome();
                }

                downDirection = false;
                setUpTint();
                imageUp();

                if(pullDownEvents != null) pullDownEvents.opened(downDirection);
            }
        });

        this.startAnimator();
        dragState = 1;
    }

    public void slideToSecondHome() {
        this.stopAnimator();
        this.animator = ValueAnimator.ofFloat(windowHeight, windowHeight * this.bottomLocation);
        this.setButtonAnimationProps();
        this.animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float disp = (float) valueAnimator.getAnimatedValue();
                setTranslationY(disp);
            }
        });

        this.animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                dragState = 0;
                downDirection = false;
            }
        });

        this.startAnimator();
        dragState = 1;
    }

    public void slideToFirstHome() {
        this.stopAnimator();
        this.animator = ValueAnimator.ofFloat((windowHeight * this.bottomLocation) - windowHeight, 0);
        this.setButtonAnimationProps();
        this.animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float disp = (float) valueAnimator.getAnimatedValue();
                setTranslationY(disp);
            }
        });

        this.animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                dragState = 0;
                downDirection = true;
            }
        });

        this.startAnimator();
        dragState = 1;
    }
}

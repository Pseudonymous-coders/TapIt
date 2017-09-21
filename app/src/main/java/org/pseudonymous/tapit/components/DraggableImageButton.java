package org.pseudonymous.tapit.components;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;

import org.pseudonymous.tapit.configs.Logger;

/**
 *
 * Created by smerkous on 9/17/17.
 */

public class DraggableImageButton extends android.support.v7.widget.AppCompatImageButton implements View.OnTouchListener{
    private float curY, minAutoPullDown = 0.6f, bottomLocation = 0.90f, velocity = 0;
    private int dragState = 0, windowHeight, downTint, upTint;
    private boolean downDirection = true, stayedAtHome = false;
    private long animationDuration = 2000, maxVelocity = 10000, minDuration = 100;
    private ValueAnimator animator;
    private VelocityTracker velocityTracker = null;
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
                this.curY = rawY;
                this.stayedAtHome = true;

                if(this.velocityTracker == null) {
                    this.velocityTracker = VelocityTracker.obtain();
                } else {
                    this.velocityTracker.clear();
                }

                this.velocityTracker.addMovement(motionEvent);

                if (this.pullDownEvents != null) this.windowHeight = this.pullDownEvents.started(this.downDirection);
                break;
            }

            case MotionEvent.ACTION_MOVE: {
                float disp = (rawY - this.curY);

                if(this.downDirection) {
                    setTranslationY(disp);
                    if(disp > (this.windowHeight * (this.minAutoPullDown / 15))) {
                        this.stayedAtHome = false;
                    }
                    if(disp < 0) disp = 0;
                } else {
                    setTranslationY((this.windowHeight * this.bottomLocation) + disp);
                    disp = this.windowHeight + disp;
                    if(disp < this.windowHeight - (this.windowHeight * (this.minAutoPullDown / 15))) {
                        this.stayedAtHome = false;
                    }
                    if(disp > this.windowHeight) disp = this.windowHeight;
                }

                this.velocityTracker.addMovement(motionEvent);
                this.velocityTracker.computeCurrentVelocity(1000);

                if(this.pullDownEvents != null) this.pullDownEvents.moved(disp, this.downDirection);
                break;
            }

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP: {
                this.velocity = 0;
                boolean passed = false;

                if(this.downDirection) {
                    if((rawY / this.curY) < 0.5) {
                        this.slideToFirstHome();
                        passed = true;
                    }
                } else {
                    if((this.curY / rawY) < 0.98) {
                        this.slideToSecondHome();
                        passed = true;
                    }
                }

                if(!passed) {
                    if (stayedAtHome) {
                        Logger.Log("The menu button was clicked on!");
                        if (downDirection) {
                            this.slideDown();
                        } else {
                            this.slideUp();
                        }
                        passed = true;
                    }
                }

                if(!passed) {
                    if (this.velocityTracker != null) {
                        this.velocity = this.velocityTracker.getYVelocity();
                    }

                    if (this.downDirection) {
                        if (rawY > (this.windowHeight * this.minAutoPullDown)) {
                            this.slideDown();
                        } else {
                            this.velocity = 0;
                            this.slideUp();
                        }
                    } else {
                        if (rawY < this.windowHeight - (this.windowHeight * this.minAutoPullDown)) {
                            this.slideUp();
                        } else {
                            this.velocity = 0;
                            this.slideDown();
                        }
                    }
                }

                if(this.velocityTracker != null) {
                    this.velocityTracker.clear();
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
        this.setVelocitySubtraction();
    }

    private void setButtonAnimationProps() {
        this.setAnimtorProps(800, new LinearInterpolator());
    }

    private void setAnimtorProps(long duration, TimeInterpolator interpolator) {
        this.animator.setInterpolator(interpolator);
        this.animator.setDuration(duration);
        this.animationDuration = duration;

    }

    private void setVelocitySubtraction() {
        long finalVelocity = Math.round(Math.abs(this.velocity));
        if(finalVelocity > this.maxVelocity) finalVelocity = this.maxVelocity;
        long newDuration = this.animationDuration - finalVelocity;
        if(newDuration < this.minDuration) newDuration = this.minDuration;
        if(newDuration > (this.animationDuration / 2) && finalVelocity > 1) {
            this.animator.setInterpolator(new DecelerateInterpolator());
        }
        this.animator.setDuration(newDuration);
    }

    public void slideUp() {
        this.stopAnimator();
        final float smallDiscrepancyFix = this.windowHeight * (1 - this.bottomLocation);
        final boolean incompleteSlide = this.getY() < (this.windowHeight * this.minAutoPullDown);
        this.animator = ValueAnimator.ofFloat(this.getTranslationY() , -smallDiscrepancyFix);
        this.setSlideAnimationProps();


        this.animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float disp = (float) valueAnimator.getAnimatedValue();
                setTranslationY((incompleteSlide) ? disp + smallDiscrepancyFix : disp);
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

                downDirection = true;
                if(!incompleteSlide) slideToFirstHome();

                if(pullDownEvents != null) pullDownEvents.returned(downDirection);
            }
        });

        this.startAnimator();
        dragState = 1;
    }

    public void slideDown() {
        this.stopAnimator();
        final boolean incompleteSlide = this.getY() > this.windowHeight - (this.windowHeight * this.minAutoPullDown);
        this.animator = ValueAnimator.ofFloat(this.getTranslationY(), (this.windowHeight == 0) ? this.curY : this.windowHeight);
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

                downDirection = false;
                setUpTint();
                imageUp();

                if(!incompleteSlide) slideToSecondHome();

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

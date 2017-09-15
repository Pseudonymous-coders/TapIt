package org.pseudonymous.tapit.components;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import org.pseudonymous.tapit.configs.Logger;
import org.pseudonymous.tapit.engine.Engine;

/**
 *
 * Created by smerkous on 9/15/17.
 */

public class SplashView extends SurfaceView {
    private SurfaceHolder holder;
    private Engine gameEngine;
    private Circle circle = null;
    private SplashText text = null;
    private int bgColor = 0;

    public interface SplashViewCallbacks {
        void onComplete();
    }

    private SplashViewCallbacks splashViewCallbacks = null;

    //Create multiple contructors for the xml inflator (It'll prevent it from crashing)
    public SplashView(Context context) {
        super(context);
        pseudoConstructor(context);
    }

    public SplashView(Context context, AttributeSet attrs) {
        super(context, attrs);
        pseudoConstructor(context);
    }

    public SplashView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        pseudoConstructor(context);
    }

    public void pseudoConstructor(Context context) {
        gameEngine = new Engine(this);

        setFocusable(true);
        setFocusableInTouchMode(true);
    }

    public void setBackgroundColor(int bgColor) {
        this.bgColor = bgColor;
    }

    public void setCircle(Circle circle) {
        circle.setCircleEvents(new Circle.CircleEvents() {
            @Override
            public void onClick(Circle circle) {

            }

            @Override
            public void onDestroyed(Circle circle) {
                if(splashViewCallbacks != null) splashViewCallbacks.onComplete();
            }

            @Override
            public void onCleanup(Circle circle) {

            }
        });
        this.circle = circle;
    }

    public void setText(SplashText text) {
        this.text = text;
    }

    public void setSplashViewCallbacks(SplashViewCallbacks splashViewCallbacks) {
        this.splashViewCallbacks = splashViewCallbacks;
    }

    public void setTicksPerSecond(int ticksPerSecond) {
        this.gameEngine.setTicksPerSecond(ticksPerSecond);
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        canvas.drawColor(bgColor);

        if(circle != null) {
            circle.drawToCanvas(canvas);
        }

        if(text != null) {
            text.drawToCanvas(canvas);
        }
    }

    public void startSplash() {
        holder = getHolder();
        holder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder surfaceHolder) {
                //Only start the game engine when the surface has been created
                if(gameEngine != null && !gameEngine.isRunning()) {
                    try {
                        gameEngine.startEngine();
                        gameEngine.setDims(getWidth(), getHeight());
                    } catch (Exception err){
                        Logger.LogError("Failed to start the splash screen game engine!");
                        err.printStackTrace();
                    }
                } else {
                    Logger.LogWarning("Couldn't start the splash screen game engine because it's null or it's already running!");
                }

            }

            @Override
            public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
                //Destroy the engine so that it doesn't call anymore events to the canvas
                gameEngine.pauseEngine();
            }
        });
    }

    public void destroySplash() {
        gameEngine.killEngine();
    }
}

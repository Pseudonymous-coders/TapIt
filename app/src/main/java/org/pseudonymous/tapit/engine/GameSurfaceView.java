package org.pseudonymous.tapit.engine;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by smerkous on 9/3/17.
 */

public class GameSurfaceView extends SurfaceView {
    private SurfaceHolder holder;
    private Engine gameEngine;
    private List<TickEvent> tickEvents;
    private int backgroundColor;

    public interface EngineEvents {
        void onStart();

        void onTick(Engine engine);

        void onKilled();
    }

    private EngineEvents engineEventCallbacks = null;

    //Create multiple contructors for the xml inflator (It'll prevent it from crashing)
    public GameSurfaceView(Context context) {
        super(context);
        pseudoConstructor(context);
    }

    public GameSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        pseudoConstructor(context);
    }

    public GameSurfaceView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        pseudoConstructor(context);
    }

    public void pseudoConstructor(Context context) {
        gameEngine = new Engine(this);
        this.tickEvents = new ArrayList<>();
    }

    public void setTicksPerSecond(int ticksPerSecond) {
        this.gameEngine.setTicksPerSecond(ticksPerSecond);
    }

    public void setGameCallbacks(EngineEvents engineEvents) {
        this.engineEventCallbacks = engineEvents;
    }

    public void addTickEvent(TickEvent event) {
        event.inheritAttributes(this.gameEngine); //Inherit the predefined attributes of the engine
        this.tickEvents.add(event);
    }

    public void setBackgroundColor(int backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public void startEngine() {
        holder = getHolder();
        holder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder surfaceHolder) {
                //Only start the game engine when
                gameEngine.start();
                gameEngine.setDims(getWidth(), getHeight());
                if (engineEventCallbacks != null) engineEventCallbacks.onStart();
            }

            @Override
            public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
                //Destroy the engine so that it doesn't call anymore events to the canvas
                gameEngine.kill();
                if (engineEventCallbacks != null) engineEventCallbacks.onKilled();
            }
        });
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        canvas.drawColor(backgroundColor);
        this.gameEngine.setCurrentView(canvas);
        if (this.engineEventCallbacks != null) engineEventCallbacks.onTick(this.gameEngine);

        //Increment all of the local tick events to check if an event loop has occured
        for (TickEvent event : this.tickEvents) {
            event.engineTicked(this.gameEngine);
            event.canvasDrawLoop(this.gameEngine, canvas);
        }
    }

    public void pauseEngine() {
        this.gameEngine.pauseEngine();
    }

    public void continueEngine() {
        this.gameEngine.continueEngine();
    }

    public void destroyEngine() {
        this.gameEngine.kill();
    }
}

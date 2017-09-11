package org.pseudonymous.tapit.engine;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import org.pseudonymous.tapit.components.Circle;
import org.pseudonymous.tapit.configs.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by smerkous on 9/3/17.
 */

public class GameSurfaceView extends SurfaceView implements View.OnTouchListener {
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

        setFocusable(true);
        setFocusableInTouchMode(true);
        setOnTouchListener(this);
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
                try {
                    gameEngine.start();
                    gameEngine.setDims(getWidth(), getHeight());
                } catch (Exception ignored){}
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

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        if(motionEvent.getAction() != MotionEvent.ACTION_DOWN) return true;
        float xTouch = motionEvent.getX(0);
        float yTouch = motionEvent.getY(0);

        Logger.Log("GameSurfaceView touched (x: %.2f, y: %.2f)", xTouch, yTouch);

        for(TickEvent event : this.tickEvents) {
            for(Circle circle : event.getAllCircles()) {
                circle.emitTouchEvent(xTouch, yTouch);
            }
        }
        return true;
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

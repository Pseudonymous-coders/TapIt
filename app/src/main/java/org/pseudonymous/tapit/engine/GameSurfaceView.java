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
import org.pseudonymous.tapit.configs.CircleProps;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by smerkous on 9/3/17.
 */

public class GameSurfaceView extends SurfaceView implements View.OnTouchListener {
    private SurfaceHolder holder;
    private Engine gameEngine;
    private List<TickEvent> tickEvents;
    private List<Circle> circles;
    private int backgroundColor;
    private long circleId = 0;
    private float padding = 0.01f;

    public interface PlayerEvents {
        void onClicked(Circle circle);
        void onDestroyed(Circle circle);
    }

    public interface EngineEvents {
        void onStart();
        void onTick(Engine engine);
        void onPaused();
        void onKilled();
    }

    private PlayerEvents playerEvents = null;
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
        this.circles = new ArrayList<>();

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

    public void setPlayerCallbacks(PlayerEvents playerEvents) {
        this.playerEvents = playerEvents;
    }

    public void addTickEvent(TickEvent event) {
        event.inheritAttributes(this.gameEngine); //Inherit the predefined attributes of the engine
        this.tickEvents.add(event);
    }

    public void setBackgroundColor(int backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public void setCirclePadding(float padding) {
        this.padding = padding;
    }

    public void addCircle(Circle c) {
        c.setCircleEvents(new Circle.CircleEvents() {
            @Override
            public void onClick(Circle circle) {
                if(playerEvents != null) playerEvents.onClicked(circle);
            }

            @Override
            public void onDestroyed(Circle circle) {
                for(int ind = 0; ind < circles.size(); ind++) {
                    if(circle.getCircleId() == circles.get(ind).getCircleId()) {
                        circles.remove(ind); //Remove the current circle from the screen
                    }
                }
                if(playerEvents != null) playerEvents.onDestroyed(circle);
            }
        });
        c.setCircleId(circleId);
        this.circles.add(c);
        circleId++;
    }

    public void addCircles(CircleProps []circles) {
       for(CircleProps props : circles) {
            Circle circle = new Circle(this.gameEngine, props.getColor());
            //Temporarily set the circle its max size so we can compare its distance to the other circles
            circle.setScaledRadius(props.getRadius);
            int sR = circle.getRadius();

            for(Circle c : this.circles) {
                int r = c.getRadius();
                float minDistanceApart = (this.gameEngine.getWidth() * this.padding) + (float) sR + (float) r; 
                
            }
       }
    }

    public int getCircleCount() {
        return this.circles.size();
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        canvas.drawColor(backgroundColor);
        // NOT NEEDED - this.gameEngine.setCurrentView(canvas);
        if (this.engineEventCallbacks != null) engineEventCallbacks.onTick(this.gameEngine);

        //Increment all of the local tick events to check if an event loop has occured
        for (TickEvent event : this.tickEvents) {
            event.engineTicked(this.gameEngine);
        }

        for(Circle circle : this.circles) {
            circle.drawToCanvas(canvas);
        }
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        if(motionEvent.getAction() != MotionEvent.ACTION_DOWN) return true;
        float xTouch = motionEvent.getX(0);
        float yTouch = motionEvent.getY(0);

        Logger.Log("GameSurfaceView touched (x: %.2f, y: %.2f)", xTouch, yTouch);

        for(Circle circle : this.circles) {
            circle.emitTouchEvent(xTouch, yTouch);
        }
        return true;
    }

    public void startEngine() {
        holder = getHolder();
        holder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder surfaceHolder) {
                //Only start the game engine when the surface has been created
                if(gameEngine != null && !gameEngine.isRunning()) {
                    try {
                        gameEngine.start();
                        gameEngine.setDims(getWidth(), getHeight());
                    } catch (Exception ignored){}
                    
                    if (engineEventCallbacks != null) engineEventCallbacks.onStart();
                }

            }

            @Override
            public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
                //Destroy the engine so that it doesn't call anymore events to the canvas
                pauseEngine();
            }
        });
    }

    public void pauseEngine() {
        this.gameEngine.pauseEngine();
        if(engineEventCallbacks != null) engineEventCallbacks.onPaused();
    }

    public void resumeEngine() {
        this.gameEngine.resumeEngine();
    }

    public void destroyEngine() {
        this.gameEngine.killEngine();
        if (engineEventCallbacks != null) engineEventCallbacks.onKilled();
    }
}

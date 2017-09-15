package org.pseudonymous.tapit.engine;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import org.pseudonymous.tapit.components.Circle;
import org.pseudonymous.tapit.configs.Logger;
import org.pseudonymous.tapit.configs.CircleProps;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 *
 * Created by smerkous on 9/3/17.
 */

public class GameSurfaceView extends SurfaceView implements View.OnTouchListener {
    private SurfaceHolder holder;
    private Engine gameEngine;
    private final List<TickEvent> tickEvents = new CopyOnWriteArrayList<>();
    private final List<Circle> circles = new CopyOnWriteArrayList<>();
    private int backgroundColor;
    private long circleId = 0;
    private float padding = 0.09f;
    private boolean paused = false;

    public enum GameMode {
        RANDOM, TRIANGLE, SQUARE, POLYGON, POLYGON_STATIC
    }

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

    private GameMode gameMode = GameMode.TRIANGLE;
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
        synchronized (this.tickEvents) {
            this.tickEvents.add(event);
        }
    }

    public void setBackgroundColor(int backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public void setCirclePadding(float padding) {
        this.padding = padding;
    }

    public void setGameMode(GameMode gameMode) {
        this.gameMode = gameMode;
    }

    public void removeCircle(long id) {
        synchronized (this.circles) {
            for (int ind = 0; ind < this.circles.size(); ind++) {
                if (id == this.circles.get(ind).getCircleId()) {
                    this.circles.remove(ind); //Remove the current circle from the screen
                }
            }
        }
    }

    public void addCircle(Circle c) {
        c.setCircleEvents(new Circle.CircleEvents() {
            @Override
            public void onClick(Circle circle) {
                if(playerEvents != null) playerEvents.onClicked(circle);
            }

            @Override
            public void onDestroyed(Circle circle) {
                removeCircle(circle.getCircleId());
                if(playerEvents != null) playerEvents.onDestroyed(circle);
            }

            @Override
            public void onCleanup(Circle circle) {
                removeCircle(circle.getCircleId());
            }
        });
        c.setCircleId(circleId);
        synchronized (this.circles) {
            this.circles.add(c);
        }
        circleId++;
    }

    public void addCircles(CircleProps []circles) {
        if(gameMode != GameMode.RANDOM) {
            this.clearAllCircles();
        } else {
            this.garbageCollectCircles();
        }

        PositionEngine positionEngine = new PositionEngine(this.gameEngine);

        switch (gameMode) {
            case TRIANGLE:
                positionEngine.setSides(3);
                break;
            case SQUARE:
                positionEngine.setSides(4);
                break;
            case POLYGON:
                positionEngine.setSides(circles.length);
                break;
            case POLYGON_STATIC:
                positionEngine.setSidesStatic(circles.length);
        }

        positionEngine.rebasePoints();

        for(CircleProps props : circles) {
            Circle circle = new Circle(this.gameEngine, props.getColor());
            //Set the circle's max radius so we can compare its distance to the other circles
            circle.setMaxScaledRadius(props.getRadius());
            int sR = circle.getMaxRadius();

            switch(gameMode) {
                case RANDOM:
                    //Set the location of the circle before starting the animation
                    boolean passed = false;
                    while(!passed) {
                        circle.setRandomLocation(this.padding);

                        passed = true;
                        synchronized (this.circles) {
                            for (Circle c : this.circles) {
                                int r = c.getMaxRadius();
                                float minDistanceApart = (((this.gameEngine.getWidth() < this.gameEngine.getHeight()) ?
                                        this.gameEngine.getHeight() : this.gameEngine.getHeight()) * this.padding) + (float) sR + (float) r;
                                //Set the circle to a new random location and check to
                                if (circle.distanceFrom(c) < minDistanceApart) {
                                    passed = false;
                                }
                            }
                        }
                    }
                    break;

                case TRIANGLE:
                case SQUARE:
                case POLYGON:
                    Point circlePos = positionEngine.getNextPoint(circle, props, this.padding);
                    circle.setPosition(circlePos.x, circlePos.y);
                    break;
            }


           //Don't render the circle until the circle animation starts
            circle.setScaledRadius(0.0f);

            //Add the circle to the global list
            this.addCircle(circle);

            //Start the animation
            circle.startAnimation(props.getRadius(), props.getWait());
       }
    }

    public void garbageCollectCircles() {
        synchronized (this.circles) {
            for(int ind = 0; ind < this.circles.size(); ind++) {
                Circle circle = this.circles.get(ind);
                if(circle.isDead()) this.circles.remove(ind);
            }
        }
    }

    public void clearAllCircles() {
        synchronized (this.circles) {
            this.circles.clear();
        }
    }

    public int getCircleCount() {
        return this.circles.size();
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        canvas.drawColor(backgroundColor);

        //Stop the engine from ticking and anything from rendering if the game is paused
        if(this.paused) return;

        // NOT NEEDED - this.gameEngine.setCurrentView(canvas);
        if (this.engineEventCallbacks != null) engineEventCallbacks.onTick(this.gameEngine);

        //Increment all of the local tick events to check if an event loop has occured
        synchronized (this.tickEvents) {
            for (TickEvent event : this.tickEvents) {
                event.engineTicked(this.gameEngine);
            }
        }

        synchronized (this.circles) {
            for (Circle circle : this.circles) {
                circle.drawToCanvas(canvas);
            }
        }
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        if(motionEvent.getAction() != MotionEvent.ACTION_DOWN) return true;
        float xTouch = motionEvent.getX(0);
        float yTouch = motionEvent.getY(0);

        Logger.Log("GameSurfaceView touched (x: %.2f, y: %.2f)", xTouch, yTouch);

        synchronized (this.circles) {
            for (Circle circle : this.circles) {
                circle.emitTouchEvent(xTouch, yTouch);
            }
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
                        gameEngine.startEngine();
                        gameEngine.setDims(getWidth(), getHeight());
                    } catch (Exception err){
                        Logger.LogError("Failed to start game engine!");
                        err.printStackTrace();
                    }
                    if (engineEventCallbacks != null) engineEventCallbacks.onStart();
                } else {
                    Logger.LogWarning("Couldn't start the game engine because it's null or it's already running!");
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
        this.paused = true;
        this.gameEngine.pauseEngine();
        if(engineEventCallbacks != null) engineEventCallbacks.onPaused();
    }

    public void resumeEngine() {
        this.paused = false;
        this.gameEngine.resumeEngine();
    }

    public void destroyEngine() {
        this.gameEngine.killEngine();
        if (engineEventCallbacks != null) engineEventCallbacks.onKilled();
    }
}

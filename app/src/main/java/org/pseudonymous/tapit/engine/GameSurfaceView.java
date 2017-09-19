package org.pseudonymous.tapit.engine;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.SurfaceTexture;
import android.util.AttributeSet;
import android.util.SparseArray;
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
    private volatile SurfaceHolder holder;
    private volatile Engine gameEngine;
    private volatile EventEngine eventEngine;
    private final List<TickEvent> tickEvents = new CopyOnWriteArrayList<>();
    private final List<Circle> circles = new CopyOnWriteArrayList<>();
    private volatile int backgroundColor;
    private volatile long circleId = 0, waveCircleCount = 0, circlesClicked = 0;
    private volatile float padding = 0.09f;
    private volatile boolean paused = false, exists = false;

    public enum GameMode {
        RANDOM, WAVE
    }

    public enum Difficulty {
        EASY, MEDIUM, HARD
    }

    public enum GenerationMode {
        RANDOM, TRIANGLE, SQUARE, POLYGON, POLYGON_STATIC
    }

    public interface PlayerEvents {
        void onClicked(Circle circle, GameMode gameMode, Difficulty difficulty);
        void onWaveCompleted(GameMode gameMode, Difficulty difficulty);
        void onDestroyed(Circle circle, GameMode gameMode, Difficulty difficulty);
        void onBackgroundTouch(PointF position, GameMode gameMode, Difficulty difficulty);
    }

    public interface EngineEvents {
        void onStart();
        void onTick(Engine engine);
        void onPaused();
        void onKilled();
    }

    private GameMode gameMode = GameMode.WAVE;
    private GenerationMode generationMode = GenerationMode.TRIANGLE;
    private Difficulty difficulty = Difficulty.EASY;
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
        eventEngine = new EventEngine();

        setFocusable(true);
        setFocusableInTouchMode(true);
        setOnTouchListener(this);
    }

    public void setTicksPerSecond(int ticksPerSecond) {
        this.gameEngine.setTicksPerSecond(ticksPerSecond);
        this.eventEngine.setTicksPerSecond(ticksPerSecond);
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
            this.eventEngine.addTickEvent(event);
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

    public void setGenerationMode(GenerationMode generationMode) {
        this.generationMode = generationMode;
    }

    public void setDifficulty(Difficulty difficulty) {
        this.difficulty = difficulty;
    }

    public GameMode getGameMode() {
        return this.gameMode;
    }

    public GenerationMode getGenerationMode() {
        return this.generationMode;
    }

    public Difficulty getDifficulty() {
        return this.difficulty;
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
                switch(gameMode) {
                    case RANDOM:
                        if(playerEvents != null) playerEvents.onClicked(circle, gameMode, difficulty);
                        break;
                    case WAVE:
                        if(circlesClicked >= waveCircleCount) {
                            Logger.Log("Wave completed! (Circles: %d)", circlesClicked);
                            if(playerEvents != null) playerEvents.onWaveCompleted(gameMode, difficulty);
                        }
                        break;
                };
            }

            @Override
            public void onDestroyed(Circle circle) {
                removeCircle(circle.getCircleId());
                if(playerEvents != null) playerEvents.onDestroyed(circle, gameMode, difficulty);
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
        if(this.generationMode != GenerationMode.RANDOM) {
            this.clearAllCircles();
        } else {
            this.garbageCollectCircles();
        }

        if(this.gameMode == GameMode.WAVE) {
            this.waveCircleCount = circles.length;
            this.circlesClicked = 0;
        }

        PositionEngine positionEngine = new PositionEngine(this.gameEngine);

        switch (this.generationMode) {
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

            switch(this.generationMode) {
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

        //Update the usable game engine to the event engine
        this.eventEngine.setCurrentEngine(gameEngine);

        synchronized (this.circles) {
            for (Circle circle : this.circles) {
                circle.drawToCanvas(canvas);
            }
        }
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        if(this.paused) return true;
        int pIndex = motionEvent.getActionIndex();
        int pId = motionEvent.getPointerId(pIndex);
        int mAction = motionEvent.getActionMasked();
        switch(mAction) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN: {
                float xTouch = motionEvent.getX(pIndex);
                float yTouch = motionEvent.getY(pIndex);
                Logger.Log("GameSurfaceView touched (x: %.2f, y: %.2f)", xTouch, yTouch);
                synchronized (this.circles) {
                    boolean touched = false;
                    for (Circle circle : this.circles) {
                        if(circle.emitTouchEvent(xTouch, yTouch)) touched = true;
                    }

                    if(!touched) {
                        Logger.Log("The player missed all of the cirlces");
                        PointF pF = new PointF();
                        pF.x = xTouch;
                        pF.y = yTouch;
                        if(this.playerEvents != null) this.playerEvents.onBackgroundTouch(pF, this.gameMode, this.difficulty);
                    }
                }
                break;
            }
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
            case MotionEvent.ACTION_CANCEL:
                Logger.Log("%d pointer removed from canvas!", pId);
                break;
        }

        /*

        int action = motionEvent.getActionMasked();

        //Check to make sure we are only handling the down press pointer events
        if(action != MotionEvent.ACTION_DOWN && action != MotionEvent.ACTION_POINTER_DOWN) return true;

        //Handle multi touch events
        Logger.Log("Pointer count %d", motionEvent.getPointerCount());
        for(int ind = 0; ind < motionEvent.getPointerCount(); ind++) {
            float xTouch = motionEvent.getX(ind);
            float yTouch = motionEvent.getY(ind);

            Logger.Log("GameSurfaceView touched (x: %.2f, y: %.2f)", xTouch, yTouch);

            synchronized (this.circles) {
                for (Circle circle : this.circles) {
                    circle.emitTouchEvent(xTouch, yTouch);
                }
            }
        }

        invalidate();*/

        return true;
    }

    public void startEngine() {
        holder = getHolder();
        holder.setKeepScreenOn(true);
        holder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder surfaceHolder) {
                //Only start the game engine when the surface has been created
                if(gameEngine != null && !gameEngine.isRunning()) {
                    try {
                        Engine tempEngine = new Engine(GameSurfaceView.this);
                        tempEngine.setTicksPerSecond(gameEngine.getTicksPerSecond());
                        tempEngine.setDims(gameEngine.getWidth(), gameEngine.getHeight());
                        tempEngine.setCurrentView(gameEngine.getCurrentView());
                        if(gameEngine.isPaused()) {
                            tempEngine.pauseEngine();
                        } else {
                            tempEngine.resumeEngine();
                        }
                        gameEngine.killEngine();
                        gameEngine = tempEngine;
                        gameEngine.startEngine();
                        gameEngine.setDims(getWidth(), getHeight());

                        EventEngine tempEventEngine = new EventEngine();
                        tempEventEngine.setTicksPerSecond(eventEngine.getTicksPerSecond());
                        tempEventEngine.setTickEvents(eventEngine.getTickEvents());
                        tempEventEngine.setCurrentEngine(eventEngine.getCurrentEngine());
                        if(eventEngine.isPaused()) {
                            tempEventEngine.pauseEngine();
                        } else {
                            tempEventEngine.resumeEngine();
                        }
                        eventEngine.killEngine();
                        eventEngine = tempEventEngine;
                        eventEngine.pauseEngine();
                        eventEngine.startEngine();
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
                Logger.Log("GameSurfaceView changed (x: %d, y: %d)", i1, i2);
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
                //Destroy the engine so that it doesn't call anymore events to the canvas
                pauseEngine();
                cleanUpSurface();
            }
        });
        exists = true;
    }

    public void cleanUpSurface() {
        this.paused = true;
        this.gameEngine.pauseEngine();
        this.eventEngine.pauseEngine();
        holder = null;
        exists = false;
    }

    public boolean exists() {
        return this.exists;
    }

    public boolean isPaused() {
        return this.gameEngine.isPaused();
    }

    public void pauseEngine() {
        Logger.Log("Pausing the engines!");
        this.paused = true;
        this.gameEngine.pauseEngine();
        this.eventEngine.pauseEngine();
        if(engineEventCallbacks != null) engineEventCallbacks.onPaused();
    }

    public void resumeEngine() {
        Logger.Log("Resuming the engines!");
        this.paused = false;
        this.gameEngine.resumeEngine();
        this.eventEngine.resumeEngine();
    }

    public void destroyEngine() {
        Logger.Log("Destroying the engines!");
        this.gameEngine.killEngine();
        this.eventEngine.killEngine();
        if (engineEventCallbacks != null) engineEventCallbacks.onKilled();
    }
}

package org.pseudonymous.tapit.engine;

import android.graphics.Canvas;

import org.pseudonymous.tapit.components.Circle;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by smerkous on 9/3/17.
 */

public class TickEvent {
    private static final double TIME_DIFF_RATIO = 0.5;
    private static final long TIME_DIFF_DEAD_ZONE = 20; //In milliseconds
    private float eventsPerSecond = 0;
    private int onTickCallEvent = 0;
    private int currentTick = 0;
    private long lastTime = 0;
    private List<Circle> circles;

    public interface AttachedEvent {
        void onEvent(Engine engine, long elapsedTime);
    }

    public interface DrawLoop {
        List<Circle> onDrawLoop(Engine engine, List<Circle> circles);
    }

    private AttachedEvent attachedEvent = null;
    private DrawLoop drawLoop = null;

    public TickEvent(float perSecond) {
        this.eventsPerSecond = perSecond;
        this.onTickCallEvent = -1;
        this.circles = new ArrayList<>();
    }

    public void setEventsPerSecond(float perSecond) {
        this.eventsPerSecond = perSecond;
    }

    void inheritAttributes(Engine engine) {
        this.onTickCallEvent = (int) (((float) engine.getTicksPerSecond()) / eventsPerSecond);
    }

    public void setAttachedEvent(AttachedEvent attachedEvent) {
        this.attachedEvent = attachedEvent;
    }

    public void setDrawLoop(DrawLoop drawLoop) {
        this.drawLoop = drawLoop;
    }

    void canvasDrawLoop(Engine engine, Canvas canvas) {
        if (this.drawLoop != null) {
            this.circles = this.drawLoop.onDrawLoop(engine, this.circles);
            for (Circle circle : this.circles) {
                circle.drawToCanvas(canvas);
            }
        }
    }

    //Engine specific callings
    void engineTicked(Engine engine) {
        this.currentTick++;

        long elaspedTime = (System.currentTimeMillis() - this.lastTime);

        if (this.lastTime != 0) {
            long timeDiff = (long) (1000 / this.eventsPerSecond) - elaspedTime;
            if (Math.abs(timeDiff) > TIME_DIFF_DEAD_ZONE) {
                this.eventsPerSecond += ((double) timeDiff / 1000d) * TIME_DIFF_RATIO;
            }
        }

        if (this.currentTick >= this.onTickCallEvent) {
            if (this.attachedEvent != null)
                this.attachedEvent.onEvent(engine, elaspedTime); //Call the event that's attached to this object
            lastTime = System.currentTimeMillis();
            this.resetCurrentTick();
        }
    }

    private void resetCurrentTick() {
        this.currentTick = 0;
    }
}

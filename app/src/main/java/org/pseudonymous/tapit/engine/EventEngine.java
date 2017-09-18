package org.pseudonymous.tapit.engine;

import android.graphics.Canvas;
import android.view.SurfaceView;

import org.pseudonymous.tapit.configs.Logger;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 *
 * Created by smerkous on 9/3/17.
 */

public class EventEngine extends Thread {
    private int ticksPerSecond = 30;
    private boolean running = false, paused = false;
    private long startTime;
    private Engine gameEngine;
    private List<TickEvent> tickEvents = new CopyOnWriteArrayList<>();

    public void setTickEvents(List<TickEvent> tickEvents) {
        this.tickEvents = tickEvents;
    }

    public void addTickEvent(TickEvent tickEvent) {
        this.tickEvents.add(tickEvent);
    }

    public void setCurrentEngine(Engine gameEngine) {
        this.gameEngine = gameEngine;
    }

    public void setTicksPerSecond(int ticks) {
        this.ticksPerSecond = ticks;
    }

    public int getTicksPerSecond() {
        return this.ticksPerSecond;
    }

    public List<TickEvent> getTickEvents() {
        return this.tickEvents;
    }

    public Engine getCurrentEngine() {
        return this.gameEngine;
    }

    @Override
    public void run() {
        long lTicksPS = 1000 / this.ticksPerSecond, startTime, sleepTime;
        this.startTime = System.currentTimeMillis();

        while (running) {
            if (this.paused) {
                try {
                    Thread.sleep(100); //Wait for the engine to unpause before we continue to render our next elements
                } catch (InterruptedException ignored) {
                    Logger.LogWarning("Killing the event engine (Reason: The thread was interrupted)");
                    this.running = false;
                    break;
                }
                continue;
            }
            //Time the view callback difference (This should stay well under 10 millis)
            startTime = System.currentTimeMillis();

            try {
                //Increment all of the local tick events to check if an event loop has occured
                for (TickEvent event : this.tickEvents) {
                    event.engineTicked(this.gameEngine);
                }
            } catch(Throwable err) {
                Logger.LogError("The event engine failed to run a tick (All errors were'nt handled)");
                err.printStackTrace();
                break;
            }

            //Sleep the remainder of the time so that the game doesn't run too fast
            sleepTime = lTicksPS - (System.currentTimeMillis() - startTime);
            try {
                if (sleepTime > 0) {
                    sleep(sleepTime);
                } else {
                    sleep(10); //This is required for older phones with poor specs, so we don't kill the battery and CPU
                }
            } catch (Exception ignored) {
            }
        }
    }

    public void pauseEngine() {
        this.paused = true;
    }

	public void resumeEngine() {
        this.paused = false;
    }

	public boolean isPaused() {
		return this.paused;
	}

	public boolean isRunning() {
        return this.running;
    }

    public long getElapsedTime() {
        return System.currentTimeMillis() - this.startTime;
    }

    public void startEngine() {
        this.running = true;
        this.start();
    }

    //Kill the game engine
    public void killEngine() {
        this.running = false;
        boolean retry = true;

        //Lock back onto the main thread before continuing onto the next method
        while (retry) {
            try {
                this.join();
                retry = false;
            } catch (InterruptedException ignored) {
            }
        }

        Logger.LogWarning("Killing the event engine (Reason: The kill method was called)");
    }
}

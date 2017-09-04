package org.pseudonymous.tapit.engine;

import android.graphics.Canvas;
import android.graphics.Color;

import org.pseudonymous.tapit.configs.Logger;

/**
 * Created by smerkous on 9/3/17.
 */

public class Engine extends Thread {
    private int ticksPerSecond = 30, width = 100, height = 100;
    private boolean running = true, pause = false;
    private Canvas currentView;
    private GameSurfaceView view;
    private long startTime;

    public Engine(GameSurfaceView view) {
        this.view = view;
    }

    /**
     * Sets the amount of times the engine will tick in one second (The faster the smoother)
     * <p>
     * Note: This method must be called before any TickEvent is attached or the timing will be off
     *
     * @param ticks The amount of ticks there should be per second (similar to FPS)
     */
    public void setTicksPerSecond(int ticks) {
        this.ticksPerSecond = ticks;
    }

    public int getTicksPerSecond() {
        return this.ticksPerSecond;
    }

    public void setDims(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    @Override
    public void run() {
        long lTicksPS = 1000 / this.ticksPerSecond, startTime, sleepTime;
        this.startTime = System.currentTimeMillis();

        while (running) {
            Canvas c = null;
            if (this.pause) {
                try {
                    Thread.sleep(100); //Wait for the engine to unpause before we continue to render our next elements
                } catch (InterruptedException ignored) {
                    Logger.LogWarning("Killing the engine (Reason: The thread was interrupted)");
                    break;
                }
            }
            //Time the view callback difference (This should stay well under 10 millis)
            startTime = System.currentTimeMillis();

            try {
                c = view.getHolder().lockCanvas();
                synchronized (view.getHolder()) { //Make sure no other thread is locked onto the canvas object
                    view.draw(c);
                }
            } catch (NullPointerException appClosed) {
                Logger.LogWarning("Killing the engine (Reason: The Canvas was destroyed)");
                break; //Close the thread since the app has been closed
            } finally {
                if (c != null) {
                    view.getHolder().unlockCanvasAndPost(c); //Even when there's an error we should unlock the canvas object
                }
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
        this.pause = true;
    }

    public void continueEngine() {
        this.pause = false;
    }

    public void setCurrentView(Canvas currentView) {
        this.currentView = currentView;
    }

    public long getElapsedTime() {
        return System.currentTimeMillis() - this.startTime;
    }

    public Canvas getCurrentView() {
        return this.currentView;
    }

    //Kill the game engine
    public void kill() {
        this.running = false;
        this.view = null;
        boolean retry = true;

        //Lock back onto the main thread before continuing onto the next method
        while (retry) {
            try {
                this.join();
                retry = false;
            } catch (InterruptedException ignored) {
            }
        }

        Logger.LogWarning("Killing the engine (Reason: The kill method was called)");
    }
}

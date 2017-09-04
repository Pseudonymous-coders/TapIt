package org.pseudonymous.tapit;

import android.annotation.SuppressLint;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import org.pseudonymous.tapit.components.Circle;
import org.pseudonymous.tapit.configs.Logger;
import org.pseudonymous.tapit.engine.Engine;
import org.pseudonymous.tapit.engine.GameSurfaceView;
import org.pseudonymous.tapit.engine.TickEvent;

import java.util.List;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class MainActivity extends AppCompatActivity {
    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;

    private View mContentView;
    private View mControlsView;
    private boolean mVisible;
    private String appName = "TapIt";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        mVisible = true;
        mContentView = findViewById(R.id.fullscreen_content);
        mContentView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        );

        GameSurfaceView sf = findViewById(R.id.GameZone);
        sf.setTicksPerSecond(30);
        sf.setBackgroundColor(Color.BLACK);

        final int[] radius = {1};
        final TickEvent circleHandler = new TickEvent(10f); //Happens every 500 milliseconds (1/2)
        circleHandler.setDrawLoop(new TickEvent.DrawLoop() {
            @Override
            public List<Circle> onDrawLoop(Engine engine, List<Circle> circles) {
                if (engine.getElapsedTime() > 4000) {
                    Circle toAdd = new Circle(Color.RED);
                    toAdd.inheritParentAttributes(engine);
                    toAdd.setScaledPosition(0.1f, 0.1f);
                    toAdd.setScaledRadius(0.1f);
                    circles.add(toAdd);
                }

                if (engine.getElapsedTime() > 6000) {
                    for (int ind = 0; ind < circles.size(); ind++) {
                        Circle c = circles.get(ind);
                        c.setScaledRadius(c.getScaledRadius() - 0.05f);
                    }
                } else {
                    for (int ind = 0; ind < circles.size(); ind++) {
                        Circle c = circles.get(ind);
                        c.setScaledRadius(c.getScaledRadius() + 0.05f);
                    }
                }
                return circles;
            }
        });

        circleHandler.setAttachedEvent(new TickEvent.AttachedEvent() {
            @Override
            public void onEvent(Engine engine, long elapsedTime) {
                Logger.Log("SAMPLE EVENT CALLED %d", elapsedTime);
            }
        });

        sf.addTickEvent(circleHandler);
        sf.setGameCallbacks(new GameSurfaceView.EngineEvents() {
            @Override
            public void onStart() {
                Logger.Log("The game engine has started");
            }

            @Override
            public void onTick(Engine engine) {
                //log("TICK");
            }

            @Override
            public void onKilled() {
                Logger.Log("The game engine is destroyed");
            }
        });

        sf.startEngine();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

    }

    private void drawCircle(Integer x, Integer y){

    }
}

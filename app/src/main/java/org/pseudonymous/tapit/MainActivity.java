package org.pseudonymous.tapit;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
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
import android.view.animation.LinearInterpolator;
import android.widget.Button;

import org.pseudonymous.tapit.components.Circle;
import org.pseudonymous.tapit.configs.Logger;
import org.pseudonymous.tapit.engine.Engine;
import org.pseudonymous.tapit.engine.GameSurfaceView;
import org.pseudonymous.tapit.engine.TickEvent;

import java.util.ArrayList;
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
    private Button startButton;
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

        final ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, -300f);
        final ValueAnimator downAnimator = ValueAnimator.ofFloat(-300f, 600f);

        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float value = (float) valueAnimator.getAnimatedValue();
                startButton.setTranslationY(value);
            }
        });

        downAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float value = (float) valueAnimator.getAnimatedValue();
                startButton.setTranslationY(value);
            }
        });

        valueAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                downAnimator.start();
            }
        });

        valueAnimator.setInterpolator(new LinearInterpolator());
        valueAnimator.setDuration(300);

        downAnimator.setInterpolator(new LinearInterpolator());
        valueAnimator.setDuration(1500);

        startButton = findViewById(R.id.start_button);
        startButton.setVisibility(View.INVISIBLE);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                valueAnimator.start();
            }
        });

        GameSurfaceView sf = findViewById(R.id.GameZone);
        sf.setTicksPerSecond(30);
        sf.setBackgroundColor(Color.BLACK);

        final int[] radius = {1};
        final TickEvent circleHandler = new TickEvent(10f); //Happens every 500 milliseconds (1/2)
        circleHandler.setDrawLoop(new TickEvent.DrawLoop() {
            @Override
            public List<Circle> onDrawLoop(Engine engine, List<Circle> circles) {
                List<Circle> newCircles = new ArrayList<>();
                newCircles.addAll(circles);
                if (engine.getElapsedTime() > 4000 && radius[0] == 1) {
                    Circle toAdd = new Circle(Color.RED);
                    toAdd.inheritParentAttributes(engine);
                    toAdd.setCircleEvents(new Circle.CircleEvents() {
                        @Override
                        public void onClick() {
                            Logger.Log("THE CIRCLE WAS CLICKED ON");
                        }

                        @Override
                        public void onDestroyed() {
                            Logger.Log("The player lost!");
                        }
                    });
                    toAdd.setScaledPosition(0.1f, 0.1f);
                    toAdd.setScaledRadius(0.05f);
                    newCircles.add(toAdd);
                    radius[0] = 0;
                } else if(engine.getElapsedTime() > 4000 && radius[0] == 0) {
                    for(Circle circle : newCircles) {
                        circle.startAnimation(0.5f, 500, 1000, 1000);
                    }
                    radius[0] = 2;
                }
                return newCircles;
            }
        });

        circleHandler.setAttachedEvent(new TickEvent.AttachedEvent() {
            @Override
            public void onEvent(Engine engine, long elapsedTime) {
                //Logger.Log("SAMPLE EVENT CALLED %d", elapsedTime);
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

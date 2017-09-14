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
import org.pseudonymous.tapit.components.StartButton;
import org.pseudonymous.tapit.configs.Configs;
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
    private StartButton startButton;
    private GameSurfaceView game;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Set the fullscreen flags and remove the title before setting the content view
        Configs.setPreliminaryScreenFlags(this);

        //Unwrap and attach the xml content view to the main activity
        setContentView(R.layout.activity_main);

        //Set and lock the window orientation and size
        Configs.setFullScreen(this);
        Configs.lockRotation(this);

        //Define the start button and its click operation
        startButton = findViewById(R.id.start_button);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startButton.startAnimation();
            }
        });

        //Load the game surface view
        game = findViewById(R.id.GameZone);
        game.setTicksPerSecond(60);
        game.setBackgroundColor(Configs.getColor(R.color.colorPrimaryDark, this));

        startGame();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

    }

    protected void startGame() {

        final int[] radius = {1};
        final TickEvent circleHandler = new TickEvent(0.33f); //Happens every 500 milliseconds (1/2)

        /*circleHandler.setDrawLoop(new TickEvent.DrawLoop() {
            @Override
            public List<Circle> onDrawLoop(Engine engine, List<Circle> circles) {
                List<Circle> newCircles = new ArrayList<>();
                newCircles.addAll(circles);
                if (engine.getElapsedTime() > 4000 && radius[0] == 1) {
                    Circle toAdd = new Circle(engine, Configs.getColor(R.color.green, MainActivity.this));
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
                    toAdd.setScaledPosition(0.5f, 0.3f);
                    toAdd.setScaledRadius(0.05f);
                    newCircles.add(toAdd);
                    radius[0] = 0;
                } else if(engine.getElapsedTime() > 4000 && radius[0] == 0) {
                    for(Circle circle : newCircles) {
                        circle.startAnimation(0.2f, 200, 1000, 200);
                    }
                    radius[0] = 2;
                }
                return newCircles;
            }
        });*/

        circleHandler.setAttachedEvent(new TickEvent.AttachedEvent() {
            @Override
            public void onEvent(Engine engine, long elapsedTime) {
                //game.addCircle(
                //Logger.Log("SAMPLE EVENT CALLED %d", elapsedTime);
            }
        });

        game.addTickEvent(circleHandler);

        game.setPlayerCallbacks(new GameSurfaceView.PlayerEvents() {
            @Override
            public void onClicked(Circle circle) {

            }

            @Override
            public void onDestroyed(Circle circle) {

            }
        });

        game.setGameCallbacks(new GameSurfaceView.EngineEvents() {
            @Override
            public void onStart() {
                Logger.Log("The game engine has started");
            }

            @Override
            public void onTick(Engine engine) {
                //log("TICK");
            }

            @Override
            public void onPaused() {

            }

            @Override
            public void onKilled() {
                Logger.Log("The game engine is destroyed");
            }
        });

        game.startEngine();
    }


    /*// Not working, we need to revise how we draw circles and such...
    private List<Circle> drawCircle(float x, float y, Engine engine, List<Circle> circles){
        List<Circle> newCircles = new ArrayList<>();
        newCircles.addAll(circles);

        Circle circle = new Circle(getResources().getColor(R.color.green));
        circle.inheritParentAttributes(engine);

        circle.setCircleEvents(new Circle.CircleEvents() {
            @Override
            public void onClick() {
                Logger.Log("THE CIRCLE WAS CLICKED ON");
            }

            @Override
            public void onDestroyed() {
                Logger.Log("The player lost!");
            }
        });
        circle.setScaledPosition(x, y);
        circle.setScaledRadius(0.05f);
        newCircles.add(circle);
        circle.startAnimation(0.2f, 200, 1000, 200);
        return newCircles;
    }*/

}

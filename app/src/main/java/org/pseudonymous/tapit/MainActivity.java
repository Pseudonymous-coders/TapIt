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
import org.pseudonymous.tapit.components.ScoreText;
import org.pseudonymous.tapit.components.StartButton;
import org.pseudonymous.tapit.configs.CircleProps;
import org.pseudonymous.tapit.configs.Configs;
import org.pseudonymous.tapit.configs.Logger;
import org.pseudonymous.tapit.engine.Engine;
import org.pseudonymous.tapit.engine.GameSurfaceView;
import org.pseudonymous.tapit.engine.TickEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class MainActivity extends AppCompatActivity {
    private StartButton startButton;
    private ScoreText currentScore, highScore;
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

        startButton.setAnimationCallbacks(new StartButton.AnimationCallbacks() {
            @Override
            public void onAnimationEnd() {
                game.resumeEngine();
            }
        });

        //Define the current and high score text view
        currentScore = findViewById(R.id.cur_score);
        highScore = findViewById(R.id.high_score);

        //Load the game surface view
        game = findViewById(R.id.game_view);
        game.setTicksPerSecond(60);
        game.setBackgroundColor(Configs.getColor(R.color.colorPrimaryDark, this));
        game.setGameMode(GameSurfaceView.GameMode.POLYGON);

        startGame();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

    }

    protected void startGame() {
        final TickEvent circleHandler = new TickEvent(0.33f); //Happens every 500 milliseconds (1/2)

        circleHandler.setAttachedEvent(new TickEvent.AttachedEvent() {
            @Override
            public void onEvent(Engine engine, long elapsedTime) {
                Logger.Log("Creating the new circles");

                int circleCount = ThreadLocalRandom.current().nextInt(1, 5);
                float circleSize = 0.6f / circleCount;
                int waitTime = 600 * circleCount;
                if(waitTime < 2000) waitTime = 2000;

                CircleProps prop = new CircleProps(Configs.getColor(R.color.green, MainActivity.this), circleSize, waitTime);
                CircleProps props[] = new CircleProps[circleCount];
                for(int ind = 0; ind < props.length; ind++) {
                    props[ind] = prop;
                }

                game.addCircles(props);
                Logger.Log("Created! %d", game.getCircleCount());
            }
        });

        game.addTickEvent(circleHandler);

        game.setPlayerCallbacks(new GameSurfaceView.PlayerEvents() {
            @Override
            public void onClicked(Circle circle) {
                currentScore.incrementScore(); //Add one to the current score

                if(currentScore.getScore() > highScore.getScore()) {
                    highScore.setScore(currentScore.getScore());
                }
            }

            @Override
            public void onDestroyed(Circle circle) {
                currentScore.setScore(0);
            }
        });

        game.setGameCallbacks(new GameSurfaceView.EngineEvents() {
            @Override
            public void onStart() {
                Logger.Log("The game engine has started");
            }

            @Override
            public void onTick(Engine engine) {
                //Logger.Log("TICK");
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
        game.pauseEngine();
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

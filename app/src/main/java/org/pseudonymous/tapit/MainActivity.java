package org.pseudonymous.tapit;

import android.graphics.Color;
import android.graphics.PointF;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import org.pseudonymous.tapit.components.Circle;
import org.pseudonymous.tapit.components.DraggableImageButton;
import org.pseudonymous.tapit.components.ScoreText;
import org.pseudonymous.tapit.components.StartButton;
import org.pseudonymous.tapit.components.TimeBar;
import org.pseudonymous.tapit.configs.CircleProps;
import org.pseudonymous.tapit.configs.Configs;
import org.pseudonymous.tapit.configs.Logger;
import org.pseudonymous.tapit.engine.Engine;
import org.pseudonymous.tapit.engine.GameSurfaceView;
import org.pseudonymous.tapit.engine.TickEvent;

import java.util.concurrent.ThreadLocalRandom;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class MainActivity extends AppCompatActivity {
    private StartButton startButton;
    private ScoreText currentScore, highScore;
    private TimeBar timeBar;
    private GameSurfaceView game;
    private ViewGroup menuPanel;
    private DraggableImageButton menuButton;
    private boolean playingWhileMenuPullDown = false, isMenuShown = false;
    private float tickerSpeed= 0.5f;
    private int windowWidth, windowHeight;

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

        //Load the menu panel, and make it invisible
        menuPanel = findViewById(R.id.menu_panel);
        menuPanel.setVisibility(View.INVISIBLE);

        //Define the start button and its click operation
        startButton = findViewById(R.id.start_button);
        startButton.setBackgroundColor(Configs.getColor(R.color.colorSecondary, this));
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startButton.downAnimation();
            }
        });

        startButton.setAnimationCallbacks(new StartButton.AnimationCallbacks() {
            @Override
            public void onAnimationEnd() {
                if(!isMenuShown) game.resumeEngine();
            }
        });

        //Define the current and high score text view
        currentScore = findViewById(R.id.cur_score);
        highScore = findViewById(R.id.high_score);

        //Get the custom top progress bar
        timeBar = findViewById(R.id.time_bar);

        //Get the menu pull down icon
        menuButton = findViewById(R.id.menu_button);
        menuButton.setMinAutoPullDown(0.25f); //The arrow must pass 25% of the screen for the window to pull down
        menuButton.setBottomLocation(0.93f); //What percent of the screen should the bottom menu arrow be at
        menuButton.setTints(Configs.getColor(R.color.colorSecondary, this), Configs.getColor(R.color.colorPrimary, this));
        menuButton.setButtonImage(Configs.getBitmap(R.drawable.more, this));
        menuButton.setPullDownEvents(new DraggableImageButton.PullDownEvents() {
            @Override
            public void returned(boolean downDirection) {
                if(downDirection) {
                    menuPanel.setVisibility(View.INVISIBLE);
                    menuPanel.setTranslationY(0);
                    Logger.Log("Hiding menu panel");

                    if (playingWhileMenuPullDown) {
                        Logger.Log("The player was recently playing... resuming the engine");
                        game.resumeEngine();
                    }
                    isMenuShown = false;
                } else {
                    startButton.setVisibility(View.INVISIBLE);
                    game.setVisibility(View.INVISIBLE);
                    isMenuShown = true;
                }
            }

            @Override
            public int started(boolean downDirection) {
                if(downDirection) {
                    windowWidth = getWidth();
                    windowHeight = getHeight();

                    menuPanel.setVisibility(View.VISIBLE);
                    menuPanel.setTranslationY(-windowHeight);
                    menuPanel.requestFocus();

                    ViewGroup.LayoutParams params = menuPanel.getLayoutParams();
                    params.width = windowWidth;
                    params.height = windowHeight;
                    menuPanel.setLayoutParams(params);
                    Logger.Log("Showing the menu panel");

                    playingWhileMenuPullDown = !game.isPaused();
                    if (playingWhileMenuPullDown) {
                        game.pauseEngine();
                    }
                } else {
                    startButton.setVisibility(View.VISIBLE);
                    game.setVisibility(View.VISIBLE);
                    //game.startEngine();
                }

                return windowHeight;
            }

            @Override
            public void moved(float displacement, boolean downDirection) {
                float buttonDisplacement = (startButton.getTop() - (windowHeight * 0.02f));
                menuPanel.setTranslationY(displacement - windowHeight);

                //Move the start button with the menu pulldown
                if(downDirection) {
                    if (menuButton.getY() > buttonDisplacement) {
                        startButton.setTranslationY(displacement - buttonDisplacement);
                    }
                } else {
                    if (displacement - buttonDisplacement >= 0) {
                        startButton.setTranslationY(displacement - buttonDisplacement);
                    }
                }
            }

            @Override
            public void opened(boolean downDirection) {
                if(downDirection) {
                    startButton.setVisibility(View.INVISIBLE);
                    game.setVisibility(View.INVISIBLE);
                    Logger.Log("The menu panel is open!");
                    isMenuShown = true;
                } else {
                    Logger.Log("The menu panel is closed!");
                    if (playingWhileMenuPullDown) {
                        Logger.Log("The player was recently playing... resuming the engine");
                        game.resumeEngine();
                    }
                    isMenuShown = false;
                }
            }
        });

        //Load the game surface view
        game = findViewById(R.id.game_view);
        game.setTicksPerSecond(60);
        game.setBackgroundColor(Configs.getColor(R.color.colorPrimary, this));
        game.setGameMode(GameSurfaceView.GameMode.POLYGON);

        startGame();
    }

    private int getWidth() {
        return this.getWindow().getDecorView().getWidth();
    }

    private int getHeight() {
        return this.getWindow().getDecorView().getHeight();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Logger.Log("Pausing the game engine!");
        game.pauseEngine();
        if(game.exists()) {
            game.cleanUpSurface();
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Logger.Log("Restarting the game engine");
        game.resumeEngine();
        if(!game.exists()) {
            game.startEngine();
        }
    }

    protected void startGame() {
        final TickEvent circleHandler = new TickEvent(tickerSpeed); //Happens every 500 milliseconds (1/2)

        circleHandler.setAttachedEvent(new TickEvent.AttachedEvent() {
            @Override
            public void onEvent(Engine engine, long elapsedTime) {
                Logger.Log("Creating the new circles");
                if (tickerSpeed < 2f) tickerSpeed += 0.05f;

                circleHandler.setEventsPerSecond(tickerSpeed);

                int circleCount = ThreadLocalRandom.current().nextInt(1, 5);
                float circleSize = 0.2f; // 0.6f / circleCount;
                int waitTime = 800; //600 * circleCount;
//                if(waitTime < 2000) waitTime = 2000;

                CircleProps prop = new CircleProps(Configs.getColor(R.color.circleColor, MainActivity.this), circleSize, waitTime);
                CircleProps props[] = new CircleProps[circleCount];
                for(int ind = 0; ind < props.length; ind++) {
                    props[ind] = prop;
                }

                game.addCircles(props);
                timeBar.startAnimation((int) (1000.0f / tickerSpeed));
                Logger.Log("Created! %d", game.getCircleCount());
            }
        });

        //Pause the engine before starting it because we don't want the user to see random circles to appear
        game.pauseEngine();
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
                endGame();
            }

            @Override
            public void onBackgroundTouch(PointF position) {
                endGame();
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
    }

    public void endGame() {
        currentScore.setScore(0);
        tickerSpeed = 0.5f;
        game.pauseEngine();
        game.clearAllCircles();
        startButton.upAnimation();
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

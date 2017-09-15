package org.pseudonymous.tapit;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.SurfaceView;

import org.pseudonymous.tapit.components.Circle;
import org.pseudonymous.tapit.components.SplashText;
import org.pseudonymous.tapit.components.SplashView;
import org.pseudonymous.tapit.configs.Configs;

/**
 *
 * Created by smerkous on 9/15/17.
 */

public class SplashActivity extends AppCompatActivity {
    private SplashView splashView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        splashView = findViewById(R.id.splash_view);
        splashView.setTicksPerSecond(60);
        splashView.setBackgroundColor(Configs.getColor(R.color.colorPrimaryDark, this));

        /*splashView.setSplashViewCallbacks(new SplashView.SplashViewCallbacks() {
            @Override
            public void onComplete() {
                startMainActivity();
            }
        });*/

        startMainActivity();

//        splashView.startSplash();

//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    Thread.sleep(300);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//                createCircle();
//                createText();
//            }
//        }).start();
    }

    public void createCircle() {
        Circle circle = new Circle(Configs.getColor(R.color.green, this));
        circle.setParentDims(splashView.getWidth(), splashView.getHeight());
        circle.setScaledPosition(0.5f, 0.5f); //Center the circle onto the screen
        circle.setScaledRadius(0.0f);
        splashView.setCircle(circle);
        circle.startAnimation(0.7f, 300, 1500, 200); //Set the radius to fit half of the screen
    }

    public void createText() {
        SplashText text = new SplashText("TapIt", Configs.getColor(R.color.colorPrimaryDark, this));
        text.setParentDims(splashView.getWidth(), splashView.getHeight());
        text.setScaledPosition(0.5f, 0.5f); //Center the circle onto the screen
        text.setScaledSize(0.0f);
        splashView.setText(text);
        text.startAnimation(0.5f, 300, 1500, 200); //Set the scaled size of the text so that it fits inside the circle
    }

    public void startMainActivity() {
        splashView.destroySplash();
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}

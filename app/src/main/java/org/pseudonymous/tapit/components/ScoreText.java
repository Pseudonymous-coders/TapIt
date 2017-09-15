package org.pseudonymous.tapit.components;

import android.content.Context;
import android.support.v4.widget.TextViewCompat;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 *
 * Created by smerkous on 9/14/17.
 */

public class ScoreText extends android.support.v7.widget.AppCompatTextView {
    private int currentScore = 0;

    public ScoreText(Context context) {
        super(context);
        pseudoConstructor();
    }

    public ScoreText(Context context, AttributeSet attrs) {
        super(context, attrs);
        pseudoConstructor();
    }

    public ScoreText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        pseudoConstructor();
    }

    public void pseudoConstructor() {
        this.setScore(this.currentScore);
    }

    public void setScore(int number) {
        this.currentScore = number;
        this.setText(String.valueOf(number));
    }

    public void incrementScore() {
        this.currentScore++;
        this.setScore(this.currentScore);
    }

    public int getScore() {
        return this.currentScore;
    }
}

package org.pseudonymous.tapit.components;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.view.View;

import org.pseudonymous.tapit.R;

public class SettingsRectangle extends View {

    Drawable shape;

    public SettingsRectangle(Context context) {
        super(context);
        shape = context.getResources().getDrawable(R.drawable.rectangle);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

//        shape.setBounds(left, top, right, bottom);
//        shape.draw(canvas)
    }

    // ... Additional methods omitted for brevity

}
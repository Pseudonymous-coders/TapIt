package org.pseudonymous.tapit.configs;


import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import org.pseudonymous.tapit.R;

public class Configs {
    @SuppressWarnings("deprecation")
    public static int getColor(int id, Context context){
        return context.getResources().getColor(id);
    }

    @SuppressWarnings("deprecation")
    public static Bitmap getBitmap(int id, Context context) {
        try {
            Drawable drawable = context.getDrawable(id);
            Canvas canvas = new Canvas();
            if(drawable == null) throw new NullPointerException();
            Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
            canvas.setBitmap(bitmap);
            drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
            drawable.draw(canvas);
            return bitmap;
        } catch (NullPointerException err) {
            Logger.LogError("Failed rendering drawable into a bitmap, attempting to load it through a Bitmap Factory!");
            return BitmapFactory.decodeResource(context.getResources(), id);
        }
    }

    public static void setPreliminaryScreenFlags(Activity activity) {
        activity.requestWindowFeature(Window.FEATURE_NO_TITLE);
        activity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    public static void setFullScreen(Activity activity) {
        View fullScreenContent = activity.findViewById(R.id.fullscreen_content);
        fullScreenContent.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        );
    }

    public static void lockRotation(Activity activity) {
        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }
}

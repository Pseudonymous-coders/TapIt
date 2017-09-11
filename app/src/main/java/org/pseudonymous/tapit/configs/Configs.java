package org.pseudonymous.tapit.configs;


import android.app.Activity;
public class Configs {
    public static int getColor(int id, Activity activity){
        return activity.getResources().getColor(id);
    }
}

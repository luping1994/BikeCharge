package net.suntrans.bikecharge.utils;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;

import net.suntrans.bikecharge.activity.LoginActivity;
import android.content.Context;
/**
 * Created by Looney on 2017/6/3.
 */

public class ActivityUtils {
    public static void startLogin(Activity activity){
        Intent intent = new Intent(activity, LoginActivity.class);
        intent.putExtra(
                LoginActivity.EXTRA_TRANSITION, LoginActivity.TRANSITION_SLIDE_BOTTOM);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            ActivityOptions transitionActivity = ActivityOptions.makeSceneTransitionAnimation(activity);
            activity.startActivity(intent, transitionActivity.toBundle());
        } else {
            activity.startActivity(intent);
        }
    }
}

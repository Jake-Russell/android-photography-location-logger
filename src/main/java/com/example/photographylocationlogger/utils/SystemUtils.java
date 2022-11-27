package com.example.photographylocationlogger.utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.view.WindowManager;

import java.util.List;


/**
 * System Utils is responsible for handling common system queries, for example, whether an app
 * is currently open in the foreground or not
 *
 * @author Jake Russell
 * @version 1.0
 * @since 15/05/2021
 */
public class SystemUtils {
    /**
     * Determines if the application is currently in the foreground or not, and returns the result
     *
     * @param context the current context
     * @return bool - true if the app is in the foreground, false otherwise
     */
    public static boolean isAppInForeground(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);

        // Get the info from the currently running task
        List<ActivityManager.RunningAppProcessInfo> runningAppProcesses = activityManager.getRunningAppProcesses();
        if (runningAppProcesses != null) {
            final String packageName = context.getPackageName();
            for (ActivityManager.RunningAppProcessInfo appProcess : runningAppProcesses) {
                if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND && appProcess.processName.equals(packageName)) {
                    return true;
                }
            }
        }
        return false;
    }


    /**
     * Hides the keyboard
     *
     * @param activity the current activity
     */
    public static void hideSoftKeyboard(Activity activity) {
        activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }
}

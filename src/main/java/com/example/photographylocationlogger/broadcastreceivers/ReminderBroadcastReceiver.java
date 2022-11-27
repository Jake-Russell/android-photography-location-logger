package com.example.photographylocationlogger.broadcastreceivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.photographylocationlogger.MainActivity;
import com.example.photographylocationlogger.utils.NotificationUtils;
import com.example.photographylocationlogger.utils.SystemUtils;

/**
 * ReminderBroadcastReceiver is responsible for handling the event of an alarm being triggered by
 * the alarm manager. If appropriate (i.e., the app is not already open), then this class will
 * trigger a notification to be sent out using the NotificationUtils class.
 *
 * @author Jake Russell
 * @version 1.0
 * @since 15/05/2021
 */
public class ReminderBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (!SystemUtils.isAppInForeground(context)) {
            NotificationUtils.sendNotification(context, "You still have saved locations!",
                    "Open the app to check out your saved locations", MainActivity.class);
        }
    }
}

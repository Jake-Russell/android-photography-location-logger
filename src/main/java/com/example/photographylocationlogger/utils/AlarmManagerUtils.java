package com.example.photographylocationlogger.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.example.photographylocationlogger.broadcastreceivers.ReminderBroadcastReceiver;

import java.util.Calendar;

/**
 * AlarmManagerUtils is responsible for handling operations related to the AlarmManager, i.e.,
 * setting and cancelling an alarm
 *
 * @author Jake Russell
 * @version 1.0
 * @since 15/05/2021
 */
public class AlarmManagerUtils {
    /**
     * Sets the alarm manager to repeat every day at a specified time
     *
     * @param context the current context
     * @param hour    the hour to set the alarm manager for
     * @param minute  the minute to set the alarm manager for
     * @param second  the second to set the alarm manager for
     */
    public static void setAlarmManager(Context context, int hour, int minute, int second) {
        Intent intent = new Intent(context, ReminderBroadcastReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, second);

        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY, pendingIntent);
    }


    /**
     * Cancels the currently set alarm manager
     *
     * @param context the current context
     */
    public static void cancelAlarmManager(Context context) {
        Intent intent = new Intent(context, ReminderBroadcastReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
    }
}

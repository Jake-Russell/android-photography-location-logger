package com.example.photographylocationlogger.utils;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.photographylocationlogger.R;

import java.util.Random;

/**
 * NotificationUtils is responsible for handling all operations related to Notifications, i.e.,
 * creating a notification channel, and sending notifications.
 *
 * @author Jake Russell
 * @version 1.0
 * @since 16/05/2021
 */
public class NotificationUtils {
    private static final String TAG = "NotificationUtils";
    private static final String NOTIFICATION_CHANNEL_ID = "photographyLocationLoggerNotification";

    /**
     * Creates a notification channel to send notifications on - required for SDK >= 26
     *
     * @param context the current context
     */
    public static void createNotificationChannel(Context context) {
        // Create the notification channel if the SDK >= 26
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelName = "Photography Location Logger Notification Channel";
            String channelDescription = "Notification Channel for Photography Location Logger";
            int channelImportance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, channelImportance);
            channel.setDescription(channelDescription);
            channel.enableLights(true);
            channel.enableVibration(true);
            channel.setLightColor(Color.RED);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);

            context.getSystemService(NotificationManager.class).createNotificationChannel(channel);
        }
    }


    /**
     * Creates and sends a notification to the device, given the required fields
     *
     * @param context      the current context
     * @param title        the title of the notification
     * @param body         the body of the notification
     * @param activityName the activity to open when the notification is pressed
     */
    public static void sendNotification(Context context, String title, String body, Class activityName) {
        Intent intent = new Intent(context, activityName);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 1010, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Creates the notification using a NotificationBuilder
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.photography_location_logger_logo_round)
                .setContentTitle(title)
                .setContentText(body)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        // Sends the notification with a unique id
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
        notificationManagerCompat.notify(new Random().nextInt(), notificationBuilder.build());
    }
}


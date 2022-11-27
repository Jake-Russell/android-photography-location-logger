package com.example.photographylocationlogger.broadcastreceivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.util.Log;
import android.widget.Toast;

import com.example.photographylocationlogger.MainActivity;
import com.example.photographylocationlogger.utils.NotificationUtils;
import com.example.photographylocationlogger.utils.SystemUtils;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.util.List;

/**
 * GeofenceBroadcastReceiver is responsible for handling the event of a geofence being triggered,
 * either by being entered, or by being dwelled. If appropriate (i.e., the app is not already open),
 * then this class will trigger a notification to be sent out using the NotificationUtils class.
 *
 * @author Jake Russell
 * @version 1.0
 * @since 15/05/2021
 */
public class GeofenceBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = "GeofenceBroadcastReceiver";

    /**
     * Called when GeofenceBroadcastReceiver is receiving an Intent broadcast
     *
     * @param context the current context
     * @param intent  the intent sent to the broadcast receiver, which contains a GeofencingEvent
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);

        // If the GeofencingEvent has an error
        if (geofencingEvent.hasError()) {
            Log.e(TAG, "onReceive: Error receiving geofence event: " + geofencingEvent.getErrorCode());
            return;
        }

        // Determine which geofence triggered the broadcast receiver
        List<Geofence> geofenceList = geofencingEvent.getTriggeringGeofences();
        for (Geofence geofence : geofenceList) {
            Log.d(TAG, "onReceive: " + geofence.getRequestId());
        }

        Location location = geofencingEvent.getTriggeringLocation();
        int transitionType = geofencingEvent.getGeofenceTransition();

        // Only send notifications if the app is not already open
        if (!SystemUtils.isAppInForeground(context)) {
            if (transitionType == Geofence.GEOFENCE_TRANSITION_ENTER) {
                Toast.makeText(context, "Geofence Entered...", Toast.LENGTH_SHORT).show();
                NotificationUtils.sendNotification(context, "Location Entered!", "location has been entered", MainActivity.class);

            } else if (transitionType == Geofence.GEOFENCE_TRANSITION_DWELL) {
                Toast.makeText(context, "Geofence Dwelled...", Toast.LENGTH_SHORT).show();
                NotificationUtils.sendNotification(context, "Location Dwelled!", "location has been dwelled", MainActivity.class);
            }
        }
    }
}
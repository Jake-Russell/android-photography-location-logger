package com.example.photographylocationlogger.utils;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.example.photographylocationlogger.broadcastreceivers.GeofenceBroadcastReceiver;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofenceStatusCodes;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.maps.model.LatLng;

/**
 * GeofenceUtils is responsible for handling operations related to Geofences and GeofencingRequests
 *
 * @author Jake Russell
 * @version 1.0
 * @since 15/05/2021
 */
public class GeofenceUtils {
    /**
     * Creates and returns a new GeofencingRequest
     *
     * @param geofence the Geofence to set the GeofencingRequest for
     * @return the newly created GeofencingRequest
     */
    public static GeofencingRequest getGeofencingRequest(Geofence geofence) {
        return new GeofencingRequest.Builder()
                .addGeofence(geofence)
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .build();
    }


    /**
     * Creates and returns a new Geofence
     *
     * @param id              the id for the Geofence
     * @param radius          the radius for the Geofence
     * @param transitionTypes the transition types for the Geofence
     * @return the newly created Geofence
     */
    public static Geofence getGeofence(String id, LatLng latLng, float radius, int transitionTypes) {
        return new Geofence.Builder()
                .setCircularRegion(latLng.latitude, latLng.longitude, radius)
                .setRequestId(id)
                .setTransitionTypes(transitionTypes)
                .setLoiteringDelay(0)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .build();
    }


    /**
     * Creates and returns a new Pending Intent
     *
     * @param context the current context
     * @return the newly created context
     */
    public static PendingIntent getPendingIntent(Context context) {
        Intent intent = new Intent(context, GeofenceBroadcastReceiver.class);
        return PendingIntent.getBroadcast(context, 1010, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }


    /**
     * Returns the error string in the event of an error occurring whilst adding the Geofence
     *
     * @param e the exception thrown
     * @return the error string
     */
    public static String getErrorString(Exception e) {
        if (e instanceof ApiException) {
            ApiException apiException = (ApiException) e;
            switch (apiException.getStatusCode()) {
                case GeofenceStatusCodes
                        .GEOFENCE_NOT_AVAILABLE:
                    return "Geofence is not available";
                case GeofenceStatusCodes
                        .GEOFENCE_TOO_MANY_GEOFENCES:
                    return "Too many Geofences created";
                case GeofenceStatusCodes
                        .GEOFENCE_TOO_MANY_PENDING_INTENTS:
                    return "Too many Pending Intents created";
            }
        }
        return e.getLocalizedMessage();
    }
}

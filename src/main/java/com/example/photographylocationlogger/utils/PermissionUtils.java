package com.example.photographylocationlogger.utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

/**
 * PermissionUtils is responsible for handling operations relating to device permissions, specifically,
 * checking to see if permissions have been granted for a given permission.
 *
 * @author Jake Russell
 * @version 1.0
 * @since 16/05/2021
 */
public class PermissionUtils {
    private static final String TAG = "PermissionUtils";
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private static final int PERMISSIONS_REQUEST_ACCESS_CAMERA = 2;
    private static final int PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 3;
    private static final int PERMISSIONS_REQUEST_BACKGROUND_LOCATION = 4;


    /**
     * Checks to see if the user has granted Fine Location Permissions or not
     *
     * @param context the context
     * @return boolean - true if permissions have been granted, false otherwise
     */
    public static boolean checkLocationPermission(Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }


    /**
     * Checks to see if the user has granted Background Location Permissions or not
     *
     * @param context the context
     * @return boolean - true if permissions have been granted, false otherwise
     */
    public static boolean checkBackgroundPermission(Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }


    /**
     * Checks to see if the user has granted Camera Permissions or not
     *
     * @param context the context
     * @return boolean - true if permissions have been granted, false otherwise
     */
    public static boolean checkCameraPermission(Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED;
    }


    /**
     * Checks to see if the user has granted External Write Permissions or not
     *
     * @param context the context
     * @return boolean - true if the user has granted permissions, false otherwise
     */
    public static boolean checkExternalWritePermissions(Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED;
    }


    /**
     * Handles the result of the request for location permissions.
     *
     * @param requestCode  the request code passed in requestPermissions()
     * @param grantResults the grant results for the corresponding permissions
     * @return String - whether a given permission has been granted or not
     */
    public static String onRequestPermissionsResult(int requestCode, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result array will be empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "onRequestPermissionsResult: Location Access Granted");
                    return "Location Access Granted";
                }
            }

            case PERMISSIONS_REQUEST_ACCESS_CAMERA: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "onRequestPermissionsResult: Camera Access Granted");
                    return "Camera Access Granted";
                }
            }

            case PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "onRequestPermissionsResult: External Write Access Granted");
                    return "External Write Access Granted";
                }
            }

            case PERMISSIONS_REQUEST_BACKGROUND_LOCATION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "onRequestPermissionsResult: Background Location Access Granted");
                    return "Background Location Access Granted";
                }
            }

            default:
                Log.d(TAG, "onRequestPermissionsResult: No Permissions Granted");
                return "No Permissions Granted";
        }
    }
}

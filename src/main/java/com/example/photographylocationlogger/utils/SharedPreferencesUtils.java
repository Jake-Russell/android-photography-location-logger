package com.example.photographylocationlogger.utils;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Map;

/**
 * SharedPreferencesUtils is responsible for handling any operations related to Shared Preferences,
 * for example, saving and deleting.
 *
 * @author Jake Russell
 * @version 1.0
 * @since 16/05/2021
 */
public class SharedPreferencesUtils {
    private static final String SHARED_PREFERENCE_NAME = "Photography Location Logger";

    /**
     * Saves a new key, value pair to Shared Preferences
     *
     * @param context the context
     * @param key     the key to save to Shared Preferences
     * @param value   the value to save to Shared Preferences
     */
    public static void saveToSharedPreferences(Context context, String key, String value) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.apply();
    }


    /**
     * Checks if a given key is already in Shared Preferences or not
     *
     * @param context the context
     * @param key     the key to check if it is in Shared Preferences
     * @return boolean - true if the key is in Shared Preferences already, false otherwise
     */
    public static boolean isInSharedPreferences(Context context, String key) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.contains(key);
    }


    /**
     * Returns a specific value from Shared Preferences, given the key
     *
     * @param context the context
     * @param key     the key to return the value of
     * @return string - the value of the given key
     */
    public static String loadFromSharedPreferences(Context context, String key) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(key, null);
    }


    /**
     * Returns all key, value pairs currently stored in Shared Preferences
     *
     * @param context the context
     * @return map - all key, value pairs currently stored in Shared Preferences
     */
    public static Map<String, ?> getAllSharedPreferences(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getAll();
    }


    /**
     * Removes a key, value pair from Shared Preferences, given the key
     *
     * @param context the context
     * @param key     the key of the key, value pair in Shared Preferences to remove
     */
    public static void removeFromSharedPreferences(Context context, String key) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(key);
        editor.apply();
    }


    /**
     * Removes all current key, value pairs from Shared Preferences
     *
     * @param context the context
     */
    public static void clearAllSharedPreferences(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }
}

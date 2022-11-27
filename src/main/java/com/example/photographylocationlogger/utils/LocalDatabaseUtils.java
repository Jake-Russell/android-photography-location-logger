package com.example.photographylocationlogger.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.photographylocationlogger.data.PhotographyLocation;

/**
 * LocalDatabaseUtils is responsible for handling all operations related to the local SQLite database.
 * It extends SQLiteOpenHelper so that it has access to the required Override methods for creating
 * a local SQLite database.
 *
 * @author Jake Russell
 * @version 1.0
 * @since 16/05/2021
 */
public class LocalDatabaseUtils extends SQLiteOpenHelper {
    private static final String TAG = "LocalDatabaseUtils";

    // Set database name and column names
    private static final String SAVED_PHOTOGRAPHY_LOCATIONS_TABLE_NAME = "Saved_Photography_Locations_Table";
    private static final String PHOTOGRAPHY_LOCATIONS_COLUMN_1 = "Document_Id";
    private static final String PHOTOGRAPHY_LOCATIONS_COLUMN_2 = "Location_Name";
    private static final String PHOTOGRAPHY_LOCATIONS_COLUMN_3 = "Latitude";
    private static final String PHOTOGRAPHY_LOCATIONS_COLUMN_4 = "Longitude";
    private static final String PHOTOGRAPHY_LOCATIONS_COLUMN_5 = "What3Words";

    /**
     * Constructor makes call to super class constructor, with database name. This subsequently makes
     * a call to onCreate or onUpgrade, depending on if the database already exists or not.
     *
     * @param context the current context
     */
    public LocalDatabaseUtils(Context context) {
        super(context, SAVED_PHOTOGRAPHY_LOCATIONS_TABLE_NAME, null, 1);
    }


    /**
     * Called when the database is created for the first time.
     *
     * @param db the database
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        String createPhotographyLocationTable = "CREATE TABLE " + SAVED_PHOTOGRAPHY_LOCATIONS_TABLE_NAME + " (" +
                PHOTOGRAPHY_LOCATIONS_COLUMN_1 + " TEXT PRIMARY KEY, " + PHOTOGRAPHY_LOCATIONS_COLUMN_2 + " TEXT, " +
                PHOTOGRAPHY_LOCATIONS_COLUMN_3 + " REAL, " + PHOTOGRAPHY_LOCATIONS_COLUMN_4 + " REAL, " +
                PHOTOGRAPHY_LOCATIONS_COLUMN_5 + " TEXT)";

        db.execSQL(createPhotographyLocationTable);
    }


    /**
     * Called when the database needs to be upgraded.
     *
     * @param db         the database
     * @param oldVersion the old database version
     * @param newVersion the new database version
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Since in this case I do not want to alter the database structure, a simple Log is made
        // saying that the database already exists.
        Log.e(TAG, "Database Already Exists");
    }


    /**
     * Used to add data, namely a photography location, to the database
     *
     * @param photographyLocation the photography location to add to the database
     * @return boolean - true if the data was saved to the database successfully, false otherwise
     */
    public boolean addData(PhotographyLocation photographyLocation) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues photographyLocationData = new ContentValues();
        photographyLocationData.put(PHOTOGRAPHY_LOCATIONS_COLUMN_1, photographyLocation.getDocumentId());
        photographyLocationData.put(PHOTOGRAPHY_LOCATIONS_COLUMN_2, photographyLocation.getLocationName());
        photographyLocationData.put(PHOTOGRAPHY_LOCATIONS_COLUMN_3, photographyLocation.getLatitude());
        photographyLocationData.put(PHOTOGRAPHY_LOCATIONS_COLUMN_4, photographyLocation.getLongitude());
        photographyLocationData.put(PHOTOGRAPHY_LOCATIONS_COLUMN_5, photographyLocation.getWhat3Words());

        long result = db.insert(SAVED_PHOTOGRAPHY_LOCATIONS_TABLE_NAME, null, photographyLocationData);
        db.close();
        return result != -1;
    }


    /**
     * Used to remove data, namely a photography location, from the database
     *
     * @param photographyLocation the photography location to remove from the database
     */
    public void removeData(PhotographyLocation photographyLocation) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(SAVED_PHOTOGRAPHY_LOCATIONS_TABLE_NAME, "Document_Id=?",
                new String[]{photographyLocation.getDocumentId()});
        db.close();
    }


    /**
     * Used to check if data, namely a photography location, is already in the database or not
     *
     * @param photographyLocation the photography location to check if it is in the database
     * @return boolean - true if the photography location is in the database, false otherwise
     */
    public boolean isInDatabase(PhotographyLocation photographyLocation) {
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT * FROM " + SAVED_PHOTOGRAPHY_LOCATIONS_TABLE_NAME + " WHERE " +
                PHOTOGRAPHY_LOCATIONS_COLUMN_1 + " = '" + photographyLocation.getDocumentId() + "'";
        Cursor cursor = db.rawQuery(query, null);
        return cursor.getCount() > 0;
    }


    /**
     * Used to getAllData from the database
     *
     * @return all photography locations saved in the database
     */
    public Cursor getData() {
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT * FROM " + SAVED_PHOTOGRAPHY_LOCATIONS_TABLE_NAME;
        return db.rawQuery(query, null);
    }
}
package com.example.photographylocationlogger;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.MenuItem;
import android.webkit.WebView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import org.jetbrains.annotations.NotNull;

/**
 * HelpActivity is responsible for holding the WebView for which the help webpage is displayed in.
 *
 * @author Jake Russell
 * @version 1.0
 * @since 18/05/2021
 */
public class HelpActivity extends AppCompatActivity {

    private String path;

    /**
     * onCreate is the main landing point of HelpActivity, and is called to have the activity
     * instantiate its user interface.
     *
     * @param savedInstanceState if non-null, the activity is being re-constructed from a previous
     *                           state defined in the bundle
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(this, R.color.light_blue)));

        this.path = getIntent().getStringExtra("fragmentPath");

        // Initialise the WebView and set the URL
        WebView webView = findViewById(R.id.webView);
        webView.loadUrl("file:///android_asset/help.html");
    }


    /**
     * Handles the event of an item in the options menu being selected
     *
     * @param item the menu item that was selected
     * @return boolean - true to apply the menu item behaviour specified in this method
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        switch (path) {
            case "SavedLocations":
                intent.putExtra("fragmentToLoad", R.id.nav_saved_locations);
                break;
            case "Search":
                intent.putExtra("fragmentToLoad", R.id.nav_search);
                break;
            case "AddReview":
                intent.putExtra("fragmentToLoad", R.id.nav_add_review);
                break;
            case "Home":
                intent.putExtra("fragmentToLoad", R.id.nav_home);
                break;
        }
        startActivity(intent);
        return true;
    }


    /**
     * Called to retrieve per-instance state from an activity before being killed, so that the state
     * can be restored in onCreate(Bundle). For this activity, nothing in addition to the super needs
     * to be saved.
     *
     * @param outState bundle in which the saved state is placed
     */
    @Override
    protected void onSaveInstanceState(@NonNull @NotNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }
}
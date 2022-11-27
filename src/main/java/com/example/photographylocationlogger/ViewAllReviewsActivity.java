package com.example.photographylocationlogger;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.ShareActionProvider;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuItemCompat;

import com.example.photographylocationlogger.data.PhotographyLocation;
import com.example.photographylocationlogger.data.Review;
import com.example.photographylocationlogger.utils.LocalDatabaseUtils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.squareup.picasso.Picasso;
import com.synnapps.carouselview.CarouselView;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * AllReviewsActivity is the activity responsible for showing the user all reviews of a given
 * photography location. It also handles the events of both saving and sharing a given location.
 *
 * @author Jake Russell
 * @version 1.0
 * @since 16/05/2021
 */
public class ViewAllReviewsActivity extends AppCompatActivity {
    private static final String TAG = "AllReviewsActivity";

    private PhotographyLocation photographyLocation;
    private String path;

    private LocalDatabaseUtils localDatabaseUtils;


    /**
     * onCreate is the main landing point of AllReviewsActivity, and is called to
     * have the activity instantiate its user interface.
     *
     * @param savedInstanceState if non-null, the activity is being re-constructed from a previous
     *                           state defined in the bundle
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_all_reviews);

        // Set the action bar
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(this, R.color.light_blue)));

        // Get the photography location object
        photographyLocation = (PhotographyLocation) getIntent().getSerializableExtra("PhotographyLocation");

        path = getIntent().getStringExtra("fragmentPath");

        // Initialise the local database
        localDatabaseUtils = new LocalDatabaseUtils(getApplicationContext());

        initialiseWidgets();
    }


    /**
     * Initialises all widget components required for the activity's UI
     */
    @SuppressLint("ClickableViewAccessibility")
    private void initialiseWidgets() {
        TextView locationName = findViewById(R.id.view_all_reviews_location_name);
        RatingBar ratingBar = findViewById(R.id.view_all_reviews_rating_bar);
        CardView carouselViewCard = findViewById(R.id.view_all_reviews_carousel_view_card);
        CarouselView carouselView = findViewById(R.id.view_all_reviews_carousel_view);
        FloatingActionButton saveLocationFab = findViewById(R.id.view_all_reviews_save_location_fab);
        FloatingActionButton googleSearchFab = findViewById(R.id.view_all_reviews_google_search_fab);

        locationName.setText(photographyLocation.getLocationName());
        ratingBar.setRating((float) photographyLocation.getAverageRating());
        List<String> images = new ArrayList<>();

        ListView listView = findViewById(R.id.view_all_reviews_list_view);
        ViewAllReviewsListAdapter adapter = new ViewAllReviewsListAdapter(this, R.layout.view_all_reviews_adapter_view_layout, (ArrayList<Review>) photographyLocation.getReviews());
        listView.setAdapter(adapter);

        // Get all Image URLs
        for (Review review : photographyLocation.getReviews()) {
            String imageURL = review.getImageURL();
            if (imageURL != null && !imageURL.equals("")) {
                images.add(imageURL);
            }
        }

        // Hide or show the image carousel, based on whether there are images to show or not
        if (images.size() == 0) {
            carouselViewCard.setVisibility(View.GONE);
            carouselView.setVisibility(View.GONE);
        } else {
            carouselView.setPageCount(images.size());
            carouselView.setImageListener((position, imageView) -> Picasso.get().load(images.get(position)).into(imageView));
        }

        // Set the correct image to the Floating Action Button, based on if the location has been saved or not
        if (localDatabaseUtils.isInDatabase(photographyLocation)) {
            saveLocationFab.setImageResource(R.drawable.ic_saved);
            saveLocationFab.setTag("Saved");
        } else {
            saveLocationFab.setImageResource(R.drawable.ic_save);
            saveLocationFab.setTag("");
        }

        // Handle the onClick event of the Floating Action Button accordingly, based on if the location is currently saved or not
        saveLocationFab.setOnClickListener(view -> {
            if (saveLocationFab.getTag() != "Saved") {
                saveLocationFab.setTag("Saved");
                saveLocationFab.setImageResource(R.drawable.ic_saved);
                saveLocation();
            } else {
                saveLocationFab.setTag("");
                saveLocationFab.setImageResource(R.drawable.ic_save);
                removeLocation();
            }
        });

        // Handle the onClick event for the Google Search Floating Action Button
        googleSearchFab.setOnClickListener(v -> {
            Uri webPage = Uri.parse("https://www.google.com/search?q=" + photographyLocation.getLocationName() + "&tbm=isch");
            Intent webIntent = new Intent(Intent.ACTION_VIEW, webPage);
            startActivity(webIntent);
        });

        // Ensures that the ListView can be scrolled within the ScrollView
        listView.setOnTouchListener((v, event) -> {
            int action = event.getAction();
            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    // Disallow ScrollView to intercept touch events.
                    v.getParent().requestDisallowInterceptTouchEvent(true);
                    break;

                case MotionEvent.ACTION_UP:
                    // Allow ScrollView to intercept touch events.
                    v.getParent().requestDisallowInterceptTouchEvent(false);
                    break;
            }

            // Handle ListView touch events.
            v.onTouchEvent(event);
            return true;
        });
    }


    /**
     * Saves a photography location to the local SQLite database
     */
    private void saveLocation() {
        boolean locationSavedSuccessfully = localDatabaseUtils.addData(photographyLocation);

        if (locationSavedSuccessfully) {
            Toast.makeText(this, "Location Successfully Saved", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show();
        }
    }


    /**
     * Removes a photography location from the local SQLite database
     */
    private void removeLocation() {
        localDatabaseUtils.removeData(photographyLocation);
        Toast.makeText(this, "Location successfully unsaved", Toast.LENGTH_SHORT).show();
    }


    /**
     * Sets the share intent for the Share Action Provider
     *
     * @return the configured share intent for the Share Action Provider
     */
    private Intent setShareIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        Geocoder geocoder = new Geocoder(this);
        List<Address> addresses = new ArrayList<>();
        String locationUrl = "https://www.google.com/maps/search/?api=1&query=" + photographyLocation.getLatitude() + "," + photographyLocation.getLongitude();
        try {
            addresses = geocoder.getFromLocation(photographyLocation.getLatitude(), photographyLocation.getLongitude(), 1);
        } catch (IOException e) {
            Log.e(TAG, "Invalid LatLng");
        }

        shareIntent.putExtra(Intent.EXTRA_TEXT, "Check out the photography location " + photographyLocation.getLocationName() + "!\n\n" +
                "It's in " + addresses.get(0).getSubAdminArea() + ", " + addresses.get(0).getCountryName() +
                " and has been rated " + photographyLocation.getAverageRating() + " stars." + "\n\n" +
                "You can view it here: " + locationUrl);
        return shareIntent;
    }


    /**
     * Initialises the custom menu and Share Action Provider
     *
     * @param menu the options menu to place the Share Action Provider
     * @return boolean - true for the menu to be displayed
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.share_menu, menu);
        MenuItem menuItem = menu.findItem(R.id.action_share);
        ShareActionProvider shareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);

        shareActionProvider.setShareIntent(setShareIntent());
        return super.onCreateOptionsMenu(menu);
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
        if (path.equals("SavedLocations")) {
            intent.putExtra("fragmentToLoad", R.id.nav_saved_locations);
        } else {
            intent.putExtra("fragmentToLoad", R.id.nav_search);
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
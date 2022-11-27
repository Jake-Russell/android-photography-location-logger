package com.example.photographylocationlogger;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.example.photographylocationlogger.ui.addreview.AddReviewFragment;
import com.example.photographylocationlogger.ui.home.HomeFragment;
import com.example.photographylocationlogger.ui.savedlocations.SavedLocationsFragment;
import com.example.photographylocationlogger.ui.search.MapFragment;
import com.example.photographylocationlogger.utils.NotificationUtils;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;

import org.jetbrains.annotations.NotNull;

/**
 * MainActivity is the main landing point for the application. It is responsible for initialising
 * the main UI, most notably, the side navigation pane.
 *
 * @author Jake Russell
 * @version 1.0
 * @since 18/05/2021
 */
public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private static final String TAG = "MainActivity";
    private DrawerLayout drawer;
    private NavigationView navigationView;

    /**
     * onCreate is the main landing point of MainActivity, and is called to have the activity
     * instantiate its user interface.
     *
     * @param savedInstanceState if non-null, the activity is being re-constructed from a previous
     *                           state defined in the bundle
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Initialize Firebase Auth with an anonymous user
        FirebaseAuth.getInstance().signInAnonymously()
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInAnonymously:success");
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "signInAnonymously:failure", task.getException());
                    }
                });

        // Create a Notification Channel if one does not already exist
        NotificationUtils.createNotificationChannel(this);

        // Initialise the Navigation View
        drawer = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Initialise the Action Bar Toggle
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        // If this is the first time opening the app, load the home fragment, else, if the screen
        // has been rotated instead, load the previously opened fragment
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeFragment()).commit();
            navigationView.setCheckedItem(R.id.nav_home);
        } else {
            Fragment fragment = getSupportFragmentManager().getFragment(savedInstanceState, "FRAGMENT_TO_LOAD");
            switchFragment(fragment.getId());
        }

        // If the back button has been pressed from a different activity, then the intent will have
        // a fragmentToLoad key, which indicates which fragment was previously opened
        if (getIntent().hasExtra("fragmentToLoad")) {
            switchFragment(getIntent().getExtras().getInt("fragmentToLoad"));
        }
    }


    /**
     * Switches the fragment to the given resource id, and changes the navigation view's checked
     * item accordingly
     *
     * @param id the resource id of the fragment to switch to
     */
    private void switchFragment(int id) {
        switch (id) {
            case R.id.nav_home:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeFragment(), "HOME").commit();
                navigationView.setCheckedItem(R.id.nav_home);
                break;
            case R.id.nav_search:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new MapFragment(), "SEARCH").commit();
                navigationView.setCheckedItem(R.id.nav_search);
                break;
            case R.id.nav_add_review:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new AddReviewFragment(), "ADD_REVIEW").commit();
                navigationView.setCheckedItem(R.id.nav_add_review);
                break;
            case R.id.nav_saved_locations:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new SavedLocationsFragment(), "SAVED_LOCATIONS").commit();
                navigationView.setCheckedItem(R.id.nav_saved_locations);
                break;
            case R.id.nav_help:
                Intent intent = new Intent(this, HelpActivity.class);
                int checkedItem = navigationView.getCheckedItem().getItemId();
                switch (checkedItem) {
                    case R.id.nav_home:
                        intent.putExtra("fragmentPath", "Home");
                        break;
                    case R.id.nav_search:
                        intent.putExtra("fragmentPath", "Search");
                        break;
                    case R.id.nav_add_review:
                        intent.putExtra("fragmentPath", "AddReview");
                        break;
                    case R.id.nav_saved_locations:
                        intent.putExtra("fragmentPath", "SavedLocations");
                        break;
                }
                startActivity(intent);
                break;
        }
    }


    /**
     * Called when an item in the navigation menu is selected
     *
     * @param item the selected item in the navigation menu
     * @return true to display the item as the selected item
     */
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switchFragment(item.getItemId());
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    /**
     * Called when the activity has detected the user's press of the back key
     */
    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
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
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if (fragment instanceof HomeFragment) {
            getSupportFragmentManager().putFragment(outState, "FRAGMENT_TO_LOAD", fragment);
        } else if (fragment instanceof MapFragment) {
            getSupportFragmentManager().putFragment(outState, "FRAGMENT_TO_LOAD", fragment);
        } else if (fragment instanceof AddReviewFragment) {
            getSupportFragmentManager().putFragment(outState, "FRAGMENT_TO_LOAD", fragment);
        } else if (fragment instanceof SavedLocationsFragment) {
            getSupportFragmentManager().putFragment(outState, "FRAGMENT_TO_LOAD", fragment);
        }
    }
}
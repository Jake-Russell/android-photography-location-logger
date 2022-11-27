package com.example.photographylocationlogger.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.photographylocationlogger.HelpActivity;
import com.example.photographylocationlogger.R;
import com.example.photographylocationlogger.ui.addreview.AddReviewFragment;
import com.example.photographylocationlogger.ui.savedlocations.SavedLocationsFragment;
import com.example.photographylocationlogger.ui.search.MapFragment;
import com.google.android.material.navigation.NavigationView;

/**
 * HomeFragment is the fragment responsible for showing the user the main home screen of the
 * application. It also handles the events of navigating to other UI screens within the app.
 *
 * @author Jake Russell
 * @version 1.0
 * @since 17/05/2021
 */
public class HomeFragment extends Fragment {
    private static final String TAG = "HomeFragment";

    /**
     * onCreateView is the main landing point of HomeFragment, and is called to have the
     * fragment instantiate its user interface view.
     *
     * @param inflater           the LayoutInflater object used to inflate views in the fragment
     * @param container          if non-null, this is the parent view that the fragment's UI is attached to
     * @param savedInstanceState if non-null, the fragment is being re-constructed from a previous
     *                           state defined in the bundle
     * @return view - the view for the fragment's UI
     */
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);
        initialiseWidgets(view);
        return view;
    }


    /**
     * Initialises all widget components required for the fragment's UI
     *
     * @param view the inflated view for the fragment's UI
     */
    private void initialiseWidgets(View view) {
        NavigationView navigationView = requireActivity().findViewById(R.id.nav_view);

        Button homeButtonSearch = view.findViewById(R.id.home_button_search);
        Button homeButtonAddReview = view.findViewById(R.id.home_button_add_review);
        Button homeButtonSavedLocations = view.findViewById(R.id.home_button_saved_locations);
        Button homeButtonHelp = view.findViewById(R.id.home_button_help);

        // Set relevant onClickListeners to change the navigation checked item and switch fragment
        homeButtonSearch.setOnClickListener(v -> {
            navigationView.setCheckedItem(R.id.nav_search);
            FragmentTransaction fragmentTransaction = requireActivity().getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container, new MapFragment()).commit();
        });

        homeButtonAddReview.setOnClickListener(v -> {
            navigationView.setCheckedItem(R.id.nav_add_review);
            FragmentTransaction fragmentTransaction = requireActivity().getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container, new AddReviewFragment()).commit();
        });

        homeButtonSavedLocations.setOnClickListener(v -> {
            navigationView.setCheckedItem(R.id.nav_saved_locations);
            FragmentTransaction fragmentTransaction = requireActivity().getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container, new SavedLocationsFragment()).commit();
        });

        homeButtonHelp.setOnClickListener(v -> {
            navigationView.setCheckedItem(R.id.nav_help);
            Intent intent = new Intent(getActivity(), HelpActivity.class);
            startActivity(intent);
        });
    }
}
package com.example.photographylocationlogger.ui.savedlocations;

import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.photographylocationlogger.R;
import com.example.photographylocationlogger.SavedLocationsListAdapter;
import com.example.photographylocationlogger.data.PhotographyLocation;
import com.example.photographylocationlogger.utils.AlarmManagerUtils;
import com.example.photographylocationlogger.utils.LocalDatabaseUtils;
import com.example.photographylocationlogger.utils.SharedPreferencesUtils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

/**
 * SavedLocationsFragment is the fragment responsible for showing the user all of their saved locations.
 * It also handles the events of turning notifications for reminders of saved locations on and off.
 *
 * @author Jake Russell
 * @version 1.0
 * @since 15/05/2021
 */
public class SavedLocationsFragment extends Fragment {
    private static final String TAG = "SavedLocationsFragment";

    private ArrayList<PhotographyLocation> savedLocationList = new ArrayList<>();

    /**
     * onCreateView is the main landing point of SavedLocationsFragment, and is called to have the
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

        View view = inflater.inflate(R.layout.fragment_saved_locations, container, false);

        // Instantiate a new LocalDatabaseUtils object and read in all saved photography locations
        // from the SQLite database
        LocalDatabaseUtils localDatabaseUtils = new LocalDatabaseUtils(getContext());
        Cursor data = localDatabaseUtils.getData();
        while (data.moveToNext()) {
            PhotographyLocation photographyLocation = new PhotographyLocation(data.getString(0),
                    data.getString(1),
                    data.getDouble(2),
                    data.getDouble(3),
                    data.getString(4));
            this.savedLocationList.add(photographyLocation);
        }
        data.close();

        initialiseWidgets(view);
        return view;
    }


    /**
     * Initialises all widget components required for the fragment's UI
     *
     * @param view the inflated view for the fragment's UI
     */
    private void initialiseWidgets(View view) {
        ListView listView = view.findViewById(R.id.saved_locations_list_view);

        // Set the custom listView adapter
        SavedLocationsListAdapter adapter = new SavedLocationsListAdapter(requireActivity().getApplicationContext(), R.layout.saved_locations_adapter_view_layout2, this.savedLocationList);
        listView.setAdapter(adapter);

        // Initialise the FloatingActionButton, and set the correct image resource and tag based on
        // the value of "Notifications Active" held in Shared Preferences
        FloatingActionButton savedLocationsNotifications = view.findViewById(R.id.saved_locations_notifications_fab);
        if (SharedPreferencesUtils.loadFromSharedPreferences(requireContext(), "Notifications Active") == null ||
                !SharedPreferencesUtils.loadFromSharedPreferences(requireContext(), "Notifications Active").equals("True")) {
            savedLocationsNotifications.setImageResource(R.drawable.ic_notifications_disabled);
            savedLocationsNotifications.setTag("");
        } else {
            savedLocationsNotifications.setImageResource(R.drawable.ic_notifications_active);
            savedLocationsNotifications.setTag("Notifications Active");
        }

        // Set onClickListener for the FloatingActionButton, and handle this accordingly
        savedLocationsNotifications.setOnClickListener(v -> {
            if (savedLocationsNotifications.getTag() != "Notifications Active") {
                savedLocationsNotifications.setTag("Notifications Active");
                savedLocationsNotifications.setImageResource(R.drawable.ic_notifications_active);
                setNotifications();
            } else {
                savedLocationsNotifications.setTag("");
                savedLocationsNotifications.setImageResource(R.drawable.ic_notifications_disabled);
                removeNotifications();
            }
        });
    }


    /**
     * Updates Shared Preferences, and sets reminder notifications to be triggered at 12PM each day
     */
    private void setNotifications() {
        SharedPreferencesUtils.saveToSharedPreferences(requireContext(), "Notifications Active", "True");
        Toast.makeText(getContext(), "Notifications Set", Toast.LENGTH_SHORT).show();

        AlarmManagerUtils.setAlarmManager(requireContext(), 12, 0, 0);
    }


    /**
     * Updates Shared Preferences, and disables reminder notifications by cancelling the alarm
     * manager
     */
    private void removeNotifications() {
        SharedPreferencesUtils.saveToSharedPreferences(requireContext(), "Notifications Active", "False");
        Toast.makeText(getContext(), "Notifications Disabled", Toast.LENGTH_SHORT).show();

        AlarmManagerUtils.cancelAlarmManager(requireContext());
    }
}
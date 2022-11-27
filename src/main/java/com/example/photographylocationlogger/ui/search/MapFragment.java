package com.example.photographylocationlogger.ui.search;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.photographylocationlogger.CustomInfoWindowAdapter;
import com.example.photographylocationlogger.R;
import com.example.photographylocationlogger.ViewAllReviewsActivity;
import com.example.photographylocationlogger.data.PhotographyLocation;
import com.example.photographylocationlogger.data.Review;
import com.example.photographylocationlogger.utils.GeofenceUtils;
import com.example.photographylocationlogger.utils.PermissionUtils;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * MapFragment is the fragment responsible for displaying a Google Maps instance to the user, with
 * markers on all reviewed photography locations. It is also responsible for handling the events of
 * searching for a location, getting the user's current location, changing the map type, and clicking
 * on a marker.
 *
 * @author Jake Russell
 * @version 1.0
 * @since 17/05/2021
 */
public class MapFragment extends Fragment implements GoogleMap.OnInfoWindowClickListener, GoogleMap.OnMarkerClickListener {
    private static final String TAG = "MapFragment";
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private static final int PERMISSIONS_REQUEST_ACCESS_BACKGROUND_LOCATION = 4;

    private static final float DEFAULT_ZOOM = 13f;
    private static final int DEFAULT_RADIUS = 1000;

    private FusedLocationProviderClient fusedLocationProviderClient;
    private GoogleMap map;

    private GeofencingClient geofencingClient;

    /**
     * onCreateView is the main landing point of MapFragment, and is called to have the
     * fragment instantiate its user interface view.
     *
     * @param inflater           the LayoutInflater object used to inflate views in the fragment
     * @param container          if non-null, this is the parent view that the fragment's UI is attached to
     * @param savedInstanceState if non-null, the fragment is being re-constructed from a previous
     *                           state defined in the bundle
     * @return view - the view for the fragment's UI
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_map, container, false);

        // Prompt the user for fine access location permission
        if (!PermissionUtils.checkLocationPermission(requireContext())) {
            requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }

        // Prompt the user for background location permission for SDK >= 29
        if (Build.VERSION.SDK_INT >= 29) {
            if (!PermissionUtils.checkBackgroundPermission(requireContext())) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION}, PERMISSIONS_REQUEST_ACCESS_BACKGROUND_LOCATION);
            }
        }

        // Initialise FusedLocationProviderClient
        this.fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        // Initialise GeofencingClient
        this.geofencingClient = LocationServices.getGeofencingClient(requireActivity());

        initialiseMap();
        initialiseWidgets(view);

        return view;
    }


    /**
     * Initialises all widget components required for the fragment's UI
     *
     * @param view the inflated view for the fragment's UI
     */
    private void initialiseWidgets(View view) {
        EditText searchText = view.findViewById(R.id.map_search_bar);
        ImageView gps = view.findViewById(R.id.map_gps_icon);
        ImageView mapType = view.findViewById(R.id.map_map_type_icon);

        // Set onEditorActionListener, including searching with the enter key
        searchText.setOnEditorActionListener((v, actionId, keyEvent) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE
                    || keyEvent.getAction() == KeyEvent.ACTION_DOWN || keyEvent.getAction() == KeyEvent.KEYCODE_ENTER) {
                searchMap(searchText.getText().toString());
            }
            return false;
        });

        // Set onClickListener for GPS button
        gps.setOnClickListener(v -> getDeviceLocation());

        // Set onClickListener for Map Type button
        mapType.setOnClickListener(v -> {
            if (map.getMapType() == GoogleMap.MAP_TYPE_HYBRID) {
                map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            } else {
                map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
            }
        });
    }


    /**
     * Initialises the map
     */
    private void initialiseMap() {
        SupportMapFragment supportMapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map_google_map);

        if (supportMapFragment != null) {
            supportMapFragment.getMapAsync(googleMap -> {
                // When map is loaded
                Log.d(TAG, "onMapReady: map is ready");
                this.map = googleMap;
                this.map.setMapType(GoogleMap.MAP_TYPE_HYBRID);

                this.map.setOnMarkerClickListener(this);
                this.map.setOnInfoWindowClickListener(this);

                // Places Markers on Map
                placeMarkers();

                // Get the current location of the device and set the position of the map
                getDeviceLocation();

                this.map.getUiSettings().setMyLocationButtonEnabled(false);
                this.map.getUiSettings().setZoomControlsEnabled(true);
            });
        } else {
            Log.e(TAG, "initialiseMap: An error occurred when initialising the map");
        }
    }


    /**
     * Handles the result of the request for location permissions.
     *
     * @param requestCode  the request code
     * @param permissions  the permissions
     * @param grantResults the grant results
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if ("Location Access Granted".equals(PermissionUtils.onRequestPermissionsResult(requestCode, grantResults))) {
            requireActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new MapFragment()).commit();
        }
        if ("Background Location Access Granted".equals(PermissionUtils.onRequestPermissionsResult(requestCode, grantResults))) {
            requireActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new MapFragment()).commit();
        }
    }


    /**
     * Searches the map for the location specified in the search text box, and moves the camera
     * accordingly.
     */
    private void searchMap(String searchText) {
        Geocoder geocoder = new Geocoder(requireContext());
        List<Address> searchedLocations = new ArrayList<>();
        try {
            searchedLocations = geocoder.getFromLocationName(searchText, 1);
        } catch (IOException e) {
            Log.e(TAG, "geoLocate: IOException: " + e.getMessage());
        }

        if (searchedLocations.size() > 0) {
            Address address = searchedLocations.get(0);
            moveCamera(new LatLng(address.getLatitude(), address.getLongitude()));
        } else {
            Toast.makeText(requireContext(), "Unable to search for location " + searchText, Toast.LENGTH_SHORT).show();
        }
    }


    /**
     * Gets the current location of the device, and positions the map's camera accordingly.
     */
    private void getDeviceLocation() {
        Log.d(TAG, "getDeviceLocation: Getting the device's current location");
        try {
            // If Fine Location Permission has been granted
            if (PermissionUtils.checkLocationPermission(requireContext())) {
                this.map.setMyLocationEnabled(true);
                Task<Location> locationResult = this.fusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(requireActivity(), task -> {
                    // If task is successful, move camera, else produce an error
                    if (task.isSuccessful() && task.getResult() != null) {
                        Log.d(TAG, "onComplete: location found");
                        Location currentLocation = task.getResult();
                        moveCamera(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()));
                        this.map.setMyLocationEnabled(true);
                    } else {
                        Log.d(TAG, "onComplete: current location is null");
                        Toast.makeText(requireActivity(), "Unable to get current location", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage(), e);
        }
    }


    /**
     * Moves the camera on the map to a given latitude and longitude
     *
     * @param latLng the latitude and longitude to move the camera to
     */
    private void moveCamera(LatLng latLng) {
        this.map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, MapFragment.DEFAULT_ZOOM));
        this.map.setInfoWindowAdapter(new CustomInfoWindowAdapter(requireContext()));
    }


    /**
     * Places markers on the map for all saved photography locations in the Firestore database
     */
    private void placeMarkers() {
        FirebaseFirestore.getInstance().collection("Photography Locations").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot doc : task.getResult()) {
                    // For each document in the database, get all required data
                    String locationName = Objects.requireNonNull(doc.get("Location Name")).toString();
                    double latitude = Double.parseDouble(Objects.requireNonNull(doc.get("Latitude")).toString());
                    double longitude = Double.parseDouble(Objects.requireNonNull(doc.get("Longitude")).toString());
                    List<Double> locationRatings = (List<Double>) doc.get("Ratings");
                    List<String> locationReviews = (List<String>) doc.get("Reviews");
                    List<String> locationImageUrls = (List<String>) doc.get("Image URLs");
                    String what3words = Objects.requireNonNull(doc.get("What3Words")).toString();

                    // Create a new Review Object for each Rating, Review, and Image URL
                    List<Review> reviews = new ArrayList<>();
                    if (locationRatings != null && locationReviews != null && locationImageUrls != null) {
                        for (int i = 0; i < Objects.requireNonNull(locationReviews).size(); i++) {
                            reviews.add(new Review(Double.parseDouble(String.valueOf(locationRatings.get(i))),
                                    locationReviews.get(i), locationImageUrls.get(i)));
                        }
                    }

                    // Create a new Photography Location Object
                    PhotographyLocation photographyLocation = new PhotographyLocation(doc.getId(), locationName, latitude,
                            longitude, reviews, what3words);

                    Log.d(TAG, "Photography Location with ID " + doc.getId() + " = " + doc.getData());

                    // Create new LatLng object for the photography location and add the corresponding marker with
                    // the photography location as the tag
                    LatLng latLng = new LatLng(photographyLocation.getLatitude(), photographyLocation.getLongitude());
                    Marker marker = this.map.addMarker(new MarkerOptions().position(latLng));
                    marker.setTag(photographyLocation);

                    addGeofence(latLng, doc.getId());
                }
            }
        });
    }


    /**
     * Adds a geofence around a photography location, given a LatLng, and an ID for the geofence,
     * which in this case is the same ID as ID of the photography location.
     *
     * @param latLng the LatLng Object to set the center the geofence on
     * @param id     the unique ID for the geofence
     */
    private void addGeofence(LatLng latLng, String id) {
        // Create a new Geofence, and set the transition types as enter or dwell
        Geofence geofence = GeofenceUtils.getGeofence(id, latLng, DEFAULT_RADIUS,
                Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_DWELL);
        GeofencingRequest geofencingRequest = GeofenceUtils.getGeofencingRequest(geofence);
        PendingIntent pendingIntent = GeofenceUtils.getPendingIntent(requireContext());

        // If the SDK >= 29, then both Fine Access Location and Background Location permissions are required
        // If the SDK < 29, then only Fine Access Location permissions are required
        if (Build.VERSION.SDK_INT >= 29 && PermissionUtils.checkLocationPermission(getActivity()) &&
                PermissionUtils.checkBackgroundPermission(getActivity()) ||
                Build.VERSION.SDK_INT < 29 && PermissionUtils.checkLocationPermission(getActivity())) {
            this.geofencingClient.addGeofences(geofencingRequest, pendingIntent)
                    .addOnSuccessListener(unused -> Log.i(TAG, "onSuccess: Geofence Added..."))
                    .addOnFailureListener(e -> {
                        String errorMessage = GeofenceUtils.getErrorString(e);
                        Log.e(TAG, "onFailure: " + "geofence not created: " + errorMessage);
                    });
        }
    }


    /**
     * Called when a marker has been clicked or tapped
     *
     * @param marker the marker that was clicked or tapped
     * @return true if the listener has consumed the event, i.e., the default behaviour should not
     * occur, false otherwise
     */
    @Override
    public boolean onMarkerClick(@NotNull Marker marker) {
        // Handler required to move the camera to the marker's position
        new Handler().postDelayed(() -> moveCamera(marker.getPosition()), 0);
        return false;
    }


    /**
     * Called when the marker's info window is clicked
     *
     * @param marker the marker of the info window that was clicked
     */
    @Override
    public void onInfoWindowClick(Marker marker) {
        Intent intent = new Intent(getActivity(), ViewAllReviewsActivity.class);
        intent.putExtra("PhotographyLocation", (PhotographyLocation) marker.getTag());
        intent.putExtra("fragmentPath", "Search");
        startActivity(intent);
    }
}
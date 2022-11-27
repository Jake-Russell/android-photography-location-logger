package com.example.photographylocationlogger;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.photographylocationlogger.data.PhotographyLocation;
import com.example.photographylocationlogger.data.Review;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * SavedLocationsListAdapter is the custom list adapter for viewing all of the user's saved locations.
 * It creates the view and populates it with the corresponding data for each saved location.
 *
 * @author Jake Russell
 * @version 1.0
 * @since 18/05/2021
 */
public class SavedLocationsListAdapter extends ArrayAdapter<PhotographyLocation> {
    private static final String TAG = "SavedPhotographyLocationListAdapter";

    private Context context;
    private int resource;

    /**
     * Constructor sets the context and resource
     *
     * @param context  the context
     * @param resource the resource
     * @param objects  the list of saved photography locations
     */
    public SavedLocationsListAdapter(@NonNull Context context, int resource, @NonNull ArrayList<PhotographyLocation> objects) {
        super(context, resource, objects);
        this.context = context;
        this.resource = resource;
    }


    /**
     * Gets a View that displays the data at the specified position in the data set.
     *
     * @param position    the position of the item within the adapter's data set of the item whose view
     *                    is needed
     * @param convertView the old view to reuse, if possible
     * @param parent      the parent this view will eventually be attached to
     * @return a view corresponding to the data at the specified position
     */
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        // Get the location's information
        String documentId = getItem(position).getDocumentId();
        String locationName = getItem(position).getLocationName();
        double latitude = getItem(position).getLatitude();
        double longitude = getItem(position).getLongitude();
        List<Review> reviews = getItem(position).getReviews();
        String what3words = getItem(position).getWhat3Words();

        // Create the Photography Location Object with the information
        PhotographyLocation photographyLocation = new PhotographyLocation(documentId, locationName, latitude, longitude, reviews, what3words);

        // Create the ViewHolder object
        ViewHolder holder;

        // Check if the old view is null, and inflate a new view if it is
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(this.context);
            convertView = inflater.inflate(this.resource, parent, false);

            holder = new ViewHolder(convertView, photographyLocation);
            holder.locationName = convertView.findViewById(R.id.saved_location_title);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.locationName.setText(photographyLocation.getLocationName());

        // Set onClickListener for a Photography Location
        LinearLayout savedLocation = convertView.findViewById(R.id.saved_location);
        savedLocation.setOnClickListener(v ->
                FirebaseFirestore.getInstance().collection("Photography Locations").document(holder.photographyLocation.getDocumentId())
                        .get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            // Get the most recent version of the photography location from the
                            // database, and start an intent to the AllReviews page for the given
                            // photography location
                            String firebaseLocationName = Objects.requireNonNull(document.get("Location Name")).toString();
                            double firebaseLatitude = Double.parseDouble(Objects.requireNonNull(document.get("Latitude")).toString());
                            double firebaseLongitude = Double.parseDouble(Objects.requireNonNull(document.get("Longitude")).toString());
                            List<Double> firebaseLocationRatings = (List<Double>) document.get("Ratings");
                            List<String> firebaseLocationReviews = (List<String>) document.get("Reviews");
                            List<String> firebaseLocationImageUrls = (List<String>) document.get("Image URLs");
                            String firebaseWhat3Words = Objects.requireNonNull(document.get("What3Words")).toString();

                            List<Review> firebaseReviews = new ArrayList<>();
                            for (int i = 0; i < firebaseLocationReviews.size(); i++) {
                                firebaseReviews.add(new Review(Double.parseDouble(String.valueOf(firebaseLocationRatings.get(i))), firebaseLocationReviews.get(i), firebaseLocationImageUrls.get(i)));
                            }

                            holder.photographyLocation = new PhotographyLocation(document.getId(), firebaseLocationName, firebaseLatitude,
                                    firebaseLongitude, firebaseReviews, firebaseWhat3Words);

                            Intent intent = new Intent(context, ViewAllReviewsActivity.class);
                            intent.putExtra("PhotographyLocation", holder.photographyLocation);
                            intent.putExtra("fragmentPath", "SavedLocations");
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            context.startActivity(intent);

                            Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                        } else {
                            Log.d(TAG, "No such document");
                        }
                    } else {
                        Log.d(TAG, "get failed with ", task.getException());
                    }
                }));

        return convertView;
    }


    /**
     * Holds variables in a View
     */
    class ViewHolder implements OnMapReadyCallback {
        MapView mapView;
        GoogleMap map;
        TextView locationName;
        View layout;
        PhotographyLocation photographyLocation;

        /**
         * Constructor initialises the ViewHolder
         *
         * @param itemView            the View
         * @param photographyLocation the PhotographyLocation
         */
        public ViewHolder(@NonNull View itemView, PhotographyLocation photographyLocation) {
            this.layout = itemView;
            this.photographyLocation = photographyLocation;
            this.locationName = layout.findViewById(R.id.saved_location_title);

            this.mapView = this.layout.findViewById(R.id.saved_location_map_view);
            this.mapView.onCreate(null);
            this.mapView.onResume();
            this.mapView.setClickable(false);
            this.mapView.getMapAsync(this);
        }


        /**
         * Called when the map is ready to be used
         *
         * @param googleMap a non-null instance of a GoogleMap associated with the MapView
         */
        @Override
        public void onMapReady(@NotNull GoogleMap googleMap) {
            MapsInitializer.initialize(context);
            this.map = googleMap;
            setMapLocation();
        }


        /**
         * Sets the map's location to the location of the photography location, and adds a marker
         * at this location
         */
        private void setMapLocation() {
            if (this.map == null) return;

            // Add a marker for this item and set the camera
            LatLng latLng = new LatLng(this.photographyLocation.getLatitude(), this.photographyLocation.getLongitude());
            this.map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 13f));
            this.map.addMarker(new MarkerOptions().position(latLng));

            // Set the map type back to normal.
            this.map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        }
    }
}

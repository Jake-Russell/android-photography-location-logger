package com.example.photographylocationlogger.ui.addreview;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.photographylocationlogger.AsyncResponse;
import com.example.photographylocationlogger.R;
import com.example.photographylocationlogger.ViewAllReviewsActivity;
import com.example.photographylocationlogger.What3WordsTask;
import com.example.photographylocationlogger.data.PhotographyLocation;
import com.example.photographylocationlogger.data.Review;
import com.example.photographylocationlogger.ui.home.HomeFragment;
import com.example.photographylocationlogger.ui.search.MapFragment;
import com.example.photographylocationlogger.utils.PermissionUtils;
import com.example.photographylocationlogger.utils.SystemUtils;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static android.app.Activity.RESULT_OK;

/**
 * AddReviewFragment is the fragment responsible for allowing a user to add a new review of a
 * location. It is also responsible for determining whether a location is a new location or a
 * location that has already been reviewed, and handling either event accordingly.
 *
 * @author Jake Russell
 * @version 1.0
 * @since 17/05/2021
 */
public class AddReviewFragment extends Fragment implements AsyncResponse {
    private static final String TAG = "AddReviewFragment";
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private static final int PERMISSIONS_REQUEST_ACCESS_CAMERA = 2;
    private static final int IMAGE_REQUEST = 3;
    private static final int CAMERA_REQUEST = 4;

    private FirebaseFirestore database = FirebaseFirestore.getInstance();
    private HashMap<String, Object> dataToAdd = new HashMap<>();

    private FusedLocationProviderClient fusedLocationProviderClient;

    private Button uploadImageButton;
    private Button takeImageButton;
    private Button removeImageButton;
    private EditText locationName;
    private EditText review;
    private RatingBar ratingBar;
    private ImageView reviewImageview;
    private LatLng latLng;
    private Dialog locationSuggestionDialog;

    private Uri imageUri = null;
    private File imageFile;
    private String fileName;
    private String url;

    private boolean locationSuggestionsShown = false;
    private boolean locationAlreadyExists = false;
    private String existingDocumentId = null;
    private int shownDialogCount = 0;


    /**
     * onCreateView is the main landing point of AddReviewFragment, and is called to have the
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

        View view = inflater.inflate(R.layout.fragment_add_review, container, false);

        // If savedInstanceState != null, then the fragment needs to restore some data from a previous
        // state. Namely, this data is whether the location suggestions have already been shown,
        // whether the location already exists, the image Uri, and the filename
        if (savedInstanceState != null) {
            this.locationSuggestionsShown = savedInstanceState.getBoolean("locationSuggestionsShown");
            this.locationAlreadyExists = savedInstanceState.getBoolean("locationAlreadyExists");
            try {
                this.imageUri = Uri.parse(savedInstanceState.getString("imageUri"));
                this.url = this.imageUri.toString();
                this.fileName = savedInstanceState.getString("filename");
                this.existingDocumentId = savedInstanceState.getString("existingDocumentId");
            } catch (NullPointerException e) {
                Log.d(TAG, "onCreateView: Image URI is null");
            }
        }

        initialiseWidgets(view);

        // Initialise FusedLocationProviderClient
        this.fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        // Prompt the user for fine access location permission.
        if (!PermissionUtils.checkLocationPermission(requireActivity().getApplicationContext())) {
            requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }

        // Get the current location of the device and set the position of the map.
        getDeviceLocation();

        return view;
    }


    /**
     * Initialises all widget components required for the fragment's UI
     *
     * @param view the inflated view for the fragment's UI
     */
    private void initialiseWidgets(View view) {
        Button submitReviewButton = view.findViewById(R.id.add_review_submit_button);
        this.uploadImageButton = view.findViewById(R.id.add_review_upload_image_button);
        this.takeImageButton = view.findViewById(R.id.add_review_take_image_button);
        this.removeImageButton = view.findViewById(R.id.add_review_remove_image_button);
        this.locationName = view.findViewById(R.id.add_review_location_name);
        this.review = view.findViewById(R.id.add_review_review);
        this.ratingBar = view.findViewById(R.id.add_review_rating_bar);
        this.reviewImageview = view.findViewById(R.id.add_review_image);

        this.locationSuggestionDialog = new Dialog(requireActivity());

        // Set onClickListeners for all buttons in the UI
        submitReviewButton.setOnClickListener(v -> submitReview());
        this.uploadImageButton.setOnClickListener(v -> openGallery());
        this.takeImageButton.setOnClickListener(v -> openCamera());
        this.removeImageButton.setOnClickListener(v -> removeImage());

        // Show or hide the relevant widgets based on if an image has been added to the review
        if (this.imageUri != null) {
            this.uploadImageButton.setVisibility(View.GONE);
            this.takeImageButton.setVisibility(View.GONE);
            this.reviewImageview.setImageURI(this.imageUri);
        } else {
            this.reviewImageview.setImageResource(R.drawable.image_upload);
            this.removeImageButton.setVisibility(View.INVISIBLE);
            this.reviewImageview.setVisibility(View.GONE);
        }

        if (this.locationAlreadyExists) {
            this.locationName.setEnabled(false);
        }
    }


    /**
     * Adds a new review the database. If the location is a new location, then the What3Words location
     * needs to be calculated. This is done using the What3WordsTask AsyncTask. Alternatively, if the
     * location is an existing location, then the Ratings, Reviews, and Image URL arrays are updated.
     */
    private void submitReview() {
        String locationNameText = this.locationName.getText().toString();
        double ratingNumber = this.ratingBar.getRating();
        String reviewText = this.review.getText().toString();

        // If any required fields are empty
        if (locationNameText.isEmpty()) {
            Toast.makeText(getActivity(), "Please give the location a name", Toast.LENGTH_SHORT).show();
        } else if (reviewText.isEmpty()) {
            Toast.makeText(requireActivity(), "Please write a review for " + locationNameText, Toast.LENGTH_SHORT).show();
        } else {
            if (!this.locationAlreadyExists) {
                // If the location does not already exist, then a new document needs to be created
                // in the Firestore database.
                List<Double> ratingsList = new ArrayList<>();
                List<String> reviewsList = new ArrayList<>();
                List<String> imageUrlList = new ArrayList<>();
                ratingsList.add((double) this.ratingBar.getRating());
                reviewsList.add(this.review.getText().toString());

                this.dataToAdd.put("Location Name", locationNameText);
                this.dataToAdd.put("Ratings", ratingsList);
                this.dataToAdd.put("Reviews", reviewsList);
                this.dataToAdd.put("Latitude", this.latLng.latitude);
                this.dataToAdd.put("Longitude", this.latLng.longitude);

                // If image URL is not null, then add the URL, else, add an empty string
                if (this.url != null) {
                    imageUrlList.add(this.url);
                } else {
                    imageUrlList.add("");
                }
                this.dataToAdd.put("Image URLs", imageUrlList);

                // Create a new What3WordsTask to calculate the What3Words address of the location.
                // The result is obtained in the Override method processFinish, where the location
                // is saved to the database as well.
                new What3WordsTask(this, this.latLng, requireContext()).execute();
            } else {
                // Else, the location does already exist, so just the Ratings, Reviews and Image
                // URL arrays are updated
                DocumentReference photographyLocationReference = this.database.collection("Photography Locations").document(this.existingDocumentId);
                photographyLocationReference.get().addOnCompleteListener(task -> {
                    DocumentSnapshot documentSnapshot = task.getResult();
                    List<Double> ratings = (List<Double>) documentSnapshot.get("Ratings");
                    List<String> reviews = (List<String>) documentSnapshot.get("Reviews");
                    List<String> imageURLs = (List<String>) documentSnapshot.get("Image URLs");

                    ratings.add(ratingNumber);
                    reviews.add(reviewText);
                    imageURLs.add(url);

                    photographyLocationReference.update("Ratings", ratings);
                    photographyLocationReference.update("Reviews", reviews);
                    photographyLocationReference.update("Image URLs", imageURLs);
                });
            }
            resetFields();
            SystemUtils.hideSoftKeyboard(requireActivity());
            Toast.makeText(getActivity(), "Review Added", Toast.LENGTH_SHORT).show();

            NavigationView navigationView = requireActivity().findViewById(R.id.nav_view);
            navigationView.setCheckedItem(R.id.nav_home);
            FragmentTransaction fragmentTransaction = requireActivity().getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container, new HomeFragment()).commit();
        }
    }


    /**
     * Opens a Content Picker so that an image can be chosen to upload
     */
    private void openGallery() {
        Intent intent = new Intent();
        intent.setType("image/");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, IMAGE_REQUEST);
    }


    /**
     * Opens the camera so that the user can take a picture to upload
     */
    private void openCamera() {
        // Checks whether the user has granted camera permissions or not
        if (PermissionUtils.checkCameraPermission(requireActivity().getApplicationContext())) {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            this.fileName = System.currentTimeMillis() + ".jpg";
            this.imageFile = getImageFileGivenFileName(this.fileName);

            // Wrap the file in a file provider (required for SDK >= 24)
            Uri fileProvider = FileProvider.getUriForFile(requireActivity(), "com.example.photographylocationlogger", imageFile);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider);

            // Ensure there is an app on the device to handle the request
            if (intent.resolveActivity(requireActivity().getPackageManager()) != null) {
                startActivityForResult(intent, CAMERA_REQUEST);
            }
        } else {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, PERMISSIONS_REQUEST_ACCESS_CAMERA);
        }
    }


    /**
     * Creates a file given the filename
     *
     * @param fileName the filename of the file to create
     * @return
     */
    private File getImageFileGivenFileName(String fileName) {
        // Using getExternalFilesDir allows package-specific directories to be accessed. This
        // prevents the need to also request external read / write permissions to use the camera
        File storageDirectory = new File(requireActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES), TAG);

        // Create the storage directory if it does not exist
        if (!storageDirectory.exists() && !storageDirectory.mkdirs()) {
            Log.d(TAG, "getImageFileGivenFileName: Failed to create file directory");
        }

        // Return the file target for the photo based on filename
        return new File(storageDirectory.getPath() + File.separator + fileName);
    }


    /**
     * Adds an image to the UI and uploads it to Firebase Storage
     */
    private void uploadImage() {
        // Show Progress Dialog to indicate that the image is being uploaded
        ProgressDialog progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("Uploading Image");
        progressDialog.show();

        if (imageUri != null) {
            // Upload image to Firebase Storage
            StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Uploads").child(fileName);
            storageReference.putFile(imageUri).addOnCompleteListener(task -> storageReference.getDownloadUrl().addOnSuccessListener(uri -> {
                url = uri.toString();
                Log.d(TAG, "Download URL = " + url);
                progressDialog.dismiss();
                Toast.makeText(getActivity(), "Image Upload Successful", Toast.LENGTH_SHORT).show();

                // Handle which UI widgets should be visible now that an image has been uploaded
                reviewImageview.setVisibility(View.VISIBLE);
                removeImageButton.setVisibility(View.VISIBLE);
                uploadImageButton.setVisibility(View.GONE);
                takeImageButton.setVisibility(View.GONE);
            }));
        }
    }


    /**
     * Removes an image from the UI and Firebase Storage
     */
    private void removeImage() {
        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Uploads").child(this.fileName);
        storageReference.delete().addOnSuccessListener(aVoid -> {
            Toast.makeText(getActivity(), "Image successfully deleted", Toast.LENGTH_SHORT).show();
            this.uploadImageButton.setVisibility(View.VISIBLE);
            this.takeImageButton.setVisibility(View.VISIBLE);
            this.removeImageButton.setVisibility(View.INVISIBLE);
            this.reviewImageview.setVisibility(View.GONE);
            this.imageUri = null;
            this.fileName = null;
        }).addOnFailureListener(e -> Toast.makeText(getActivity(), "Image not deleted", Toast.LENGTH_SHORT).show());
    }


    /**
     * Gets the correct file extension for the uploaded file
     *
     * @param uri the uri to get the file extension of
     * @return String - the file extension of the uploaded file
     */
    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = requireActivity().getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();

        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }


    /**
     * Resets all fields after a review has been successfully submitted
     */
    private void resetFields() {
        this.locationName.setText("");
        this.ratingBar.setRating(0);
        this.review.setText("");
        this.imageUri = null;
        this.fileName = null;

        this.uploadImageButton.setVisibility(View.VISIBLE);
        this.takeImageButton.setVisibility(View.VISIBLE);
        this.removeImageButton.setVisibility(View.INVISIBLE);
        this.reviewImageview.setVisibility(View.GONE);
    }


    /**
     * Gets the current location of the device.
     */
    private void getDeviceLocation() {
        Log.d(TAG, "getDeviceLocation: Getting the device's current location");
        try {
            if (PermissionUtils.checkLocationPermission(requireActivity().getApplicationContext())) {
                Task<Location> locationResult = this.fusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(requireActivity(), task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        Log.d(TAG, "onComplete: found location!");
                        Location currentLocation = task.getResult();
                        LatLng latLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                        this.latLng = latLng;
                        calculateDistance(latLng);
                    } else {
                        Log.d(TAG, "onComplete: current location is null");
                        Toast.makeText(getActivity(), "Unable to get current location", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage(), e);
        }
    }


    /**
     * Calculates the distance from the user's current location to all other reviewed photography
     * locations. Any location whose distance is less than 0.5Km from the user's current location
     * is suggested to the user as a potential location that they are trying to add a review of.
     *
     * @param currentLatLng the user's current location
     */
    public void calculateDistance(LatLng currentLatLng) {
        // Create a Map to store location suggestions and their corresponding distance from the
        // user's current location
        Map<PhotographyLocation, Double> locationSuggestions = new HashMap<>();

        FirebaseFirestore.getInstance().collection("Photography Locations").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot doc : task.getResult()) {
                    List<Review> reviews = new ArrayList<>();
                    // For each element in the Reviews array, create a new Review Object, and add this
                    // to the list of Reviews
                    for (int i = 0; i < ((List<String>) doc.get("Reviews")).size(); i++) {
                        reviews.add(new Review(
                                Double.valueOf(String.valueOf(((List<Double>) doc.get("Ratings")).get(i))),
                                ((List<String>) doc.get("Reviews")).get(i),
                                ((List<String>) doc.get("Image URLs")).get(i)));
                    }

                    // Create a new Photography Location Object
                    PhotographyLocation photographyLocation = new PhotographyLocation(doc.getId(), doc.get("Location Name").toString(),
                            Double.parseDouble(doc.get("Latitude").toString()),
                            Double.parseDouble(doc.get("Longitude").toString()), reviews, doc.get("What3Words").toString());

                    // Create a new LatLng Object for the Photography Location's latitude and longitude
                    LatLng latLng = new LatLng(photographyLocation.getLatitude(), photographyLocation.getLongitude());

                    // If the distance from the user's current location to the photography location's
                    // LatLng is <= 0.5Km, then add the location and its distance to the map
                    if (calculateDistanceBetween(currentLatLng, latLng) <= 0.5) {
                        Log.d(TAG, "Is this location " + photographyLocation.getLocationName() + "?");
                        locationSuggestions.put(photographyLocation, calculateDistanceBetween(currentLatLng, latLng));
                    }
                }

                // Sort the map into increasing order of distance, i.e., the photography location
                // closest to the user's current location (within 0.5Km) is the first key in the
                // map
                LinkedHashMap<PhotographyLocation, Double> sortedLocationSuggestions = new LinkedHashMap<>();
                locationSuggestions.entrySet()
                        .stream()
                        .sorted(Map.Entry.comparingByValue())
                        .forEachOrdered(location -> sortedLocationSuggestions.put(location.getKey(), location.getValue()));

                showLocationSuggestions(new ArrayList<>(sortedLocationSuggestions.keySet()));
            }
        });
    }


    /**
     * Shows the popup dialog of a suggested photography location, and handles a 'Yes' or 'No' button
     * click accordingly
     *
     * @param photographyLocations the list of photography location suggestions
     */
    public void showLocationSuggestions(List<PhotographyLocation> photographyLocations) {
        // If the number of suggestions shown is less than the number of suggested photography locations calculated
        if (this.shownDialogCount != photographyLocations.size() && !this.locationSuggestionsShown) {
            // Initialise the dialog and UI widgets
            locationSuggestionDialog.setContentView(R.layout.location_suggestion_popup_window);
            TextView suggestedLocationName = locationSuggestionDialog.findViewById(R.id.duplicateLocationName);
            AppCompatButton noButton = locationSuggestionDialog.findViewById(R.id.noButton);
            AppCompatButton yesButton = locationSuggestionDialog.findViewById(R.id.yesButton);

            suggestedLocationName.setText(photographyLocations.get(this.shownDialogCount).getLocationName());

            // Set the onClickListener for the 'No' button
            noButton.setOnClickListener(v -> {
                locationSuggestionDialog.dismiss();
                this.shownDialogCount++;

                // Wait 300 milliseconds before showing the next location suggestion
                Handler handler = new Handler();
                handler.postDelayed(() -> showLocationSuggestions(photographyLocations), 300);
            });


            // Set the onClickListener for the 'Yes' button
            yesButton.setOnClickListener(v -> {
                // Pre-fill the location name on the review page, and make it disabled
                locationName.setText(photographyLocations.get(shownDialogCount).getLocationName());
                locationName.setEnabled(false);
                this.latLng = new LatLng(photographyLocations.get(shownDialogCount).getLatitude(), photographyLocations.get(shownDialogCount).getLongitude());

                // Set global variables for the case of the location already existing in the database
                // and dismiss the dialog
                locationAlreadyExists = true;
                existingDocumentId = photographyLocations.get(shownDialogCount).getDocumentId();
                locationSuggestionsShown = true;
                locationSuggestionDialog.dismiss();
            });
            locationSuggestionDialog.show();
        } else {
            // If all suggestions have been shown, and the user has said 'No' to all of them
            locationSuggestionsShown = true;
        }
    }


    /**
     * Uses the Haversine Formula to calculate the great-circle distance between two latitude and
     * longitude points.
     *
     * @param latLngFrom the user's current latitude and longitude
     * @param latLngTo   the latitude and longitude of the photography location
     * @return double - the distance between the two latitudes and longitudes
     */
    public double calculateDistanceBetween(LatLng latLngFrom, LatLng latLngTo) {
        // The Haversine Formula works as follows:
        // a = sin²(ΔlatDifference/2) + cos(lat1).cos(lt2).sin²(ΔlonDifference/2)
        // c = 2.atan2(√a, √(1−a))
        // d = R.c
        // Note also that the latitude and longitudes must be in radians

        // Radius of Earth (R)
        double radiusOfEarth = 6371.0;

        // Convert latitudes and longitudes into radians
        double latitudeFromInRadians = Math.toRadians(latLngFrom.latitude);
        double longitudeFromInRadians = Math.toRadians(latLngFrom.longitude);
        double latitudeToInRadians = Math.toRadians(latLngTo.latitude);
        double longitudeToInRadians = Math.toRadians(latLngTo.longitude);

        // Calculate the difference between the two latitudes and longitudes
        // latitudeDifference = ΔlatDifference
        // longitudeDifference = ΔlonDifference
        double latitudeDifference = latitudeToInRadians - latitudeFromInRadians;
        double longitudeDifference = longitudeToInRadians - longitudeFromInRadians;

        // Calculate sin² latitude / 2 and sin² longitude / 2
        // sinLatitudeSquared = sin²(ΔlatDifference/2)
        // sinLongitudeSquared = sin²(ΔlonDifference/2)
        double sinLatitudeSquared = Math.pow(Math.sin(latitudeDifference / 2), 2);
        double sinLongitudeSquared = Math.pow(Math.sin(longitudeDifference / 2), 2);

        // Calculate a, using the values calculated above
        double a = sinLatitudeSquared + Math.cos(latitudeToInRadians) * Math.cos(latitudeFromInRadians) * sinLongitudeSquared;

        // Calculate c, using a
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        // Calculate distance, using c and the earth's radius
        double distance = radiusOfEarth * c;
        Log.d(TAG, "calculateDistanceBetween: distance between " + latLngFrom.toString() +
                " and " + latLngTo.toString() + " is " + distance + "km");

        return distance;
    }


    /**
     * Handles the result of the request for location permissions.
     *
     * @param requestCode  the request code passed in requestPermissions()
     * @param permissions  the requested permissions
     * @param grantResults the grant results for the corresponding permissions
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (PermissionUtils.onRequestPermissionsResult(requestCode, grantResults)) {
            case "Location Access Granted":
                requireActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new AddReviewFragment()).commit();
                break;
            case "Camera Access Granted":
                openCamera();
                break;
        }
    }


    /**
     * Called when an activity launched, i.e., opening the camera or content picker has finished.
     * If the resultCode returned is RESULT_OK, then the operation was successful, and the application
     * can use this data as necessary.
     *
     * @param requestCode the integer request code originally supplied to startActivityForResult()
     * @param resultCode  the integer result code returned by the child activity through setResult()
     * @param data        an intent which can return result data to the caller
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case IMAGE_REQUEST:
                if (resultCode == RESULT_OK) {
                    this.imageUri = data.getData();
                    this.fileName = System.currentTimeMillis() + "." + getFileExtension(this.imageUri);
                    uploadImage();
                } else {
                    Log.d(TAG, "onActivityResult: No image selected");
                }
                break;

            case CAMERA_REQUEST:
                if (resultCode == RESULT_OK) {
                    this.imageUri = Uri.fromFile(this.imageFile);
                    uploadImage();
                } else {
                    Log.d(TAG, "onActivityResult: No image taken");
                }
                break;
        }
        Log.d(TAG, "onActivityResult: Image URI = " + this.imageUri);
        this.reviewImageview.setImageURI(this.imageUri);
    }


    /**
     * AsyncResponse Interface called when the What3Words location of a given set of co-ordinates
     * has been calculated. The photography location can then be added to the database with this
     * value
     *
     * @param what3words the What3Words address returned from the What3Words AsyncTask
     */
    @Override
    public void processFinish(String what3words) {
        this.dataToAdd.put("What3Words", what3words);
        this.database.collection("Photography Locations").add(dataToAdd);
    }


    /**
     * Called to retrieve per-instance state from an activity before being killed, so that the state
     * can be restored in onCreate(Bundle). For this activity, nothing in addition to the super needs
     * to be saved.
     *
     * @param outState bundle in which the saved state is placed
     */
    @Override
    public void onSaveInstanceState(@NonNull @NotNull Bundle outState) {
        outState.putBoolean("locationSuggestionsShown", this.locationSuggestionsShown);
        outState.putBoolean("locationAlreadyExists", this.locationAlreadyExists);
        if (this.imageUri != null) {
            outState.putString("imageUri", this.imageUri.toString());
            outState.putString("filename", this.fileName);
        }
        if (this.existingDocumentId != null) {
            outState.putString("existingDocumentId", this.existingDocumentId);
        }
        super.onSaveInstanceState(outState);
    }
}
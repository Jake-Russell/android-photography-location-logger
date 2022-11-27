package com.example.photographylocationlogger;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.app.ActivityCompat;

import com.example.photographylocationlogger.data.Review;
import com.example.photographylocationlogger.utils.PermissionUtils;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.ArrayList;

/**
 * AllReviewsListAdapter is the custom list adapter for viewing all reviews of a location. It
 * creates the view and populates it with the corresponding data for each review.
 *
 * @author Jake Russell
 * @version 1.0
 * @since 18/05/2021
 */
public class ViewAllReviewsListAdapter extends ArrayAdapter<Review> {
    private static final String TAG = "AllReviewsListAdapter";

    private Context context;
    private int resource;
    private Dialog largeImageDialog;

    /**
     * Constructor sets the context, resource and dialog
     *
     * @param context  the context
     * @param resource the resource
     * @param objects  the list of reviews
     */
    public ViewAllReviewsListAdapter(@NonNull Context context, int resource, @NonNull ArrayList<Review> objects) {
        super(context, resource, objects);
        this.context = context;
        this.resource = resource;
        this.largeImageDialog = new Dialog(context);
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
        // Get the review's information
        double locationRating = getItem(position).getRating();
        String locationReview = getItem(position).getReview();
        String locationImageURL = getItem(position).getImageURL();

        // Create the Review Object with the information
        Review review = new Review(locationRating, locationReview, locationImageURL);

        // Create the ViewHolder object
        ViewAllReviewsListAdapter.ViewHolder holder;

        // Check if the old view is null, and inflate a new view if it is
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(this.context);
            convertView = inflater.inflate(this.resource, parent, false);

            holder = new ViewHolder(convertView, review);
            holder.ratingBar = convertView.findViewById(R.id.ratingBar);
            holder.locationReview = convertView.findViewById(R.id.locationReview);
            holder.locationImage = convertView.findViewById(R.id.locationImage);

            convertView.setTag(holder);
        } else {
            holder = (ViewAllReviewsListAdapter.ViewHolder) convertView.getTag();
        }

        holder.ratingBar.setRating((float) review.getRating());
        holder.locationReview.setText(review.getReview());

        // Where appropriate, load the image for the review into the view
        if (review.getImageURL() != null && !review.getImageURL().equals("")) {
            try {
                Picasso.get()
                        .load(review.getImageURL())
                        .placeholder(R.drawable.progress_animation)
                        .resizeDimen(R.dimen.image_size, R.dimen.image_size)
                        .centerInside()
                        .onlyScaleDown()
                        .into(holder.locationImage);
            } catch (IllegalArgumentException e) {
                Log.e(TAG, "Error loading image for review - " + review.getReview());
            }
        } else {
            holder.locationImage.setImageDrawable(null);
            Log.d(TAG, "No image for review - " + review.getReview());
        }

        // Set onClickListener for the image, displaying the larger image dialog once clicked
        holder.locationImage.setOnClickListener(v -> {
            largeImageDialog.setContentView(R.layout.large_image_popup_window);
            TextView closeDialog = largeImageDialog.findViewById(R.id.closeDialog);
            ImageView largeImageView = largeImageDialog.findViewById(R.id.largeImageView);

            Picasso.get()
                    .load(review.getImageURL())
                    .placeholder(R.drawable.progress_animation)
                    .fit()
                    .centerInside()
                    .into(largeImageView);

            closeDialog.setOnClickListener(v1 -> largeImageDialog.dismiss());
            largeImageDialog.show();
        });

        // Set onLongClickListener for the image, allowing the user to save the image to their gallery
        holder.locationImage.setOnLongClickListener(v -> {
            if (PermissionUtils.checkExternalWritePermissions(context)) {
                showPopUp(review.getImageURL());
            } else {
                Log.i(TAG, "onLongClick: No Write Permission");
                ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            }
            return false;
        });
        return convertView;
    }


    /**
     * Displays a popup to prompt the user for input regarding whether they wish to save the image or not
     *
     * @param imageUrl the URL of the image to save
     */
    public void showPopUp(String imageUrl) {
        Dialog saveImageDialog = new Dialog(context);
        saveImageDialog.setContentView(R.layout.save_image_confirmation_popup_window);

        AppCompatButton noButton = saveImageDialog.findViewById(R.id.noButton);
        AppCompatButton yesButton = saveImageDialog.findViewById(R.id.yesButton);

        // Set onClickListener for 'No' button
        noButton.setOnClickListener(v -> {
            saveImageDialog.dismiss();
        });

        // Set onClickListener for 'Yes' button
        yesButton.setOnClickListener(v -> {
            saveToGallery(imageUrl);
            saveImageDialog.dismiss();
        });
        saveImageDialog.show();
    }


    /**
     * Saves an image to the user's gallery, given the image URL
     *
     * @param imageUrl the URL of the image to save
     */
    public void saveToGallery(String imageUrl) {
        // Load the image, and save it using MediaStore
        Picasso.get().load(imageUrl).into(new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                MediaStore.Images.Media.insertImage(context.getContentResolver(), bitmap, "Title", "Description");
                Toast.makeText(context, "Image successfully saved!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                // Required but not used
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {
                // Required but not used
            }
        });
    }


    /**
     * Holds variables in a View
     */
    private static class ViewHolder {
        RatingBar ratingBar;
        TextView locationReview;
        ImageView locationImage;
        View layout;
        Review review;

        /**
         * Constructor initialises the ViewHolder
         *
         * @param itemView the View
         * @param review   the Review
         */
        public ViewHolder(@NonNull View itemView, Review review) {
            layout = itemView;
            this.review = review;
            ratingBar = layout.findViewById(R.id.ratingBar);
            locationReview = layout.findViewById(R.id.locationReview);
            locationImage = layout.findViewById(R.id.locationImage);
        }
    }
}

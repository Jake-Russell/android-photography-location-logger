package com.example.photographylocationlogger;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RatingBar;
import android.widget.TextView;

import com.example.photographylocationlogger.data.PhotographyLocation;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

import org.jetbrains.annotations.NotNull;

/**
 * CustomInfoWindowAdapter is responsible for showing the custom info window when a Google Maps
 * marker has been clicked. It uses the tag of the marker (the photography location object) to
 * render the window with the correct data
 *
 * @author Jake Russell
 * @version 1.0
 * @since 18/05/2021
 */
public class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

    private View window;

    /**
     * Constructor inflates the info window view
     *
     * @param context the current context
     */
    public CustomInfoWindowAdapter(Context context) {
        window = LayoutInflater.from(context).inflate(R.layout.custom_info_window, null);
    }


    /**
     * Renders the info window with all of the required data about the photography location the
     * marker represents
     *
     * @param marker the marker that has been clicked
     * @param view   the view
     */
    private void renderWindow(Marker marker, View view) {
        PhotographyLocation photographyLocation = (PhotographyLocation) marker.getTag();

        TextView textViewTitle = view.findViewById(R.id.infoWindowTitle);
        textViewTitle.setText(photographyLocation.getLocationName());

        RatingBar ratingBar = view.findViewById(R.id.infoWindowRatingBar);
        ratingBar.setRating((float) (photographyLocation.getAverageRating()));

        TextView what3wordsTextView = view.findViewById(R.id.infoWindowWhat3WordsTextView);
        what3wordsTextView.setText(photographyLocation.getWhat3Words());
    }


    /**
     * Provides a custom info window for a marker. The view returned is used for the entire info
     * window
     *
     * @param marker the marker for which an info window is being populated
     * @return a custom info window for the marker
     */
    @Override
    public View getInfoWindow(@NotNull Marker marker) {
        renderWindow(marker, window);
        return window;
    }


    /**
     * Provides custom contents for ht default info window frame of a marker. This method is only
     * called if getInfoWindow() first returns null
     *
     * @param marker the marker for which an info window is being populated
     * @return a customer view to display as contents in the info window for the marker
     */
    @Override
    public View getInfoContents(@NotNull Marker marker) {
        renderWindow(marker, window);
        return window;
    }
}

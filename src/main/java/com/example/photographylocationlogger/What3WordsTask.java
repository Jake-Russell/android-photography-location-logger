package com.example.photographylocationlogger;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.what3words.javawrapper.What3WordsV3;
import com.what3words.javawrapper.request.Coordinates;
import com.what3words.javawrapper.response.ConvertTo3WA;

/**
 * What3WordsTask is an AsyncTask responsible for converting a set of latitude and longitude
 * coordinates into a What3Words location.
 */
public class What3WordsTask extends AsyncTask<Void, Integer, String> {

    public AsyncResponse asyncResponse;
    private LatLng latLng;
    private Context context;

    /**
     * Constructor used to create a new What3WordsTask instance
     *
     * @param asyncResponse the AsyncResponse interface instance to be used
     * @param latLng        the LatLng object to convert to a What3Words location
     * @param context       the current context
     */
    public What3WordsTask(AsyncResponse asyncResponse, LatLng latLng, Context context) {
        this.asyncResponse = asyncResponse;
        this.latLng = latLng;
        this.context = context;
    }


    /**
     * Performs the computation of the What3Words address on a background thread
     *
     * @param voids the parameters of the task
     * @return String - the calculated What3Words address for the given latitude and longitude
     */
    @Override
    protected String doInBackground(Void... voids) {
        What3WordsV3 wrapper = new What3WordsV3(context.getString(R.string.what3words_API_key));
        ConvertTo3WA convertTo3WA = wrapper.convertTo3wa(new Coordinates(this.latLng.latitude, this.latLng.longitude)).execute();
        return convertTo3WA.getWords();
    }


    /**
     * Runs on the UI thread after doInBackground() and calls the processFinish method on AsyncResponse
     *
     * @param result the result of the operation computed by doInBackground()
     */
    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        Log.d("What3WordsTask", "What 3 Words = " + result);
        asyncResponse.processFinish(result);
    }
}

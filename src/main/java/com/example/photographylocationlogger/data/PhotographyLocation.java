package com.example.photographylocationlogger.data;

import java.io.Serializable;
import java.util.List;

/**
 * PhotographyLocation is a data class used to represent a Photography Location within the database
 *
 * @author Jake Russell
 * @version 1.0
 * @since 18/05/2021
 */
public class PhotographyLocation implements Serializable {

    private String locationName;
    private double latitude;
    private double longitude;

    private List<Review> reviews;

    private String what3words;
    private String documentId;

    /**
     * Constructor used when creating a new Photography Location for the user's saved locations. No
     * reviews are displayed here, so none are passed into the constructor.
     *
     * @param documentId   the ID of the document in the database
     * @param locationName the photography location's name
     * @param latitude     the photography location's latitude
     * @param longitude    the photography location's longitude
     * @param what3words   the what3words address of the photography location
     */
    public PhotographyLocation(String documentId, String locationName, double latitude, double longitude, String what3words) {
        this.documentId = documentId;
        this.locationName = locationName;
        this.latitude = latitude;
        this.longitude = longitude;
        this.what3words = what3words;
    }


    /**
     * Constructor used when creating a new Photography Location from the database
     *
     * @param documentId   the ID of the document in the database
     * @param locationName the photography location's name
     * @param latitude     the photography location's latitude
     * @param longitude    the photography location's longitude
     * @param reviews      the list of reviews associated to the photography location
     * @param what3words   the what3words address of the photography location
     */
    public PhotographyLocation(String documentId, String locationName, double latitude, double longitude, List<Review> reviews, String what3words) {
        this.documentId = documentId;
        this.locationName = locationName;
        this.latitude = latitude;
        this.longitude = longitude;
        this.reviews = reviews;
        this.what3words = what3words;
    }


    /**
     * Calculates and returns the average rating for a photography location
     *
     * @return double - the average rating for a photography location
     */
    public double getAverageRating() {
        double sum = 0.0;
        for (Review review : this.reviews) {
            sum += review.getRating();
        }
        return sum / this.reviews.size();
    }


    /**
     * Returns the photography location's name
     *
     * @return String - the photography location's name
     */
    public String getLocationName() {
        return locationName;
    }


    /**
     * Returns the photography location's latitude
     *
     * @return double - the photography location's latitude
     */
    public double getLatitude() {
        return latitude;
    }


    /**
     * Returns the photography location's longitude
     *
     * @return double - the photography location's longitude
     */
    public double getLongitude() {
        return longitude;
    }


    /**
     * Returns all reviews of a photography location
     *
     * @return List<Review> - a list of all reviews of a photography location
     */
    public List<Review> getReviews() {
        return reviews;
    }


    /**
     * Returns the what3words address of a photography location
     *
     * @return String - the what3words address of a photography location
     */
    public String getWhat3Words() {
        return what3words;
    }


    /**
     * Returns the document ID of a photography location
     *
     * @return String - the document ID of a photography location
     */
    public String getDocumentId() {
        return documentId;
    }
}

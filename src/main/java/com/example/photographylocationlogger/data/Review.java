package com.example.photographylocationlogger.data;

import java.io.Serializable;

/**
 * Review is a data class used to represent a Review within the database
 *
 * @author Jake Russell
 * @version 1.0
 * @since 18/05/2021
 */
public class Review implements Serializable {
    private double rating;
    private String review;
    private String imageURL;

    /**
     * Constructor used to create a new instance of Review
     *
     * @param rating   the review's rating
     * @param review   the review's review
     * @param imageURL the review's image URL
     */
    public Review(double rating, String review, String imageURL) {
        this.rating = rating;
        this.review = review;
        this.imageURL = imageURL;
    }


    /**
     * Returns the rating of a review
     *
     * @return double - the rating of a review
     */
    public double getRating() {
        return rating;
    }


    /**
     * Returns the review of a review
     *
     * @return String - the review of a review
     */
    public String getReview() {
        return review;
    }


    /**
     * Returns the image URL of a review
     *
     * @return String - the image URL of a review
     */
    public String getImageURL() {
        return imageURL;
    }
}

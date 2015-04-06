package com.swpbiz.foodcoma.models;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by vee on 4/5/15.
 */
public class Review {

    private int ratingStar;
    private String reviewContent;

    public Review() {
        this.ratingStar = 0;
        this.reviewContent = "";
    }

    public int getRatingStar() {
        return ratingStar;
    }

    public void setRatingStar(int ratingStar) {
        this.ratingStar = ratingStar;
    }

    public String getReviewContent() {
        return reviewContent;
    }

    public void setReviewContent(String reviewContent) {
        this.reviewContent = reviewContent;
    }

    public static Review fromJSON(JSONObject json) {
        Review review = new Review();
        try {
            review.setRatingStar(json.getInt("rating"));
            review.setReviewContent(json.getString("text"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return review;
    }

    public static ArrayList<Review> fromJSONArray(JSONArray jsonArray) {
        ArrayList<Review> reviews = new ArrayList<Review>();
        for(int i = 0; i < jsonArray.length(); i++) {
            try {
                reviews.add(Review.fromJSON(jsonArray.getJSONObject(i)));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return reviews;
    }

}

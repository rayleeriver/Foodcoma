package com.swpbiz.foodcoma.models;

import android.location.Location;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by abgandhi on 3/24/15.
 */
public class Restaurant {
    String name;
    String RestaurantId;
    String iconurl;
    String priceLevel;
    String rating;
    String photoReference;

    public String getPhotoReference() {
        return photoReference;
    }

    public void setPhotoReference(String photoReference) {
        this.photoReference = photoReference;
    }

    public String getRestaurantId() {
        return RestaurantId;
    }

    public void setRestaurantId(String restaurantId) {
        RestaurantId = restaurantId;
    }

    public String getIconurl() {
        return iconurl;
    }

    public void setIconurl(String iconurl) {
        this.iconurl = iconurl;
    }

    public String getPriceLevel() {
        return priceLevel;
    }

    public void setPriceLevel(String priceLevel) {
        this.priceLevel = priceLevel;
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public Location getRestaurantLocation() {
        return restaurantLocation;
    }

    public void setRestaurantLocation(Location restaurantLocation) {
        this.restaurantLocation = restaurantLocation;

    }

    Location restaurantLocation;




    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public static ArrayList<Restaurant> getArrayFromJson(JSONObject result) {
        ArrayList<Restaurant> restaurants = new ArrayList<Restaurant>();
        try {
            JSONArray jarray = (JSONArray)result.getJSONArray("results");
            for (int i = 0; i < jarray.length(); i++) {
                Restaurant res = new Restaurant();
                JSONObject jres = jarray.getJSONObject(i);
                if (jres.getString("name") != null) {
                    res.setName(jres.getString("name"));
                } else {
                    res.setName("");
                }

                if (jres.getString("icon") != null) {
                    res.setIconurl(jres.getString("icon"));
                } else {
                    res.setIconurl("");
                }
                if (jres.has("price_level") && jres.getString("price_level") != null) {
                    res.setPriceLevel(jres.getString("price_level"));
                } else {
                    res.setPriceLevel("");
                }
                if ( jres.has("id") && jres.getString("id") != null) {
                    res.setRestaurantId(jres.getString("id"));
                } else {
                    res.setRestaurantId("");
                }
                if ( jres.has("rating") && jres.getString("rating") != null) {
                    res.setRating(jres.getString("rating"));
                } else {
                    res.setRating("");
                }

                if (jres.has("photos") && jres.getJSONArray("photos") != null) {
                    JSONArray jphotos = jres.getJSONArray("photos");
                    if (jphotos.getJSONObject(0).has("photo_reference") &&
                            jphotos.getJSONObject(0).getString("photo_reference") != null) {
                        res.setPhotoReference(jphotos.getJSONObject(0).getString("photo_reference"));
                    }
                }

        //        res.restaurantLocation.setLatitude(jres.getJSONObject("geometry").getJSONObject("location").getDouble("lat"));
        //        res.restaurantLocation.setLongitude(jres.getJSONObject("geometry").getJSONObject("location").getDouble("lng"));
                restaurants.add(res);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return restaurants;
    }
}

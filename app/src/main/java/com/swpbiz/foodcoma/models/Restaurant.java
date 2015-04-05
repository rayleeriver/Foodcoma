package com.swpbiz.foodcoma.models;

import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by abgandhi on 3/24/15.
 */
public class Restaurant implements Parcelable {
    String name;
    String RestaurantId;
    String iconurl;
    String priceLevel;
    String rating;
    String photoReference;
    String resAddress;
    Location restaurantLocation;

    public Restaurant() {
        name = "";
        RestaurantId = "";
        iconurl = "";
        priceLevel = "";
        rating = "";
        photoReference = "";
        resAddress = "";
        restaurantLocation = new Location("");
    }

    public Restaurant(Parcel source) {
        name = source.readString();
        RestaurantId = source.readString();
        iconurl = source.readString();
        priceLevel = source.readString();
        rating = source.readString();
        photoReference = source.readString();
        resAddress = source.readString();
        restaurantLocation = new Location((Location)source.readParcelable(Location.class.getClassLoader()));
    }

    public String getResAddress() {
        return resAddress;
    }

    public void setResAddress(String resAddress) {
        if(resAddress != null)
            this.resAddress = resAddress;
    }

    public String getPhotoReference() {
        return photoReference;
    }

    public void setPhotoReference(String photoReference) {
        if(photoReference != null)
            this.photoReference = photoReference;
    }

    public String getRestaurantId() {
        return RestaurantId;
    }

    public void setRestaurantId(String restaurantId) {
        if(restaurantId != null)
            RestaurantId = restaurantId;
    }

    public String getIconurl() {
        return iconurl;
    }

    public void setIconurl(String iconurl) {
        if(iconurl != null)
            this.iconurl = iconurl;
    }

    public String getPriceLevel() {
        return priceLevel;
    }

    public void setPriceLevel(String priceLevel) {
        if(priceLevel != null)
            this.priceLevel = priceLevel;
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        if(rating != null)
            this.rating = rating;
    }

    public Location getRestaurantLocation() {
        return restaurantLocation;
    }

    public void setRestaurantLocation(Location restaurantLocation) {
        if(restaurantLocation != null)
            this.restaurantLocation = restaurantLocation;

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if(name != null)
            this.name = name;
    }

    public static ArrayList<Restaurant> getArrayFromJson(JSONObject result) {
        ArrayList<Restaurant> restaurants = new ArrayList<Restaurant>();
        try {
            JSONArray jarray = (JSONArray) result.getJSONArray("results");
            for (int i = 0; i < jarray.length(); i++) {
                Restaurant res = new Restaurant();
                JSONObject jres = jarray.getJSONObject(i);

                if(jres.has("name"))
                    res.setName(jres.getString("name"));
                if(jres.has("icon"))
                    res.setIconurl(jres.getString("icon"));
                if(jres.has("price_level"))
                    res.setPriceLevel(jres.getString("price_level"));
                if(jres.has("place_id"))
                    res.setRestaurantId(jres.getString("place_id"));
                if(jres.has("rating"))
                    res.setRating(jres.getString("rating"));
                if(jres.has("vicinity"))
                    res.setResAddress(jres.getString("vicinity"));

                if (jres.has("photos") && jres.getJSONArray("photos") != null) {
                    JSONArray jphotos = jres.getJSONArray("photos");
                    if (jphotos.getJSONObject(0).has("photo_reference") &&
                            jphotos.getJSONObject(0).getString("photo_reference") != null) {
                        res.setPhotoReference(jphotos.getJSONObject(0).getString("photo_reference"));
                    }
                }
                if (jres.has("geometry") && jres.getJSONObject("geometry").has("location")) {
                    res.restaurantLocation.setLatitude(jres.getJSONObject("geometry").getJSONObject("location").getDouble("lat"));
                    res.restaurantLocation.setLongitude(jres.getJSONObject("geometry").getJSONObject("location").getDouble("lng"));

                }
                restaurants.add(res);

            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return restaurants;
    }

    JSONObject getJsonObject() {
        JSONObject obj = new JSONObject();
        try {
            obj.put("restaurantname", getName());
            obj.put("restaurantid", getRestaurantId());
            obj.put("restaurantaddress", getResAddress());
            obj.put("reslatitude", getRestaurantLocation().getLatitude());
            obj.put("reslongitude", getRestaurantLocation().getLongitude());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return obj;
    }

    public static Restaurant getResFromJsonObject(JSONObject jres) {
        Restaurant res = new Restaurant();
        try {

            if (jres.has("restaurantname") && jres.getString("restaurantname") != null) {
                res.setName(jres.getString("restaurantname"));
            } else {
                res.setName("");
            }

            if (jres.has("reslatitude") && jres.has("reslongitude")) {
                res.restaurantLocation.setLatitude(jres.getDouble("reslatitude"));
                res.restaurantLocation.setLongitude(jres.getDouble("reslongitude"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return res;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(RestaurantId);
        dest.writeString(iconurl);
        dest.writeString(priceLevel);
        dest.writeString(rating);
        dest.writeString(photoReference);
        dest.writeString(resAddress);
        dest.writeParcelable(restaurantLocation, PARCELABLE_WRITE_RETURN_VALUE);
    }

    public static final Parcelable.Creator<Restaurant> CREATOR
            = new Parcelable.Creator<Restaurant>() {
        @Override
        public Restaurant createFromParcel(Parcel source) {
            return new Restaurant(source);
        }

        @Override
        public Restaurant[] newArray(int size) {
            return new Restaurant[0];
        }
    };
}

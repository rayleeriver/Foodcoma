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
        this.resAddress = resAddress;
    }

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public static ArrayList<Restaurant> getArrayFromJson(JSONObject result) {
        ArrayList<Restaurant> restaurants = new ArrayList<Restaurant>();
        try {
            JSONArray jarray = (JSONArray) result.getJSONArray("results");
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
                if (jres.has("place_id") && jres.getString("place_id") != null) {
                    res.setRestaurantId(jres.getString("place_id"));
                } else {
                    res.setRestaurantId("");
                }
                if (jres.has("rating") && jres.getString("rating") != null) {
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

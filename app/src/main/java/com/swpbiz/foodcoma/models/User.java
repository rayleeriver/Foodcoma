package com.swpbiz.foodcoma.models;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

// @Table(name = "user")

public class User implements  Parcelable {

//    @Column(name = "userId", unique = true)
    private String userId;
//    @Column(name = "phoneNumber")
    private String phoneNumber;
//    @Column(name = "name")
    private String name;
//    @Column(name = "avatar")
    private String contactPhotoUri;
//    @Column(name = "location")
    private String location;

    public User() {
        userId = "";
        phoneNumber = "";
        name = "";
        contactPhotoUri = "";
        location = "";
    }

    public User(String name, String phoneNumber){
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.userId = "";
        this.contactPhotoUri ="";
        this.location = "";
    }

    public User(Parcel source) {
        userId = source.readString();
        phoneNumber = source.readString();
        name = source.readString();
        contactPhotoUri = source.readString();
        location = source.readString();
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String id) {
        this.userId = id;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContactPhotoUri() {
        return contactPhotoUri;
    }

    public void setContactPhotoUri(String contactPhotoUri) {
        this.contactPhotoUri = contactPhotoUri;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public HashMap<String, String> getPreferences() {
        return preferences;
    }

    public void setPreferences(HashMap<String, String> preferences) {
        this.preferences = preferences;
    }

    private HashMap<String, String> preferences;

    public JSONObject getJsonObject() {
        JSONObject data = new JSONObject();
        try {
            data.put("userId", userId);
            data.put("phonenumber", phoneNumber);
            data.put("name", name);
            data.put("contactPhotoUri", contactPhotoUri);
            data.put("location", location);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return data;
    }

    public static User getUserFromJsonObject(JSONObject data) {
        User user = new User();
        try {
            user.setName(data.getString("name"));
         user.setUserId(data.getString("userId"));
            user.setPhoneNumber(data.getString("phonenumber"));
            user.setContactPhotoUri(data.getString("contactPhotoUri"));
            user.setLocation(data.getString("location"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return user;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(userId);
        dest.writeString(phoneNumber);
        dest.writeString(name);
        dest.writeString(contactPhotoUri);
        dest.writeString(location);
    }

    public static final Parcelable.Creator<User> CREATOR
            = new Parcelable.Creator<User>() {
        @Override
        public User createFromParcel(Parcel source) {
            return new User(source);
        }

        @Override
        public User[] newArray(int size) {
            return new User[0];
        }
    };

    public static String getTrimmedPhoneNumber(String phoneNumber) {
        phoneNumber = phoneNumber.replaceAll("[^0-9]", ""); // get Digits from string
        if (phoneNumber.length() > 10) {
            phoneNumber = phoneNumber.substring(phoneNumber.length() - 10);
        }
        return phoneNumber;
    }


}

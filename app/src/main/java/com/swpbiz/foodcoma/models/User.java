package com.swpbiz.foodcoma.models;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

// @Table(name = "user")
public class User {
//    @Column(name = "userId", unique = true)
    private String userId;
//    @Column(name = "phoneNumber")
    private String phoneNumber;
//    @Column(name = "name")
    private String name;
//    @Column(name = "avatar")
    private String avatar;
//    @Column(name = "location")
    private String location;
//    @Column(name = "rsvp")
    private String rsvp;

    public User() {
        userId = "";
        phoneNumber = "";
        name = "";
        avatar = "";
        location = "";
        rsvp = "";
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

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
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
            data.put("phonenumber",phoneNumber);
            data.put("name", name);
            data.put("avatar", avatar);
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
            user.setAvatar(data.getString("avatar"));
            user.setLocation(data.getString("location"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return user;
    }

    public String getRsvp() {
        return rsvp;
    }

    public void setRsvp(String rsvp) {
        this.rsvp = rsvp;
    }
}

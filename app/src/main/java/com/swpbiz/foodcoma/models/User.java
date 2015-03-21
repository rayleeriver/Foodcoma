package com.swpbiz.foodcoma.models;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class User {
    private String id;
    private String phoneNumber;
    private String name;
    private String avatar;
    private String location;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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
            data.put("id", id);
            data.put("phonenumber",phoneNumber);
            data.put("name", name);
            data.put("avatar", avatar);
            data.put("location", location);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return data;
    }
}

package com.swpbiz.foodcoma.models;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import java.util.ArrayList;

import java.io.Serializable;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

// @Table(name = "invitation")

public class Invitation implements Parcelable {


    // @Column(name = "invitationId", unique = true)
    private String invitationId;
    // @Column(name = "users")
    private HashMap<String, User> users;

    // @Column(name = "owner")
    private User owner;
    // @Column(name = "mapUrl")

    private Restaurant restaurant;
    private String placeName;

    private String mapUrl;
    // @Column(name = "timeOfEvent")
    private long timeOfEvent;

    private boolean accept;

    public Invitation() {
        invitationId = "";
        placeName = "";
        users = new HashMap<String, User>();
        owner = new User();
        mapUrl = "";
        timeOfEvent = 0;
        restaurant = new Restaurant();
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public String getMapUrl() {
        return mapUrl;
    }

    public void setMapUrl(String mapUrl) {
        this.mapUrl = mapUrl;
    }

    public String getInvitationId() {
        return invitationId;
    }

    public void setInvitationId(String id) {
        this.invitationId = id;
    }

    public HashMap<String, User> getUsers() {
        return users;
    }

    public List<User> getUsersList() {
        List<User> userList = new ArrayList<User>();
        if(users != null){
            for(Map.Entry<String,User> item : users.entrySet()){
                userList.add(item.getValue());
            }
        }
        return userList;
    }

    public void setUsers(HashMap<String, User> users) {
        this.users = users;
    }

    public long getTimeOfEvent() {
        return timeOfEvent;
    }

    public void setTimeOfEvent(long timeOfEvent) {
        this.timeOfEvent = timeOfEvent;
    }

    public String getPlaceName() {
        return placeName;
    }

    public void setPlaceName(String placeName) {
        this.placeName = placeName;
    }

    public boolean isAccept() {
        return accept;
    }

    public void setAccept(boolean accept) {
        this.accept = accept;
    }

    public Restaurant getRestaurant() {
        return restaurant;
    }

    public void setRestaurant(Restaurant restaurant) {
        this.restaurant = restaurant;
    }

    public JSONObject getJsonObject() {
        JSONObject data = new JSONObject();
        JSONArray usersJSONarray;

        try {
            data.put("mapurl", mapUrl);
            data.put("invitationId", invitationId);
            data.put("timeofevent", timeOfEvent);
            data.put("owner", owner.getJsonObject());
            usersJSONarray = new JSONArray();
            if (users != null && users.size() != 0) {
                // Get a set of the entries
                Set set = users.entrySet();
                // Get an iterator
                Iterator itr = set.iterator();
                // Display elements
                while (itr.hasNext()) {
                    Map.Entry me = (Map.Entry) itr.next();
                    User user;
                    String phoneNumber;

                    JSONObject uobj = new JSONObject();
                    phoneNumber = (String) me.getKey();
                    user = (User) me.getValue();
                    uobj = user.getJsonObject();
                    usersJSONarray.put(uobj);
                }

                data.put("users", usersJSONarray);
            }
            if (restaurant == null) {
                restaurant =  new Restaurant();
            }
            data.put("restaurant",restaurant.getJsonObject());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return data;

    }

    public static Invitation getInvitationFromJsonObject(String data) {
        Invitation i = new Invitation();
        try {
            JSONObject obj = new JSONObject(data);
            JSONObject d = obj.getJSONObject("data");
            i.setTimeOfEvent(d.getLong("timeofevent"));
            i.setMapUrl(d.getString("mapurl"));
            i.setOwner(User.getUserFromJsonObject(d.getJSONObject("owner")));
            JSONArray users = d.getJSONArray("users");
            HashMap<String, User> usersHashMap = new HashMap<>();
            for (int j = 0; j < users.length(); j++) {
                User user = User.getUserFromJsonObject(users.getJSONObject(j));
                usersHashMap.put(user.getPhoneNumber(), user);
            }
            i.setUsers(usersHashMap);
            i.setRestaurant(Restaurant.getResFromJsonObject( d.getJSONObject("restaurant")));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        // i.save();
        return i;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(invitationId);
        dest.writeParcelable(owner, PARCELABLE_WRITE_RETURN_VALUE);
        dest.writeString(placeName);
        dest.writeString(mapUrl);
        dest.writeLong(timeOfEvent);


  //      Write the size of users HashMap
        dest.writeInt(users.size());

//        Write each key and value of users HashMap
        for(Map.Entry<String, User> item : users.entrySet()) {
            dest.writeString(item.getKey());
            dest.writeParcelable(item.getValue(), PARCELABLE_WRITE_RETURN_VALUE);
        }
        dest.writeParcelable(restaurant, PARCELABLE_WRITE_RETURN_VALUE);

        dest.writeByte((byte) (accept ? 1 : 0));
    }

    public static final Parcelable.Creator<Invitation> CREATOR
            = new Parcelable.Creator<Invitation>() {

        @Override
        public Invitation createFromParcel(Parcel source) {
            return new Invitation(source);
        }

        @Override
        public Invitation[] newArray(int size) {
            return new Invitation[0];
        }

    };

    private Invitation(Parcel in) {
        invitationId = in.readString();
        owner = in.readParcelable(User.class.getClassLoader());
        placeName = in.readString();
        mapUrl = in.readString();
        timeOfEvent = in.readLong();

        // Get the size of users HashMap
        int userSize = in.readInt();

        // Create HashMap if needed
        if(userSize > 0){
            users = new HashMap<String, User>();
        }

        // Populate data into the HashMap
        for(int i = 0; i < userSize; i++) {
            users.put(in.readString(), (User) in.readParcelable(User.class.getClassLoader()));
        }

        restaurant = in.readParcelable(Restaurant.class.getClassLoader());

        accept = in.readByte() != 0;

    }
}

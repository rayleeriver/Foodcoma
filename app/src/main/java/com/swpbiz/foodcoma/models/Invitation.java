package com.swpbiz.foodcoma.models;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

// @Table(name = "invitation")
public class Invitation {

    // @Column(name = "invitationId", unique = true)
    private String invitationId;
    // @Column(name = "users")
    private HashMap<String, User> users;

    // @Column(name = "owner")
    private User owner;
    // @Column(name = "mapUrl")
    private String mapUrl;
    // @Column(name = "timeOfEvent")
    private long timeOfEvent;

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

    public void setUsers(HashMap<String, User> users) {
        this.users = users;
    }

    public long getTimeOfEvent() {
        return timeOfEvent;
    }

    public void setTimeOfEvent(long timeOfEvent) {
        this.timeOfEvent = timeOfEvent;
    }

    public JSONObject getJsonObject() {
        JSONObject data = new JSONObject();
        JSONArray usersJSONarray;

        try {
            data.put("mapurl", mapUrl);
            data.put("invitationId",invitationId);
            data.put("timeofevent",timeOfEvent);
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
            JSONArray users = d.getJSONArray("users");
            HashMap<String, User> usersHashMap = new HashMap<>();
            for(int j = 0; j < users.length(); j++){
                User user = User.getUserFromJsonObject(users.getJSONObject(j));
                usersHashMap.put(user.getPhoneNumber(), user);
            }
            i.setUsers(usersHashMap);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        // i.save();
        return i;
    }


}

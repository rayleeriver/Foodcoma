package com.swpbiz.foodcoma.models;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Invitation {
    private String id;
    private HashMap<User, Rsvp> users;
    private String mapUrl;
    private long timeOfEvent;

    public String getMapUrl() {
        return mapUrl;
    }

    public void setMapUrl(String mapUrl) {
        this.mapUrl = mapUrl;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public HashMap<User, Rsvp> getUsers() {
        return users;
    }

    public void setUsers(HashMap<User, Rsvp> users) {
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
            data.put("id",id);
            data.put("timeofevent",timeOfEvent);
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
                    JSONObject uobj = new JSONObject();
                    user = (User) me.getKey();
                    uobj = user.getJsonObject();
                    Rsvp rsvp = (Rsvp) me.getValue();
                    uobj.put("rsvp", rsvp);
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
//            i.setUsers();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return i;
    }


}

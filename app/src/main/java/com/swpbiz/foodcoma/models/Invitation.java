package com.swpbiz.foodcoma.models;

import java.util.HashMap;
import java.util.List;

public class Invitation {
    private String id;
    private HashMap<User, Rsvp> users;

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

    private String mapUrl;
    private long timeOfEvent;

}

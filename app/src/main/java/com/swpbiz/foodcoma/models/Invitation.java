package com.swpbiz.foodcoma.models;

import android.app.Application;
import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;

import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.swpbiz.foodcoma.FoodcomaApplication;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

// @Table(name = "invitation")

public class Invitation implements Parcelable {

    public static final String PARSE_ACCEPTED_USERS = "acceptedUsers";

    // @Column(name = "invitationId", unique = true)
    private String invitationId;
    // @Column(name = "users")
    private HashMap<String, User> users = new HashMap<String, User>();

    private Set<String> acceptedUsers = new HashSet<>();

    // @Column(name = "owner")
    private User owner;

    private Restaurant restaurant;

    // @Column(name = "timeOfEvent")
    private long timeOfEvent;

    public Invitation() {
        invitationId = "";
        restaurant = new Restaurant();
        owner = new User();
        timeOfEvent = 0;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
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
        userList.add(owner);
        return userList;
    }

    public void setUsers(HashMap<String, User> users) {
        this.users = users;
    }

    public Set<String> getAcceptedUsers() {
        return acceptedUsers;
    }

    public void setAcceptedUsers(Set<String> acceptedUsers) {
        this.acceptedUsers = acceptedUsers;
    }

    public long getTimeOfEvent() {
        return timeOfEvent;
    }

    public void setTimeOfEvent(long timeOfEvent) {
        this.timeOfEvent = timeOfEvent;
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
            data.put("restaurant", restaurant.getJsonObject());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return data;

    }

    public static Invitation getInvitationFromJsonObject(String data) {
        Invitation i = new Invitation();
        try {
            JSONObject d = new JSONObject(data);
            String invitationIdJson = d.optString("invitationId");
            if (invitationIdJson != null && invitationIdJson.length() > 0) {
                i.setInvitationId(d.optString("invitationId"));
            }
            i.setTimeOfEvent(d.getLong("timeofevent"));
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
        dest.writeLong(timeOfEvent);


  //      Write the size of users HashMap
        dest.writeInt(users.size());

//        Write each key and value of users HashMap
        for(Map.Entry<String, User> item : users.entrySet()) {
            dest.writeString(item.getKey());
            dest.writeParcelable(item.getValue(), PARCELABLE_WRITE_RETURN_VALUE);
        }

        dest.writeStringList(new ArrayList<String>(acceptedUsers));
        dest.writeParcelable(restaurant, PARCELABLE_WRITE_RETURN_VALUE);
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

        List<String> acceptedUsersList = new ArrayList<>();
        in.readStringList(acceptedUsersList);
        acceptedUsers.addAll(acceptedUsersList);

        restaurant = in.readParcelable(Restaurant.class.getClassLoader());
    }

    public List<String> getAllPhoneNumbers() {
        ArrayList<String> allPhoneNumbers = new ArrayList<String>();

        // don't forget to include the owner of the invitation
        allPhoneNumbers.add(owner.getPhoneNumber());

        if (getUsers() != null) {
            allPhoneNumbers.addAll(getUsers().keySet());
        }

        return allPhoneNumbers;
    }

    public void addAcceptedUser(String phoneNumber) {
        getAcceptedUsers().add(phoneNumber);
    }

    public void removeAcceptedUser(String phoneNumber) {
        getAcceptedUsers().remove(phoneNumber);
    }

    public boolean isAccepted() {
        return getAcceptedUsers() != null && getAcceptedUsers().size() > 0;
    }

    public boolean isAccepted(String phoneNumber) {
        if (getAcceptedUsers() == null) return false;
        return getAcceptedUsers().contains(phoneNumber);
    }

    public List<User> getUserListExcluding(String phoneNumber) {
        List<User> userList = new ArrayList<User>();
        if(users != null){
            for(Map.Entry<String,User> item : users.entrySet()){
                if (!item.getKey().equals(phoneNumber)) {
                    userList.add(item.getValue());
                }
            }
        }
        if (!owner.getPhoneNumber().equals(phoneNumber)) {
            userList.add(owner);
        }

        Collections.sort(userList, new UsersComparator());
        return userList;
    }

    public List<User> getUserListExcludingSortByAccepted(String phoneNumber) {
        if (acceptedUsers == null || acceptedUsers.size() ==0) {
            return getUserListExcluding(phoneNumber);
        }

        List<User> acceptedList = new ArrayList<>();
        List<User> pendingList = new ArrayList<>();
        for (User user: getUserListExcluding(phoneNumber)) {
            if (acceptedUsers.contains(user.getPhoneNumber())) {
                acceptedList.add(user);
            } else {
                pendingList.add(user);
            }
        }
        acceptedList.addAll(pendingList);
        return acceptedList;
    }

    public static Invitation fromParseObject(ParseObject object) throws JSONException {
        Invitation invitation = new Invitation();
        invitation.setInvitationId(object.getObjectId());

        Restaurant restaurant = new Restaurant();
        restaurant.setName(object.getString("placeName"));
        Location restaurantLocation = new Location("Restaurant");
        ParseGeoPoint placeLatLng = object.getParseGeoPoint("placeLatLng");
        restaurantLocation.setLatitude(placeLatLng.getLatitude());
        restaurantLocation.setLongitude(placeLatLng.getLongitude());
        restaurant.setRestaurantLocation(restaurantLocation);
        invitation.setRestaurant(restaurant);

        JSONArray usersJsonArray = object.getJSONArray("users");
        if (usersJsonArray!= null) {
            for (int i = 0; i < usersJsonArray.length(); i++) {
                String userPhoneNumber = usersJsonArray.getString(i);
                User user = new User();
                user.setPhoneNumber(userPhoneNumber);
                invitation.getUsers().put(userPhoneNumber, user);
            }
        }

        JSONArray acceptedUsersJsonArray = object.getJSONArray("acceptedUsers");
        if (acceptedUsersJsonArray!= null) {
            for (int i = 0; i < acceptedUsersJsonArray.length(); i++) {
                invitation.getAcceptedUsers().add(acceptedUsersJsonArray.getString(i));
            }
        }

        invitation.setTimeOfEvent(object.getLong("timeofevent"));
        invitation.getOwner().setPhoneNumber(object.getString("owner"));

        return invitation;
    }

    public Invitation inflateWithContacts(Application application) {
        for (Map.Entry<String, User> entry: users.entrySet()) {
            users.put(entry.getKey(), ((FoodcomaApplication) application).findContactByPhoneNumber(entry.getKey()));
        }
        return this;
    }
}

package com.swpbiz.foodcoma.services;

import android.content.Context;
import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class LocationUpdatedAsyncTask extends AsyncTask {

    Context context;
    String phonenumber;
    Location location;

    public LocationUpdatedAsyncTask(Context context, Location location) {
        this.context = context;
        this.location = location;
    }

    @Override
    protected Object doInBackground(Object[] params) {
        ParseQuery pushQuery = ParseInstallation.getQuery();
        ParseInstallation installation = ParseInstallation.getCurrentInstallation();
        phonenumber = (String)installation.get("phonenumber");

        if (phonenumber == null) {
            return null;
        }

        ParsePush push = new ParsePush();

        JSONObject data =  new JSONObject();
        try {
            data.put("title","location update for " + phonenumber);
            data.put("alert","Location update for " + phonenumber);

            JSONObject latlng = new JSONObject();
            latlng.put("lat", location.getLatitude());
            latlng.put("lng", location.getLongitude());

            data.put("data", latlng);
        } catch (JSONException e) {
            e.printStackTrace();
        }

//        pushQuery.whereNotEqualTo("phonenumber", phonenumber);
//        push.setQuery(pushQuery); // Set our Installation query
//        push.setData(data);
//        push.sendInBackground();

        ParseUser user = ParseUser.getCurrentUser();
        if (user != null) {
            user.put("userlocation", new ParseGeoPoint(location.getLatitude(), location.getLongitude()));
            user.saveInBackground();
            Log.d("DEBUG", "location:" + location.toString());
        }
        return location;
    }

    @Override
    protected void onPostExecute(Object o) {
        if (location != null) {
           // Toast.makeText(context, "Location changed to: " + location.toString(), Toast.LENGTH_SHORT).show();
        }
    }
}

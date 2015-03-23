package com.swpbiz.foodcoma.services;

import android.content.Context;
import android.location.Location;
import android.os.AsyncTask;
import android.widget.Toast;

import com.parse.ParseInstallation;
import com.parse.ParsePush;
import com.parse.ParseQuery;

import org.json.JSONException;
import org.json.JSONObject;

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
//        pushQuery.whereEqualTo("phonenumber", phonenumber); // Receiver list (Currently set it to the owner for testing purpose)
        ParsePush push = new ParsePush();

        JSONObject data =  new JSONObject();
        try {
            data.put("title","location update for " + phonenumber);
            data.put("alert","New Invitation");

            JSONObject latlng = new JSONObject();
            latlng.put("lat", location.getLatitude());
            latlng.put("lng", location.getLongitude());

            data.put("data", latlng);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        push.setQuery(pushQuery); // Set our Installation query
        push.setData(data);
        push.sendInBackground();

        return location;
    }

    @Override
    protected void onPostExecute(Object o) {
        Toast.makeText(context, "Location changed to: " + location.toString(), Toast.LENGTH_SHORT).show();

    }
}

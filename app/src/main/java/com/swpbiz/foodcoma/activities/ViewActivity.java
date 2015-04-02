package com.swpbiz.foodcoma.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;

import android.graphics.Color;
import android.location.Location;
import android.os.Parcelable;

import android.content.SharedPreferences;
import android.location.Location;
import android.os.SystemClock;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import android.widget.ListView;

import android.view.animation.BounceInterpolator;
import android.widget.EditText;

import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.activeandroid.query.From;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Notifications;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.FindCallback;
import com.parse.ParseGeoPoint;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.swpbiz.foodcoma.FoodcomaApplication;
import com.swpbiz.foodcoma.R;
import com.swpbiz.foodcoma.adapters.FriendListAdapter;
import com.swpbiz.foodcoma.models.Invitation;
import com.swpbiz.foodcoma.models.User;
import com.swpbiz.foodcoma.utils.MyDateTimeUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.logging.Handler;
import java.util.logging.LogRecord;


public class ViewActivity extends ActionBarActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,LocationListener {

    private static final int REQUEST_PLACE_PICKER = 12123;
    private SupportMapFragment mapFragment;
    private GoogleMap map;
    private GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;
    private long UPDATE_INTERVAL = 60000;
    private long FASTEST_INTERVAL = 5000;
    private TextView tvTime;
    private TextView tvDate;
    private TextView tvEventName;
    private TextView tvCreator;
    private RelativeLayout rlAccept;
    private RelativeLayout rlReject;
    private Invitation invitation;

    private ListView lvContacts;
    private User user;

    private String phonenumber;
    private android.os.Handler handler;
    private ArrayList<Marker> markers;



    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view);


        SharedPreferences sharedPref = getSharedPreferences("foodcoma", Context.MODE_PRIVATE);
        phonenumber = sharedPref.getString(getString(R.string.my_phone_number), null);

        markers = new ArrayList<Marker>();
        handler =  new android.os.Handler();



        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.fragmentMap);
        if (mapFragment != null) {
            mapFragment.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap googleMap) {
                    loadMap(googleMap);
                }
            });
        } else {
            Toast.makeText(this, "Error -Map fragment was null!!!", Toast.LENGTH_SHORT).show();
        }
        
        Intent i = getIntent();
        if (i != null) {
            String activityName = i.getStringExtra("activityname");
            if (activityName != null && activityName.equals("CreateActivity")) {
                // From MainActivity
                invitation = getIntent().getParcelableExtra("invitation");
            } else {
                String data = i.getStringExtra("data");
                // From Push Notifications
                invitation = Invitation.getInvitationFromJsonObject(data);
            }

            setupViews();

            tvDate.setText(MyDateTimeUtil.getDateFromEpoch(invitation.getTimeOfEvent()));
            tvTime.setText(MyDateTimeUtil.getTimeFromEpoch(invitation.getTimeOfEvent()));
            tvEventName.setText(invitation.getPlaceName());
            tvCreator.setText(invitation.getOwner().getName());

            if (invitation.isAccept()) {
                rlAccept.setBackgroundColor(getResources().getColor(R.color.primary_dark));
            } else {
                rlAccept.setBackgroundColor(Color.parseColor("#cccccc"));
            }
        }
    }

    private void setupViews() {
        tvDate = (TextView) findViewById(R.id.tvDate);
        tvTime = (TextView) findViewById(R.id.tvTime);
        tvEventName = (TextView) findViewById(R.id.tvEventName);
        tvCreator = (TextView) findViewById(R.id.tvCreator);
        rlAccept = (RelativeLayout) findViewById(R.id.rlAccept);
        rlReject = (RelativeLayout) findViewById(R.id.rlReject);
        lvContacts = (ListView) findViewById(R.id.lvContacts);

//        Log.d("DEBUG-FRIENDS", invitation.getUsers().keySet().toString());
        Log.d("DEBUG-FRIENDS", invitation.getUsersList().size() + "");

        FriendListAdapter adapter = new FriendListAdapter(this, invitation.getUsersList());
        lvContacts.setAdapter(adapter);

        // When the user clicks 'Accept' (I'm going)
        rlAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Send 'Accept' push noti to everyone
                ParseQuery pushQuery = ParseInstallation.getQuery();
                ParseInstallation installation = ParseInstallation.getCurrentInstallation();
                String phonenumber = (String)installation.get("phonenumber");
                HashMap<String, User> users = invitation.getUsers();
                User user = users.get(phonenumber);

                if (user != null) {
                    user.setRsvp("ACCEPTED");

                    pushQuery.whereEqualTo("phonenumber", phonenumber);

                    ParsePush push2 = new ParsePush();

                    JSONObject data = new JSONObject();
                    try {
                        data.put("title", "Foodcoma");
                        data.put("alert", phonenumber + " has accepted invitation");
                        data.put("data", invitation.getJsonObject());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    push2.setQuery(pushQuery); // Set our Installation query
                    push2.setData(data);
                    push2.sendInBackground();
                }
            }
        });

        rlReject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: send reject push noti
            }
        });

    }

    protected void loadMap(GoogleMap googleMap) {
        map = googleMap;
        if (map != null) {
            // Map is ready
            Toast.makeText(this, "Map Fragment was loaded properly!", Toast.LENGTH_SHORT).show();
            map.setMyLocationEnabled(true);
            // Now that map has loaded, let's get our location!
            googleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this).build();

            connectClient();
        } else {
            Toast.makeText(this, "Error - Map was null!!", Toast.LENGTH_SHORT).show();
        }
    }


    private void connectClient() {
        if (isGooglePlayServicesAvailable() && googleApiClient != null) {
            googleApiClient.connect();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_view, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement


        return super.onOptionsItemSelected(item);
    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {

            map.clear();

            LatLng resLoc = new LatLng(invitation.getRestaurant().getRestaurantLocation().getLatitude(), invitation.getRestaurant().getRestaurantLocation().getLongitude());
            Marker marker = map.addMarker(new MarkerOptions().position(resLoc).title(invitation.getRestaurant().getName()));
            marker.showInfoWindow();

            Set set = invitation.getUsers().entrySet();

            ArrayList<String> phonenumbers = new ArrayList<String>();
            // Get an iterator
            Iterator itr = set.iterator();
            int index = 0;
            // Display elements
            while (itr.hasNext()) {
                Map.Entry me = (Map.Entry) itr.next();
                phonenumbers.add(index, (String)me.getKey());
                index++;
            }

        /* fetch object of all the users from parse for this Invitation to get their location */
            ParseQuery<ParseUser> query = ParseUser.getQuery();
            query.whereContainedIn("phonenumber", phonenumbers);
            query.findInBackground(new FindCallback<ParseUser>() {
                @Override
                public void done(List<ParseUser> parseUsers, com.parse.ParseException e) {
                    String userphonenumber;
                    if (e == null) {
                        Log.d("DEBUG", "Retrieved " + parseUsers.size() + " phonenumber");

                        for (int i = 0; i < parseUsers.size(); i++) {
                            userphonenumber = parseUsers.get(i).getString("phonenumber");
                            ParseGeoPoint uloc = parseUsers.get(i).getParseGeoPoint("userlocation");
                            LatLng userloc = new LatLng(uloc.getLatitude(),uloc.getLongitude());
                            Marker marker = map.addMarker(new MarkerOptions().position(userloc).title(userphonenumber));
                            marker.showInfoWindow();
                        }

                    } else {
                        Log.d("score", "Error: " + e.getMessage());
                    }
                }

//                @Override
//                public void done(List<ParseObject> parseObjects, com.parse.ParseException e) {
//                    String userphonenumber;
//                    if (e == null) {
//                        Log.d("DEBUG", "Retrieved " + parseObjects.size() + " phonenumber");
//                        map.clear();
//                        for (int i = 0; i < parseObjects.size(); i++) {
//                            userphonenumber = parseObjects.get(i).getString("phonenumber");
//                            ParseGeoPoint uloc = parseObjects.get(i).getParseGeoPoint("userlocation");
//                            LatLng userloc = new LatLng(uloc.getLatitude(),uloc.getLongitude());
//                            Marker marker = map.addMarker(new MarkerOptions().position(userloc).title(userphonenumber));
//                            marker.showInfoWindow();
//                        }
//
//                    } else {
//                        Log.d("score", "Error: " + e.getMessage());
//                    }
//                }



            });
        }
    };


    @Override
    protected void onStart() {
        super.onStart();
        connectClient();
        handler.postDelayed(runnable,1000);
    }

    @Override
    protected void onStop() {
        if (googleApiClient != null) {
            googleApiClient.disconnect();
        }
        super.onStop();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode)  {
            case CONNECTION_FAILURE_RESOLUTION_REQUEST:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        googleApiClient.connect();
                }
        }
    }

    public boolean isGooglePlayServicesAvailable() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (ConnectionResult.SUCCESS == resultCode) {
            Log.d("Location Updates", "Google Play service is available");
            return true;
        } else {
            Toast.makeText(this, "Error - Google Play Services is not available", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        Location location = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
        if (location != null) {
            Toast.makeText(this, "GPS Location was found!!", Toast.LENGTH_SHORT).show();
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 17);
            map.animateCamera(cameraUpdate);
            LatLng loc = new LatLng(location.getLatitude(), location.getLongitude());
            addMarker(loc, phonenumber);
            startLocationUpdates();
        } else {
            Toast.makeText(this, "Error - current location is null, enable GPS!", Toast.LENGTH_SHORT).show();
        }
    }

    public void addMarker(LatLng location, String title) {
        // inflate message_item.xml view
        View messageView = LayoutInflater.from(ViewActivity.this).
                inflate(R.layout.message_item, null);
        BitmapDescriptor defaultMarker =
                BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED);
        // Extract content from alert dialog
//        String title = ((EditText) messageView.findViewById(R.id.etTitle)).
//                getText().toString();
//        String snippet = ((EditText) messageView.findViewById(R.id.etSnippet)).
//                getText().toString();




        LatLng myloc = new LatLng(location.latitude,location.longitude);
        map.clear();
        Marker marker = map.addMarker(new MarkerOptions().position(myloc).title(title).icon(defaultMarker));
        marker.showInfoWindow();

        // Creates and adds marker to the map
//            Marker marker = map.addMarker(new MarkerOptions()
//                    .position(myloc)
//                    .title(title)
//                    .snippet(snippet)
//                    .icon(defaultMarker));
//
//            dropPinEffect(marker);

    }

    protected void startLocationUpdates() {
        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        locationRequest.setInterval(UPDATE_INTERVAL);
        locationRequest.setFastestInterval(FASTEST_INTERVAL);
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient,
                locationRequest, this);
    }

    @Override
    public void onLocationChanged(Location location) {
//        String msg = "Update location: " +
//                Double.toString(location.getLatitude()) + ", " +
//                Double.toString(location.getLongitude());
//        Log.d("DEBUG",msg);
//        LatLng loc = new LatLng(location.getLatitude(), location.getLongitude());
//        markers.clear();
//        for (int i = 0; i < markers.size(); i++) {
//            addMarker(loc, phonenumber);
//        }
//
//        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnectionSuspended(int i) {
        if (i == CAUSE_SERVICE_DISCONNECTED)
            Toast.makeText(this, "Disconnected.  Please re-connect.", Toast.LENGTH_SHORT).show();
        else if (i == CAUSE_NETWORK_LOST)
            Toast.makeText(this, "Network Lost.  Please re-connect.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                connectionResult.startResolutionForResult(this,
                        CONNECTION_FAILURE_RESOLUTION_REQUEST);
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(getApplicationContext(), "Sorry.  Location service is not available to you",
                    Toast.LENGTH_SHORT).show();
        }
    }

}

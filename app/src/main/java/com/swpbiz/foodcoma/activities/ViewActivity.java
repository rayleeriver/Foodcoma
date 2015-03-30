package com.swpbiz.foodcoma.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Color;
import android.location.Location;
import android.os.Parcelable;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
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
import com.google.android.gms.maps.model.LatLng;
import com.parse.ParseInstallation;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.swpbiz.foodcoma.R;
import com.swpbiz.foodcoma.adapters.FriendListAdapter;
import com.swpbiz.foodcoma.models.Invitation;
import com.swpbiz.foodcoma.models.User;
import com.swpbiz.foodcoma.utils.MyDateTimeUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;


public class ViewActivity extends ActionBarActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final int REQUEST_PLACE_PICKER = 12123;
    private SupportMapFragment mapFragment;
    private GoogleMap map;
    private GoogleApiClient googleApiClient;
//    private LocationRequest locationRequest;
//    private long UPDATE_INTERVAL = 60000;
//    private long FASTEST_INTERVAL = 5000;
    private TextView tvTime;
    private TextView tvDate;
    private TextView tvEventName;
    private TextView tvCreator;
    private RelativeLayout rlAccept;
    private RelativeLayout rlReject;
    private Invitation invitation;
    private ListView lvContacts;
    private User user;


    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view);

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
            Log.d("DEBUG", "Get the intent");

            invitation = getIntent().getParcelableExtra("invitation");
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

        Log.d("DEBUG-FRIENDS", invitation.getUsers().keySet().toString());
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
                user.setRsvp("ACCEPTED");

                pushQuery.whereEqualTo("phonenumber", phonenumber);

                ParsePush push2 = new ParsePush();

                JSONObject data =  new JSONObject();
                try {
                    data.put("title","Foodcoma");
                    data.put("alert", phonenumber + " has accepted invitation");
                    data.put("data", invitation.getJsonObject());
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                push2.setQuery(pushQuery); // Set our Installation query
                push2.setData(data);
                push2.sendInBackground();
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

    @Override
    protected void onStart() {
        super.onStart();
        connectClient();
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
//            startLocationUpdates();
        } else {
            Toast.makeText(this, "Error - current location is null, enable GPS!", Toast.LENGTH_SHORT).show();
        }
    }

//    protected void startLocationUpdates() {
//        locationRequest = new LocationRequest();
//        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
//        locationRequest.setInterval(UPDATE_INTERVAL);
//        locationRequest.setFastestInterval(FASTEST_INTERVAL);
//        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient,
//                locationRequest, this);
//    }

//    @Override
//    public void onLocationChanged(Location location) {
//        String msg = "Update location: " +
//                Double.toString(location.getLatitude()) + ", " +
//                Double.toString(location.getLongitude());
//        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
//    }
//
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

package com.swpbiz.foodcoma.activities;

import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.location.Location;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseInstallation;
import com.parse.ParsePush;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.SignUpCallback;
import com.swpbiz.foodcoma.FoodcomaApplication;
import com.swpbiz.foodcoma.R;
import com.swpbiz.foodcoma.services.AndroidLocationServices;

import java.util.List;


public class MainActivity extends ActionBarActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,LocationListener {
    private GoogleMap map;
    private GoogleApiClient googleApiClient;
    public String phoneNumber;
    static final int SET_NUMBER = 1;

    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        phoneNumber = getPhoneNumber();
        Toast.makeText(this, "Your phone number: " + phoneNumber, Toast.LENGTH_SHORT).show();

        // Check whether the user has set the number before, if not, call the SetNumberActivity
        if(phoneNumber == null) {
            Intent i = new Intent(MainActivity.this, SetNumberActivity.class);
            startActivityForResult(i, SET_NUMBER);
        } else {
            subscribeWithParse();
        }

        if (getIntent().getExtras() != null) {
            String[] names = getIntent().getExtras().getStringArray("names");
            Toast.makeText(this, "Names selected: " + TextUtils.join(", ", names), Toast.LENGTH_SHORT).show();
        }

        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this).build();

        connectClient();

//        Intent i = new Intent(MainActivity.this, LocationIntentService.class);
//        startService(i);

        Intent intent = new Intent(this, AndroidLocationServices.class);
        startService(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_create) {
            Intent i = new Intent(MainActivity.this, CreateActivity.class);
            startActivity(i);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == SET_NUMBER) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                phoneNumber = getPhoneNumber();
                registerWithParse();

            }
        }
    }

    private void registerWithParse() {
        ParseInstallation installation = ParseInstallation.getCurrentInstallation();

        installation.put("phonenumber", phoneNumber);

        ParseUser user = new ParseUser();
        user.setUsername(phoneNumber); // Mandatory
        user.setPassword(phoneNumber); // Mandatory
        user.setEmail(phoneNumber+ "@foodcoma.com"); // Mandatory
        user.put("phonenumber", phoneNumber);
        FoodcomaApplication mapp = (FoodcomaApplication)getApplicationContext();
        user.put("userlocation",new ParseGeoPoint(mapp.getMylatitude(),mapp.getMylongitude()));

        user.signUpInBackground(new SignUpCallback() {
            public void done(ParseException e) {
                if (e == null) {
                    Log.d("DEBUG","Sign up successful");
                    subscribeWithParse();
                } else {
                    Log.d("DEBUG","Sign up failed");
                }
            }
        });

        // Save the current Installation to Parse.
        installation.saveInBackground();

    }

    private void subscribeWithParse() {
        ParseUser.logInInBackground(phoneNumber, phoneNumber, new LogInCallback() {
            public void done(ParseUser user, ParseException e) {
                if (user != null) {
                    Log.d("DEBUG","Sign In successful");
                } else {
                    Log.d("DEBUG","Sign In failed");
                }
            }
        });


        ParseUser.getCurrentUser().saveInBackground();

        ParsePush.subscribeInBackground("", new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    List<String> subscribedChannels = ParseInstallation.getCurrentInstallation().getList("channels");
                    Log.d("DEBUG", "Channel List: " + subscribedChannels.toString());
                    Log.d("com.parse.push", "successfully subscribed to the broadcast channel.");
                } else {
                    Log.e("com.parse.push", "failed to subscribe for push", e);
                }
            }
        });
    }

    private String getPhoneNumber() {
        SharedPreferences sharedPref = getSharedPreferences("foodcoma", Context.MODE_PRIVATE);
        return sharedPref.getString(getString(R.string.my_phone_number), null);
    }

    private void connectClient() {
        if (isGooglePlayServicesAvailable() && googleApiClient != null) {
            googleApiClient.connect();
        }
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

            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

            FoodcomaApplication mapp = (FoodcomaApplication)getApplicationContext();
            mapp.setMylatitude(location.getLatitude());
            mapp.setMylongitude(location.getLongitude());
            ParseUser user = ParseUser.getCurrentUser();
            user.put("userlocation",new ParseGeoPoint(mapp.getMylatitude(),mapp.getMylongitude()));
            user.saveInBackground();
            Toast.makeText(this, "GPS Location was found!!" + location.getLatitude()+ ","+ location.getLongitude(), Toast.LENGTH_SHORT).show();
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

    @Override
    public void onLocationChanged(Location location) {
        String msg = "Update location: " +
                Double.toString(location.getLatitude()) + ", " +
                Double.toString(location.getLongitude());
        FoodcomaApplication mapp = (FoodcomaApplication)getApplicationContext();
        mapp.setMylatitude(location.getLatitude());
        mapp.setMylongitude(location.getLongitude());
        ParseUser user = ParseUser.getCurrentUser();
        user.put("userlocation",new ParseGeoPoint(mapp.getMylatitude(),mapp.getMylongitude()));
        user.saveInBackground();
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
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
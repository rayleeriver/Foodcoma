package com.swpbiz.foodcoma.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.ContactsContract;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.parse.FindCallback;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.SignUpCallback;
import com.swpbiz.foodcoma.FoodcomaApplication;
import com.swpbiz.foodcoma.R;
import com.swpbiz.foodcoma.adapters.InvitationsArrayAdapter;
import com.swpbiz.foodcoma.models.Invitation;
import com.swpbiz.foodcoma.models.InvitationsComparator;
import com.swpbiz.foodcoma.models.User;
import com.swpbiz.foodcoma.services.AndroidLocationServices;
import com.swpbiz.foodcoma.services.ContactsLoaderIntentService;
import com.swpbiz.foodcoma.services.ContactsLoaderIntentServiceBroadcastReceiver;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;


public class MainActivity extends ActionBarActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private final static String TAG = MainActivity.class.getSimpleName();
    private GoogleMap map;
    private GoogleApiClient googleApiClient;
    public String phoneNumber;
    static final int SET_NUMBER = 1;
    ListView lvInvitations;
    InvitationsArrayAdapter lvInvitationsAdapter;
    List<Invitation> invitations = new ArrayList<>();
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private SwipeRefreshLayout swipeRefreshLayout;

    Intent androidLocationServiceIntent;
    BroadcastReceiver contactsLoaderIntentServiceReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setLogo(R.mipmap.ic_burger);
        getSupportActionBar().setDisplayUseLogoEnabled(true);

        contactsLoaderIntentServiceReceiver = new ContactsLoaderIntentServiceBroadcastReceiver(getApplication());

        setupInvitationsList();

        phoneNumber = getPhoneNumber();
        //Toast.makeText(this, "Your phone number: " + phoneNumber, Toast.LENGTH_SHORT).show();
        Log.d("DEBUG", "Your phone number: " + phoneNumber);

        // Check whether the user has set the number before, if not, call the SetNumberActivity
        if (phoneNumber == null) {
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

        androidLocationServiceIntent= new Intent(this, AndroidLocationServices.class);
        startService(androidLocationServiceIntent);
    }

    private void setupInvitationsList() {
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeContainer);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                populateMyInvitations(invitations);
                swipeRefreshLayout.setRefreshing(false);
            }
        });

        swipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        lvInvitations = (ListView) findViewById(R.id.lvInvitations);
        lvInvitationsAdapter = new InvitationsArrayAdapter(this, invitations, getPhoneNumber());
        lvInvitations.setAdapter(lvInvitationsAdapter);

        lvInvitations.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent i = new Intent(MainActivity.this, ViewActivity.class);
                i.putExtra("invitation", (Parcelable) invitations.get(position).inflateWithContacts(getApplication()));
                startActivity(i);
                overridePendingTransition(R.anim.right_in, R.anim.left_out);
            }
        });
    }

    private void populateMyInvitations(final List<Invitation> myInvitations) {

        // from past 1 hour to future
        Date fromDate = new Date(new Date().getTime() - 2*3600*1000);

        ParseQuery<ParseObject> myInvitationsQuery = ParseQuery.getQuery("Invitation");
        myInvitationsQuery.whereEqualTo("owner", phoneNumber);
        myInvitationsQuery.whereGreaterThan("timeofevent", fromDate.getTime());

        ParseQuery<ParseObject> receivedInvitationsQuery = ParseQuery.getQuery("Invitation");
        receivedInvitationsQuery.whereEqualTo("users", phoneNumber);
        receivedInvitationsQuery.whereGreaterThan("timeofevent", fromDate.getTime());

        List<ParseQuery<ParseObject>> queries = new ArrayList<>();
        queries.add(myInvitationsQuery);
        queries.add(receivedInvitationsQuery);

        ParseQuery<ParseObject> mainQuery = ParseQuery.or(queries);
        mainQuery.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null) {
                    lvInvitationsAdapter.clear();
                    Log.d(TAG, "get my invitations count: " + objects.size());

                    for (ParseObject object : objects) {
                        Invitation invitation = null;
                        try {
                            invitation = Invitation.fromParseObject(object);
                        } catch (JSONException e1) {
                            e1.printStackTrace();
                        }


                        if (invitation.getOwner().getPhoneNumber().equals(phoneNumber)) {
                            invitation.getOwner().setName("me");
                        } else {
                            String[] projection = new String[]{
                                    ContactsContract.PhoneLookup.DISPLAY_NAME,
                                    ContactsContract.PhoneLookup._ID};

                            Uri contactUri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(invitation.getOwner().getPhoneNumber()));
                            Cursor cursor = getContentResolver().query(contactUri, projection, null, null, null);

                            if (cursor.moveToFirst()) {
                                String name = cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
                                invitation.getOwner().setName(name);
                            }
                        }

                        List<String> userPhonenumberList = object.getList("users");
                        final HashMap<String, User> usersMap = new HashMap<String, User>();
                        if (userPhonenumberList != null) {
                            for (final String userPhoneNumber : userPhonenumberList) {
                                User user = new User();
                                user.setPhoneNumber(userPhoneNumber);
                                usersMap.put(userPhoneNumber, user);
                            }
                        }
                        invitation.setUsers(usersMap);

                        List<String> acceptedUsersList = object.getList(Invitation.PARSE_ACCEPTED_USERS);
                        invitation.setAcceptedUsers(new HashSet<String>(acceptedUsersList));

//                        if (object.<String>getList("acceptedUsers") != null) {
//                            invitation.getAcceptedUsersPhoneNumbers().addAll(object.<String>getList("acceptedUsers"));
//                        }

                        myInvitations.add(invitation);

                        Collections.sort(myInvitations, new InvitationsComparator());
                        lvInvitationsAdapter.notifyDataSetChanged();
                    }
                } else {
                    Log.e(TAG, e.getMessage());
                }
            }
        });
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
            overridePendingTransition(R.anim.right_in, R.anim.left_out);
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
        user.setEmail(phoneNumber + "@foodcoma.com"); // Mandatory
        user.put("phonenumber", phoneNumber);
        FoodcomaApplication mapp = (FoodcomaApplication) getApplicationContext();
        user.put("userlocation", new ParseGeoPoint(mapp.getMylatitude(), mapp.getMylongitude()));

        user.signUpInBackground(new SignUpCallback() {
            public void done(ParseException e) {
                if (e == null) {
                    Log.d("DEBUG", "Sign up successful");
                    subscribeWithParse();
                } else {
                    Log.d("DEBUG", "Sign up failed");
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
                    Log.d("DEBUG", "Sign In successful");
                } else {
                    Log.d("DEBUG", "Sign In failed");
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
    protected void onResume() {
        super.onResume();
        populateMyInvitations(invitations);

        IntentFilter filter = new IntentFilter(ContactsLoaderIntentService.ACTION);
        LocalBroadcastManager.getInstance(this).registerReceiver(contactsLoaderIntentServiceReceiver, filter);
    }

    @Override
    protected void onStop() {
        if (googleApiClient != null) {
            googleApiClient.disconnect();
        }
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        stopService(androidLocationServiceIntent);
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

            FoodcomaApplication mapp = (FoodcomaApplication) getApplicationContext();
            mapp.setMylatitude(location.getLatitude());
            mapp.setMylongitude(location.getLongitude());
            ParseUser user = ParseUser.getCurrentUser();
            if (user != null) {
                user.put("userlocation", new ParseGeoPoint(mapp.getMylatitude(), mapp.getMylongitude()));
                user.saveInBackground();
            }
        } else {
            Toast.makeText(this, "Error - current location is null, enable GPS!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        String msg = "Update location: " +
                Double.toString(location.getLatitude()) + ", " +
                Double.toString(location.getLongitude());
        FoodcomaApplication mapp = (FoodcomaApplication) getApplicationContext();
        mapp.setMylatitude(location.getLatitude());
        mapp.setMylongitude(location.getLongitude());
        ParseUser user = ParseUser.getCurrentUser();
        user.put("userlocation", new ParseGeoPoint(mapp.getMylatitude(), mapp.getMylongitude()));
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
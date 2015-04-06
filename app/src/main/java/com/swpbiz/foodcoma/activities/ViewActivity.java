package com.swpbiz.foodcoma.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.parse.FindCallback;
import com.parse.GetCallback;
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

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class ViewActivity extends ActionBarActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private static final String TAG = ViewActivity.class.getSimpleName();

    private static final int REQUEST_PLACE_PICKER = 12123;
    private SupportMapFragment mapFragment;
    private GoogleMap map;
    private GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;
    private long UPDATE_INTERVAL = 60000;
    private long FASTEST_INTERVAL = 5000;
    private float RADIUS = 50; // 50 Meters
    private final String API_KEY = "AIzaSyCqT9dz3gMHQO1P27j0md99PrdpuX30shI";
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
    private LatLngBounds.Builder latLngBoundsBuilder;


    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view);

        getSupportActionBar().setDisplayShowHomeEnabled(true);

        SharedPreferences sharedPref = getSharedPreferences("foodcoma", Context.MODE_PRIVATE);
        phonenumber = sharedPref.getString(getString(R.string.my_phone_number), null);

        markers = new ArrayList<Marker>();
        handler = new android.os.Handler();


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
//            String data = i.getStringExtra("data");
//
//            if (data != null) {
//                // From Push Notifications
//                invitation = Invitation.getInvitationFromJsonObject(data);
//            } else {
                invitation = getIntent().getParcelableExtra("invitation");
//            }


            setupViews();

            tvDate.setText(MyDateTimeUtil.getDateFromEpoch(invitation.getTimeOfEvent()).toUpperCase());
            tvTime.setText(MyDateTimeUtil.getTimeFromEpoch(invitation.getTimeOfEvent()));
            tvEventName.setText(invitation.getRestaurant().getName());

            String ownerName = invitation.getOwner().getName();
            if (ownerName == null || ownerName.length() == 0) {
                ownerName = invitation.getOwner().getPhoneNumber();
            }
            tvCreator.setText("By " + ownerName);

        }
    }

    private void setupViews() {
        tvDate = (TextView) findViewById(R.id.tvDate);
        tvTime = (TextView) findViewById(R.id.tvTime);
        tvEventName = (TextView) findViewById(R.id.tvEventName);
        tvCreator = (TextView) findViewById(R.id.tvCreator);
        rlAccept = (RelativeLayout) findViewById(R.id.rlAccept);
        lvContacts = (ListView) findViewById(R.id.lvContacts);

        Log.d("DEBUG-FRIENDS", invitation.getUsersList().size() + "");

        FriendListAdapter adapter = new FriendListAdapter(this, invitation, getApplication());
        adapter.addAll(invitation.getUserListExcluding(phonenumber));
        lvContacts.setAdapter(adapter);

        if (invitation.isAccepted(phonenumber)) {
            rlAccept.setBackgroundColor(getResources().getColor(R.color.primary_dark));
        } else {
            rlAccept.setBackgroundColor(Color.parseColor("#cccccc"));
        }

        rlAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ParseQuery<ParseObject> query = ParseQuery.getQuery("Invitation");

                final List<String> activePhoneNumberList = new ArrayList<>();
                activePhoneNumberList.add(phonenumber);

                if (invitation.isAccepted(phonenumber)) {
                    invitation.removeAcceptedUser(phonenumber);
                    query.getInBackground(invitation.getInvitationId(), new GetCallback<ParseObject>() {
                        @Override
                        public void done(ParseObject parseObject, com.parse.ParseException e) {
                            if (parseObject != null) {
                                parseObject.removeAll("acceptedUsers", activePhoneNumberList);
                                parseObject.saveInBackground();
                            }
                        }
                    });
                    rlAccept.setBackgroundColor(Color.parseColor("#cccccc"));
                    sendAcceptInvitationPushNotification(false);

                } else {
                    invitation.addAcceptedUser(phonenumber);
                    query.getInBackground(invitation.getInvitationId(), new GetCallback<ParseObject>() {
                        @Override
                        public void done(ParseObject parseObject, com.parse.ParseException e) {
                            if (parseObject != null) {
                                Log.d(TAG, "activePhoneNumberList: " + activePhoneNumberList.size());
                                parseObject.addAll("acceptedUsers", activePhoneNumberList);
                                parseObject.saveInBackground();
                            }
                        }
                    });
                    rlAccept.setBackgroundColor(getResources().getColor(R.color.primary_dark));
                    sendAcceptInvitationPushNotification(true);
                }

            }
        });
    }

    private void sendAcceptInvitationPushNotification(Boolean acceptedInvitation) {
        ParseQuery pushQuery = ParseInstallation.getQuery();
        ParseInstallation installation = ParseInstallation.getCurrentInstallation();
        String phonenumber = (String) installation.get("phonenumber");

        List<String> recipients = invitation.getAllPhoneNumbers();
        recipients.remove(phonenumber);
        pushQuery.whereContainedIn("phonenumber", recipients);
        ParsePush push2 = new ParsePush();

        JSONObject data = new JSONObject();
        try {
            data.put("title", "Foodcoma");

            if (acceptedInvitation)
                data.put("alert", phonenumber + " has accepted invitation");
            else
                data.put("alert", phonenumber + " has canceled invitation");

            data.put("data", invitation.getJsonObject());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        push2.setQuery(pushQuery); // Set our Installation query
        push2.setData(data);
        push2.sendInBackground();
    }

    protected void loadMap(GoogleMap googleMap) {
        map = googleMap;
        if (map != null) {
            // Map is ready
            Toast.makeText(this, "Map Fragment was loaded properly!", Toast.LENGTH_SHORT).show();
            map.setMyLocationEnabled(false);
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

        MenuItem deleteInvitationMenuItem = menu.findItem(R.id.menuitem_delete_invitation);

        if (invitation.getOwner().getPhoneNumber().equals(phonenumber)) {
            deleteInvitationMenuItem.setVisible(true);
        } else {
            deleteInvitationMenuItem.setVisible(false);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if(id == android.R.id.home) {
            finish();
            overridePendingTransition(R.anim.left_in, R.anim.right_out);
            return true;
        } else if (id == R.id.menuitem_delete_invitation) {
            ParseQuery<ParseObject> query = ParseQuery.getQuery("Invitation");
            query.getInBackground(invitation.getInvitationId(), new GetCallback<ParseObject>() {
                @Override
                public void done(ParseObject parseObject, com.parse.ParseException e) {
                    if (parseObject != null)
                        try {
                            parseObject.delete();
                            Intent intent = new Intent(ViewActivity.this, MainActivity.class);
                            startActivity(intent);
                        } catch (com.parse.ParseException e1) {
                            e1.printStackTrace();
                        }
                }
            });

        }

        return super.onOptionsItemSelected(item);
    }


    private Runnable runnable = new Runnable() {
        @Override
        public void run() {

            map.clear();
            map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(final Marker marker) {
                    Log.d("DEBUG", "marker click");
                    String distanceUrl = "https://maps.googleapis.com/maps/api/distancematrix/json?units=imperial&origins=" + marker.getPosition().latitude + "," + marker.getPosition().longitude + "&destinations=" + invitation.getRestaurant().getRestaurantLocation().getLatitude() + "," + invitation.getRestaurant().getRestaurantLocation().getLongitude();
                    AsyncHttpClient client = new AsyncHttpClient();

                    client.get(distanceUrl, null, new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                            try {
                                JSONArray elements = response.getJSONArray("rows").getJSONObject(0).getJSONArray("elements");
                                Log.d("DEBUG-VIEW-ACTIVITY", elements.getJSONObject(0).getJSONObject("distance").getString("text"));
                                marker.setSnippet(elements.getJSONObject(0).getJSONObject("distance").getString("text"));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                    });

                    return false;
                }
            });


            Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.mipmap.restaurant_icon);
            bmp = Bitmap.createScaledBitmap(bmp, 50, 50, false);
            BitmapDescriptor RestaurantMarker =
                    BitmapDescriptorFactory.fromBitmap(bmp);

            latLngBoundsBuilder = new LatLngBounds.Builder();

            LatLng resLoc = new LatLng(invitation.getRestaurant().getRestaurantLocation().getLatitude(), invitation.getRestaurant().getRestaurantLocation().getLongitude());
            latLngBoundsBuilder.include(resLoc);


            Marker marker = map.addMarker(new MarkerOptions().position(resLoc).title(invitation.getRestaurant().getName()).icon(RestaurantMarker).flat(true));
            marker.showInfoWindow();

            /* fetch object of all the users from parse for this Invitation to get their location */
            ParseQuery<ParseUser> query = ParseUser.getQuery();
            query.whereContainedIn("phonenumber", invitation.getAllPhoneNumbers());
            query.findInBackground(new FindCallback<ParseUser>() {
                @Override
                public void done(List<ParseUser> parseUsers, com.parse.ParseException e) {
                    BitmapDescriptor carMarker =
                            BitmapDescriptorFactory.fromResource(R.mipmap.car);
                    String userphonenumber;
                    ArrayList<String> AllphoneNumbers = new ArrayList<String>();
                    ArrayList<String> phoneNumbersReached = new ArrayList<String>();
                    if (e == null) {
                        Log.d("DEBUG", "Retrieved " + parseUsers.size() + " phonenumber");

                        latLngBoundsBuilder.include(new LatLng(((FoodcomaApplication) getApplication()).getMylatitude(), ((FoodcomaApplication) getApplication()).getMylongitude()));
                        for (int i = 0; i < parseUsers.size(); i++) {
                            userphonenumber = parseUsers.get(i).getString("phonenumber");
                            AllphoneNumbers.add(userphonenumber);
                            ParseGeoPoint uloc = parseUsers.get(i).getParseGeoPoint("userlocation");

                            LatLng userloc = new LatLng(uloc.getLatitude(), uloc.getLongitude());
                            Marker marker = map.addMarker(new MarkerOptions().position(userloc).title(userphonenumber).icon(carMarker).flat(true));
                            latLngBoundsBuilder.include(userloc);
                            Location ul = new Location("");
                            ul.setLatitude(uloc.getLatitude());
                            ul.setLongitude(uloc.getLongitude());
                            float distanceInMeters = invitation.getRestaurant().getRestaurantLocation().distanceTo(ul);
                            Log.d("DEBUG-VIEW_ACTIVITY", distanceInMeters + "distance, res location" + invitation.getRestaurant().getRestaurantLocation() + "user location" + ul);
                            if (distanceInMeters < RADIUS) {
                                phoneNumbersReached.add(userphonenumber);
                                Log.d("DEBUG-VIEW-ACTIVITY", userphonenumber + " reached");
                            }
                        }

                        map.animateCamera(CameraUpdateFactory.newLatLngBounds(latLngBoundsBuilder.build(), 80));
                    } else {
                        Log.d("score", "Error: " + e.getMessage());
                    }

                    if (phoneNumbersReached.size() > 0) {
                        ParseQuery pushQuery = ParseInstallation.getQuery();
                        // Send Notification to all

                        pushQuery.whereContainedIn("phonenumber", AllphoneNumbers);

                        ParsePush push2 = new ParsePush();

                        JSONObject data = new JSONObject();

                        try {
                            data.put("title", "Foodcoma");
                            data.put("alert", phoneNumbersReached + " have Reached");
                            data.put("data", invitation.getJsonObject());
                        } catch (JSONException e1) {
                            e1.printStackTrace();
                        }

                        push2.setQuery(pushQuery); // Set our Installation query
                        push2.setData(data);
                        push2.sendInBackground();
                    }
                }
            });
        }
    };


    @Override
    protected void onStart() {
        super.onStart();
        connectClient();
        handler.postDelayed(runnable, 1000);
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
        switch (requestCode) {
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


        LatLng myloc = new LatLng(location.latitude, location.longitude);
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

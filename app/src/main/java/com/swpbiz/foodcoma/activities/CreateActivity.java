package com.swpbiz.foodcoma.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.fourmob.datetimepicker.date.DatePickerDialog;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.SaveCallback;
import com.sleepbot.datetimepicker.time.RadialPickerLayout;
import com.sleepbot.datetimepicker.time.TimePickerDialog;
import com.swpbiz.foodcoma.FoodcomaApplication;
import com.swpbiz.foodcoma.R;
import com.swpbiz.foodcoma.adapters.ContactsArrayAdapter;
import com.swpbiz.foodcoma.models.Invitation;
import com.swpbiz.foodcoma.models.Restaurant;
import com.swpbiz.foodcoma.models.User;
import com.swpbiz.foodcoma.utils.MyDateTimeUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;


public class CreateActivity extends ActionBarActivity implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {

    public static final String DATEPICKER_TAG = "datepicker";
    public static final String TIMEPICKER_TAG = "timepicker";
    private TextView tvCreateDate;
    private TextView tvCreateTime;
    private TextView tvPlaceName;
    private String dateValue;
    private String timeValue;
    private Calendar calendar;
    private String phonenumber;

    ContactsArrayAdapter contactsArrayAdapter;
    Restaurant restaurant;
    final int REQUEST_PLACE_PICKER = 1;
    static final int SET_RESTAURANT = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create);

        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // init values
        calendar = Calendar.getInstance();
        dateValue = MyDateTimeUtil.convertToFullDateString(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        timeValue = MyDateTimeUtil.convertToTimeString(calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE));

        setupViews();

        setDateTimePickerListener();

        if (savedInstanceState != null) {
            createDateTimeDialogs();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_create, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_send) {
            final Invitation invitation = createInvitation();
//            Log.d("DEBUG", "location " + invitation.getMapUrl());
            Log.d("DEBUG", "datetime " + invitation.getTimeOfEvent());


            final ParseQuery pushQuery = ParseInstallation.getQuery();
            ParseInstallation installation = ParseInstallation.getCurrentInstallation();
            phonenumber = (String) installation.get("phonenumber");

            final ParseObject parseinvitation = new ParseObject("Invitation");
            parseinvitation.put("owner", invitation.getOwner().getPhoneNumber());
            parseinvitation.put("timeofevent", invitation.getTimeOfEvent());
            if (parseinvitation.get("invitationid") == null
                    || parseinvitation.get("invitationid").toString().isEmpty()) {
                parseinvitation.put("invitationid", 1);
            }
            parseinvitation.put("placeName", invitation.getRestaurant().getName());

            ParseGeoPoint placeLatLng = new ParseGeoPoint(invitation.getRestaurant().getRestaurantLocation().getLatitude(), invitation.getRestaurant().getRestaurantLocation().getLongitude());
            parseinvitation.put("placeLatLng", placeLatLng);

            ArrayList<String> userPhonenumberList = new ArrayList<String>(invitation.getUsers().keySet());
            parseinvitation.put("users", userPhonenumberList);

            parseinvitation.put("acceptedUsers", new ArrayList<String>(invitation.getAcceptedUsers()));

            parseinvitation.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {

                    invitation.setInvitationId(parseinvitation.getObjectId());

                    JSONObject data = new JSONObject();
                    try {
                        data.put("title", "Foodcoma");
                        data.put("alert", "New Invitation");
                        data.put("data", invitation.getJsonObject());
                    } catch (JSONException ex) {
                        ex.printStackTrace();
                    }

                    ArrayList<String> phoneNumbers = new ArrayList<String>(invitation.getUsers().keySet());
                    pushQuery.whereContainedIn("phonenumber", phoneNumbers);
                    ParsePush push2 = new ParsePush();
                    push2.setQuery(pushQuery); // Set our Installation query
                    push2.setData(data);
                    push2.sendInBackground();


                }
            });
           Intent i = new Intent(CreateActivity.this, ViewActivity.class);

            i.putExtra("invitation", invitation);
            startActivity(i);
            return true;
        } else if(id == android.R.id.home) {
            finish();
            overridePendingTransition(R.anim.left_in, R.anim.right_out);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupViews() {
        tvPlaceName = (TextView) findViewById(R.id.tvPlaceName);
        tvCreateDate = (TextView) findViewById(R.id.tvCreateDate);
        tvCreateTime = (TextView) findViewById(R.id.tvCreateTime);

        tvCreateDate.setText(MyDateTimeUtil.convertToShortDateString(calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)));
        tvCreateTime.setText(timeValue);

        ListView lvContacts = (ListView) findViewById(R.id.lvContacts);
        contactsArrayAdapter = new ContactsArrayAdapter(this);
        contactsArrayAdapter.addAll(((FoodcomaApplication) getApplication()).getContacts());
        lvContacts.setAdapter(contactsArrayAdapter);
    }

    private Invitation createInvitation() {
        String placeName = tvPlaceName.getText().toString();
        String date = tvCreateDate.getText().toString();
        String time = tvCreateTime.getText().toString();

        Invitation invitation = new Invitation();

        SharedPreferences sharedPref = getSharedPreferences("foodcoma", Context.MODE_PRIVATE);
        phonenumber = sharedPref.getString(getString(R.string.my_phone_number), null);

        User owner = new User();
        owner.setPhoneNumber(phonenumber);
        owner.setName(phonenumber);
        invitation.setOwner(owner);
        invitation.setTimeOfEvent(MyDateTimeUtil.getEpochTime(dateValue, timeValue)); // set date/time later
        invitation.setUsers(getFriendsSelected());

        invitation.getAcceptedUsers().add(phonenumber);

        if (restaurant == null) {
            restaurant = new Restaurant();
            restaurant.setName(placeName);
        }
        invitation.setRestaurant(restaurant);

//        invitation.save();

        return invitation;
    }

    // List of people who will be invited <phoneNumber, User>
    private HashMap<String, User> getFriendsSelected() {
        return (HashMap) contactsArrayAdapter.getNamesSelected();
    }

    private void setDateTimePickerListener() {

        final DatePickerDialog datePickerDialog = DatePickerDialog.newInstance(this, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), false);
        final TimePickerDialog timePickerDialog = TimePickerDialog.newInstance(this, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false, false);

        tvCreateDate.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                datePickerDialog.setYearRange(2015, 2028);
                datePickerDialog.setCloseOnSingleTapDay(true);
                datePickerDialog.show(getSupportFragmentManager(), DATEPICKER_TAG);
            }
        });

        tvCreateTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timePickerDialog.setCloseOnSingleTapMinute(true);
                timePickerDialog.show(getSupportFragmentManager(), TIMEPICKER_TAG);
            }
        });
    }

    private void createDateTimeDialogs() {
        DatePickerDialog dpd = (DatePickerDialog) getSupportFragmentManager().findFragmentByTag(DATEPICKER_TAG);
        if (dpd != null) {
            dpd.setOnDateSetListener(this);
        }

        TimePickerDialog tpd = (TimePickerDialog) getSupportFragmentManager().findFragmentByTag(TIMEPICKER_TAG);
        if (tpd != null) {
            tpd.setOnTimeSetListener(this);
        }
    }

    @Override
    public void onDateSet(DatePickerDialog datePickerDialog, int year, int month, int day) {

        String result = MyDateTimeUtil.convertToShortDateString(month, day);
        dateValue = MyDateTimeUtil.convertToFullDateString(year, month, day);
        MyDateTimeUtil.getEpochTime(dateValue, timeValue);

        tvCreateDate.setText(result);

    }

    @Override
    public void onTimeSet(RadialPickerLayout radialPickerLayout, int hourOfDay, int minute) {

        String result = MyDateTimeUtil.convertToTimeString(hourOfDay, minute);
        timeValue = result;
        MyDateTimeUtil.getEpochTime(dateValue, timeValue);

        tvCreateTime.setText(result);

    }

    public void findRestaurants(View view) {
        Intent i = new Intent(CreateActivity.this, RestaurantActivity.class);
        //  startActivity(i);
        startActivityForResult(i, SET_RESTAURANT);
    }


    public void gotoGoogleMaps(View view) {
        FoodcomaApplication mapp = (FoodcomaApplication) getApplicationContext();

        // Share Via google maps
        Uri uri = Uri.parse("geo:" + mapp.getMylatitude() + "," + mapp.getMylongitude() + "?q=restaurant");
        //Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
        startActivity(intent);

        // Construct an intent for the place picker
//         try {
//             PlacePicker.IntentBuilder intentBuilder =
//                     new PlacePicker.IntentBuilder();
//             Intent intent = intentBuilder.build(this);
//             Start the intent by requesting a result,
//             identified by a request code.
//             startActivityForResult(intent, REQUEST_PLACE_PICKER);
//
//         } catch (GooglePlayServicesRepairableException e) {
//             ...
//         } catch (GooglePlayServicesNotAvailableException e) {
//             ...
//         }
    }

    @Override
    protected void onActivityResult(int requestCode,
                                    int resultCode, Intent data) {

        if (requestCode == REQUEST_PLACE_PICKER
                && resultCode == Activity.RESULT_OK) {

            // The user has selected a place. Extract the name and address.
            final Place place = PlacePicker.getPlace(data, this);

            final CharSequence name = place.getName();
            final CharSequence address = place.getAddress();
            String attributions = PlacePicker.getAttributions(data);
            if (attributions == null) {
                attributions = "";
            }
            Log.d("DEBUG", "name: " + name.toString() + "address: " + address.toString());

        } else if (requestCode == SET_RESTAURANT && resultCode == Activity.RESULT_OK) {
            restaurant = data.getParcelableExtra("restaurant");
            tvPlaceName.setText(restaurant.getName());
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}

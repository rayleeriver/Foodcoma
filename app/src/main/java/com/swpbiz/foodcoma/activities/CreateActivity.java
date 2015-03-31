package com.swpbiz.foodcoma.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Parcelable;
import android.provider.ContactsContract;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.fourmob.datetimepicker.date.DatePickerDialog;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLngBounds;
import com.parse.ParseObject;
import com.sleepbot.datetimepicker.time.RadialPickerLayout;
import com.sleepbot.datetimepicker.time.TimePickerDialog;
import com.swpbiz.foodcoma.FoodcomaApplication;
import com.swpbiz.foodcoma.R;
import com.swpbiz.foodcoma.adapters.MyCursorAdapter;
import com.swpbiz.foodcoma.models.Invitation;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;

import com.parse.ParseInstallation;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.swpbiz.foodcoma.models.User;
import com.swpbiz.foodcoma.utils.MyDateTimeUtil;

import org.json.JSONException;
import org.json.JSONObject;


import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;


public class CreateActivity extends ActionBarActivity implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {

    public static final String DATEPICKER_TAG = "datepicker";
    public static final String TIMEPICKER_TAG = "timepicker";
    private TextView tvCreateDate;
    private TextView tvCreateTime;
    private TextView tvLocation;
    private String dateValue;
    private String timeValue;
    private Calendar calendar;
    private String phonenumber;
    MyCursorAdapter adapter;
    final int REQUEST_PLACE_PICKER = 1;
    public static final int CONTACT_LOADER_ID = 78; // From docs: A unique identifier for this loader.

    private LoaderManager.LoaderCallbacks<Cursor> contactsLoader =
            new LoaderManager.LoaderCallbacks<Cursor>() {
                @Override
                public Loader<Cursor> onCreateLoader(int id, Bundle args) {
                    // Define the columns to retrieve
                    String[] projectionFields =  new String[] { ContactsContract.Contacts._ID,
                            ContactsContract.Contacts.DISPLAY_NAME,
                            ContactsContract.Contacts.PHOTO_URI,
                            ContactsContract.Contacts.HAS_PHONE_NUMBER};
                    // Construct the loader
                    CursorLoader cursorLoader = new CursorLoader(CreateActivity.this,
                            ContactsContract.Contacts.CONTENT_URI, // URI
                            projectionFields,  // projection fields
                            ContactsContract.Contacts.HAS_PHONE_NUMBER + " != '0'", // the selection criteria
                            null, // the selection args
                            ContactsContract.Contacts.DISPLAY_NAME + " ASC" // the sort order
                    );
                    // Return the loader for use
                    return cursorLoader;
                }

                // When the system finishes retrieving the Cursor through the CursorLoader,
                // a call to the onLoadFinished() method takes place.
                @Override
                public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
                    // The swapCursor() method assigns the new Cursor to the adapter
                    adapter.swapCursor(cursor);
                }

                // This method is triggered when the loader is being reset
                // and the loader data is no longer available. Called if the data
                // in the provider changes and the Cursor becomes stale.
                @Override
                public void onLoaderReset(Loader<Cursor> loader) {
                    // Clear the Cursor we were using with another call to the swapCursor()
                    adapter.swapCursor(null);
                }
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create);
        setupCursorAdapter();

        // Initialize the loader with a special ID and the defined callbacks from above
        getSupportLoaderManager().initLoader(CONTACT_LOADER_ID,
                new Bundle(), contactsLoader);

        // init values
        calendar = Calendar.getInstance();
        dateValue = MyDateTimeUtil.convertToFullDateString(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        timeValue = MyDateTimeUtil.convertToTimeString(calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE));

        setupViews();

        setDateTimePickerListener();

        if(savedInstanceState != null){
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
            Invitation invitation = createInvitation();
            Log.d("DEBUG", "location " + invitation.getMapUrl());
            Log.d("DEBUG", "datetime " + invitation.getTimeOfEvent());


            ParseQuery pushQuery = ParseInstallation.getQuery();
            ParseInstallation installation = ParseInstallation.getCurrentInstallation();
            phonenumber = (String)installation.get("phonenumber");

            ParseObject parseinvitation = new ParseObject("Invitation");
            parseinvitation.put("timeofevent", invitation.getTimeOfEvent());
            if (parseinvitation.get("invitationid") == null || parseinvitation.get("invitationid").toString().isEmpty()) {
                parseinvitation.put("invitationid", 1);
            }
            parseinvitation.put("mapurl", invitation.getMapUrl());
            ArrayList<String> userPhonenumberList = new ArrayList<String>(invitation.getUsers().keySet());
            parseinvitation.put("users",userPhonenumberList);
            parseinvitation.saveInBackground();
            invitation.setInvitationId(parseinvitation.getString("invitationid"));

            JSONObject data =  new JSONObject();
            try {
                data.put("title","Foodcoma");
                data.put("alert","New Invitation");
                data.put("data", invitation.getJsonObject());
            } catch (JSONException e) {
                e.printStackTrace();
            }

            ArrayList<String> phoneNumbers = new ArrayList<String>(invitation.getUsers().keySet());
            pushQuery.whereContainedIn("phonenumber", phoneNumbers);
            ParsePush push2 = new ParsePush();
            push2.setQuery(pushQuery); // Set our Installation query
            push2.setData(data);
            push2.sendInBackground();

            Intent i = new Intent(CreateActivity.this, ViewActivity.class);

            // i.putExtra("data", invitation.getJsonObject());

            i.putExtra("activityname","CreateActivity");
            i.putExtra("invitation",(Parcelable)invitation);

            startActivity(i);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupViews() {
        tvLocation = (TextView) findViewById(R.id.tvLocation);
        tvCreateDate = (TextView) findViewById(R.id.tvCreateDate);
        tvCreateTime = (TextView) findViewById(R.id.tvCreateTime);

        tvCreateDate.setText(MyDateTimeUtil.convertToShortDateString(calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)));
        tvCreateTime.setText(timeValue);
    }

    // Create simple cursor adapter to connect the cursor dataset we load with a ListView
    private void setupCursorAdapter() {
        adapter = new MyCursorAdapter(this, null);

        ListView lvContacts = (ListView) findViewById(R.id.lvContacts);
        lvContacts.setAdapter(adapter);

    }

    private Invitation createInvitation() {
        String location = tvLocation.getText().toString();
        String date = tvCreateDate.getText().toString();
        String time = tvCreateTime.getText().toString();

        Invitation invitation = new Invitation();

        SharedPreferences sharedPref = getSharedPreferences("foodcoma", Context.MODE_PRIVATE);
        phonenumber = sharedPref.getString(getString(R.string.my_phone_number), null);

        User owner = new User();
        owner.setPhoneNumber(phonenumber);
        owner.setName(phonenumber);
        invitation.setOwner(owner);
        invitation.setMapUrl(location);
        invitation.setTimeOfEvent(MyDateTimeUtil.getEpochTime(dateValue, timeValue)); // set date/time later
        invitation.setUsers(getFriendsSelected());
//        invitation.save();

        return invitation;
    }

    // List of people who will be invited <phoneNumber, User>
    private HashMap<String, User> getFriendsSelected() {
        return (HashMap) adapter.getNamesSelected();
    }

    private void setDateTimePickerListener() {

        final DatePickerDialog datePickerDialog = DatePickerDialog.newInstance(this, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), false);
        final TimePickerDialog timePickerDialog = TimePickerDialog.newInstance(this, calendar.get(Calendar.HOUR_OF_DAY) ,calendar.get(Calendar.MINUTE), false, false);

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
        startActivity(i);
    }

     public void gotoGoogleMaps(View view) {
         FoodcomaApplication mapp = (FoodcomaApplication)getApplicationContext();

         // Share Via google maps
         Uri uri = Uri.parse("geo:"+mapp.getMylatitude()+","+mapp.getMylongitude()+"?q=restaurant");
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

        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}

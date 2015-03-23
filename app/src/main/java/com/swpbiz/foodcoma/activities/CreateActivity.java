package com.swpbiz.foodcoma.activities;

import android.content.Intent;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.fourmob.datetimepicker.date.DatePickerDialog;
import com.sleepbot.datetimepicker.time.RadialPickerLayout;
import com.sleepbot.datetimepicker.time.TimePickerDialog;
import com.swpbiz.foodcoma.R;
import com.swpbiz.foodcoma.adapters.MyCursorAdapter;
import com.swpbiz.foodcoma.models.Invitation;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import com.parse.ParseInstallation;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.swpbiz.foodcoma.models.User;

import org.json.JSONException;
import org.json.JSONObject;


import java.util.HashMap;
import java.util.Set;


public class CreateActivity extends ActionBarActivity implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {

    public static final String DATEPICKER_TAG = "datepicker";
    public static final String TIMEPICKER_TAG = "timepicker";
    public static final String MONTH_NAME[] = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
    private TextView tvCreateDate;
    private TextView tvCreateTime;
    private TextView tvLocation;
    private String dateValue;
    private String timeValue;
    private Calendar calendar;
    private String phonenumber;
    MyCursorAdapter adapter;
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
                            "HAS_PHONE_NUMBER != '0'", // the selection criteria
                            null, // the selection args
                            null // the sort order
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
        dateValue = convertToFullDateString(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        timeValue = convertToTimeString(calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE));

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
            // pushQuery.whereEqualTo("phonenumber", "16504850366");
            pushQuery.whereEqualTo("phonenumber", phonenumber); // Receiver list (Currently set it to the owner for testing purpose)
            ParsePush push2 = new ParsePush();

            JSONObject data =  new JSONObject();
            try {
                data.put("title","Foodcoma");
                data.put("alert","New Invitation");
                data.put("data", invitation.getJsonObject());
            } catch (JSONException e) {
                e.printStackTrace();
            }

            push2.setQuery(pushQuery); // Set our Installation query
            push2.setData(data);
            push2.sendInBackground();

            Intent i = new Intent(CreateActivity.this, ViewActivity.class);
            startActivity(i);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupViews() {
        tvLocation = (TextView) findViewById(R.id.tvLocation);
        tvCreateDate = (TextView) findViewById(R.id.tvCreateDate);
        tvCreateTime = (TextView) findViewById(R.id.tvCreateTime);

        tvCreateDate.setText(convertToShortDateString(calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)));
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

        Set<User> namesSelected = adapter.getNamesSelected();
        User friends[] = namesSelected.toArray(new User[namesSelected.size()]);
        for(int i = 0; i < friends.length; i++) {
            Log.d("DEBUG-FRIENDS", friends[i].getName() + " " + friends[i].getPhoneNumber());
        }

        Invitation invitation = new Invitation();
        User owner = new User();
        owner.setPhoneNumber(phonenumber);
        owner.setName(phonenumber);
        invitation.setOwner(owner);
        invitation.setMapUrl(location);
        invitation.setTimeOfEvent(getEpochTime()); // set date/time later

        // List of people who will be invited
        HashMap<String, User> invitedPeople = new HashMap<>();

        // Create a dummy user
        User dummyUser = new User();
        dummyUser.setName("A");
        dummyUser.setPhoneNumber(phonenumber); // Set it to YOUR phone number for testing purpose
//        dummyUser.save();

        invitedPeople.put(dummyUser.getPhoneNumber(), dummyUser);

        invitation.setUsers(invitedPeople);
//        invitation.save();

        return invitation;
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

        String result = convertToShortDateString(month, day);
        dateValue = convertToFullDateString(year, month, day);
        getEpochTime();

        tvCreateDate.setText(result);

    }

    @Override
    public void onTimeSet(RadialPickerLayout radialPickerLayout, int hourOfDay, int minute) {

        String result = convertToTimeString(hourOfDay, minute);
        timeValue = result;
        getEpochTime();

        tvCreateTime.setText(result);

    }

    // Convert hourOfDay(24-hour) and minute to a format like >> 5:03PM
    private String convertToTimeString(int hourOfDay, int minute) {

        // AM or PM
        String meridian = (hourOfDay >= 12) ? "PM" : "AM";

        // Convert to 12-hour
        int hour = hourOfDay;
        if(hourOfDay > 12){
            hour = hourOfDay - 12;
        }
        else if(hourOfDay == 0){
            hour = 12;
        }

        // Make it a format like >> 5:03PM
        String result = hour + ":" + String.format("%02d", minute) + meridian;

        return result;
    }

    private String convertToFullDateString(int year, int month, int date) {
        return year + "-" + String.format("%02d", month + 1) + "-" + String.format("%02d", date); // 2015-03-12
    }

    private String convertToShortDateString(int month, int date) {
        return MONTH_NAME[month] + " " + date; // MAR 5
    }

    private long getEpochTime() {
        // Format: 2015-03-19 9:03PM
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mmaa");
        Date date = null;
        try {
            date = df.parse(dateValue + " " + timeValue);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        long epoch = date.getTime();
        Log.d("DEBUG-EPOCH", epoch + "");
        return epoch;

    }
}

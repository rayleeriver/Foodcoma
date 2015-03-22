package com.swpbiz.foodcoma;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.fourmob.datetimepicker.date.DatePickerDialog;
import com.sleepbot.datetimepicker.time.RadialPickerLayout;
import com.sleepbot.datetimepicker.time.TimePickerDialog;
import com.swpbiz.foodcoma.models.Invitation;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import com.parse.ParseInstallation;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.swpbiz.foodcoma.models.Invitation;

import org.json.JSONException;
import org.json.JSONObject;


import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;


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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create);

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
            String phonenumber = (String)installation.get("phonenumber");
            pushQuery.whereEqualTo("phonenumber", phonenumber);
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

    private Invitation createInvitation() {
        String location = tvLocation.getText().toString();
        String date = tvCreateDate.getText().toString();
        String time = tvCreateTime.getText().toString();
        // Get friend list here

        Invitation invitation = new Invitation();
        invitation.setMapUrl(location);
        invitation.setTimeOfEvent(0); // set date/time later

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

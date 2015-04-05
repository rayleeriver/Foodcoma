package com.swpbiz.foodcoma.utils;

import android.util.Log;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by vee on 3/28/15.
 */
public class MyDateTimeUtil {

    public static final String MONTH_NAME[] = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
    static DateFormat hourMinuteFormat = new SimpleDateFormat("hh:mmaa");
    static DateFormat monthDateFormat = new SimpleDateFormat("MMM d");

    public static String getTimeFromEpoch(long time) {
        Date date = new Date(time);
        String formatted = hourMinuteFormat.format(date);
        return formatted;
    }

    public static String getDateFromEpoch(long time) {
        Date date = new Date(time);
        String formatted = monthDateFormat.format(date);
        return formatted;
    }

    // Convert hourOfDay(24-hour) and minute to a format >> 5:03PM
    public static String convertToTimeString(int hourOfDay, int minute) {

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

    // Get year, month, and date and return 2015-03-12
    public static String convertToFullDateString(int year, int month, int date) {
        return year + "-" + String.format("%02d", month + 1) + "-" + String.format("%02d", date); // 2015-03-12
    }

    // Get year, month, and date and return 2015-03-12
    public static String convertToShortDateString(int month, int date) {
        return MONTH_NAME[month] + " " + date; // MAR 5
    }

    // Get 2015-03-19 and 9:03PM and return epoch time
    public static long getEpochTime(String dateValue, String timeValue) {
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

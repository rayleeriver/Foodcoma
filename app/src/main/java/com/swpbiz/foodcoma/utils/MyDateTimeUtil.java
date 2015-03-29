package com.swpbiz.foodcoma.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by vee on 3/28/15.
 */
public class MyDateTimeUtil {

    public static String getTimeFromEpoch(long time) {
        Date date = new Date(time);
        DateFormat format = new SimpleDateFormat("HH:mmaa");
        // format.setTimeZone(TimeZone.getTimeZone("Australia/Sydney"));
        String formatted = format.format(date);

        return formatted;
    }

    public static String getDateFromEpoch(long time) {
        Date date = new Date(time);
        DateFormat format = new SimpleDateFormat("MMM d");
        // format.setTimeZone(TimeZone.getTimeZone("Australia/Sydney"));
        String formatted = format.format(date);

        return formatted;
    }
}

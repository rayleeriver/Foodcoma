package com.swpbiz.foodcoma.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by vee on 3/28/15.
 */
public class MyDateTimeUtil {

    static DateFormat hourMinuteFormat = new SimpleDateFormat("HH:mmaa");
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
}

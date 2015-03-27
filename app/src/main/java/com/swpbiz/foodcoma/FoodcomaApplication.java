package com.swpbiz.foodcoma;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseACL;
import com.parse.ParseCrashReporting;
import com.parse.ParseInstallation;
import com.parse.ParseUser;

/**
 * Created by abgandhi on 3/17/15.
 */
public class FoodcomaApplication extends Application {
    private String APP_ID = "pF8mnLsZyo0WHQ87JiTCRk6BKHJw5S5XIixKSWRf";
    private String CLIENT_KEY = "A22nT1dRN0DFfoOlPCu8Sj7THo3QKCTArixwFa5I";
    private String MY_PHONE_NUMBER;

    @Override
    public void onCreate() {
        super.onCreate();

        // Initialize Crash Reporting.
        ParseCrashReporting.enable(this);

        // Enable Local Datastore.
        Parse.enableLocalDatastore(this);

        // Add your initialization code here
        Parse.initialize(this, APP_ID, CLIENT_KEY);

        ParseUser.enableAutomaticUser();
        ParseUser.getCurrentUser().saveInBackground();
        ParseACL defaultACL = new ParseACL();
        // Optionally enable public read access.
        defaultACL.setPublicReadAccess(true);
        ParseACL.setDefaultACL(defaultACL, true);

    }

    public String getPhoneNumber() {
        return MY_PHONE_NUMBER;
    }

    public void setPhoneNumber(String MY_PHONE_NUMBER) {
        this.MY_PHONE_NUMBER = MY_PHONE_NUMBER;
    }
}

package com.swpbiz.foodcoma;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;

import com.parse.Parse;
import com.parse.ParseACL;
import com.parse.ParseCrashReporting;
import com.swpbiz.foodcoma.models.User;
import com.swpbiz.foodcoma.services.ContactsLoaderIntentService;
import com.swpbiz.foodcoma.services.ContactsLoaderIntentServiceBroadcastReceiver;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by abgandhi on 3/17/15.
 */
public class FoodcomaApplication extends Application {
    private String APP_ID = "pF8mnLsZyo0WHQ87JiTCRk6BKHJw5S5XIixKSWRf";
    private String CLIENT_KEY = "A22nT1dRN0DFfoOlPCu8Sj7THo3QKCTArixwFa5I";
    private String MY_PHONE_NUMBER;
    public Double mylongitude;
    public Double mylatitude;

    List<User> contacts = new ArrayList<>();

    @Override
    public void onCreate() {
        super.onCreate();

        // Initialize Crash Reporting.
        ParseCrashReporting.enable(this);

        // Enable Local Datastore.
        Parse.enableLocalDatastore(this);

        // Add your initialization code here
        Parse.initialize(this, APP_ID, CLIENT_KEY);

        //    ParseUser.enableAutomaticUser();
        //     ParseUser.getCurrentUser().saveInBackground();
        ParseACL defaultACL = new ParseACL();
        // Optionally enable public read access.
        defaultACL.setPublicReadAccess(true);
        defaultACL.setPublicWriteAccess(true);
        ParseACL.setDefaultACL(defaultACL, true);

        mylatitude = new Double(0);
        mylongitude = new Double(0);

        Intent intent = new Intent(getApplicationContext(), ContactsLoaderIntentService.class);
        startService(intent);

        BroadcastReceiver contactsLoaderIntentServiceReceiver = new ContactsLoaderIntentServiceBroadcastReceiver(this);
        IntentFilter filter = new IntentFilter(ContactsLoaderIntentService.ACTION);
        LocalBroadcastManager.getInstance(this).registerReceiver(contactsLoaderIntentServiceReceiver, filter);

    }

    public String getPhoneNumber() {
        return MY_PHONE_NUMBER;
    }

    public Double getMylongitude() {
        return mylongitude;
    }

    public void setMylongitude(Double mylongitude) {
        this.mylongitude = mylongitude;
    }

    public Double getMylatitude() {
        return mylatitude;
    }

    public void setMylatitude(Double mylatitude) {
        this.mylatitude = mylatitude;
    }

    public void setPhoneNumber(String MY_PHONE_NUMBER) {
        this.MY_PHONE_NUMBER = MY_PHONE_NUMBER;
    }

    public List<User> getContacts() {
        return contacts;
    }

    public User findContactByPhoneNumber(String phoneNumber) {
        if (contacts.size() == 0) {
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        for (User contact : contacts) {
            if (contact.getPhoneNumber().equals(phoneNumber)) {
                return contact;
            }
        }
        return null;
    }

}

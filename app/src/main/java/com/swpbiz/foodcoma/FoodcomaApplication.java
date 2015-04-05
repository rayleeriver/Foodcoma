package com.swpbiz.foodcoma;

import android.app.Application;
import android.content.ContentResolver;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;

import com.parse.Parse;
import com.parse.ParseACL;
import com.parse.ParseCrashReporting;
import com.parse.ParseInstallation;
import com.parse.ParseUser;
import com.swpbiz.foodcoma.activities.CreateActivity;

import java.util.ArrayList;

/**
 * Created by abgandhi on 3/17/15.
 */
public class FoodcomaApplication extends Application {
    private String APP_ID = "pF8mnLsZyo0WHQ87JiTCRk6BKHJw5S5XIixKSWRf";
    private String CLIENT_KEY = "A22nT1dRN0DFfoOlPCu8Sj7THo3QKCTArixwFa5I";
    private String MY_PHONE_NUMBER;
    public Double mylongitude;
    public Double mylatitude;

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
        ParseACL.setDefaultACL(defaultACL, true);

        mylatitude = new Double(0);
        mylongitude = new Double(0);



        ContentResolver cr = getBaseContext().getContentResolver(); //Activity/Application android.content.Context
        Cursor cursor = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
        if(cursor.moveToFirst())
        {
            ArrayList<String> alContacts = new ArrayList<String>();
            do
            {
                String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));

                if(Integer.parseInt(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0)
                {
                    Cursor pCur = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,null,ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = ?",new String[]{ id }, null);
                    while (pCur.moveToNext())
                    {
                        String contactNumber = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        alContacts.add(contactNumber);
                        break;
                    }
                    pCur.close();
                }

            } while (cursor.moveToNext()) ;
        }
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

}

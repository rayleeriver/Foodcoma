package com.swpbiz.foodcoma.services;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;
import android.util.Log;

import com.swpbiz.foodcoma.FoodcomaApplication;
import com.swpbiz.foodcoma.models.User;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ContactsLoaderIntentServiceBroadcastReceiver extends BroadcastReceiver {

    public static final String TAG = ContactsLoaderIntentService.class.getSimpleName();

    Application application;

    public ContactsLoaderIntentServiceBroadcastReceiver(Application application) {
        this.application = application;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "contacts list received from local intent service");

        List<Parcelable> parcelableUsers = intent.getParcelableArrayListExtra("contacts");
        for (Parcelable parcelableUser: parcelableUsers) {
            ((FoodcomaApplication) application).getContacts().add((User) parcelableUser);
        }
        Collections.sort(((FoodcomaApplication) application).getContacts(), new Comparator<User>() {
            @Override
            public int compare(User lhs, User rhs) {
                return lhs.getName().compareTo(rhs.getName());
            }
        });
    }
}

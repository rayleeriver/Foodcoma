package com.swpbiz.foodcoma.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParsePush;
import com.parse.SaveCallback;
import com.swpbiz.foodcoma.R;

import java.util.List;


public class MainActivity extends ActionBarActivity {
    public String phoneNumber;
    static final int SET_NUMBER = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        phoneNumber = getPhoneNumber();
        Toast.makeText(this, "Your phone number: " + phoneNumber, Toast.LENGTH_SHORT).show();

        // Check whether the user has set the number before, if not, call the SetNumberActivity
        if(phoneNumber == null) {
            Intent i = new Intent(MainActivity.this, SetNumberActivity.class);
            startActivityForResult(i, SET_NUMBER);
        } else {
            registerWithParse();
        }

        if (getIntent().getExtras() != null) {
            String[] names = getIntent().getExtras().getStringArray("names");
            Toast.makeText(this, "Names selected: " + TextUtils.join(", ", names), Toast.LENGTH_SHORT).show();
        }
      }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_create) {
            Intent i = new Intent(MainActivity.this, CreateActivity.class);
            startActivity(i);
            return true;
        }

        if (id == R.id.action_contact) {
            Intent i = new Intent(MainActivity.this, ContactsActivity.class);
            startActivity(i);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == SET_NUMBER) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                phoneNumber = getPhoneNumber();
                registerWithParse();
            }
        }
    }

    private void registerWithParse() {

        ParseInstallation installation = ParseInstallation.getCurrentInstallation();

        installation.put("phonenumber", phoneNumber);

        // Save the current Installation to Parse.
        installation.saveInBackground();

        ParsePush.subscribeInBackground("", new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    List<String> subscribedChannels = ParseInstallation.getCurrentInstallation().getList("channels");
                    Log.d("DEBUG", "Channel List: " + subscribedChannels.toString());
                    Log.d("com.parse.push", "successfully subscribed to the broadcast channel.");
                } else {
                    Log.e("com.parse.push", "failed to subscribe for push", e);
                }
            }
        });


    }

    private String getPhoneNumber() {
        SharedPreferences sharedPref = getSharedPreferences("foodcoma", Context.MODE_PRIVATE);
        return sharedPref.getString(getString(R.string.my_phone_number), null);
    }
}
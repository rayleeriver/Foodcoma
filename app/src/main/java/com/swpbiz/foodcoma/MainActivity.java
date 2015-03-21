package com.swpbiz.foodcoma;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.telephony.gsm.SmsManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParsePush;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.List;


public class MainActivity extends ActionBarActivity {
    public String phoneNumber;
    private static MainActivity inst;

    public static MainActivity instance() {
        return inst;
    }

    @Override
    public void onStart() {
        super.onStart();
        inst = this;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SmsManager sm = SmsManager.getDefault();
        sm.sendTextMessage("+1412-818-2411", null, "Hi", null, null);

      }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
    
    public void savePhoneNumber (String address) {
        phoneNumber = address;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_create) {
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
}
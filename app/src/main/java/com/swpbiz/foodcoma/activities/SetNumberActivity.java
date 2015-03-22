package com.swpbiz.foodcoma.activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.telephony.gsm.SmsManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.swpbiz.foodcoma.R;


public class SetNumberActivity extends ActionBarActivity {

    private static SetNumberActivity inst;
    public static SetNumberActivity instance() {
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
        setContentView(R.layout.activity_set_number);

        final Button btPhoneNumber = (Button) findViewById(R.id.btPhoneNumber);
        final EditText etPhoneNumber = (EditText) findViewById(R.id.etPhoneNumber);
        etPhoneNumber.setText(getNumberFromTelephonyManager());


        btPhoneNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String ownerNumber = etPhoneNumber.getText().toString();
                findViewById(R.id.llSetNumber).setVisibility(View.GONE);
                findViewById(R.id.llLoading).setVisibility(View.VISIBLE);

                SmsManager sm = SmsManager.getDefault();
                sm.sendTextMessage(ownerNumber, null, "Hi", null, null);

                // Now you're waiting. SmsReceiver will be the one who closes this activity.
            }
        });
    }

    public void savePhoneNumber(String phoneNumber) {
        SharedPreferences sharedPref = getSharedPreferences("foodcoma", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(getString(R.string.my_phone_number), phoneNumber);
        editor.commit();
        Log.d("DEBUG", "savePhoneNumber: " + phoneNumber);
    }

    // Get called from SmsReceiver after it saves the phone number
    public void closeActivity() {
        setResult(RESULT_OK);
        finish();
    }

    private String getNumberFromTelephonyManager() {
        TelephonyManager tm = (TelephonyManager)getSystemService(TELEPHONY_SERVICE);
        return tm.getLine1Number();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_set_number, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}

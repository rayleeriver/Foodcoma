package com.swpbiz.foodcoma;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.telephony.gsm.SmsManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;


public class SetNumberActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_number);

        Button btPhoneNumber = (Button) findViewById(R.id.btPhoneNumber);
        final EditText etPhoneNumber = (EditText) findViewById(R.id.etPhoneNumber);
        etPhoneNumber.setText(getNumber());


        btPhoneNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String ownerNumber = etPhoneNumber.getText().toString();
                FoodcomaApplication app = (FoodcomaApplication) getApplicationContext();
                app.setPhoneNumber(ownerNumber);
                SmsManager sm = SmsManager.getDefault();
                sm.sendTextMessage(ownerNumber, null, "Hi", null, null);
                // TODO: save it to sharedPreference
                Intent i = new Intent(SetNumberActivity.this, MainActivity.class);
                startActivity(i);
            }
        });
    }

    private String getNumber() {
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

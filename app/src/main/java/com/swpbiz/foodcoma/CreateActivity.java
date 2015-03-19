package com.swpbiz.foodcoma;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;

import com.swpbiz.foodcoma.models.Invitation;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;


public class CreateActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_create, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_send) {
            Invitation invitation = createInvitation();
            Log.d("DEBUG", "location " + invitation.getMapUrl());
            Log.d("DEBUG", "datetime " + invitation.getTimeOfEvent());
            Intent i = new Intent(CreateActivity.this, ViewActivity.class);
            startActivity(i);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private Invitation createInvitation() {
        EditText etLocation = (EditText) findViewById(R.id.etLocation);
        EditText etDate = (EditText) findViewById(R.id.etDate);
        EditText etTime = (EditText) findViewById(R.id.etTime);
        // Friend list

        String location = etLocation.getText().toString();
        String date = etDate.getText().toString();
        String time = etTime.getText().toString();

        Invitation invitation = new Invitation();
        invitation.setMapUrl(location);
        invitation.setTimeOfEvent(0); // set it later

        return invitation;
    }
}

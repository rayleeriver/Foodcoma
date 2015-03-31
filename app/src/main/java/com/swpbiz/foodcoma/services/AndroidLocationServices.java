package com.swpbiz.foodcoma.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;


import com.parse.Parse;

import org.json.JSONArray;
import org.json.JSONObject;

public class AndroidLocationServices extends Service {

    PowerManager.WakeLock wakeLock;

    private LocationManager locationManager;

    public AndroidLocationServices() {
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        PowerManager pm = (PowerManager) getSystemService(this.POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "DoNotSleep");
    }

    @Override
    @Deprecated
    public void onStart(Intent intent, int startId) {
        // TODO Auto-generated method stub
        super.onStart(intent, startId);

        Log.e("Google", "Service Started");

        locationManager = (LocationManager) getApplicationContext()
                .getSystemService(Context.LOCATION_SERVICE);

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                1000, 5, listener);

    }

    private LocationListener listener = new LocationListener() {

        @Override
        public void onLocationChanged(Location location) {
            Log.e("Google", "Location Changed");

            if (location == null)
                return;

            if (isConnectingToInternet(getApplicationContext())) {
                JSONArray jsonArray = new JSONArray();
                JSONObject jsonObject = new JSONObject();

                try {
                    Log.e("latitude", location.getLatitude() + "");
                    Log.e("longitude", location.getLongitude() + "");

                    LocationUpdatedAsyncTask locationUpdatedAsyncTask = new LocationUpdatedAsyncTask(getApplicationContext(), location);
                    locationUpdatedAsyncTask.execute();
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            }

        }

        @Override
        public void onProviderDisabled(String provider) {

        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        wakeLock.release();
    }

    public static boolean isConnectingToInternet(Context _context) {
        ConnectivityManager connectivity = (ConnectivityManager) _context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null) {
                for (int i = 0; i < info.length; i++) {
                    if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

}

package com.swpbiz.foodcoma.activities;

import android.preference.PreferenceActivity;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.swpbiz.foodcoma.FoodcomaApplication;
import com.swpbiz.foodcoma.R;
import com.swpbiz.foodcoma.adapters.RestaurantAdaptor;
import com.swpbiz.foodcoma.models.Restaurant;

import org.apache.http.Header;
import org.apache.http.client.ResponseHandler;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class RestaurantActivity extends ActionBarActivity {

    private final String API_KEY="AIzaSyCqT9dz3gMHQO1P27j0md99PrdpuX30shI";
    ArrayList<Restaurant> arrayRestaurants;
    RestaurantAdaptor arestaurant;
    ListView lvrestaurants;
    EditText etsearch;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant);
        arrayRestaurants =  new ArrayList<Restaurant>();
        arestaurant = new RestaurantAdaptor(this, arrayRestaurants);
        lvrestaurants = (ListView) findViewById(R.id.Lvrestaurants);
        etsearch = (EditText) findViewById(R.id.etsearch);
        lvrestaurants.setAdapter(arestaurant);
        FoodcomaApplication mapp = (FoodcomaApplication)getApplicationContext();

      //  String url = "https://maps.googleapis.com/maps/api/place/textsearch/json?query=restaurant&key="+API_KEY+"&types=food";
        String nearbyurl = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location="+ mapp.getMylatitude() +","+ mapp.getMylongitude()+"&key="+API_KEY+"&types=food&radius=5000&keyword=restaurant";

        Log.d("DEBUG",nearbyurl);
        fetchRestaurants(nearbyurl);
    }

    public void SearchRestaurant(View view ) {
        String searchQuery = etsearch.getText().toString();
        FoodcomaApplication mapp = (FoodcomaApplication)getApplicationContext();
        //String url = "https://maps.googleapis.com/maps/api/place/textsearch/json?query="+searchQuery+"&key="+API_KEY+"&types=food";
        String nearbyurl = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location="+ mapp.getMylatitude() +","+ mapp.getMylongitude()+"&key="+API_KEY+"&types=food&radius=5000&keyword="+ searchQuery;
        Log.d("DEBUG",nearbyurl);
        arestaurant.clear();
        fetchRestaurants(nearbyurl);
    }



    public void fetchRestaurants(String url) {
        AsyncHttpClient client = new AsyncHttpClient();

        client.get(url, null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Log.i("DEBUG,", response.toString());
                if (response != null) {
                    arrayRestaurants = Restaurant.getArrayFromJson(response);

                    AsyncHttpClient clientplace = new AsyncHttpClient();
                    for (int i = 0; i < arrayRestaurants.size(); i++) {
                        String placeurl = "https://maps.googleapis.com/maps/api/place/details/json?placeid=" + arrayRestaurants.get(i).getRestaurantId() + "&key=" + API_KEY;
                        clientplace.get(placeurl, null, new JsonHttpResponseHandler() {
                            @Override
                            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                                Log.i("DEBUG,", response.toString());
                                if (response != null) {
                                    try {
                                        for (int i = 0; i < arrayRestaurants.size(); i++) {
                                            if (arrayRestaurants.get(i).getRestaurantId() == response.getJSONObject("result").getString("place_id")) {
                                                arrayRestaurants.get(i).setResAddress(response.getJSONObject("result").getString("formatted_address"));
                                            }
                                        }

                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }

                            @Override
                            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                                Log.d("DEBUG", "failed API call");
                                super.onFailure(statusCode, headers, responseString, throwable);
                            }
                        });
                    }
                    arestaurant.addAll(arrayRestaurants);
                    }

                     //   if (response.getString("status") == "OK") {
                    //        arestaurant.addAll(Restaurant.getArrayFromJson(response));
                     //   }

                }



            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.d("DEBUG", "failed API call");
                super.onFailure(statusCode, headers, responseString, throwable);
            }
        });



 }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_restaurant, menu);
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

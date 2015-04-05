package com.swpbiz.foodcoma.activities;

import android.content.Intent;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.swpbiz.foodcoma.FoodcomaApplication;
import com.swpbiz.foodcoma.R;
import com.swpbiz.foodcoma.adapters.RestaurantAdaptor;
import com.swpbiz.foodcoma.models.Restaurant;

import org.apache.http.Header;
import org.json.JSONObject;

import java.util.ArrayList;

public class RestaurantActivity extends ActionBarActivity {

    private final String API_KEY = "AIzaSyCqT9dz3gMHQO1P27j0md99PrdpuX30shI";
    ArrayList<Restaurant> arrayRestaurants;
    RestaurantAdaptor aRestaurant;
    ListView lvRestaurants;
    FoodcomaApplication app;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant);
        app = (FoodcomaApplication) getApplicationContext();
        arrayRestaurants = new ArrayList<Restaurant>();
        aRestaurant = new RestaurantAdaptor(this, arrayRestaurants);
        lvRestaurants = (ListView) findViewById(R.id.Lvrestaurants);
        lvRestaurants.setAdapter(aRestaurant);

        lvRestaurants.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Restaurant res = arrayRestaurants.get(position);
                Intent returnIntent = new Intent();
                returnIntent.putExtra("restaurant", res);
                setResult(RESULT_OK, returnIntent);
                finish();
            }
        });

        String nearbyurl = getSearchUrl("restaurant");
        Log.d("DEBUG",nearbyurl);
        fetchRestaurants(nearbyurl);
    }

    public void fetchRestaurants(String url) {
        AsyncHttpClient client = new AsyncHttpClient();

        client.get(url, null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Log.i("DEBUG", response.toString());
                if (response != null) {
                    arrayRestaurants = Restaurant.getArrayFromJson(response);
                    aRestaurant.clear();
                    aRestaurant.addAll(arrayRestaurants);
                    aRestaurant.notifyDataSetChanged();
                }
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.d("DEBUG", "failed API call");
                super.onFailure(statusCode, headers, responseString, throwable);
            }
        });
    }

    private String getSearchUrl(String keyword) {
        return "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=" + app.getMylatitude() + "," + app.getMylongitude() + "&key=" + API_KEY + "&types=food&radius=5000&keyword=" + keyword;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_restaurant, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);

        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if(query != null){
                    String url = getSearchUrl(query);
                    Log.d("DEBUG-restaurant", url);
                    fetchRestaurants(url);
                }
                searchView.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

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

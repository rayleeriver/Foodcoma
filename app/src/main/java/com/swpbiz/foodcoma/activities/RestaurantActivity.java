package com.swpbiz.foodcoma.activities;

import android.content.Intent;
import android.os.PersistableBundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.swpbiz.foodcoma.EndlessScrollListener;
import com.swpbiz.foodcoma.FoodcomaApplication;
import com.swpbiz.foodcoma.R;
import com.swpbiz.foodcoma.adapters.RestaurantAdaptor;
import com.swpbiz.foodcoma.fragments.RestaurantDetailFragment;
import com.swpbiz.foodcoma.models.Restaurant;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class RestaurantActivity extends ActionBarActivity implements RestaurantDetailFragment.RestaurantDetailFragmentListener {

    private final String API_KEY = "AIzaSyCqT9dz3gMHQO1P27j0md99PrdpuX30shI";
    private ArrayList<Restaurant> allRestaurants;
    private RestaurantAdaptor aRestaurant;
    private ListView lvRestaurants;
    private FoodcomaApplication app;
    private EndlessScrollListener endlessScrollListener;
    private String nextToken;
    private AsyncHttpClient client;
    private Toast loading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant);

        app = (FoodcomaApplication) getApplicationContext();
        client = new AsyncHttpClient();

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        allRestaurants = new ArrayList<Restaurant>();
        aRestaurant = new RestaurantAdaptor(this, allRestaurants);
        lvRestaurants = (ListView) findViewById(R.id.Lvrestaurants);
        lvRestaurants.setAdapter(aRestaurant);

        lvRestaurants.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                showRestaurantDialog(position);
            }
        });

        String nearbyurl = getSearchUrl("restaurant");
        Log.d("DEBUG",nearbyurl);

        setupEndlessScroll();

        fetchRestaurants(nearbyurl);

    }

    private void showRestaurantDialog(int position) {
        Restaurant res = allRestaurants.get(position);
        FragmentManager fm = getSupportFragmentManager();
        RestaurantDetailFragment restaurantDetailFragment = RestaurantDetailFragment.newInstance(res.getRestaurantId(), position);
        restaurantDetailFragment.show(fm, "fragment_restaurant_detail");
    }

    private void setupEndlessScroll() {
        endlessScrollListener = new EndlessScrollListener() {
            @Override
            public void onLoadMore(int page, int totalItemsCount) {
                if(nextToken != null) {
                    Log.d("DEBUG", "Fetch Next: " + getNextSearchUrl());
                    fetchRestaurants(getNextSearchUrl());
                }
            }
        };
        lvRestaurants.setOnScrollListener(endlessScrollListener);
    }

    public void fetchRestaurants(String url) {

        showLoadingToast();

        client.get(url, null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Log.i("DEBUG", response.toString());
                try {
                    ArrayList<Restaurant> arrayRestaurants = Restaurant.getArrayFromJson(response);
                    allRestaurants.addAll(arrayRestaurants);
                    if(response.has("next_page_token"))
                        nextToken = response.getString("next_page_token");
                    else
                        nextToken = null;
                    // aRestaurant.addAll(arrayRestaurants);
                    aRestaurant.notifyDataSetChanged();

                    hideLoadingToast();

                } catch (JSONException e) {
                    e.printStackTrace();
                }
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
        getMenuInflater().inflate(R.menu.menu_restaurant, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);

        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setQueryHint(Html.fromHtml("<font color='#ffffff'>sushi, coffee, bakery</font>"));
        searchView.setIconifiedByDefault(false);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if(query != null){
                    String url = getSearchUrl(query);
                    Log.d("DEBUG-restaurant", url);
                    aRestaurant.clear();
                    allRestaurants.clear();
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

    private String getSearchUrl(String keyword) {
        return "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=" + app.getMylatitude() + "," + app.getMylongitude() + "&key=" + API_KEY + "&types=food&radius=5000&keyword=" + keyword;
    }

    private String getNextSearchUrl() {
        return "https://maps.googleapis.com/maps/api/place/nearbysearch/json?pagetoken=" + nextToken + "&key=" + API_KEY;
    }

    private void showLoadingToast() {
        if(loading == null) {
            loading = Toast.makeText(this, "Loading...", Toast.LENGTH_LONG);
        }
        loading.show();
    }

    private void hideLoadingToast() {
        if(loading != null){
            loading.cancel();
        }
    }

    @Override
    public void onSelectRestaurant(int position) {
        Restaurant res = allRestaurants.get(position);
        Intent returnIntent = new Intent();
        returnIntent.putExtra("restaurant", res);
        setResult(RESULT_OK, returnIntent);
        finish();
    }
}

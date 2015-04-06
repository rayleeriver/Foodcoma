package com.swpbiz.foodcoma.fragments;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v4.app.DialogFragment;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.squareup.picasso.Picasso;
import com.swpbiz.foodcoma.R;
import com.swpbiz.foodcoma.adapters.ReviewArrayAdapter;
import com.swpbiz.foodcoma.models.Review;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class RestaurantDetailFragment extends DialogFragment {
    private static final String ARG_RESTAURANT_ID = "restaurantId";
    private static final String ARG_POSITION = "position";
    private static final String API_KEY = "AIzaSyCqT9dz3gMHQO1P27j0md99PrdpuX30shI";

    private AsyncHttpClient client;
    private String restaurantId;
    private int position;

    private TextView tvRestaurantName;
    private TextView tvOpenNow;
    private TextView tvOpeningHours;
    private TextView tvAddress;
    private TextView tvPhoneNumber;
    private TextView tvTypes;
    private LinearLayout llReviews;
    private ImageView ivBg;
    private ImageView ivSelect;
    private ImageView ivCancel;
    private RelativeLayout rlReviewsHeading;

    public static RestaurantDetailFragment newInstance(String restaurantId, int position) {
        RestaurantDetailFragment fragment = new RestaurantDetailFragment();
        Bundle args = new Bundle();
        args.putString(ARG_RESTAURANT_ID, restaurantId);
        args.putInt(ARG_POSITION, position);
        fragment.setArguments(args);
        return fragment;
    }

    public RestaurantDetailFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            restaurantId = getArguments().getString(ARG_RESTAURANT_ID);
            position = getArguments().getInt(ARG_POSITION);
        }
        client = new AsyncHttpClient();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_restaurant_detail, container, false);

        // Remove dialog title
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        setupViews(v);
        fetchRestaurant();

        return v;
    }

    private void setupViews(View v) {
        tvRestaurantName = (TextView) v.findViewById(R.id.tvRestaurantName);
        tvOpenNow = (TextView) v.findViewById(R.id.tvOpenNow);
        tvOpeningHours = (TextView) v.findViewById(R.id.tvOpeningHours);
        tvAddress = (TextView) v.findViewById(R.id.tvAddress);
        tvPhoneNumber = (TextView) v.findViewById(R.id.tvPhoneNumber);
        tvTypes = (TextView) v.findViewById(R.id.tvTypes);
        llReviews = (LinearLayout) v.findViewById(R.id.llReviews);
        rlReviewsHeading = (RelativeLayout) v.findViewById(R.id.rlReviewsHeading);
        ivBg = (ImageView) v.findViewById(R.id.ivBg);
        ivSelect = (ImageView) v.findViewById(R.id.ivSelect);
        ivCancel = (ImageView) v.findViewById(R.id.ivCancel);

        // Initialize values
        tvRestaurantName.setText("Loading...");
        tvOpenNow.setText("");
        tvOpeningHours.setText("");
        tvAddress.setText("");
        tvPhoneNumber.setText("");
        tvTypes.setText("");
        
        setupListeners();
    }

    private void setupListeners() {
        ivSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RestaurantDetailFragmentListener listener = (RestaurantDetailFragmentListener) getActivity();
                listener.onSelectRestaurant(position);
                dismiss();
            }
        });

        ivCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }

    private String getPlaceUrl() {
        return "https://maps.googleapis.com/maps/api/place/details/json?placeid=" + restaurantId + "&key=" + API_KEY;
    }

    private String getPhotoUrl(String reference) {
        return "https://maps.googleapis.com/maps/api/place/photo?maxwidth=800&photoreference=" + reference + "&key=" + API_KEY;
    }

    private void fetchRestaurant() {
        String url = getPlaceUrl();
        Log.d("DEBUG", url);
        client.get(url, null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Log.i("DEBUG,", response.toString());
                try {
                    fillText(response.getJSONObject("result"));
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

    private void fillText(JSONObject response) throws JSONException {

        // Restaurant Name
        tvRestaurantName.setText(response.getString("name"));

        // Opening Now
        JSONObject openingHoursObj = response.getJSONObject("opening_hours");
        tvOpenNow.setText(Html.fromHtml(getOpenText(openingHoursObj.getString("open_now"))));

        // Opening Hours
        String openingHours = openingHoursObj.getJSONArray("weekday_text").join("<br>").replace("\"", "");
        tvOpeningHours.setText(Html.fromHtml(openingHours));

        // Address
        tvAddress.setText(response.getString("formatted_address"));

        // Phone Number
        tvPhoneNumber.setText(response.getString("formatted_phone_number"));

        // Types
        String restaurantTypes = response.getJSONArray("types").join(", ").replace("\"", "");
        tvTypes.setText(restaurantTypes);

        // Reviews
        ArrayList<Review> reviews = Review.fromJSONArray(response.getJSONArray("reviews"));
        ReviewArrayAdapter aReviews = new ReviewArrayAdapter(getActivity(), reviews);

        if(aReviews.getCount() == 0) {
            rlReviewsHeading.setVisibility(View.GONE);
        } else {
            rlReviewsHeading.setVisibility(View.VISIBLE);
        }

        for (int i = 0; i < aReviews.getCount(); i++) {
            View view = aReviews.getView(i, null, llReviews);
            llReviews.addView(view);
        }

        // Background photo
        ivBg.setImageResource(0);
        if(response.has("photos")){
            String photoRef = getPhotoReference(response.getJSONArray("photos"));
            String photoUrl = getPhotoUrl(photoRef);
            Picasso.with(getActivity()).load(photoUrl).into(ivBg);
        } else {
            ivBg.setImageResource(R.drawable.bg);
        }

    }

    private String getPhotoReference(JSONArray photos) throws JSONException {
        if(photos.length() > 0) {
            return photos.getJSONObject(0).getString("photo_reference");
        }
        return null;
    }

    private String getOpenText(String open_now) {
        if(open_now.equals("true")) return "<font color='green'>Opening now</font>";
        else if(open_now.equals("false")) return "<font color='red'>Closed</font>";
        else return "N/A";
    }

    public interface RestaurantDetailFragmentListener {
        void onSelectRestaurant(int position);
    }



}

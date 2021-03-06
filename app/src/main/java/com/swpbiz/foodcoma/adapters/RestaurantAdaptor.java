package com.swpbiz.foodcoma.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.swpbiz.foodcoma.R;
import com.swpbiz.foodcoma.models.Restaurant;

import java.util.List;

/**
 * Created by abgandhi on 3/24/15.
 */
public class RestaurantAdaptor extends ArrayAdapter<Restaurant> {
    private final String API_KEY="AIzaSyCqT9dz3gMHQO1P27j0md99PrdpuX30shI";

    public RestaurantAdaptor(Context context, List<Restaurant> objects) {
        super(context, android.R.layout.simple_list_item_1, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Restaurant res = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_restaurant, parent, false);
        }

        TextView tvresname = (TextView) convertView.findViewById(R.id.tvrestaurantname);
        TextView tvrating = (TextView) convertView.findViewById(R.id.tvrating);
        TextView tvpricelevel = (TextView) convertView.findViewById(R.id.tvpricelevel);
        TextView tvaddress = (TextView) convertView.findViewById(R.id.tvaddress);
        ImageView ivicon = (ImageView) convertView.findViewById(R.id.Iviconurl);
        RatingBar rbStar = (RatingBar) convertView.findViewById(R.id.rbStar);

        tvresname.setText(res.getName());
        tvrating.setText("(" + res.getRating() + ")");
        tvpricelevel.setText(getDollars(res.getPriceLevel()));
        tvaddress.setText(res.getResAddress());

        if(res.getRating().length() >= 1) {
            rbStar.setRating(Float.parseFloat(res.getRating()));
            rbStar.setVisibility(View.VISIBLE);
            tvrating.setVisibility(View.VISIBLE);
        } else {
            rbStar.setVisibility(View.GONE);
            tvrating.setVisibility(View.GONE);
        }

        ivicon.setImageResource(0);
        Picasso.with(getContext()).load(res.getIconurl()).into(ivicon);

        return  convertView;
    }

    private String getDollars(String priceLevel) {
        if(priceLevel.equals("")){
            return "";
        }
        String output = "";
        int priceNum = Integer.parseInt(priceLevel);
        for(int i = 0; i < priceNum; i++) {
            output = output + "$";
        }
        return output;
    }
}

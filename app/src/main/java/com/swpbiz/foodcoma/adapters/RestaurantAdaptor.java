package com.swpbiz.foodcoma.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
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
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.restaurant_item, parent, false);
        }

        TextView tvresname = (TextView) convertView.findViewById(R.id.tvrestaurantname);
        TextView tvrating = (TextView) convertView.findViewById(R.id.tvrating);
        TextView tvpricelevel = (TextView) convertView.findViewById(R.id.tvpricelevel);
        TextView tvaddress = (TextView) convertView.findViewById(R.id.tvaddress);
        ImageView ivicon = (ImageView) convertView.findViewById(R.id.Iviconurl);
        ImageView ivresphoto = (ImageView) convertView.findViewById(R.id.Ivrestauranrphoto);

        tvresname.setText(res.getName());
        tvrating.setText(res.getRating());
        tvpricelevel.setText(res.getPriceLevel());
        tvaddress.setText(res.getResAddress());

        ivicon.setImageResource(0);
        Picasso.with(getContext()).load(res.getIconurl()).into(ivicon);

        ivresphoto.setImageResource(0);
        String photourl = "https://maps.googleapis.com/maps/api/place/photo?photoreference="+res.getPhotoReference()+"&key="+API_KEY;
        Picasso.with(getContext()).load(photourl).into(ivresphoto);
        return  convertView;
    }
}

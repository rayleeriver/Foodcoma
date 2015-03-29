package com.swpbiz.foodcoma.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.swpbiz.foodcoma.R;
import com.swpbiz.foodcoma.models.User;

import java.util.List;

/**
 * Created by vee on 3/28/15.
 */
public class FriendListAdapter extends ArrayAdapter<User> {


    public FriendListAdapter(Context context, List<User> users) {
        super(context, android.R.layout.simple_list_item_1, users);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        final User user = getItem(position);

        if(convertView == null){
            LayoutInflater.from(getContext()).inflate(R.layout.item_contact, parent, false);
        }

        // Find subview
        TextView tvName = (TextView) convertView.findViewById(R.id.tvName);
        TextView tvPhoneNumber = (TextView) convertView.findViewById(R.id.tvPhoneNumber);
        ImageView ivImage = (ImageView) convertView.findViewById(R.id.ivImage);
        CheckBox cbSelected = (CheckBox) convertView.findViewById(R.id.cbSelected);

        // Populate Data
        tvName.setText(user.getName());
        tvPhoneNumber.setText(user.getPhoneNumber());
        cbSelected.setVisibility(View.GONE);



        return convertView;
    }
}

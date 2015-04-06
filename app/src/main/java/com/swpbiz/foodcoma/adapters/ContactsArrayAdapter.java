package com.swpbiz.foodcoma.adapters;

import android.content.Context;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.swpbiz.foodcoma.R;
import com.swpbiz.foodcoma.models.User;

import java.util.HashMap;
import java.util.Map;

public class ContactsArrayAdapter extends ArrayAdapter<User> {

    Map<String, User> namesSelected = new HashMap<String, User>();
    private SparseBooleanArray itemChecked = new SparseBooleanArray();

    private TextView tvPhoneNumber;
    private TextView tvName;
    private ImageView ivImage;
    private CheckBox cbSelected;

    public ContactsArrayAdapter(Context context) {
        super(context, 0);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_contact, parent, false);
        }

        tvName = (TextView) convertView.findViewById(R.id.tvName);
        tvPhoneNumber = (TextView) convertView.findViewById(R.id.tvPhoneNumber);
        ivImage = (ImageView) convertView.findViewById(R.id.ivImage);
        cbSelected = (CheckBox) convertView.findViewById(R.id.cbSelected);

        User contact = getItem(position);
        tvName.setText(contact.getName());
        tvPhoneNumber.setText(contact.getPhoneNumber());
        if (contact.getContactPhotoUri() != null) {
            Picasso.with(getContext()).load(contact.getContactPhotoUri()).into(ivImage);
        } else {
            ivImage.setImageResource(R.mipmap.ic_contact_blue);
        }

        final int pos = position;
        final String name = contact.getName();
        final String finalPhoneNumber = contact.getPhoneNumber();
        cbSelected.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(!itemChecked.get(pos)) {
                    User aUser = new User(name, finalPhoneNumber);
                    namesSelected.put(finalPhoneNumber, aUser);

                    // Log.d("DEBUG-onChecked", name);
                    itemChecked.put(pos, true);
                    Log.d("DEBUG-add", name);

                } else {
                    namesSelected.remove(finalPhoneNumber);
                    itemChecked.put(pos, false);
                    Log.d("DEBUG-remove", name);
                }

            }
        });

        // Log.d("DEBUG-cb", pos + " " + itemChecked.get(pos));
        cbSelected.setChecked(itemChecked.get(pos));
        return convertView;
    }

    public  Map<String, User>  getNamesSelected() {
        return namesSelected;
    }
}

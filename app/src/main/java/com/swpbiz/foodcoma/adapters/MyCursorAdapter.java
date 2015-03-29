package com.swpbiz.foodcoma.adapters;

import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.swpbiz.foodcoma.R;
import com.swpbiz.foodcoma.models.User;

import java.util.HashMap;
import java.util.Map;


public class MyCursorAdapter extends CursorAdapter {

    Map<String, User> namesSelected = new HashMap<String, User>();
    private SparseBooleanArray itemChecked = new SparseBooleanArray();
    private TextView tvPhoneNumber;
    private TextView tvName;
    private ImageView ivImage;
    private CheckBox cbSelected;

    public MyCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.item_contact, parent, false);
    }

    private void findViews(View view){
        tvName = (TextView) view.findViewById(R.id.tvName);
        tvPhoneNumber = (TextView) view.findViewById(R.id.tvPhoneNumber);
        ivImage = (ImageView) view.findViewById(R.id.ivImage);
        cbSelected = (CheckBox) view.findViewById(R.id.cbSelected);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        findViews(view);

        int contactId = cursor.getInt(0);
        final int pos = cursor.getPosition();

        Cursor phones = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + contactId, null, null);

        // Get value of name, phone number and picture
        final String name = cursor.getString(1);
        String photoUri = cursor.getString(2);
        String phoneNumber = "N/A";
        if (phones.moveToFirst()) {
            phoneNumber = phones.getString(phones.getColumnIndex("data1"));
        }

        phoneNumber = phoneNumber.replaceAll("[^0-9]", ""); // get Digits from string

        final String finalPhoneNumber = phoneNumber;

        // Set contact name, phone number and picture
        tvName.setText(name);
        tvPhoneNumber.setText(phoneNumber);
        if (photoUri != null) Picasso.with(context).load(photoUri).into(ivImage);

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
        phones.close();
    }

    public Map<String,User> getNamesSelected() {
        return namesSelected;
    }


}
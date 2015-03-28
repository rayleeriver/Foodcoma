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
import android.widget.CompoundButton;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.swpbiz.foodcoma.R;
import com.swpbiz.foodcoma.models.User;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;


public class MyCursorAdapter extends CursorAdapter {

    Set<User> namesSelected = new HashSet<User>();
    // private ArrayList<Boolean> itemChecked = new ArrayList<Boolean>();
    private SparseBooleanArray itemChecked = new SparseBooleanArray();

    public MyCursorAdapter(Context context, Cursor c) {

        super(context, c, 0);
        Log.d("DEBUG-cursor", this.getCount() + " ");
//        for (int i = 0; i < this.getCount(); i++) {
//            itemChecked.add(i, false); // initializes all items value with false
//        }
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.item_contact, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        int contactId = cursor.getInt(0);
        final int pos = cursor.getPosition();

        Cursor c = context.getContentResolver().query(
                ContactsContract.Data.CONTENT_URI,
                new String[]{
                        ContactsContract.Contacts.Data._ID,
                        ContactsContract.CommonDataKinds.Phone.NUMBER,
                        ContactsContract.CommonDataKinds.Phone.TYPE,
                        ContactsContract.CommonDataKinds.Phone.LABEL},
                ContactsContract.Data.CONTACT_ID + "=?" + " AND "
                        + ContactsContract.Contacts.Data.MIMETYPE + "='"
                        + ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE + "'",
                new String[]{String.valueOf(contactId)}, null);


        Cursor phones = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + contactId, null, null);
        String phoneNumber = "N/A";
        if (phones.moveToFirst()) {
            phoneNumber = phones.getString(phones.getColumnIndex("data1"));
            if (phoneNumber != null) {
                TextView tv = (TextView) view.findViewById(R.id.tvPhoneNumber);
                tv.setText(phoneNumber);
            }
        }

        final String name = cursor.getString(1);

        TextView tv = (TextView) view.findViewById(R.id.tvName);
        tv.setText(name);

        String photoUri = cursor.getString(2);
        if (photoUri != null) {
            ImageView iv = (ImageView) view.findViewById(R.id.ivImage);
            Picasso.with(context).load(photoUri).into(iv);
        }

        RelativeLayout rlContactItem = (RelativeLayout) view.findViewById(R.id.rlContactItem);
        // TODO: Vee will take care of the UI

        final CheckBox cb = (CheckBox) view.findViewById(R.id.cbSelected);
        phoneNumber = phoneNumber.replaceAll("[^0-9]", ""); // get Digits from string
        final String finalPhoneNumber = phoneNumber;

        cb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cb.isChecked()) {
                    User aUser = new User();
                    aUser.setName(name);
                    aUser.setPhoneNumber(finalPhoneNumber);
                    aUser.setUserId(finalPhoneNumber);
                    aUser.setRsvp("MAYBE");
                    namesSelected.add(aUser);
                    Log.d("DEBUG-onChecked", name);
                    itemChecked.put(pos, true);

                } else {
                    // namesSelected.remove(name);
                    itemChecked.put(pos, false);
                }

            }
        });

        Log.d("DEBUG-cb", cursor.getPosition() + " " + itemChecked.get(cursor.getPosition()));
        cb.setChecked(itemChecked.get(cursor.getPosition()));

    }

    public Set<User> getNamesSelected() {
        return namesSelected;
    }


}
package com.swpbiz.foodcoma;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.HashSet;
import java.util.Set;



public class MyCursorAdapter extends CursorAdapter {

    Set<String> namesSelected = new HashSet<String>();

    public MyCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.item_contact, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        final String name = cursor.getString(1);
        TextView tv = (TextView) view.findViewById(R.id.tvName);
        tv.setText(name);

        String photoUri = cursor.getString(2);
        if (photoUri!= null) {
            ImageView iv = (ImageView) view.findViewById(R.id.ivImage);
            Picasso.with(context).load(photoUri).into(iv);
        }

        CheckBox cb = (CheckBox) view.findViewById(R.id.cbSelected);
        cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    namesSelected.add(name);
                } else {
                    namesSelected.remove(name);
                }
            }
        });
    }

    public Set<String> getNamesSelected() {
        return namesSelected;
    }


}
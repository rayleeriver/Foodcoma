package com.swpbiz.foodcoma.adapters;

import android.app.Application;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.swpbiz.foodcoma.FoodcomaApplication;
import com.swpbiz.foodcoma.R;
import com.swpbiz.foodcoma.models.Invitation;
import com.swpbiz.foodcoma.models.User;

/**
 * Created by vee on 3/28/15.
 */
public class FriendListAdapter extends ArrayAdapter<User> {

    Invitation invitation;
    Application application;

    public FriendListAdapter(Context context, Invitation invitation, Application application) {
        super(context, 0);
        this.invitation = invitation;
        this.application = application;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        // Get user
        final User user = getItem(position);

        // Find or inflate the template
        if(convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_contact, parent, false);
        }

        // Find subview
        TextView tvName = (TextView) convertView.findViewById(R.id.tvName);
        TextView tvPhoneNumber = (TextView) convertView.findViewById(R.id.tvPhoneNumber);
        ImageView ivImage = (ImageView) convertView.findViewById(R.id.ivImage);

        String contactPhotoUri = user.getContactPhotoUri();
        if (contactPhotoUri == null || contactPhotoUri.length() == 0) {
            User contactByPhoneNumber = ((FoodcomaApplication) application).findContactByPhoneNumber(user.getPhoneNumber());
            if (contactByPhoneNumber != null) {
                contactPhotoUri = contactByPhoneNumber.getContactPhotoUri();
                tvName.setText(contactByPhoneNumber.getName());
            } else {
                contactPhotoUri = null;
                tvName.setText(user.getPhoneNumber());
            }
        }
        if (contactPhotoUri != null && contactPhotoUri.length() > 0) {
            Picasso.with(getContext())
                    .load(contactPhotoUri)
                    .into(ivImage);
        } else {
            Picasso.with(getContext())
                    .load(R.drawable.ic_contact)
                    .into(ivImage);
        }
        CheckBox cbSelected = (CheckBox) convertView.findViewById(R.id.cbSelected);

        // Populate Data
        tvPhoneNumber.setText(user.getPhoneNumber());

        cbSelected.setVisibility(View.VISIBLE);
        if (invitation.getAcceptedUsers().contains(user.getPhoneNumber())) {
            cbSelected.setChecked(true);
        } else {
            cbSelected.setChecked(false);
        }
        cbSelected.setEnabled(false);

        return convertView;
    }
}

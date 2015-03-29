package com.swpbiz.foodcoma.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.swpbiz.foodcoma.R;
import com.swpbiz.foodcoma.activities.CreateActivity;
import com.swpbiz.foodcoma.activities.ViewActivity;
import com.swpbiz.foodcoma.models.Invitation;
import com.swpbiz.foodcoma.utils.MyDateTimeUtil;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class InvitationsArrayAdapter extends ArrayAdapter<Invitation> {

    public InvitationsArrayAdapter(Context context, List<Invitation> invitations) {
        super(context, 0, invitations);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final Invitation invitation = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_invitation, parent, false);
        }
        TextView tvTime = (TextView) convertView.findViewById(R.id.tvTime);
        TextView tvDate = (TextView) convertView.findViewById(R.id.tvDate);
        TextView tvEventName = (TextView) convertView.findViewById(R.id.tvEventName);
        TextView tvCreator = (TextView) convertView.findViewById(R.id.tvCreator);
        ImageView ivAccepted = (ImageView) convertView.findViewById(R.id.ivAccepted);

        tvTime.setText(MyDateTimeUtil.getTimeFromEpoch(invitation.getTimeOfEvent()));
        tvDate.setText(MyDateTimeUtil.getDateFromEpoch(invitation.getTimeOfEvent()));
        tvEventName.setText(invitation.getPlaceName());
        tvCreator.setText("By " + invitation.getOwner().getName());
        if (invitation.isAccept()) {
            tvTime.setBackgroundColor(getContext().getResources().getColor(R.color.primary));
            tvDate.setBackgroundColor(getContext().getResources().getColor(R.color.primary_light));
            ivAccepted.setVisibility(View.VISIBLE);
        } else {
            tvTime.setBackgroundColor(Color.parseColor("#cccccc"));
            tvDate.setBackgroundColor(Color.parseColor("#dedede"));
            ivAccepted.setVisibility(View.GONE);
        }

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getContext(), ViewActivity.class);
                i.putExtra("invitation", invitation);
                getContext().startActivity(i);
            }
        });

        return convertView;
    }
}

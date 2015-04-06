package com.swpbiz.foodcoma.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RatingBar;
import android.widget.TextView;

import com.swpbiz.foodcoma.R;
import com.swpbiz.foodcoma.models.Review;

import java.util.List;

/**
 * Created by vee on 4/5/15.
 */
public class ReviewArrayAdapter extends ArrayAdapter<Review> {



    public ReviewArrayAdapter(Context context, List<Review> reviews) {
        super(context, android.R.layout.simple_list_item_1, reviews);
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        Review review = getItem(position);

        if(convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_review, parent, false);
        }

        TextView tvReview = (TextView) convertView.findViewById(R.id.tvReview);
        RatingBar rbRating = (RatingBar) convertView.findViewById(R.id.rbRating);

        if(review.getRatingStar() > 0) {
            rbRating.setRating(review.getRatingStar());
            rbRating.setVisibility(View.VISIBLE);
        } else {
            rbRating.setVisibility(View.GONE);
        }

        tvReview.setText(review.getReviewContent());

        return convertView;
    }
}

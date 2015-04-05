package com.swpbiz.foodcoma;

import android.app.Activity;
import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParsePushBroadcastReceiver;
import com.parse.ParseQuery;
import com.swpbiz.foodcoma.activities.ViewActivity;
import com.swpbiz.foodcoma.models.Invitation;

import org.json.JSONException;
import org.json.JSONObject;

public class FoodcomaNotificationReceiver extends ParsePushBroadcastReceiver {

    public FoodcomaNotificationReceiver() {

    }

    @Override
    protected Class<? extends Activity> getActivity(Context arg0, Intent arg1) {
        // TODO Auto-generated method stub

        return ViewActivity.class;
    }


    @Override
    protected Notification getNotification(Context context, Intent intent) {
        // TODO Auto-generated method stub
        return super.getNotification(context, intent);
    }

    @Override
    protected void onPushDismiss(Context context, Intent intent) {
        // TODO Auto-generated method stub
        super.onPushDismiss(context, intent);
    }

    @Override
    protected void onPushOpen(Context context, Intent intent) {
        JSONObject obj = null;
        // TODO Auto-generated method stub
        try {
            obj = new JSONObject(intent.getStringExtra("com.parse.Data"));
            Intent i = new Intent(context, ViewActivity.class);
            Log.d("DEBUG", "data: " + obj.toString());

            String data = obj.getString("data");
            handlePushMessage(context, data);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPushReceive(Context context, Intent intent) {
        //here You can handle push before appearing into status e.g if you want to stop it.
        Log.d("DEBUG", "onPushReceive");
        super.onPushReceive(context, intent);
    }

    private void handlePushMessage(final Context context, String jsonString) {

        Invitation invitationFromPush = Invitation.getInvitationFromJsonObject(jsonString);

        ParseQuery<ParseObject> query = ParseQuery.getQuery("Invitation");

        query.getInBackground(invitationFromPush.getInvitationId(), new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject parseObject, ParseException e) {
                if (e == null) {
                    if (parseObject != null) {
                        Intent i = new Intent(context, ViewActivity.class);
                        try {
                            i.putExtra("invitation", Invitation.fromParseObject(parseObject));
                        } catch (JSONException e1) {
                            e1.printStackTrace();
                        }

                        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                        context.startActivity(i);
                    }
                }
            }
        });
    }

}

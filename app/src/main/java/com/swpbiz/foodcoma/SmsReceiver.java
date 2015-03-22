package com.swpbiz.foodcoma;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.SmsMessage;
import android.widget.Toast;

import com.swpbiz.foodcoma.activities.MainActivity;

/**
 * Created by abgandhi on 3/20/15.
 */
public class SmsReceiver extends BroadcastReceiver {
    private static final String ACTION_SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";
    private Intent mIntent;
    private String address = "", str = "";
    @Override
    public void onReceive(Context context, Intent intent) {
        mIntent = intent;
        String action = intent.getAction();

        if(action.equals(ACTION_SMS_RECEIVED)){
            SmsMessage[] msgs = getMessagesFromIntent(mIntent);
            if (msgs != null) {
                for (int i = 0; i < msgs.length; i++) {
                    address = msgs[i].getOriginatingAddress();
                    str += msgs[i].getMessageBody().toString();
                    str += "\n";
                }
            }
            Toast.makeText(context, "Phone Number " + address + "verified", Toast.LENGTH_LONG).show();
            //this will update the UI with message
            MainActivity inst = MainActivity.instance();
            inst.savePhoneNumber(address);
        }
    }

    public SmsMessage[] getMessagesFromIntent(Intent intent) {
        Object[] messages = (Object[]) intent.getSerializableExtra("pdus");
        byte[][] pduObjs = new byte[messages.length][];

        for (int i = 0; i < messages.length; i++) {
            pduObjs[i] = (byte[]) messages[i];
        }
        byte[][] pdus = new byte[pduObjs.length][];
        int pduCount = pdus.length;
        SmsMessage[] msgs = new SmsMessage[pduCount];
        for (int i = 0; i < pduCount; i++) {
            pdus[i] = pduObjs[i];
            msgs[i] = SmsMessage.createFromPdu(pdus[i]);
        }
        return msgs;
    }
}

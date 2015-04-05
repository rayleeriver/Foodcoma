package com.swpbiz.foodcoma.services;

import android.app.IntentService;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.support.v4.content.LocalBroadcastManager;

import com.swpbiz.foodcoma.models.User;

import java.util.ArrayList;

public class ContactsLoaderIntentService extends IntentService {
    public static final String ACTION = "com.swpbiz.foodcoma.services.ContactsLoaderIntentService";
    ArrayList<User> contacts = new ArrayList<>();

    public ContactsLoaderIntentService() {
        super("ContactsLoaderIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        ContentResolver cr = getBaseContext().getContentResolver(); //Activity/Application android.content.Context
        Cursor cursor = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
        if(cursor.moveToFirst())
        {
            do
            {
                String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));

                if(Integer.parseInt(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0)
                {
                    Cursor pCur = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,null,ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = ?",new String[]{ id }, null);
                    while (pCur.moveToNext())
                    {
                        String contactNumber = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        String photoThumbnailUrl = pCur.getString(pCur.getColumnIndex(ContactsContract.Contacts.PHOTO_THUMBNAIL_URI));
                        String name = pCur.getString(pCur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

                        User contact = new User();
                        contact.setPhoneNumber(User.getTrimmedPhoneNumber(contactNumber));
                        contact.setName(name);
                        contact.setContactPhotoUri(photoThumbnailUrl);

                        contacts.add(contact);

                        break;
                    }
                    pCur.close();
                }

            } while (cursor.moveToNext()) ;
        }

        Intent outIntent = new Intent(ACTION);
        outIntent.putParcelableArrayListExtra("contacts", contacts);
        LocalBroadcastManager.getInstance(this).sendBroadcast(outIntent);
    }
}

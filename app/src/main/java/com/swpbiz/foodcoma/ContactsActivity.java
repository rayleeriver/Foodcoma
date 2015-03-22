package com.swpbiz.foodcoma;

import android.database.Cursor;
import android.provider.ContactsContract;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Checkable;
import android.widget.ListView;


public class ContactsActivity extends ActionBarActivity {

    SimpleCursorAdapter adapter;
    public static final int CONTACT_LOADER_ID = 78; // From docs: A unique identifier for this loader.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);
        setupCursorAdapter();

        // Initialize the loader with a special ID and the defined callbacks from above
        getSupportLoaderManager().initLoader(CONTACT_LOADER_ID,
                new Bundle(), contactsLoader);
    }

    private LoaderManager.LoaderCallbacks<Cursor> contactsLoader =
            new LoaderManager.LoaderCallbacks<Cursor>() {
                @Override
                public Loader<Cursor> onCreateLoader(int id, Bundle args) {
                    // Define the columns to retrieve
                    String[] projectionFields =  new String[] { ContactsContract.Contacts._ID,
                            ContactsContract.Contacts.DISPLAY_NAME,
                            ContactsContract.Contacts.PHOTO_URI };
                    // Construct the loader
                    CursorLoader cursorLoader = new CursorLoader(ContactsActivity.this,
                            ContactsContract.Contacts.CONTENT_URI, // URI
                            projectionFields,  // projection fields
                            null, // the selection criteria
                            null, // the selection args
                            null // the sort order
                    );
                    // Return the loader for use
                    return cursorLoader;
                }

                // When the system finishes retrieving the Cursor through the CursorLoader,
                // a call to the onLoadFinished() method takes place.
                @Override
                public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
                    // The swapCursor() method assigns the new Cursor to the adapter
                    adapter.swapCursor(cursor);
                }

                // This method is triggered when the loader is being reset
                // and the loader data is no longer available. Called if the data
                // in the provider changes and the Cursor becomes stale.
                @Override
                public void onLoaderReset(Loader<Cursor> loader) {
                    // Clear the Cursor we were using with another call to the swapCursor()
                    adapter.swapCursor(null);
                }
            };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_contacts, menu);
        return true;
    }

    // Create simple cursor adapter to connect the cursor dataset we load with a ListView
    private void setupCursorAdapter() {
        final Button done_Button = (Button) findViewById(R.id.btnDone);

        // Column data from cursor to bind views from
        String[] uiBindFrom = { ContactsContract.Contacts.DISPLAY_NAME,
                ContactsContract.Contacts.PHOTO_URI };
        // View IDs which will have the respective column data inserted
        int[] uiBindTo = { R.id.tvName, R.id.ivImage };
        // Create the simple cursor adapter to use for our list
        // specifying the template to inflate (item_contact),
        adapter = new SimpleCursorAdapter(
                this, R.layout.item_contact,
                null, uiBindFrom, uiBindTo,
                0);

        ListView lvContacts = (ListView)findViewById(R.id.lvContacts);
        lvContacts.setAdapter(adapter);


        /*lvContacts.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        done_Button.setOnClickListener(new View.OnClickListener() {
            public void onClick (View v){

                SparseBooleanArray selectedPositions = lvContacts.getCheckedItemPositions();

                for (int i=0; i<selectedPositions.size(); i++) {
                    if (selectedPositions.get(selectedPositions.keyAt(i)) == true) {
                        Log.i("DEBUG","Selected items: " + selectedPositions.get(i));
                    }
                }

                *//*Log.i("DEBUG", ":Done Button Selected:");
                SparseBooleanArray checkedPositions = lvContacts.getCheckedItemPositions();
                Log.i("DEBUG","Number of Checked Positions: " + checkedPositions.size());
                if (checkedPositions != null)
                {
                    int count = lvContacts.getCount();
                    for ( int i=0;i<count;i++)
                    {
                        Log.i("DEBUG","Selected items: " + checkedPositions.get(i));
                    }
                }*//*

            }
        });*/

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}

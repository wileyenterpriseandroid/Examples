package com.enterpriseandroid.migratecontacts;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import net.migrate.api.SchemaManager;


public class ContactsActivity extends BaseActivity
    implements LoaderManager.LoaderCallbacks<Cursor>,
    SchemaManager.SchemaLoaderListener
{
    private static final String TAG = "CONTACTS";

    private static final int CONTACTS_LOADER_ID = 42;

    private static final String[] PROJ = new String[] {
        ContactContract.Columns._ID,
        ContactContract.Columns.FIRSTNAME,
        ContactContract.Columns.LASTNAME,
        ContactContract.Columns.PHONE_NUMBER,
        ContactContract.Columns.EMAIL,
    };

    private static final String[] FROM = new String[PROJ.length - 1];

    static {
        System.arraycopy(PROJ, 1, FROM, 0, FROM.length);
    }

    private static final int[] TO = new int[] {
        R.id.row_contacts_fname,
        R.id.row_contacts_lname,
        R.id.row_contacts_phone,
        R.id.row_contacts_email,
    };

    private SimpleCursorAdapter listAdapter;

    @Override
    public void onSchemaLoaded(String schema, boolean succeeded) {
        if (!succeeded) {
            Log.w(
                TAG,
                "Failed to initialize schema: " + ContactContract.SCHEMA_ID
                    + " @ " + ContactContract.SCHEMA_CONTACT_URI);

            // add failure handling code

            return;
        }

        getLoaderManager().initLoader(CONTACTS_LOADER_ID, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.d(TAG, "Creating content loader: " + ContactContract.OBJECT_CONTACT_URI);
        return new CursorLoader(
            this,
            ContactContract.OBJECT_CONTACT_URI,
            PROJ,
            null,
            null,
            ContactContract.Columns.FIRSTNAME + " ASC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // getting a null pointer exception here

        Log.d(TAG, "Content loaded: " + cursor.getCount());
        listAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        listAdapter.swapCursor(null);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);

        findViewById(R.id.activity_contacts_add).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showDetails(null);
                    }
                });

        listAdapter = new SimpleCursorAdapter(
            this,
            R.layout.contact_row,
            null,
            FROM,
            TO,
            0);

        ListView listView
            = ((ListView) findViewById(R.id.activity_contacts_list));
        listView.setAdapter(listAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> l, View v, int p, long id) {
                showDetails(p);
            }
        });

        new SchemaManager(
            this,
            ContactContract.SCHEMA_ID,
            ((ContactsApplication) getApplication()).getUser(),
            this)
        .initSchema();
    }

    void showDetails(int pos) {
        Cursor c = (Cursor) listAdapter.getItem(pos);
        showDetails(ContactContract.OBJECT_CONTACT_URI.buildUpon()
            .appendPath(c.getString(
                    c.getColumnIndex(ContactContract.Columns._ID)))
            .build());
    }

    void showDetails(Uri uri) {
        if (BuildConfig.DEBUG) { Log.d(TAG, "show details"); }
        Intent intent = new Intent();
        intent.setClass(this, ContactDetailActivity.class);
        if (null != uri) {
            intent.putExtra(ContactDetailActivity.KEY_URI, uri.toString());
        }
        startActivity(intent);
    }
}

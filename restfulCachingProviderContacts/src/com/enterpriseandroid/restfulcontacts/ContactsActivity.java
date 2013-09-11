package com.enterpriseandroid.restfulcontacts;

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
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import com.enterpriseandroid.restfulcontacts.data.ContactsContract;


public class ContactsActivity extends BaseActivity
    implements LoaderManager.LoaderCallbacks<Cursor>
{
    private static final String TAG = "CONTACTS";

    private static final int LOADER_ID = 42;

    private static final String[] PROJ = new String[] {
        ContactsContract.Columns.ID,
        ContactsContract.Columns.FNAME,
        ContactsContract.Columns.LNAME,
        ContactsContract.Columns.PHONE,
        ContactsContract.Columns.EMAIL,
        ContactsContract.Columns.STATUS
    };

    private static final String[] FROM = new String[PROJ.length - 1];
    static { System.arraycopy(PROJ, 1, FROM, 0, FROM.length); }

    private static final int[] TO = new int[] {
        R.id.row_contacts_fname,
        R.id.row_contacts_lname,
        R.id.row_contacts_phone,
        R.id.row_contacts_email,
        R.id.row_contacts_status
    };

    private static class StatusBinder
        implements SimpleCursorAdapter.ViewBinder
    {
        public StatusBinder() { }

        @Override
        public boolean setViewValue(View view, Cursor cursor, int idx) {
            if (view.getId() != R.id.row_contacts_status) { return false; }
            setStatusBackground(cursor.getInt(idx), view);
            return true;
        }
    }


    private SimpleCursorAdapter listAdapter;

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(
            this,
            ContactsContract.URI,
            PROJ,
            null,
            null,
            ContactsContract.Columns.FNAME + " ASC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
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

        ((Button) findViewById(R.id.activity_contacts_add)).setOnClickListener(
            new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showDetails(null);
        } });

        listAdapter = new SimpleCursorAdapter(
            this,
            R.layout.contact_row,
            null,
            FROM,
            TO,
            0);
        listAdapter.setViewBinder(new StatusBinder());

        ListView listView
            = ((ListView) findViewById(R.id.activity_contacts_list));
        listView.setAdapter(listAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> l, View v, int p, long id) {
                showDetails(p);
            } });

        getLoaderManager().initLoader(LOADER_ID, null, this);
    }

    void showDetails(int pos) {
        Cursor c = (Cursor) listAdapter.getItem(pos);
        showDetails(ContactsContract.URI.buildUpon()
            .appendPath(
                c.getString(c.getColumnIndex(ContactsContract.Columns.ID)))
            .build());
    }

    void showDetails(Uri uri) {
        if (BuildConfig.DEBUG) { Log.d(TAG, "adding contact"); }
        Intent intent = new Intent();
        intent.setClass(this, ContactDetailActivity.class);
        if (null != uri) {
            intent.putExtra(ContactDetailActivity.KEY_URI, uri.toString());
        }
        startActivity(intent);
    }
}

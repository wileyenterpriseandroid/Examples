package com.enterpriseandroid.migratecontacts;

import android.app.LoaderManager;
import android.content.*;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.*;
import net.migrate.api.SchemaManager;
import net.migrate.api.WebData;

public class ContactsActivity extends BaseActivity
        implements LoaderManager.LoaderCallbacks<Cursor>,
        SchemaManager.SchemaLoaderListener
{
    private static final String TAG = "CONTACTS";

    private static final int CONTACTS_LOADER_ID = 42;

    public static final String[] PROJ = new String[] {
            ContactContract.Columns._ID,
            WebData.Object.WD_DATA_ID,
            WebData.Object.WD_IN_CONFLICT,
            ContactContract.Columns.FIRSTNAME,
            ContactContract.Columns.LASTNAME,
            ContactContract.Columns.PHONE_NUMBER,
            ContactContract.Columns.EMAIL,
    };

    private static final String[] FROM = new String[PROJ.length - 3];

    static {
        System.arraycopy(PROJ, 3, FROM, 0, FROM.length);
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
            Log.w(TAG,
                    "Failed to initialize schema: " + ContactContract.SCHEMA_ID
                            + " @ " + ContactContract.SCHEMA_CONTACT_URI);

            // add failure handling code

            return;
        }

        getLoaderManager().initLoader(CONTACTS_LOADER_ID, null, this);

        ContentObserver conflictObserver = new ContentObserver(new Handler()) {
            @Override
            public void onChange(boolean selfChange) {
                super.onChange(selfChange);
            }

            @Override
            public void onChange(boolean selfChange, Uri uri) {
                super.onChange(selfChange, uri);
            }
        };

        getContentResolver().registerContentObserver(WebData.Object.CONFLICT_CONTENT_URI,
                false, conflictObserver);
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

    private class ContactRowAdapter extends SimpleCursorAdapter {
        private ContactRowAdapter(Context context, int layout,
                                  Cursor c, String[] from, int[] to, int flags)
        {
            super(context, layout, c, from, to, flags);
        }

        @Override
        public void bindView(View view, Context context, final Cursor cursor) {
            boolean inConflict = WebData.Object.inConflict(cursor);

            if (inConflict) {
                view.setBackgroundResource(R.color.conflicted);
            }

            super.bindView(view, context, cursor);
        }
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

        listAdapter = new ContactRowAdapter(
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
                if (WebData.Object.inConflict(listAdapter.getCursor())) {
                    resolveConflict(p);
                } else {
                    showDetails(p);
                }
            }
        });

        new SchemaManager(
                this,
                ContactContract.SCHEMA_ID,
                ((ContactsApplication) getApplication()).getUser(),
                this)
                .initSchema();
    }

    void resolveConflict(int pos) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "resolve conflicts");
        }

        Cursor c = (Cursor) listAdapter.getItem(pos);

        int di = c.getColumnIndex(WebData.Object.WD_DATA_ID);
        String dataID = c.getString(di);
        resolveConflict(dataID);
    }

    void resolveConflict(String uuid) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "resolve conflict");
        }

        Intent intent = new Intent();
        intent.setClass(this, ResolveContactActivity.class);

        Uri dataUri = WebData.Object.objectUri(ContactContract.SCHEMA_ID)
                .buildUpon().appendPath(uuid).build();

        if (null != dataUri) {
            intent.putExtra(ResolveContactActivity.OBJECT_KEY_URI, dataUri.toString());
        }

        startActivity(intent);
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

package com.enterpriseandroid.migratecontacts;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.*;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import net.migrate.api.SchemaManager;

public class ContactDetailActivity extends Activity
        implements LoaderManager.LoaderCallbacks<Cursor>,
        SchemaManager.SchemaLoaderListener
{
    public static final String KEY_URI = "ContactDetailActivity.CONTACT_URI";

    private static final String TAG = "DETAILS";

    private static final int LOADER_ID = 58;

    private static final String[] PROJ = new String[] {
            ContactContract.Columns._ID,
            ContactContract.Columns.FIRSTNAME,
            ContactContract.Columns.LASTNAME,
            ContactContract.Columns.PHONE_NUMBER,
            ContactContract.Columns.EMAIL,
    };

    static class UpdateContact extends AsyncTask<Uri, Void, Void> {
        private final ContentResolver resolver;
        private final ContentValues vals;

        public UpdateContact(ContentResolver resolver, ContentValues vals) {
            this.resolver = resolver;
            this.vals = vals;
        }

        @Override
        protected Void doInBackground(Uri... args) {
            Uri uri = args[0];
            if (null != uri) { resolver.update(uri, vals, null, null); }
            else { resolver.insert(ContactContract.OBJECT_CONTACT_URI, vals); }
            return null;
        }
    }

    static class DeleteContact extends AsyncTask<Uri, Void, Void> {
        private final ContentResolver resolver;

        public DeleteContact(ContentResolver resolver) {
            this.resolver = resolver;
        }

        @Override
        protected Void doInBackground(Uri... args) {
            resolver.delete(args[0], null, null);
            return null;
        }
    }

    private TextView fnameView;
    private String fname = "";
    private TextView lnameView;
    private String lname = "";
    private TextView phoneView;
    private String phone = "";
    private TextView emailView;
    private String email = "";

    private Uri contactUri;

    private Button updateButton;
    private Button deleteButton;

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

        updateButton.setEnabled(true);
        deleteButton.setEnabled(true);

        if (null != contactUri) {
            getLoaderManager().initLoader(LOADER_ID, null, this);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this, contactUri, PROJ, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        populateView(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) { }

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);

        if (null == state) {
            state = getIntent().getExtras();
        }
        String uri = null;
        if (null != state) {
            uri = state.getString(KEY_URI);
        }
        if (null != uri) {
            contactUri = Uri.parse(uri);
        }

        setContentView(R.layout.activity_contact_detail);

        fnameView = (TextView) findViewById(R.id.activity_detail_fname);
        lnameView = (TextView) findViewById(R.id.activity_detail_lname);
        phoneView = (TextView) findViewById(R.id.activity_detail_phone);
        emailView = (TextView) findViewById(R.id.activity_detail_email);

        updateButton = ((Button) findViewById(R.id.activity_detail_update));
        updateButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override public void onClick(View v) { update(); }
                });
        updateButton.setEnabled(false);

        deleteButton = ((Button) findViewById(R.id.activity_detail_delete));
        deleteButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override public void onClick(View v) { delete( ); }
                });
        deleteButton.setEnabled(false);

        new SchemaManager(
                this,
                ContactContract.SCHEMA_ID,
                ((ContactsApplication) getApplication()).getUser(),
                this)
                .initSchema();
    }

    @Override
    protected void onSaveInstanceState(Bundle state) {
        if (null != contactUri) {
            state.putString(KEY_URI, contactUri.toString());
        }
    }

    void delete() {
        if (null != contactUri) {
            new DeleteContact(getContentResolver()).execute(contactUri);
        }
        goToContacts();
    }

    void update() {
        if (TextUtils.isEmpty(fnameView.getText().toString())) {
            Toast.makeText(
                    this,
                    R.string.name_required,
                    Toast.LENGTH_SHORT)
                    .show();
            return;
        }

        ContentValues vals = new ContentValues();
        addString(fnameView, fname, vals, ContactContract.Columns.FIRSTNAME);
        addString(lnameView, lname, vals, ContactContract.Columns.LASTNAME);
        addString(phoneView, phone, vals, ContactContract.Columns.PHONE_NUMBER);
        addString(emailView, email, vals, ContactContract.Columns.EMAIL);

        new UpdateContact(getContentResolver(), vals).execute(contactUri);

        goToContacts();
    }

    private void populateView(Cursor c) {
        if (!c.moveToNext()) { return; }

        String s;
        s = getString(c, ContactContract.Columns.FIRSTNAME);
        fname = (TextUtils.isEmpty(s)) ? "" : s;
        fnameView.setText(fname);
        s = getString(c, ContactContract.Columns.LASTNAME);
        lname = (TextUtils.isEmpty(s)) ? "" : s;
        lnameView.setText(lname);
        s = getString(c, ContactContract.Columns.PHONE_NUMBER);
        phone = (TextUtils.isEmpty(s)) ? "" : s;
        phoneView.setText(phone);
        s = getString(c, ContactContract.Columns.EMAIL);
        email = (TextUtils.isEmpty(s)) ? "" : s;
        emailView.setText(email);
    }

    private void goToContacts() {
        Intent intent = new Intent(this, ContactsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);
    }

    private String getString(Cursor c, String col) {
        return c.getString(c.getColumnIndex(col));
    }

    private void addString(
            TextView view,
            String oldVal,
            ContentValues vals,
            String col)
    {
        String s = view.getText().toString();
        if (!oldVal.equals(s)) { vals.put(col, s); }
    }
}

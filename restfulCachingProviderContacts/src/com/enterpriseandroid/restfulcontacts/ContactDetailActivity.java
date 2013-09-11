package com.enterpriseandroid.restfulcontacts;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.app.LoaderManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import com.enterpriseandroid.restfulcontacts.data.ContactsContract;


public class ContactDetailActivity extends BaseActivity
    implements LoaderManager.LoaderCallbacks<Cursor>
{
    public static final String KEY_URI = "ContactDetailActivity.CONTACT_URI";

    private static final int LOADER_ID = 58;

    private static final String[] PROJ = new String[] {
        ContactsContract.Columns.ID,
        ContactsContract.Columns.FNAME,
        ContactsContract.Columns.LNAME,
        ContactsContract.Columns.PHONE,
        ContactsContract.Columns.EMAIL,
        ContactsContract.Columns.STATUS
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
            else { resolver.insert(ContactsContract.URI, vals); }
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

    private View statusView;
    private TextView fnameView;
    private String fname = "";
    private TextView lnameView;
    private String lname = "";
    private TextView phoneView;
    private String phone = "";
    private TextView emailView;
    private String email = "";
    private Uri contactUri;

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

        if (null == state) { state = getIntent().getExtras(); }
        String uri = null;
        if (null != state) { uri = state.getString(KEY_URI); }
        if (null != uri) {
            contactUri = Uri.parse(uri);
            getLoaderManager().initLoader(LOADER_ID, null, this);
        }

        setContentView(R.layout.activity_contact_details);

        statusView = findViewById(R.id.activity_detail_status);
        fnameView = (TextView) findViewById(R.id.activity_detail_fname);
        lnameView = (TextView) findViewById(R.id.activity_detail_lname);
        phoneView = (TextView) findViewById(R.id.activity_detail_phone);
        emailView = (TextView) findViewById(R.id.activity_detail_email);

        ((Button) findViewById(R.id.activity_detail_update)).setOnClickListener(
            new View.OnClickListener() {
                @Override public void onClick(View v) { update(); }
            });

        ((Button) findViewById(R.id.activity_detail_delete)).setOnClickListener(
            new View.OnClickListener() {
                @Override public void onClick(View v) { delete( ); }
            });
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
        String s = fnameView.getText().toString();
        if (TextUtils.isEmpty(s)) {
            Toast.makeText(
                this,
                R.string.name_required,
                Toast.LENGTH_SHORT)
                .show();
            return;
        }

        ContentValues vals = new ContentValues();
        addString(fnameView, fname, vals, ContactsContract.Columns.FNAME);
        addString(lnameView, lname, vals, ContactsContract.Columns.LNAME);
        addString(phoneView, phone, vals, ContactsContract.Columns.PHONE);
        addString(emailView, email, vals, ContactsContract.Columns.EMAIL);

        new UpdateContact(getContentResolver(), vals).execute(contactUri);

        goToContacts();
    }

    private void populateView(Cursor c) {
        if (!c.moveToNext()) { return; }

        setStatusBackground(
            c.getInt(c.getColumnIndex(ContactsContract.Columns.STATUS)),
            statusView);

        String s;
        s = getString(c, ContactsContract.Columns.FNAME);
        fname = (TextUtils.isEmpty(s)) ? "" : s;
        fnameView.setText(fname);
        s = getString(c, ContactsContract.Columns.LNAME);
        lname = (TextUtils.isEmpty(s)) ? "" : s;
        lnameView.setText(lname);
        s = getString(c, ContactsContract.Columns.PHONE);
        phone = (TextUtils.isEmpty(s)) ? "" : s;
        phoneView.setText(phone);
        s = getString(c, ContactsContract.Columns.EMAIL);
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

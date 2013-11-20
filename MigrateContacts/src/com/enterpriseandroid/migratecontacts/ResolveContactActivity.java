package com.enterpriseandroid.migratecontacts;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import net.migrate.api.WebData;

import java.util.ArrayList;
import java.util.List;

public class ResolveContactActivity extends Activity {
    private static final String TAG = "";

    public static final String OBJECT_KEY_URI = "object_uri";

    private Uri objectUri;
    private Uri conflictUri;

    private String fname;
    private String conflictFname;
    private TextView fnameView;
    private TextView conflictFnameView;
    private Spinner conflictFnameSpinner;

    private String lname;
    private String conflictLname;
    private TextView lnameView;
    private TextView conflictLnameView;
    private Spinner conflictLnameSpinner;

    private String phone;
    private String conflictPhone;
    private TextView phoneView;
    private TextView conflictPhoneView;
    private Spinner conflictPhoneSpinner;

    private String email;
    private String conflictEmail;
    private TextView emailView;
    private Spinner conflictEmailSpinner;

    public static final int CONTACT_LOADER_ID = 59;
    public static final int CONFLICT_LOADER_ID = 60;


    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);

        if (null == state) {
            state = getIntent().getExtras();
        }

        String uri = null;
        if (null != state) {
            uri = state.getString(OBJECT_KEY_URI);
        }

        if (null != uri) {
            objectUri = Uri.parse(uri);
            String schemaId = WebData.Schema.getSchemaId(objectUri);
            String dataID = WebData.Object.getDataID(objectUri);
            conflictUri = WebData.Object.conflictUri(schemaId);
        }

        setContentView(R.layout.activity_resolve_contact);

        fnameView = (TextView) findViewById(R.id.activity_resolve_fname);
        conflictFnameSpinner = (Spinner) findViewById(R.id.activity_resolve_fname_spinner);
        lnameView = (TextView) findViewById(R.id.activity_resolve_lname);
        conflictLnameSpinner = (Spinner) findViewById(R.id.activity_resolve_lname_spinner);
        phoneView = (TextView) findViewById(R.id.activity_resolve_phone);
        conflictPhoneSpinner = (Spinner) findViewById(R.id.activity_resolve_phone_spinner);
        emailView = (TextView) findViewById(R.id.activity_resolve_email);
        conflictEmailSpinner = (Spinner) findViewById(R.id.activity_resolve_email_spinner);

        if (null != conflictUri) {
            getLoaderManager().initLoader(CONTACT_LOADER_ID, null, new ContactLoader());
        }
    }

    private class ContactLoader implements LoaderManager.LoaderCallbacks<Cursor>
    {
        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            Log.d(TAG, "Loader for conflict data: " + objectUri);
            return new CursorLoader(
                    ResolveContactActivity.this,
                    objectUri,
                    null,
                    null,
                    null,
                    null);
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            saveContact(data);

            if (null != conflictUri) {
                getLoaderManager().initLoader(CONFLICT_LOADER_ID, null, new ConflictLoader());
            }
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
        }
    }

    private void saveContact(Cursor c) {
        if (!c.moveToNext()) {
            return;
        }

        String s;
        s = getString(c, ContactContract.Columns.FIRSTNAME);
        fname = (TextUtils.isEmpty(s)) ? "" : s;
        s = getString(c, ContactContract.Columns.LASTNAME);
        lname = (TextUtils.isEmpty(s)) ? "" : s;
        s = getString(c, ContactContract.Columns.PHONE_NUMBER);
        phone = (TextUtils.isEmpty(s)) ? "" : s;
        s = getString(c, ContactContract.Columns.EMAIL);
        email = (TextUtils.isEmpty(s)) ? "" : s;
    }

    private class ConflictLoader implements LoaderManager.LoaderCallbacks<Cursor>
    {
        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            Log.d(TAG, "Loader for object data: " + conflictUri);
            return new CursorLoader(
                    ResolveContactActivity.this,
                    conflictUri,
                    null,
                    null,
                    null,
                    null);
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            populateConflictView(data);
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
        }
    }

    private void populateConflictView(Cursor c) {
        if (!c.moveToNext()) { return; }

        saveConflict(c);

        populateConflictField(fname, conflictFname, fnameView, conflictFnameSpinner);
        populateConflictField(lname, conflictLname, lnameView, conflictLnameSpinner);
        populateConflictField(phone, conflictPhone, phoneView, conflictPhoneSpinner);
        populateConflictField(email, conflictEmail, emailView, conflictEmailSpinner);
    }

    private void populateConflictField(String value, String conflictValue,
                                       TextView textView, final Spinner spinner)
    {
        if ((value != null) && (conflictValue == null)) {
            textView.setVisibility(View.VISIBLE);
            spinner.setVisibility(View.GONE);
            textView.setText(value);
        } else if ((value == null) && (conflictValue != null)) {
            textView.setVisibility(View.VISIBLE);
            spinner.setVisibility(View.GONE);
            textView.setText(conflictValue);

        } else if ((null == value) /*&& (null == conflictFname)*/ ) {
            textView.setVisibility(View.VISIBLE);
            spinner.setVisibility(View.GONE);
            // nothing to set
        } else if (value.equals(conflictValue)) {
            textView.setVisibility(View.VISIBLE);
            spinner.setVisibility(View.GONE);

            // use either to set
            textView.setText(value);

        } else {
            textView.setVisibility(View.GONE);
            spinner.setVisibility(View.VISIBLE);

            ArrayAdapter<String> conflictAdapter =
                    new ArrayAdapter<String>(this, R.layout.spinner_text, new String[] {
                            value, conflictValue
                    });

            spinner.setAdapter(conflictAdapter);
        }
    }

    private void saveConflict(Cursor c) {
        String s;
        s = getString(c, ContactContract.Columns.FIRSTNAME);
        conflictFname = (TextUtils.isEmpty(s)) ? "" : s;
        s = getString(c, ContactContract.Columns.LASTNAME);
        conflictLname = (TextUtils.isEmpty(s)) ? "" : s;
        s = ResolveContactActivity.this.getString(c, ContactContract.Columns.PHONE_NUMBER);
        conflictPhone = (TextUtils.isEmpty(s)) ? "" : s;
        s = getString(c, ContactContract.Columns.EMAIL);
        conflictEmail = (TextUtils.isEmpty(s)) ? "" : s;
    }

    private String getString(Cursor c, String col) {
        return c.getString(c.getColumnIndex(col));
    }
}

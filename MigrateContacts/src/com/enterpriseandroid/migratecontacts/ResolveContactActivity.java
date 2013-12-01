package com.enterpriseandroid.migratecontacts;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.*;
import net.migrate.api.WebData;

public class ResolveContactActivity extends Activity {
    private static final String TAG = "";

    public static final String OBJECT_KEY_URI = "object_uri";

    private Uri contactUri;
    private Uri conflictUri;
    private Uri conflictDataUri;
    private Uri contactDataUri;

    private String fname;
    private String conflictFname;
    private TextView fnameView;
    private Spinner conflictFnameSpinner;
    private CheckBox conflictFnameCheckbox;

    private String lname;
    private String conflictLname;
    private TextView lnameView;
    private Spinner conflictLnameSpinner;
    private CheckBox conflictLnameCheckbox;

    private String phone;
    private String conflictPhone;
    private TextView phoneView;
    private Spinner conflictPhoneSpinner;
    private CheckBox conflictPhoneCheckbox;

    private String email;
    private String conflictEmail;
    private TextView emailView;
    private Spinner conflictEmailSpinner;

    private String resolvedFname;
    private String resolvedLname;
    private String resolvedPhone;
    private String resolvedEmail;

    private Button resolveButton;

    private String dataId;

    private int contactVersion;
    private int conflictVersion;

    public static final String[] OBJ_PROJ = new String[] {
            ContactContract.Columns._ID,
            WebData.Object.WD_DATA_ID,
            WebData.Object.WD_IN_CONFLICT,
            WebData.Object.WD_VERSION,
            ContactContract.Columns.FIRSTNAME,
            ContactContract.Columns.LASTNAME,
            ContactContract.Columns.PHONE_NUMBER,
            ContactContract.Columns.EMAIL,
    };

    public static final String[] CONFLICT_PROJ = new String[] {
            ContactContract.Columns._ID,
            WebData.Object.WD_DATA_ID,
            WebData.Object.WD_VERSION,
            ContactContract.Columns.FIRSTNAME,
            ContactContract.Columns.LASTNAME,
            ContactContract.Columns.PHONE_NUMBER,
            ContactContract.Columns.EMAIL,
    };

    public ResolveContactActivity() {
        super();
    }

    private CheckBox conflictEmailCheckbox;

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
            contactUri = Uri.parse(uri);
            String schemaId = WebData.Schema.getSchemaId(contactUri);
            conflictUri = WebData.Object.conflictUri(schemaId);
        }

        setContentView(R.layout.activity_resolve_contact);

        fnameView = (TextView) findViewById(R.id.activity_resolve_fname);
        conflictFnameSpinner = (Spinner) findViewById(R.id.activity_resolve_fname_spinner);
        conflictFnameCheckbox = (CheckBox) findViewById(R.id.activity_resolve_fname_checkbox);
        lnameView = (TextView) findViewById(R.id.activity_resolve_lname);
        conflictLnameSpinner = (Spinner) findViewById(R.id.activity_resolve_lname_spinner);
        conflictLnameCheckbox = (CheckBox) findViewById(R.id.activity_resolve_lname_checkbox);
        phoneView = (TextView) findViewById(R.id.activity_resolve_phone);
        conflictPhoneSpinner = (Spinner) findViewById(R.id.activity_resolve_phone_spinner);
        conflictPhoneCheckbox = (CheckBox) findViewById(R.id.activity_resolve_phone_checkbox);
        emailView = (TextView) findViewById(R.id.activity_resolve_email);
        conflictEmailSpinner = (Spinner) findViewById(R.id.activity_resolve_email_spinner);
        conflictEmailCheckbox = (CheckBox) findViewById(R.id.activity_resolve_email_checkbox);

        resolveButton = (Button) findViewById(R.id.activity_resolve_button);
        resolveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!(conflictFnameCheckbox.isChecked() &&
                        conflictLnameCheckbox.isChecked() &&
                        conflictPhoneCheckbox.isChecked() &&
                        conflictEmailCheckbox.isChecked()))
                {
                    Toast.makeText(ResolveContactActivity.this,
                            R.string.activity_resolve_need_all_fields, Toast.LENGTH_LONG).show();
                } else {
                    resolveConflict();
                }
            }
        });

        if (null != conflictUri) {
            getLoaderManager().initLoader(CONTACT_LOADER_ID, null, new ContactLoader());
        }
    }

    private void resolveConflict() {
        ContentValues resolvedValues = new ContentValues();

        resolvedValues.put(WebData.Object.WD_VERSION, conflictVersion);
        resolvedValues.put(ContactContract.Columns.FIRSTNAME, resolvedFname);
        resolvedValues.put(ContactContract.Columns.LASTNAME, resolvedLname);
        resolvedValues.put(ContactContract.Columns.PHONE_NUMBER, resolvedPhone);
        resolvedValues.put(ContactContract.Columns.EMAIL, resolvedEmail);

        ResolveConflictTask resolveTask = new ResolveConflictTask(resolvedValues);
        resolveTask.doInBackground();
    }

    private class ResolveConflictTask extends AsyncTask
    {
        private final ContentValues resolvedValues;

        ResolveConflictTask(ContentValues resolvedValues) {
            this.resolvedValues = resolvedValues;
        }

        @Override
        protected Object doInBackground(Object... params) {
            getContentResolver().update(conflictDataUri, resolvedValues, null, null);
            ResolveContactActivity.this.finish();
            return null;
        }
    }

    private class ContactLoader implements LoaderManager.LoaderCallbacks<Cursor>
    {
        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            Log.d(TAG, "Loader for conflict data: " + contactDataUri);
            return new CursorLoader(
                    ResolveContactActivity.this,
                    contactDataUri,
                    OBJ_PROJ,
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
        s = getString(c, WebData.Object.WD_DATA_ID);
        dataId = (TextUtils.isEmpty(s)) ? "" : s;

        contactDataUri = contactUri.buildUpon().appendPath(dataId).build();
        conflictDataUri = conflictUri.buildUpon().appendPath(dataId).build();

        s = getString(c, WebData.Object.WD_VERSION);
        contactVersion = Integer.parseInt((TextUtils.isEmpty(s)) ? "" : s);

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
                    CONFLICT_PROJ,
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

        resolvedFname = populateConflictField(fname, conflictFname, fnameView,
                conflictFnameSpinner, conflictFnameCheckbox);
        resolvedLname = populateConflictField(lname, conflictLname, lnameView,
                conflictLnameSpinner, conflictLnameCheckbox);
        resolvedPhone = populateConflictField(phone, conflictPhone, phoneView,
                conflictPhoneSpinner, conflictPhoneCheckbox);
        resolvedEmail = populateConflictField(email, conflictEmail, emailView,
                conflictEmailSpinner, conflictEmailCheckbox);
    }

    private String populateConflictField(String value, String conflictValue,
                                       TextView textView,
                                       final Spinner spinner,
                                       final CheckBox conflictCheckBox)
    {
        if ((value != null) && (conflictValue == null)) {
            setConflictField(value, conflictValue, textView, spinner, conflictCheckBox);
            return null;

        } else if ((value == null) && (conflictValue != null)) {
            setConflictField(value, conflictValue, textView, spinner, conflictCheckBox);
            return null;

        } else if ((null == value) /*&& (null == conflictFname)*/ ) {
            // both null
            setResolvedField(value, textView, spinner, conflictCheckBox);
            return null;

            // nothing to set
        } else if (value.equals(conflictValue)) {
            setResolvedField(value, textView, spinner, conflictCheckBox);
            return value;
        } else {
            setConflictField(value, conflictValue, textView, spinner, conflictCheckBox);
            return null;
        }
    }

    private void setConflictField(String value, String conflictValue, TextView textView,
                                  final Spinner spinner, final CheckBox conflictCheckBox)
    {
        textView.setVisibility(View.GONE);
        spinner.setVisibility(View.VISIBLE);
        conflictCheckBox.setEnabled(true);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // spinner seems to acquire color of selected - instead want color of resolved
                setSpinnerColor(spinner, conflictCheckBox);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        conflictCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                setSpinnerColor(spinner, conflictCheckBox);
            }
        });

        ArrayAdapter<String> conflictAdapter =
                new ArrayAdapter<String>(this, R.layout.spinner_conflict_text, new String[] {
                        value, conflictValue
                });

        spinner.setAdapter(conflictAdapter);
    }

    private void setResolvedField(String value, TextView textView, Spinner spinner, CheckBox conflictCheckBox) {
        textView.setVisibility(View.VISIBLE);
        spinner.setVisibility(View.GONE);
        conflictCheckBox.setChecked(true);
        conflictCheckBox.setEnabled(false);
        if (value != null) {
            textView.setText(value);
        }
    }

    private void setSpinnerColor(Spinner spinner, CheckBox conflictCheckBox) {
        TextView spinnerTextView = (TextView) spinner.findViewById(R.id.conflict_spinner_text_view);
        if (conflictCheckBox.isChecked()) {
            spinnerTextView.setTextColor(getResources().getColor(R.color.resolved));
        } else {
            spinnerTextView.setTextColor(getResources().getColor(R.color.conflicted));
        }
    }

    private void saveConflict(Cursor c) {
        String s;

        s = getString(c, WebData.Object.WD_VERSION);
        conflictVersion = Integer.parseInt((TextUtils.isEmpty(s)) ? "" : s);

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

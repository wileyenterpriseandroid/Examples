package com.enterpriseandroid.migrateclinic;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ListView;
import android.app.LoaderManager;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;

import com.enterpriseandroid.migrateclinic.data.PatientContract;
import com.enterpriseandroid.migrateclinic.data.SchemaManager;
import com.enterpriseandroid.migrateclinic.data.XRayContract;


public class PatientDetailActivity extends BaseActivity
    implements SchemaManager.SchemaLoaderListener
{
    public static final String PARAM_PATIENT = "PatientDetailActivity.PATIENT";

    private static final String TAG = "PATIENT";

    private static final int PATIENT_LOADER_ID = 56;
    private static final int XRAY_LOADER_ID = 58;

    static final String[] PROJ_PATIENT = new String[] {
        PatientContract.Columns.ID,
        PatientContract.Columns.SSN,
        PatientContract.Columns.FIRSTNAME,
        PatientContract.Columns.LASTNAME,
        PatientContract.Columns.INSURER,
    };

    static final String[] PROJ_XRAY = new String[] {
        XRayContract.Columns.ID,
        XRayContract.Columns.DESCRIPTION,
        XRayContract.Columns.NOTES
    };

    static final String[] FROM = new String[] {
        XRayContract.Columns.DESCRIPTION,
        XRayContract.Columns.NOTES
    };

    static final int[] TO = new int[] {
        R.id.row_xray_description,
        R.id.row_xray_notes
    };

    static class LoadPatient implements LoaderManager.LoaderCallbacks<Cursor> {
        private final PatientDetailActivity ctxt;

        public LoadPatient(PatientDetailActivity ctxt) {
            this.ctxt = ctxt;
        }

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            Uri uri =  (null == args) ? null : (Uri) args.getParcelable(PARAM_PATIENT);
            return (null == uri)
                    ? null
                    : new CursorLoader(ctxt, uri, PROJ_PATIENT, null, null, null);
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
            ctxt.populatePatient((!cursor.moveToNext()) ? null : cursor);
        }

        @Override
        public void onLoaderReset(Loader<Cursor> arg0) {
            ctxt.populatePatient(null);
        }
    }

    class UpdatePatient extends AsyncTask<Uri, Void, Uri> {
        private final ContentValues vals = new ContentValues();

        public UpdatePatient(ContentValues vals) {
            this.vals.putAll(vals);
        }

        @Override
        protected void onPreExecute() { task = this; }

        @Override
        protected Uri doInBackground(Uri... args) {
            Uri uri = args[0];
            ContentResolver resolver = getContentResolver();
            if (null != uri) { resolver.update(uri, vals, null, null); }
            else { uri = resolver.insert(PatientContract.OBJECT_PATIENT_URI, vals); }
            return uri;
        }

        @Override
        protected void onPostExecute(Uri uri) {
            setPatientId(ContentUris.parseId(uri));
            done();
        }

        @Override
        protected void onCancelled() { done(); }

        private void done() { task = null; }
    }

    static class DeletePatient extends AsyncTask<Uri, Void, Void> {
        private final ContentResolver resolver;

        public DeletePatient(ContentResolver resolver) {
            this.resolver = resolver;
        }

        @Override
        protected Void doInBackground(Uri... args) {
            resolver.delete(args[0], null, null);
            return null;
        }
    }

    static class LoadXRays implements LoaderManager.LoaderCallbacks<Cursor> {
        private final PatientDetailActivity ctxt;

        public LoadXRays(PatientDetailActivity ctxt) {
            this.ctxt = ctxt;
        }

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            String ssn =  (null == args) ? null : args.getString(PARAM_PATIENT);
            return (null == ssn)
                    ? null
                    : new CursorLoader(
                            ctxt,
                            XRayContract.OBJECT_XRAY_URI,
                            PROJ_XRAY,
                            XRayContract.Columns.SSN + "=?",
                            new String[] { ssn },
                            XRayContract.Columns.TIMESTAMP + " ASC");
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
            ctxt.populateXRays((!cursor.moveToNext()) ? null : cursor);
        }

        @Override
        public void onLoaderReset(Loader<Cursor> arg0) {
            ctxt.populateXRays(null);
        }
    }

    SimpleCursorAdapter listAdapter;
    AsyncTask<Uri, ?, ?> task;

    private long patientId;

    private TextView ssnView;
    private String ssn = "";
    private TextView fnameView;
    private String fname = "";
    private TextView lnameView;
    private String lname = "";
    private TextView insurerView;
    private String insurer = "";
    private ListView xrayView;

    private Button updatePatient;
    private Button deletePatient;
    private Button addXray;

    @Override
    public void onSchemaLoaded(String schema, boolean succeeded) {
        if (!succeeded) {
            Log.w(TAG, "Failed loading schema: " + schema);
            return;
        }

        if (XRayContract.SCHEMA_ID.equals(schema)) {
            if (!TextUtils.isEmpty(ssn)) {
                Bundle args = new Bundle();
                args.putString(PARAM_PATIENT, ssn);
                getLoaderManager().initLoader(XRAY_LOADER_ID, args, new LoadXRays(this));
            }
            return;
        }

        if (PatientContract.SCHEMA_ID.equals(schema)) {
            Uri uri = getPatientUri();
            if (null != uri) {
                Bundle args = new Bundle();
                args.putParcelable(PARAM_PATIENT, uri);
                getLoaderManager().initLoader(PATIENT_LOADER_ID, args, new LoadPatient(this));
            }
        }
    }

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);

        if (null == state) { state = getIntent().getExtras(); }
        if (null != state) { setPatientId(state.getInt(PARAM_PATIENT)); }

        setContentView(R.layout.activity_patient_details);

        ssnView = (TextView) findViewById(R.id.activity_detail_ssn);
        fnameView = (TextView) findViewById(R.id.activity_detail_fname);
        lnameView = (TextView) findViewById(R.id.activity_detail_lname);
        insurerView = (TextView) findViewById(R.id.activity_detail_insurer);

        xrayView = (ListView) findViewById(R.id.activity_detail_xrays);
        listAdapter = new SimpleCursorAdapter(this, R.layout.xray_row, null, FROM, TO, 0);

        xrayView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> l, View v, int p, long id) {
                showXRay(p);
            } });
        addXray = ((Button) findViewById(R.id.activity_detail_add_xray));
        addXray.setOnClickListener(
                new View.OnClickListener() {
                    @Override public void onClick(View v) { showXRay(-1); }
                });
        addXray.setEnabled(false);

        updatePatient = ((Button) findViewById(R.id.activity_detail_update));
        updatePatient.setOnClickListener(
                new View.OnClickListener() {
                    @Override public void onClick(View v) { update(); }
                });
        updatePatient.setEnabled(false);

        deletePatient = ((Button) findViewById(R.id.activity_detail_delete));
        deletePatient.setOnClickListener(
                new View.OnClickListener() {
                    @Override public void onClick(View v) { delete( ); }
                });
        deletePatient.setEnabled(false);

        ((ClinicApplication) getApplication()).initPatientDb(this, this);
    }

    @Override
    protected void onPause() {
         super.onPause();
         if (null != task) { task.cancel(true); }
    }

    @Override
    protected void onSaveInstanceState(Bundle state) {
        if (0 < patientId) { state.putLong(PARAM_PATIENT, patientId); }
    }

    void setPatientId(long id) { patientId = id; }

    void delete() {
        if (0 < patientId) {
            new DeletePatient(getContentResolver()).execute(getPatientUri());
        }
        goTo(PatientsActivity.class);
    }

    void update() {
        if (TextUtils.isEmpty(ssnView.getText().toString())) {
            Toast.makeText(
                    this,
                    R.string.ssn_required,
                    Toast.LENGTH_SHORT)
                    .show();
            return;
        }

        ContentValues vals = new ContentValues();
        addString(ssnView, ssn, vals, PatientContract.Columns.SSN);
        addString(fnameView, fname, vals, PatientContract.Columns.FIRSTNAME);
        addString(lnameView, lname, vals, PatientContract.Columns.LASTNAME);
        addString(insurerView, insurer, vals, PatientContract.Columns.INSURER);

        new UpdatePatient(vals).execute(getPatientUri());
    }

    void populateXRays(Cursor c) { listAdapter.swapCursor(c); }

    void populatePatient(Cursor c) {
        ssn = getString(c, PatientContract.Columns.SSN);
        ssnView.setText(ssn);
        fname = getString(c, PatientContract.Columns.FIRSTNAME);
        fnameView.setText(fname);
        lname = getString(c, PatientContract.Columns.LASTNAME);
        lnameView.setText(lname);
        insurer = getString(c, PatientContract.Columns.INSURER);
        insurerView.setText(insurer);

        updatePatient.setEnabled(true);
        deletePatient.setEnabled(0 >= patientId);
        addXray.setEnabled(0 >= patientId);

        ((ClinicApplication) getApplication()).initXrayDb(this, this);
    }

    void showXRay(int pos) {
        if (!((ClinicApplication) getApplication()).xRayDetailContractReady()) {
            Toast.makeText(this, R.string.retry_xray, Toast.LENGTH_LONG).show();
            return;
        }

        Intent intent = new Intent(this, XRayActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        if (0 < pos) {
            Cursor c = (Cursor) listAdapter.getItem(pos);
            long id = c.getLong(c.getColumnIndex(XRayContract.Columns.ID));
            if (0 < id) { intent.putExtra(XRayActivity.PARAM_XRAY, id); }
        }
        startActivity(intent);
    }

    private Uri getPatientUri() {
        return (0 >= patientId)
                ? null
                : PatientContract.OBJECT_PATIENT_URI.buildUpon()
                    .appendPath(String.valueOf(patientId)).build();
    }
}

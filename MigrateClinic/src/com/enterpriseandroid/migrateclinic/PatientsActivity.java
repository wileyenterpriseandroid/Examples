package com.enterpriseandroid.migrateclinic;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import com.enterpriseandroid.migrateclinic.data.PatientContract;
import com.enterpriseandroid.migrateclinic.data.SchemaManager;


public class PatientsActivity extends BaseActivity
implements LoaderManager.LoaderCallbacks<Cursor>,
SchemaManager.SchemaLoaderListener
{
    private static final String TAG = "PATIENTS";

    private static final int PATIENTS_LOADER_ID = 42;

    private static final String[] PROJ = new String[] {
        PatientContract.Columns.ID,
        PatientContract.Columns.SSN,
        PatientContract.Columns.FIRSTNAME,
        PatientContract.Columns.LASTNAME,
        PatientContract.Columns.INSURER,
    };

    private static final String[] FROM = new String[PROJ.length - 1];
    static { System.arraycopy(PROJ, 1, FROM, 0, FROM.length); }

    private static final int[] TO = new int[] {
        R.id.row_patients_ssn,
        R.id.row_patients_fname,
        R.id.row_patients_lname,
        R.id.row_patients_insurer,
    };


    SimpleCursorAdapter listAdapter;

    @Override
    public void onSchemaLoaded(String schema, boolean succeeded) {
        if (succeeded) {
            Log.w(TAG, "Failed loading schema: " + schema);
            return;
        }
        getLoaderManager().initLoader(PATIENTS_LOADER_ID, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(
                this,
                PatientContract.OBJECT_PATIENT_URI,
                PROJ,
                null,
                null,
                PatientContract.Columns.FIRSTNAME + " ASC");
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
        setContentView(R.layout.activity_patients);

        ((Button) findViewById(R.id.activity_patients_add)).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) { showDetails(-1); }
                });

        listAdapter = new SimpleCursorAdapter(this, R.layout.patient_row, null, FROM, TO, 0);

        ListView listView = ((ListView) findViewById(R.id.activity_patients_list));
        listView.setAdapter(listAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> l, View v, int p, long id) {
                showDetails(p);
            } });

        ((ClinicApplication) getApplication()).initPatientDb(this, this);
    }

    void showDetails(int pos) {
        Intent intent = new Intent(this, XRayActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        if (0 < pos) {
            Cursor c = (Cursor) listAdapter.getItem(pos);
            long id = c.getLong(c.getColumnIndex(PatientContract.Columns.ID));
            if (0 < id) { intent.putExtra(PatientDetailActivity.PARAM_PATIENT, id); }
        }
        startActivity(intent);
    }
}

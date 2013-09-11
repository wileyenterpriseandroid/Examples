package com.enterpriseandroid.migrateclinic;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import android.app.LoaderManager;
import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.enterpriseandroid.migrateclinic.data.XRayDetailsContract;
import com.enterpriseandroid.migrateclinic.data.SchemaManager;


public class XRayActivity extends BaseActivity
implements LoaderManager.LoaderCallbacks<Cursor>,
SchemaManager.SchemaLoaderListener
{
    public static final String PARAM_XRAY = "XRayActivity.XRAY";

    private static final String TAG = "PATIENTS";

    private static final int XRAY_DETAILS_LOADER_ID = 64;
    private static final int XRAY_LOADER_ID = 666;

    private static final String[] PROJ = new String[] {
        XRayDetailsContract.Columns.SSN,
        XRayDetailsContract.Columns.FIRSTNAME,
        XRayDetailsContract.Columns.LASTNAME,
        XRayDetailsContract.Columns.XRAY
    };

    private static class XRayLoader extends AsyncTaskLoader<Bitmap> {
        private volatile boolean loaded;
        private final Uri uri;

        public XRayLoader(Context context, Uri uri) {
            super(context);
            this.uri = uri;
        }

        @Override
        public Bitmap loadInBackground() {
            Bitmap xray = null;
            InputStream in = null;
            loaded = true;
            try {
                in = getContext().getContentResolver().openInputStream(uri);
                xray = BitmapFactory.decodeStream(in);
            }
            catch (FileNotFoundException e) { }
            finally {
                if (null != in) { try { in.close(); } catch (IOException e) { } }
            }

            return xray;
        }

        // see bug: http://code.google.com/p/android/issues/detail?id=14944
        @Override
        protected void onStartLoading() {
            if (!loaded) { forceLoad(); }
        }
    }

    class XRayLoaderCallbacks implements LoaderManager.LoaderCallbacks<Bitmap> {

        @Override
        public Loader<Bitmap> onCreateLoader(int id, Bundle args) {
            Uri uri = null;
            if (null != args) {
                String s = args.getString(PARAM_XRAY);
                if (null != s) { uri = Uri.parse(s); }
            }
            return (null == uri) ? null : new XRayLoader(XRayActivity.this, uri);
        }

        @Override
        public void onLoadFinished(Loader<Bitmap> loader, Bitmap bm) {
            populateXRay(bm);
        }

        @Override
        public void onLoaderReset(Loader<Bitmap> loader) {
            populateXRay(null);
        }
    }

    private TextView ssnView;
    private String ssn = "";
    private TextView fnameView;
    private String fname = "";
    private TextView lnameView;
    private String lname = "";
    private ImageView xrayView;
    private XRayDetailsContract contract;

    private long xrayId;

    @Override
    public void onSchemaLoaded(String schema, boolean succeeded) {
        if (succeeded) {
            Log.w(TAG, "Failed loading schema: " + schema);
            return;
        }
        getLoaderManager().initLoader(XRAY_DETAILS_LOADER_ID, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(
                this,
                contract.getObjectUri(),
                PROJ,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        populateXRayDetails((!cursor.moveToNext()) ? null : cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> arg0) {
        populateXRayDetails(null);
    }

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_xray);

        if (null == state) { state = getIntent().getExtras(); }
        if (null != state) { setXRayId(state.getInt(PARAM_XRAY)); }

        ssnView = (TextView) findViewById(R.id.activity_xray_ssn);
        fnameView = (TextView) findViewById(R.id.activity_xray_fname);
        lnameView = (TextView) findViewById(R.id.activity_xray_lname);

        xrayView = (ImageView) findViewById(R.id.activity_xray_xray);

        contract = ((ClinicApplication) getApplication()).initXRayDetailDb(this, this);
    }

    @Override
    protected void onSaveInstanceState(Bundle state) {
        if (0 < xrayId) { state.putLong(PARAM_XRAY, xrayId); }
    }

    void setXRayId(long id) { xrayId = id; }

    void populateXRayDetails(Cursor c) {
        ssn = getString(c, XRayDetailsContract.Columns.SSN);
        ssnView.setText(ssn);
        fname = getString(c, XRayDetailsContract.Columns.FIRSTNAME);
        fnameView.setText(fname);
        lname = getString(c, XRayDetailsContract.Columns.LASTNAME);
        lnameView.setText(lname);
        String uri = getString(c, XRayDetailsContract.Columns.XRAY);
        if (null != uri) {
            Bundle args = new Bundle();
            args.putString(PARAM_XRAY, uri);
            getLoaderManager().initLoader(XRAY_LOADER_ID, args, new XRayLoaderCallbacks());
        }
    }

    void populateXRay(Bitmap xray) { xrayView.setImageBitmap(xray); }
}

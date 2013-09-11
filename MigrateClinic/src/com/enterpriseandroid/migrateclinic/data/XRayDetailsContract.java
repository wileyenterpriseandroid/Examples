package com.enterpriseandroid.migrateclinic.data;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.BaseColumns;

public final class XRayDetailsContract {
    public static interface ContractListener { void onContractReady(XRayDetailsContract contract); }

    public static final class Columns {
        private Columns() {}

        public static final String ID = BaseColumns._ID;
        public static final String SSN = "ssn";
        public static final String FIRSTNAME = "firstname";
        public static final String LASTNAME = "lastname";
        public static final String XRAY = "xray";
    }

    private static final String SCHEMA_ID = XRayDetailsContract.class.getName();

    private static final String TABLE_PATIENTS = "patients";
    private static final String TABLE_XRAYS = "xrays";

    private static final String TABLES
        = new StringBuilder()
        .append(TABLE_PATIENTS).append(",").append(TABLE_XRAYS)
        .toString();

    private static final String PROJECTION
        = new StringBuilder()
            .append("rowid AS ").append(Columns.ID).append(",")
            .append(TABLE_PATIENTS).append(".").append(PatientContract.Columns.SSN)
                .append(" AS ").append(Columns.SSN).append(",")
            .append(TABLE_PATIENTS).append(".").append(PatientContract.Columns.FIRSTNAME)
                .append(" AS ").append(Columns.FIRSTNAME).append(",")
            .append(TABLE_PATIENTS).append(".").append(PatientContract.Columns.LASTNAME)
                .append(" AS ").append(Columns.LASTNAME).append(",")
            .append("@ ").append(TABLE_XRAYS).append(".").append(XRayContract.Columns.XRAY)
                .append(" AS ").append(Columns.XRAY)
            .toString();

    private static final String SELECTION
        = new StringBuilder()
        .append("@").append(PatientContract.SCHEMA_ID).append(" ").append(TABLE_PATIENTS)
        .append(" INNER JOIN ")
        .append("@").append(XRayContract.SCHEMA_ID).append(" ").append(TABLE_XRAYS)
        .append(" ON ").append(TABLE_PATIENTS).append(".").append(PatientContract.Columns.SSN)
        .append("=").append(TABLE_XRAYS).append(".").append(XRayContract.Columns.SSN)
        .toString();

    static class CreateContract extends AsyncTask<Void, Void, Uri> {
        private final ContentValues view = new ContentValues();
        private final ContractListener listener;
        private final ContentResolver resolver;

        public CreateContract(ContentResolver resolver, ContractListener listener, ContentValues view) {
            this.resolver = resolver;
            this.listener = listener;
            this.view.putAll(view);
        }

        @Override
        protected Uri doInBackground(Void... args) {
            Uri uri = null;
            try { uri = resolver.insert(XRayContract.SCHEMA_XRAY_URI, view); }
            catch (IllegalArgumentException e) { }
            return uri;
        }

        @Override
        protected void onPostExecute(Uri uri) {
            if (null != uri) { listener.onContractReady(new XRayDetailsContract(uri)); }
        }
    }

    public static void createContract(Context ctxt, ContractListener listener) {
        ContentValues view = new ContentValues();
        view.put("id", SCHEMA_ID);
        view.put("tables", TABLES);
        view.put("projection", PROJECTION);
        view.put("selection", SELECTION);
        new CreateContract(ctxt.getContentResolver(), listener, view).execute();
    }

    private final Uri objectUri;

    XRayDetailsContract(Uri uri) { objectUri = uri; }

    public String getSchemaId() { return SCHEMA_ID; }
    public Uri getSchemaUri() { return XRayContract.SCHEMA_XRAY_URI; }
    public Uri getObjectUri() { return objectUri; }
}

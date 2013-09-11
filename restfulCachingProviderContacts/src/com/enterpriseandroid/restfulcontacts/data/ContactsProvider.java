package com.enterpriseandroid.restfulcontacts.data;

import android.annotation.TargetApi;
import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import net.callmeike.android.data.util.ColumnMap;
import net.callmeike.android.data.util.ProjectionMap;

import com.enterpriseandroid.restfulcontacts.BuildConfig;
import com.enterpriseandroid.restfulcontacts.svc.RESTService;


public class ContactsProvider extends ContentProvider {
    private static final String TAG = "DB";

    public static final String PK_CONSTRAINT = ContactsHelper.COL_ID + "=";
    public static final String SYNC_CONSTRAINT = ContactsHelper.COL_SYNC + "=?";
    public static final String REMOTE_ID_CONSTRAINT = ContactsHelper.COL_REMOTE_ID + "=?";

    private static final ProjectionMap PROJ_MAP = new ProjectionMap.Builder()
        .addColumn(ContactsContract.Columns.ID, ContactsHelper.COL_ID)
        .addColumn(ContactsContract.Columns.FNAME, ContactsHelper.COL_FNAME)
        .addColumn(ContactsContract.Columns.LNAME, ContactsHelper.COL_LNAME)
        .addColumn(ContactsContract.Columns.PHONE, ContactsHelper.COL_PHONE)
        .addColumn(ContactsContract.Columns.EMAIL, ContactsHelper.COL_EMAIL)
        .addColumn(
            ContactsContract.Columns.STATUS,
            "CASE"
                + " WHEN " + ContactsHelper.COL_SYNC + " NOT NULL "
                    + " THEN " + ContactsContract.STATUS_SYNC
                + " WHEN " + ContactsHelper.COL_DIRTY + " NOT NULL "
                    + " THEN " + ContactsContract.STATUS_DIRTY
                + " ELSE " + ContactsContract.STATUS_OK + " END")
        .build();

    private static final ColumnMap COL_MAP = new ColumnMap.Builder()
        .addColumn(
            ContactsContract.Columns.ID,
            ContactsHelper.COL_ID,
            ColumnMap.Type.LONG)
        .addColumn(
            ContactsContract.Columns.FNAME,
            ContactsHelper.COL_FNAME,
            ColumnMap.Type.STRING)
        .addColumn(
            ContactsContract.Columns.LNAME,
            ContactsHelper.COL_LNAME,
            ColumnMap.Type.STRING)
        .addColumn(
            ContactsContract.Columns.PHONE,
            ContactsHelper.COL_PHONE,
            ColumnMap.Type.STRING)
        .addColumn(
            ContactsContract.Columns.EMAIL,
            ContactsHelper.COL_EMAIL,
            ColumnMap.Type.STRING)
        .addColumn(
            ContactsContract.Columns.REMOTE_ID,
            ContactsHelper.COL_REMOTE_ID,
            ColumnMap.Type.STRING)
        .addColumn(
            ContactsContract.Columns.DIRTY,
            ContactsHelper.COL_DIRTY,
            ColumnMap.Type.INTEGER)
        .addColumn(
            ContactsContract.Columns.SYNC,
            ContactsHelper.COL_SYNC,
            ColumnMap.Type.STRING)
        .build();

    private static final int CONTACTS_DIR = 1;
    private static final int CONTACTS_ITEM = 2;

    private static final UriMatcher uriMatcher;
    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(
            ContactsContract.AUTHORITY,
            ContactsContract.TABLE,
            CONTACTS_DIR);
        uriMatcher.addURI(
            ContactsContract.AUTHORITY,
            ContactsContract.TABLE + "/#",
            CONTACTS_ITEM);
    }

    private static final String[] PROJ_REM_ID = new String[] {
        ContactsHelper.COL_REMOTE_ID,
    };

    private static final Integer MARK = Integer.valueOf(1);


    private volatile ContactsHelper helper;

    @Override
    public boolean onCreate() {
        if (BuildConfig.DEBUG) { Log.d(TAG, "created"); }
        helper = new ContactsHelper(getContext());
        return null != helper;
    }

    @Override
    public String getType(Uri uri) {
        switch (uriMatcher.match(uri)) {
            case CONTACTS_DIR:
                return ContactsContract.CONTENT_TYPE_DIR;
            case CONTACTS_ITEM:
                return ContactsContract.CONTENT_TYPE_ITEM;
            default:
                return null;
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues vals) {
        if (BuildConfig.DEBUG) { Log.d(TAG, "insert@" + uri); }

        switch (uriMatcher.match(uri)) {
            case CONTACTS_DIR:
                break;

            default:
                throw new UnsupportedOperationException(
                    "Unrecognized URI: " + uri);
        }

        vals = COL_MAP.translateCols(vals);
        vals.put(ContactsHelper.COL_DIRTY, MARK);

        String xact = RESTService.insert(getContext(), vals);
        vals.put(ContactsHelper.COL_SYNC, xact);

        return localInsert(uri, vals);
    }

    @Override
    public int update(Uri uri, ContentValues vals, String sel, String[] sArgs) {
        if (BuildConfig.DEBUG) { Log.d(TAG, "update@" + uri); }

        boolean remoteSync = false;
        switch (uriMatcher.match(uri)) {
            case CONTACTS_DIR:
                // !!!
                // Allow transaction constrained update from the REST service.
                // Should be a separate virtual table...
                remoteSync = true;
                break;

            case CONTACTS_ITEM:
                sel = addPkConstraint(uri, sel);
                break;

            default:
                throw new UnsupportedOperationException(
                    "Unrecognized URI: " + uri);
        }

        vals = COL_MAP.translateCols(vals);

        if (!remoteSync) {
            vals.put(ContactsHelper.COL_DIRTY, MARK);
            String xact = RESTService.update(getContext(), getRemoteId(uri), vals);
            if (null != xact) { vals.put(ContactsHelper.COL_SYNC, xact); }
        }

        return localUpdate(uri, vals, sel, sArgs);
    }

    @Override
    public int delete(Uri uri, String sel, String[] sArgs) {
        if (BuildConfig.DEBUG) { Log.d(TAG, "delete@" + uri); }

        boolean remoteSync = false;
        switch (uriMatcher.match(uri)) {
            case CONTACTS_DIR:
                // !!!
                // Allow transaction constrained update from the REST service.
                // Should be a separate virtual table...
                remoteSync = true;
                break;

            case CONTACTS_ITEM:
                sel = addPkConstraint(uri, sel);
                break;

            default:
                throw new UnsupportedOperationException(
                    "Unrecognized URI: " + uri);
        }

        if (remoteSync) { return localDelete(uri, sel, sArgs); }

        ContentValues vals = new ContentValues();
        vals.put(ContactsHelper.COL_DELETED, MARK);
        vals.put(ContactsHelper.COL_DIRTY, MARK);

        String xact = RESTService.delete(getContext(), getRemoteId(uri));
        if (null != xact) { vals.put(ContactsHelper.COL_SYNC, xact); }

        return localUpdate(uri, vals, sel, sArgs);

    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    @SuppressWarnings("fallthrough")
    @Override
    public Cursor query(
        Uri uri,
        String[] proj,
        String sel,
        String[] selArgs,
        String ord)
    {
        if (BuildConfig.DEBUG) { Log.d(TAG, "query@" + uri); }

        long pk = -1;
        switch (uriMatcher.match(uri)) {
            case CONTACTS_ITEM:
                pk = ContentUris.parseId(uri);
            case CONTACTS_DIR:
                break;

            default:
                throw new IllegalArgumentException("Unrecognized URI: " + uri);
        }

        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            qb.setStrict(true);
        }

        qb.setProjectionMap(PROJ_MAP.getProjectionMap());

        qb.setTables(ContactsHelper.TAB_CONTACTS);

        qb.appendWhere("(" + ContactsHelper.COL_DELETED + " IS NULL)");
        if (0 <= pk) {
            qb.appendWhere(" AND (" + ContactsHelper.COL_ID + "=" + pk + ")");
        }

        Cursor cur = localQuery(qb, proj, sel, selArgs, ord);

        cur.setNotificationUri(getContext().getContentResolver(), uri);

        return cur;
    }

    public Uri localInsert(Uri uri, ContentValues vals) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "insert@" + uri + ": {" + vals + "}");
        }

        long pk = helper.getWritableDatabase().insert(
            ContactsHelper.TAB_CONTACTS,
            ContactsContract.Columns.FNAME,
            vals);

        if (0 > pk) { uri = null; }
        else {
            uri = uri.buildUpon().appendPath(String.valueOf(pk)).build();
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return uri;
    }

    public int localUpdate(
        Uri uri,
        ContentValues vals,
        String sel,
        String[] sArgs)
    {
        if (BuildConfig.DEBUG) {
            StringBuilder buf = new StringBuilder(sel);
            if (null != sArgs) {
                for (String s: sArgs) { buf.append(",").append(s); }
            }
            Log.d(
                TAG,
                "update@" + uri + "(" + buf.toString() +"): {" + vals + "}");
        }

        int updated = helper.getWritableDatabase().update(
            ContactsHelper.TAB_CONTACTS,
            vals,
            sel,
            sArgs);

        if (0 < updated) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return updated;
    }

    public int localDelete(Uri uri, String sel, String[] sArgs) {
        if (BuildConfig.DEBUG) {
            StringBuilder buf = new StringBuilder(sel);
            if (null != sArgs) {
                for (String s: sArgs) { buf.append(",").append(s); }
            }
            Log.d(TAG, "delete@" + uri + "(" + buf.toString() +")");
        }

        int updated = helper.getWritableDatabase().delete(
            ContactsHelper.TAB_CONTACTS,
            sel,
            sArgs);

        if (0 < updated) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return updated;
    }

    public Cursor localQuery(
        SQLiteQueryBuilder qb,
        String[] proj,
        String sel,
        String[] selArgs,
        String ord)
    {
        return qb.query(
            helper.getWritableDatabase(),
            proj,
            sel,
            selArgs,
            null,
            null,
            ord);
    }

    // !!! Race condition
    // If you try to delete or update something that has not yet
    // been synched with the server, things may happen out of order.
    // Things can get quite weird.
    // Client side only!  Not for use on server URIs!
    private String getRemoteId(Uri uri) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(ContactsHelper.TAB_CONTACTS);
        qb.appendWhere(PK_CONSTRAINT + ContentUris.parseId(uri));
        Cursor c = localQuery(qb, PROJ_REM_ID, null, null, null);
        try {
            if (1 != c.getCount()) { return null; }
            c.moveToFirst();
            return c.getString(c.getColumnIndex(ContactsHelper.COL_REMOTE_ID));
        }
        finally {
            c.close();
        }
    }

    private String addPkConstraint(Uri uri, String sel) {
        String pkConstraint = PK_CONSTRAINT + ContentUris.parseId(uri);
        sel = (null == sel)
            ? pkConstraint
            : "(" + pkConstraint + ") AND (" + sel + ")";
        return sel;
    }
}

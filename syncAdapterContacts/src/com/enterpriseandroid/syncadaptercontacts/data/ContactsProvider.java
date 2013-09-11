package com.enterpriseandroid.syncadaptercontacts.data;

import java.util.UUID;

import android.annotation.TargetApi;
import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import net.callmeike.android.data.util.ColumnMap;
import net.callmeike.android.data.util.ProjectionMap;

import com.enterpriseandroid.syncadaptercontacts.BuildConfig;


public class ContactsProvider extends ContentProvider {
    private static final String TAG = "DB";

    public static final String SYNC_UPDATE = "sync_update";

    public static final String DIRTY_CONSTRAINT = ContactsHelper.COL_DIRTY + " IS NOT NULL";
    public static final String REM_ID_CONSTRAINT = ContactsHelper.COL_REMOTE_ID + "=?";
    public static final String SYNC_CONSTRAINT = ContactsHelper.COL_SYNC + "=?";

    private static final String PK_CONSTRAINT = ContactsHelper.COL_ID + "=";
    private static final String NOT_DELETED_CONSTRAINT = "(" + ContactsHelper.COL_DELETED + " IS NULL)";

    private static final ProjectionMap PROJ_MAP = new ProjectionMap.Builder()
        .addColumn(ContactsContract.Columns.ID, ContactsHelper.COL_ID)
        .addColumn(ContactsContract.Columns.FNAME, ContactsHelper.COL_FNAME)
        .addColumn(ContactsContract.Columns.LNAME, ContactsHelper.COL_LNAME)
        .addColumn(ContactsContract.Columns.PHONE, ContactsHelper.COL_PHONE)
        .addColumn(ContactsContract.Columns.EMAIL, ContactsHelper.COL_EMAIL)
        .addColumn(ContactsContract.Columns.REMOTE_ID, ContactsHelper.COL_REMOTE_ID)
        .addColumn(ContactsContract.Columns.VERSION, ContactsHelper.COL_VERSION)
        .addColumn(ContactsContract.Columns.DELETED, ContactsHelper.COL_DELETED)
        .addColumn(ContactsContract.Columns.DIRTY, ContactsHelper.COL_DIRTY)
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
            ContactsContract.Columns.VERSION,
            ContactsHelper.COL_VERSION,
            ColumnMap.Type.LONG)
        .addColumn(
            ContactsContract.Columns.DELETED,
            ContactsHelper.COL_DELETED,
            ColumnMap.Type.INTEGER)
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


    private volatile ContactsHelper helper;

    @Override
    public boolean onCreate() {
        if (BuildConfig.DEBUG) { Log.d(TAG, "created"); }
        this.helper = new ContactsHelper(getContext());
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
        if (BuildConfig.DEBUG) { Log.d(TAG, "insert @" + uri); }

        switch (uriMatcher.match(uri)) {
            case CONTACTS_DIR:
                break;

            default:
                throw new UnsupportedOperationException(
                    "Unrecognized URI: " + uri);
        }

        return localInsert(uri, COL_MAP.translateCols(vals), isSyncUpdate(uri));
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] vals) {
        if (BuildConfig.DEBUG) { Log.d(TAG, "bulk insert @" + uri); }

        switch (uriMatcher.match(uri)) {
            case CONTACTS_DIR:
                break;

            default:
                throw new UnsupportedOperationException(
                    "Unrecognized URI: " + uri);
        }

        int count = 0;
        SQLiteDatabase db = getDb();
        try {
            db.beginTransaction();
            for (ContentValues row: vals) {
                if (0 <= localInsertRow(db, row)) { count++; }
            }
            db.setTransactionSuccessful();
        }
        finally {
            db.endTransaction();
        }

        if (0 < count) {
            getContext().getContentResolver().notifyChange(uri, null, !isSyncUpdate(uri));
        }

        return count;
    }

    @Override
    public int update(Uri uri, ContentValues vals, String sel, String[] sArgs) {
        if (BuildConfig.DEBUG) { Log.d(TAG, "update @" + uri); }

        switch (uriMatcher.match(uri)) {
            case CONTACTS_DIR:
                break;
            case CONTACTS_ITEM:
                sel = addPkConstraint(uri, sel);
                break;

            default:
                throw new UnsupportedOperationException(
                    "Unrecognized URI: " + uri);
        }

        vals = COL_MAP.translateCols(vals);

        boolean isSync = isSyncUpdate(uri);
        if (!isSync) { vals.putNull(ContactsHelper.COL_DELETED); }

        return localUpdate(uri, vals, sel, sArgs, isSync);
    }

    @Override
    public int delete(Uri uri, String sel, String[] sArgs) {
        if (BuildConfig.DEBUG) { Log.d(TAG, "delete @" + uri); }

        switch (uriMatcher.match(uri)) {
            case CONTACTS_DIR:
                break;
            case CONTACTS_ITEM:
                sel = addPkConstraint(uri, sel);
                break;

            default:
                throw new UnsupportedOperationException(
                    "Unrecognized URI: " + uri);
        }

        int deleted = 0;
        if (isSyncUpdate(uri)) {
            deleted = localDelete(uri, sel, sArgs, true);
        }
        else {
            ContentValues vals = new ContentValues();
            vals.put(ContactsHelper.COL_DELETED, ContactsHelper.MARKED);
            deleted = localUpdate(uri, vals, sel, sArgs, false);
        }

        return deleted;
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
        if (BuildConfig.DEBUG) { Log.d(TAG, "query @" + uri); }

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

        boolean andNeeded = false;
        if (0 <= pk) {
            qb.appendWhere("(" + PK_CONSTRAINT + pk + ")");
            andNeeded = true;
        }
        if (!isSyncUpdate(uri)) {
            if (andNeeded) { qb.appendWhere(" AND "); }
            qb.appendWhere(NOT_DELETED_CONSTRAINT);
        }

        Cursor cur = qb.query(getDb(), proj, sel, selArgs, null, null, ord);

        cur.setNotificationUri(getContext().getContentResolver(), uri);

        return cur;
    }

    private Uri localInsert(Uri uri, ContentValues vals, boolean isSync) {
        if (BuildConfig.DEBUG) { Log.d(TAG, "local insert @" + uri + ": {" + vals + "}"); }

        long pk = localInsertRow(getDb(), vals);

        if (0 > pk) { uri = null; }
        else {
            uri = uri.buildUpon().appendPath(String.valueOf(pk)).build();
            getContext().getContentResolver().notifyChange(uri, null, !isSync);
        }

        return uri;
    }

    private long localInsertRow(SQLiteDatabase db, ContentValues vals) {
        if (0 >= vals.size() ) { return -1; }

        // create a remote id if there isn't one
        if (null == vals.getAsString(ContactsHelper.COL_REMOTE_ID)) {
            vals.put(ContactsHelper.COL_REMOTE_ID, UUID.randomUUID().toString());
        }

        return db.insert(
            ContactsHelper.TAB_CONTACTS,
            ContactsContract.Columns.FNAME,
            vals);
    }

    private int localUpdate(
        Uri uri,
        ContentValues vals,
        String sel,
        String[] sArgs,
        boolean isSync)
    {
        if (BuildConfig.DEBUG) {
            StringBuilder buf = new StringBuilder(sel);
            if (null != sArgs) {
                for (String s: sArgs) { buf.append(",").append(s); }
            }
            Log.d(TAG, "local update @" + uri + " (" + buf.toString() +"): {" + vals + "}");
        }

        if (0 >= vals.size() ) { return 0; }

        if (!isSync) {
            vals.put(ContactsHelper.COL_DIRTY, ContactsHelper.MARKED);
        }

        int updated = getDb().update(
            ContactsHelper.TAB_CONTACTS,
            vals,
            sel,
            sArgs);

        if (0 < updated) {
            getContext().getContentResolver().notifyChange(uri, null, !isSync);
        }

        return updated;
    }

    private int localDelete(Uri uri, String sel, String[] sArgs, boolean isSync) {
        if (BuildConfig.DEBUG) {
            StringBuilder buf = new StringBuilder(sel);
            if (null != sArgs) {
                for (String s: sArgs) { buf.append(",").append(s); }
            }
            Log.d(TAG, "local delete @" + uri + " (" + buf.toString() +")");
        }

        int updated = getDb().delete(
            ContactsHelper.TAB_CONTACTS,
            sel,
            sArgs);

        // not clear this is necessary.
        // The record was already marked deleted (so invisible to queries)
        if (0 < updated) {
            getContext().getContentResolver().notifyChange(uri, null, !isSync);
        }

        return updated;
    }

    private String addPkConstraint(Uri uri, String sel) {
        String pkConstraint = PK_CONSTRAINT + ContentUris.parseId(uri);
        sel = (null == sel)
            ? pkConstraint
            : "(" + pkConstraint + ") AND (" + sel + ")";
        return sel;
    }

    // We need to know if it is the sync adapter is updating the data.
    // Return true if the write to content provider is coming from sync adapter.
    private boolean isSyncUpdate(Uri uri) {
        return null != uri.getQueryParameter(ContactsProvider.SYNC_UPDATE);
    }


    private SQLiteDatabase getDb() { return helper.getWritableDatabase(); }

}

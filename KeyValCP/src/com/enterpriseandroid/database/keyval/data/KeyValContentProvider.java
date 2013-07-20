package com.enterpriseandroid.database.keyval.data;

import java.io.FileNotFoundException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import android.annotation.TargetApi;
import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.util.Log;


public class KeyValContentProvider extends ContentProvider {
    private static final String TAG = "DB";

    private static final Map<String, ColumnDef> WRITE_COL_MAP;
    static {
        Map<String, ColumnDef> m = new HashMap<String, ColumnDef>();
        m.put(
            KeyValContract.Columns.KEY,
            new ColumnDef(KeyValHelper.COL_KEY, ColumnDef.Type.STRING));
        m.put(
            KeyValContract.Columns.VAL,
            new ColumnDef(KeyValHelper.COL_VAL, ColumnDef.Type.STRING));
        WRITE_COL_MAP = Collections.unmodifiableMap(m);
    }

    private static final String VALS_VIRTUAL_TABLE = KeyValHelper.TAB_VALS;

    private static final String VALS_PK_CONSTRAINT = KeyValHelper.COL_ID + "=";

    private static final Map<String, String> VAL_COL_AS_MAP;
    static {
        Map<String, String> m = new HashMap<String, String>();
        m.put(KeyValContract.Columns.ID,
            KeyValHelper.COL_ID + " AS " + KeyValContract.Columns.ID);
        m.put(KeyValContract.Columns.VAL,
            KeyValHelper.COL_VAL + " AS " + KeyValContract.Columns.VAL);
        m.put(KeyValContract.Columns.EXTRA,
            "CASE WHEN " + KeyValHelper.COL_EXTRA
                + " NOT NULL THEN " + KeyValHelper.COL_ID
                + " ELSE NULL END AS " + KeyValContract.Columns.EXTRA);
        // exposes the "_data" column!
        // necessary for the openFileHelper
        m.put(KeyValHelper.COL_EXTRA, KeyValHelper.COL_EXTRA);
        VAL_COL_AS_MAP = Collections.unmodifiableMap(m);
    }

    private static final String KEYVALS_VIRTUAL_TABLE
        = KeyValHelper.TAB_KEYS
            + " INNER JOIN " + KeyValHelper.TAB_VALS
            + " ON(" + KeyValHelper.COL_FK + "=" + KeyValHelper.COL_ID + ")";

    private static final String KEYVALS_PK_CONSTRAINT
        = KeyValHelper.TAB_KEYS + "." + KeyValHelper.COL_ROWID + "=";

    private static final Map<String, String> KEY_VAL_COL_AS_MAP;
    static {
        Map<String, String> m = new HashMap<String, String>();
        m.put(KeyValContract.Columns.ID,
            KeyValHelper.TAB_KEYS + "." + KeyValHelper.COL_ROWID
                + " AS " + KeyValContract.Columns.ID);
        m.put(KeyValContract.Columns.KEY,
            KeyValHelper.COL_KEY + " AS " + KeyValContract.Columns.KEY);
        m.put(KeyValContract.Columns.VAL,
            KeyValHelper.COL_VAL + " AS " + KeyValContract.Columns.VAL);
        m.put(KeyValContract.Columns.EXTRA,
            "CASE WHEN " + KeyValHelper.COL_EXTRA
                + " NOT NULL THEN " + KeyValHelper.COL_ID
                + " ELSE NULL END AS " + KeyValContract.Columns.EXTRA);
        KEY_VAL_COL_AS_MAP = Collections.unmodifiableMap(m);
    }

    private static final int STATUS_VAL_DIR = 1;
    private static final int STATUS_VAL_ITEM = 2;
    private static final int STATUS_KEYVAL_DIR = 3;
    private static final int STATUS_KEYVAL_ITEM = 4;

    private static final UriMatcher uriMatcher;
    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(
            KeyValContract.AUTHORITY,
            KeyValContract.TABLE_VALS,
            STATUS_VAL_DIR);
        uriMatcher.addURI(
            KeyValContract.AUTHORITY,
            KeyValContract.TABLE_VALS + "/#",
            STATUS_VAL_ITEM);
        uriMatcher.addURI(
            KeyValContract.AUTHORITY,
            KeyValContract.TABLE_KEYVAL,
            STATUS_KEYVAL_DIR);
        uriMatcher.addURI(
            KeyValContract.AUTHORITY,
            KeyValContract.TABLE_KEYVAL + "/#",
            STATUS_KEYVAL_ITEM);
    }

    private volatile KeyValHelper helper;

    @Override
    public boolean onCreate() {
        helper = new KeyValHelper(getContext());
        return null != helper;
    }

    @Override
    public String getType(Uri uri) {
        switch (uriMatcher.match(uri)) {
            case STATUS_VAL_DIR:
                return KeyValContract.TYPE_VALS;
            case STATUS_VAL_ITEM:
                return KeyValContract.TYPE_VAL;
            case STATUS_KEYVAL_DIR:
                return KeyValContract.TYPE_KEYVALS;
            case STATUS_KEYVAL_ITEM:
                return KeyValContract.TYPE_KEYVAL;
            default:
                return null;
        }
    }

    @Override
    public int update(Uri uri, ContentValues cv, String sel, String[] sArgs) {
        throw new UnsupportedOperationException("Update not supported");
    }

    @Override
    public int delete(Uri uri, String sel, String[] sArgs) {
        throw new UnsupportedOperationException("Delete not supported");
    }

    @Override
    public ParcelFileDescriptor openFile(Uri uri, String mode)
        throws FileNotFoundException
    {
        switch (uriMatcher.match(uri)) {
            case STATUS_VAL_ITEM:
                if (!"r".equals(mode)) {
                    throw new SecurityException("Write access forbidden");
                }
                return readExtras(uri);

            default:
                throw new UnsupportedOperationException(
                    "Unrecognized URI: " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues vals) {
        long pk;
        switch (uriMatcher.match(uri)) {
            case STATUS_KEYVAL_DIR:
                pk = insertKeyVal(vals);
                break;

            default:
                throw new UnsupportedOperationException(
                    "Unrecognized URI: " + uri);
        }

        if (0 > pk) { uri = null; }
        else {
            uri = uri.buildUpon().appendPath(String.valueOf(pk)).build();
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return uri;
    }

    @SuppressWarnings("fallthrough")
    @Override
    public Cursor query(
        Uri uri,
        String[] proj,
        String sel,
        String[] selArgs,
        String ord)
    {
        Cursor cur;

        long pk = -1;
        switch (uriMatcher.match(uri)) {
            case STATUS_VAL_ITEM:
                pk = ContentUris.parseId(uri);
            case STATUS_VAL_DIR:
                cur = queryVals(proj, sel, selArgs, ord, pk);
                break;

            case STATUS_KEYVAL_ITEM:
                pk = ContentUris.parseId(uri);
            case STATUS_KEYVAL_DIR:
                cur = queryKeyVals(proj, sel, selArgs, ord, pk);
                break;

            default:
                throw new IllegalArgumentException("Unrecognized URI: " + uri);
        }

        cur.setNotificationUri(getContext().getContentResolver(), uri);

        return cur;
    }

    private ParcelFileDescriptor readExtras(Uri uri)
        throws FileNotFoundException
    {
        return openFileHelper(uri, "r");
    }

    private long insertKeyVal(ContentValues vals) {
        long pk = -1;
        SQLiteDatabase db = helper.getWritableDatabase();
        try {
            db.beginTransaction();
            long id = helper.insertVal(
                db,
                vals.getAsString(KeyValContract.Columns.VAL));
            pk = helper.insertKey(
                db,
                vals.getAsString(KeyValContract.Columns.KEY),
                id);
            db.setTransactionSuccessful();
        }
        catch (SQLException e) { Log.w(TAG, "insert failed:", e); }
        finally { db.endTransaction(); }

        return pk;
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private Cursor queryVals(
        String[] proj,
        String sel,
        String[] selArgs,
        String ord,
        long pk)
    {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            qb.setStrict(true);
        }

        qb.setProjectionMap(VAL_COL_AS_MAP);

        qb.setTables(VALS_VIRTUAL_TABLE);

        if (0 <= pk) { qb.appendWhere(VALS_PK_CONSTRAINT + pk); }

        return qb.query(
            helper.getWritableDatabase(),
            proj,
            sel,
            selArgs,
            null,
            null,
            ord);
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private Cursor queryKeyVals(
        String[] proj,
        String sel,
        String[] selArgs,
        String ord,
        long pk)
    {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            qb.setStrict(true);
        }

        qb.setProjectionMap(KEY_VAL_COL_AS_MAP);

        qb.setTables(KEYVALS_VIRTUAL_TABLE);

        if (0 <= pk) { qb.appendWhere(KEYVALS_PK_CONSTRAINT + pk); }

        return qb.query(
            helper.getWritableDatabase(),
            proj,
            sel,
            selArgs,
            null,
            null,
            ord);
    }

    // Unused example of virtual table mapping
    @SuppressWarnings("unused")
    private ContentValues translateCols(ContentValues vals) {
        ContentValues newVals = new ContentValues();
        for (String colName: vals.keySet()) {
            ColumnDef colDef = WRITE_COL_MAP.get(colName);
            if (null == colDef) {
                throw new IllegalArgumentException(
                    "Unrecognized column: " + colName);
            }
            colDef.copy(colName, vals, newVals);
         }

        return newVals;
    }
}

package com.enterpriseandroid.database.keyval.simple.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.BaseColumns;

public class KeyValContentProvider extends ContentProvider {

    private KeyValHelper helper;

    @Override
    public boolean onCreate() {
        helper = new KeyValHelper(getContext());
        return false;
    }

    @Override
    public String getType(Uri arg0) { return KeyValContract.TYPE_KEYVAL; }

    @Override
    public Cursor query(
        Uri uri,
        String[] proj,
        String sel,
        String[] selArgs,
        String ord)
    {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(
            KeyValHelper.TAB_KEYS + " k, "
                + KeyValHelper.TAB_VALS + " v");
        Cursor c = qb.query(
            helper.getWritableDatabase(),
            new String[] {
                "k.rowid as " + BaseColumns._ID,
                KeyValHelper.COL_KEY + " as " + KeyValContract.Columns.KEY,
                KeyValHelper.COL_VAL + " as " + KeyValContract.Columns.VAL
            },
            "fk = id",
            null,
            null,
            null,
            KeyValContract.Columns.KEY + " asc");
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    }

    @Override
    public int delete(Uri uri, String sel, String[] sArgs) {
        throw new UnsupportedOperationException("Delete not supported");
    }

    @Override
    public Uri insert(Uri uri, ContentValues vals) {
        throw new UnsupportedOperationException("Insert not supported");
    }

    @Override
    public int update(Uri uri, ContentValues cv, String sel, String[] sArgs) {
        throw new UnsupportedOperationException("Update not supported");
    }
}

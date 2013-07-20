package com.enterpriseandroid.database.keyval.data;

import java.io.IOException;
import java.io.OutputStreamWriter;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.enterpriseandroid.database.keyval.R;


public class KeyValHelper extends SQLiteOpenHelper {
    static final int VERSION = 3;

    static final String DB_FILE = "keyval.db";

    static final String TAB_KEYS = "keys";
    static final String COL_ROWID = "rowid";
    static final String COL_KEY = "key";
    static final String COL_FK = "fk";
    static final String TAB_VALS = "vals";
    static final String COL_ID = "id";
    static final String COL_VAL = "val";
    static final String COL_EXTRA = "_data";
    static final String[] MAX_ID =  { "max(" + COL_ID + ")" };

    private final Context context;

    public KeyValHelper(Context context) {
        super(context, DB_FILE, null, VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
            "CREATE TABLE " + TAB_VALS + "("
                + COL_ID + " integer PRIMARY KEY AUTOINCREMENT,"
                + COL_VAL +  " text,"
                + COL_EXTRA +  " text)");

        db.execSQL(
            "CREATE TABLE " + TAB_KEYS + "("
                + COL_KEY + " text UNIQUE, "
                + COL_FK + " integer REFERENCES "
                    + TAB_VALS + "(" + COL_ID + "))");

        long id = insertVal(db, "bar");
        insertKey(db, "one", id);
        insertKey(db, "blue", id);
        insertExtra(db, id, "bar-extra");

        id = insertVal(db, "baz");
        insertKey(db, "two", id);
        id = insertVal(db, "zqx3");
        insertKey(db, "red", id);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        try { db.execSQL("drop table " + TAB_VALS); }
        catch (SQLiteException e) { }
        try { db.execSQL("drop table " + TAB_KEYS); }
        catch (SQLiteException e) { }
        onCreate(db);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        db.execSQL("pragma foreign_keys = true");
    }

    long insertVal(SQLiteDatabase db, String val) {
        ContentValues r = new ContentValues();
        r.put(COL_VAL, val);
        long id = db.insertWithOnConflict(
            TAB_VALS,
            null,
            r,
            SQLiteDatabase.CONFLICT_IGNORE);
        return id;
    }

    long insertKey(SQLiteDatabase db, String key, long fk) {
        ContentValues r = new ContentValues();
        r.put(COL_KEY, key);
        r.put(COL_FK, Long.valueOf(fk));
        return db.insertOrThrow(TAB_KEYS, null, r);
    }

    void insertExtra(SQLiteDatabase db, long val, String extra) {
        extra += ".txt";

        ContentValues r = new ContentValues();
        r.put(COL_EXTRA, context.getFilesDir() + "/" + extra);
        db.update(
            TAB_VALS,
            r,
            COL_ID + "=?",
            new String[] { String.valueOf(val) });

        OutputStreamWriter out = null;
        try {
            out = new OutputStreamWriter(context.openFileOutput(extra, Context.MODE_PRIVATE));
            out.write(context.getString(R.string.lorem_ipsum));
        }
        catch (IOException e) {
            Log.w("EXTRA", "Failed writing file: " + extra, e);
        }
        finally {
            if (null != out) { try { out.close(); } catch (IOException e) { } }
        }
    }
}

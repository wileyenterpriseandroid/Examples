package com.enterpriseandroid.database.keyval.simple.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

public class KeyValHelper extends SQLiteOpenHelper {
    static final int VERSION = 1;

    static final String DB_FILE = "keyval.db";

    static final String TAB_KEYS = "keys";
    static final String COL_KEY = "key";
    static final String COL_FK = "fk";
    static final String TAB_VALS = "vals";
    static final String COL_ID = "id";
    static final String COL_VAL = "val";

    public KeyValHelper(Context context) {
        super(context, DB_FILE, null, VERSION);
    }

//    @Override
//    public void onCreate(SQLiteDatabase db) {
//        db.execSQL(
//            "CREATE TABLE " + TAB_VALS
//                + "(" + COL_ID + " integer PRIMARY KEY AUTOINCREMENT,"
//                + COL_VAL +  " text)");
//        db.execSQL(
//            "CREATE TABLE " + TAB_KEYS + "("
//                + COL_KEY + " text UNIQUE, "
//                + COL_FK + " integer REFERENCES "
//                    + TAB_VALS + "(" + COL_ID + "))");
//
//        long id;
//        ContentValues vals = new ContentValues();
//
//        vals.put(COL_VAL, "bar");
//        id = db.insert(TAB_VALS, null, vals);
//        vals.clear();
//        vals.put(COL_KEY, "one");
//        vals.put(COL_FK, Long.valueOf(id));
//        db.insert(TAB_KEYS, null, vals);
//        vals.clear();
//        vals.put(COL_KEY, "blue");
//        vals.put(COL_FK, Long.valueOf(id));
//        db.insert(TAB_KEYS, null, vals);
//
//        vals.clear();
//        vals.put(COL_VAL, "baz");
//        id = db.insert(TAB_VALS, null, vals);
//        vals.clear();
//        vals.put(COL_KEY, "two");
//        vals.put(COL_FK, Long.valueOf(id));
//        db.insert(TAB_KEYS, null, vals);
//
//        vals.clear();
//        vals.put(COL_VAL, "zqx3");
//        id = db.insert(TAB_VALS, null, vals);
//        vals.clear();
//        vals.put(COL_KEY, "red");
//        vals.put(COL_FK, Long.valueOf(id));
//        db.insert(TAB_KEYS, null, vals);
//    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
            "CREATE TABLE " + TAB_VALS + "("
                + COL_ID + " integer PRIMARY KEY AUTOINCREMENT,"
                + COL_VAL +  " text)");

        db.execSQL(
            "CREATE TABLE " + TAB_KEYS + "("
                + COL_KEY + " text UNIQUE, "
                + COL_FK + " integer REFERENCES "
                    + TAB_VALS + "(" + COL_ID + "))");

        long id = insertVal(db, "bar");
        insertKey(db, "one", id);
        insertKey(db, "blue", id);

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

// !!!CAUTION: DON'T DO THIS!
//    void insertVal(String val, int id) {
//        ContentValues r = new ContentValues();
//        r.put(COL_ID, Integer.valueOf(id));
//        r.put(KeyValContract.Columns.VAL, val);
//        getWritableDatabase().insert(TAB_VALS, null, r);
//    }

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
}

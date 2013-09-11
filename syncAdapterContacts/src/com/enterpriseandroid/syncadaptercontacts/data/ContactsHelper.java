package com.enterpriseandroid.syncadaptercontacts.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;


public class ContactsHelper extends SQLiteOpenHelper {

    // a non-null value
    public static final Integer MARKED = Integer.valueOf(1);

    static final String TAB_CONTACTS = "contacts";

    // pk
    static final String COL_ID = "id";                // long

    // contact data
    static final String COL_FNAME = "firstName";      // string
    static final String COL_LNAME = "lastName";       // string
    static final String COL_PHONE = "phone";          // string
    static final String COL_EMAIL = "email";          // string

    // meta-data
    static final String COL_REMOTE_ID = "remoteId";   // string
    static final String COL_VERSION = "version";      // long
    static final String COL_DELETED = "deleted";      // boolean (null or MARK)
    static final String COL_DIRTY = "dirty";          // boolean (null or MARK)
    static final String COL_SYNC = "sync";            // string

    private static final int VERSION = 4;

    private static final String DB_FILE = "contacts.db";


    public ContactsHelper(Context context) {
        super(context, DB_FILE, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
            "CREATE TABLE " + TAB_CONTACTS + "("
                + COL_ID + " integer PRIMARY KEY AUTOINCREMENT,"
                + COL_FNAME + " text,"
                + COL_LNAME + " text,"
                + COL_PHONE + " text,"
                + COL_EMAIL + " text,"
                + COL_REMOTE_ID + " string UNIQUE,"
                + COL_VERSION + " integer DEFAULT 0,"
                + COL_DELETED + " integer,"
                + COL_DIRTY + " integer DEFAULT " + MARKED + ","
                + COL_SYNC + " string)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        try { db.execSQL("drop table " + TAB_CONTACTS); }
        catch (SQLiteException e) { }
        onCreate(db);
    }
}

package com.enterpriseandroid.database.keyval.simple;

import android.app.Application;
import android.database.sqlite.SQLiteDatabase;

import com.enterpriseandroid.database.keyval.simple.data.KeyValHelper;

public class KeyValApplication extends Application {
    private KeyValHelper dbHelper;
    private Thread uiThread;

    @Override
    public void onCreate() {
        super.onCreate();

        // Raw database access.  Replaced with ContentProvider
        uiThread = Thread.currentThread();
        dbHelper = new KeyValHelper(this);
    }

    /**
     * Raw database access.  Replaced with ContentProvider
     *
     * @return the global database instance
     */
    @Deprecated
    public SQLiteDatabase getDb() {
        if (Thread.currentThread().equals(uiThread)) {
            throw new RuntimeException("Database opened on main thread");
        }
        return dbHelper.getWritableDatabase();
    }
}

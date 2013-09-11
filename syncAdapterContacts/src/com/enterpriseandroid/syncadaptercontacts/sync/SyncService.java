package com.enterpriseandroid.syncadaptercontacts.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.enterpriseandroid.syncadaptercontacts.BuildConfig;
import com.enterpriseandroid.syncadaptercontacts.ContactsApplication;

public class SyncService extends Service {
    private static final String TAG = "SYNC_SVC";


    private volatile SyncAdapter synchronizer;

    @Override
    public void onCreate() {
        super.onCreate();
        synchronizer = new SyncAdapter((ContactsApplication) getApplication(), true);
        if (BuildConfig.DEBUG) { Log.d(TAG, "created"); }
    }

    @Override
    public IBinder onBind(Intent intent) {
        if (BuildConfig.DEBUG) { Log.d(TAG, "sync bound"); }
        return synchronizer.getSyncAdapterBinder();
    }

    // Ooo, eee, ooo ah, ah
    // ting, tang, walla walla, bing bang
    @Override
    public void onDestroy() {
        synchronizer = null;
        if (BuildConfig.DEBUG) { Log.d(TAG, "destroyed"); }
    }
}

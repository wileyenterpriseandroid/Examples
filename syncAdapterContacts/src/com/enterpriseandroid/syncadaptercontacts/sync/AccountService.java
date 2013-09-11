package com.enterpriseandroid.syncadaptercontacts.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.enterpriseandroid.syncadaptercontacts.BuildConfig;


public class AccountService extends Service {
    private static final String TAG = "AUTH_SVC";


    private volatile AccountMgr mgr;

    @Override
    public void onCreate() {
        super.onCreate();
        mgr = new AccountMgr(getApplicationContext());
        if (BuildConfig.DEBUG) { Log.d(TAG, "created"); }
    }

    @Override
    public IBinder onBind(Intent intent) {
        if (BuildConfig.DEBUG) { Log.d(TAG, "bound"); }
        return mgr.getIBinder();
    }

    // Ooo, eee, ooo ah, ah
    // ting, tang, walla walla, bing bang
    @Override
    public void onDestroy() {
        mgr = null;
        if (BuildConfig.DEBUG) { Log.d(TAG, "destroyed"); }
    }
}

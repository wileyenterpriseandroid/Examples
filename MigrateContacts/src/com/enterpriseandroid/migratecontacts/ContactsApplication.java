package com.enterpriseandroid.migratecontacts;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;


public class ContactsApplication extends Application
    implements SharedPreferences.OnSharedPreferenceChangeListener
{
    private static final String TAG = "APP";
    private static final String DEFAULT_USER = "user";

    private String keyUser;
    private String user;

    @Override
    public void onCreate() {
        super.onCreate();
        if (BuildConfig.DEBUG) { Log.d(TAG, "Application up!"); }

        keyUser = getString(R.string.prefs_user_key);

        PreferenceManager.getDefaultSharedPreferences(this)
            .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public synchronized void onSharedPreferenceChanged(
        SharedPreferences prefs,
        String key)
    {
        user = null;
    }

    public String getUser() {
        synchronized (this) {
            if (null == user) {
                user = PreferenceManager.getDefaultSharedPreferences(this)
                        .getString(keyUser, null);
            }

            return (null != user) ? user : DEFAULT_USER;
        }
    }
}
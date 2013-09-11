package com.enterpriseandroid.syncadaptercontacts;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

public class ContactsApplication extends Application
    implements SharedPreferences.OnSharedPreferenceChangeListener
{
    private static final String TAG = "APP";

    private static final String EMULATOR_HOST_IP = "10.0.2.2";
    private static final String AWS_HOST = "dns_name_you_purchase_or_ip";
    private static final String APPENGINE_HOST = "your_app_id_here.appspot.com";
    private static final String LOCAL_APPENGINE_HOST = EMULATOR_HOST_IP;

    private static final String SPRING_SYNC_SERVICE = "/springSyncServiceContacts";
    private static final String AWS_SERVICE = "/awsServiceContacts";
    private static final String APP_ENGINE_SERVICE = ""; // no app context for appspot.com

    private static final String TEST_PORT = "8080";
    private static final String HTTP_DEFAULT_PORT = ""; // will be 80

    private static final String CONTACTS = "/Contacts";

    private static final String HTTP = "http";
    private static final String HTTPS = "https";

    private static final String PROTOCOL = HTTP;
    private static final String HOST = EMULATOR_HOST_IP;
    private static final String PORT = TEST_PORT;
    
    /***
     * to use AWS set the SERVICE = AWS_SERVICE
     * to user google append service, set the SERVICE = APP_ENGINE_SERVICE
     */
    private static final String SERVICE = SPRING_SYNC_SERVICE;
   

    // Warning: whatever value you use here will become *persistent*
    // on first run, when it gets saved as a preference below in
    // getApiUri.
    private static final String DEFAULT_API_ROOT =
            PROTOCOL + "://" + HOST + ":" + PORT + SERVICE + CONTACTS;

    private volatile String keyApiRoot;
    private String uri;

    public ContactsApplication() {
        /**
         * Disable the persistent connection since We cannot get it work using HttpURLConnection.
         */
        System.setProperty("http.keepAlive", "false");

    }
    @Override
    public void onCreate() {
        super.onCreate();
        if (BuildConfig.DEBUG) { Log.d(TAG, "Application up!"); }

        keyApiRoot = getString(R.string.prefs_url_key);

        PreferenceManager.getDefaultSharedPreferences(this)
            .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public synchronized void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
        if (keyApiRoot.equals(key)) { uri = null; }
    }

    public synchronized String getServerUri() {
        if (null == uri) {
            // Using a preference allows setting configuration of this
            // value - but it also makes the value persistent.  Please
            // keep this in mind if you can the url using the variables
            // above.
            uri = PreferenceManager.getDefaultSharedPreferences(this)
                    .getString(keyApiRoot, DEFAULT_API_ROOT);
        }
        return uri;
    }
}
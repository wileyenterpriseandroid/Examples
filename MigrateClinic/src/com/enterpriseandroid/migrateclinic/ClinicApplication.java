package com.enterpriseandroid.migrateclinic;

import android.app.Activity;
import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import com.enterpriseandroid.migrateclinic.data.PatientContract;
import com.enterpriseandroid.migrateclinic.data.XRayDetailsContract;
import com.enterpriseandroid.migrateclinic.data.SchemaManager;
import com.enterpriseandroid.migrateclinic.data.XRayContract;


public class ClinicApplication extends Application
    implements SharedPreferences.OnSharedPreferenceChangeListener, XRayDetailsContract.ContractListener
{
    private String keyUser;
    private String user;
    private XRayDetailsContract xRayDetailsContract;

    @Override
    public void onCreate() {
        super.onCreate();
        keyUser = getString(R.string.prefs_user_key);

        PreferenceManager.getDefaultSharedPreferences(this)
            .registerOnSharedPreferenceChangeListener(this);

        XRayDetailsContract.createContract(this, this);
    }

    @Override
    public synchronized void onSharedPreferenceChanged(
            SharedPreferences prefs,
            String key)
    {
        user = null;
    }

    @Override
    public void onContractReady(XRayDetailsContract contract) {
        xRayDetailsContract = contract;
    }

    public void initPatientDb(Activity activity, SchemaManager.SchemaLoaderListener listener) {
        new SchemaManager(
                activity,
                PatientContract.SCHEMA_ID,
                PatientContract.SCHEMA_PATIENT_URI,
                getUser(),
                listener)
        .initSchema();
    }

    public void initXrayDb(Activity activity, SchemaManager.SchemaLoaderListener listener) {
        new SchemaManager(
                activity,
                XRayContract.SCHEMA_ID,
                XRayContract.SCHEMA_XRAY_URI,
                getUser(),
                listener)
        .initSchema();
    }

    public boolean xRayDetailContractReady() { return null != xRayDetailsContract; }

    public XRayDetailsContract initXRayDetailDb(Activity activity, SchemaManager.SchemaLoaderListener listener) {
        if (null == xRayDetailsContract) { return null; }

        new SchemaManager(
                activity,
                xRayDetailsContract.getSchemaId(),
                xRayDetailsContract.getSchemaUri(),
                getUser(),
                listener)
        .initSchema();

        return xRayDetailsContract;
    }

    public String getUser() {
        synchronized (this) {
            if (null == user) {
                user = PreferenceManager.getDefaultSharedPreferences(this)
                        .getString(keyUser, null);
            }
            return user;
        }
    }
}
package com.enterpriseandroid.syncadaptercontacts.sync;

import java.io.IOException;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.SyncResult;
import android.os.Bundle;
import android.util.Log;

import com.enterpriseandroid.syncadaptercontacts.BuildConfig;
import com.enterpriseandroid.syncadaptercontacts.ContactsApplication;
import com.enterpriseandroid.syncadaptercontacts.R;
import com.enterpriseandroid.syncadaptercontacts.svc.RESTService;


public class SyncAdapter extends AbstractThreadedSyncAdapter {
    private static final String TAG = "SYNC";


    private final ContactsApplication ctxt;

    public SyncAdapter(ContactsApplication ctxt, boolean autoInitialize) {
        super(ctxt, autoInitialize);
        this.ctxt = ctxt;
    }

    @Override
    public void onPerformSync(
            Account account,
            Bundle extras,
            String authority,
            ContentProviderClient provider,
            SyncResult syncResult)
    {
        if (BuildConfig.DEBUG) { Log.d(TAG, "starting sync @" + AccountMgr.acctStr(account)); }

        AccountManager mgr = AccountManager.get(ctxt);

        String tt = ctxt.getString(R.string.token_type);

        Exception e = null;
        String token = null;
        try { token = mgr.blockingGetAuthToken(account, tt, false); }
        catch (OperationCanceledException oce) { e = oce; }
        catch (AuthenticatorException ae) { e = ae; }
        catch (IOException ioe) { e = ioe; }

        if (null == token) {
            Log.e(TAG, "auth failed: " + AccountMgr.acctStr(account) + "#" + tt, e);
            return;
        }

        if (BuildConfig.DEBUG) {
            Log.d(TAG, "syncing: " + AccountMgr.acctStr(account) + "#" + token);
        }

        new RESTService(ctxt).sync(account, token);

        // force re-validation
        mgr.invalidateAuthToken(account.type, token);

        if (BuildConfig.DEBUG) { Log.d(TAG, "sync complete"); }
    }
}

package com.enterpriseandroid.syncadaptercontacts.sync;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.enterpriseandroid.syncadaptercontacts.BuildConfig;
import com.enterpriseandroid.syncadaptercontacts.R;


public class AccountMgr extends AbstractAccountAuthenticator {
    private static final String TAG = "AUTH";

    public static final String KEY_TOKEN_TYPE = "AccountAuth.TOKEN_TYPE";

    public static final String acctStr(Account account) {
        return "(" + account.name + "," + account.type + ")";
    }


    private final Context ctxt;
    private final InstallationId id;

    public AccountMgr(Context context) {
        super(context);
        this.ctxt = context;
        this.id = new InstallationId(ctxt);
    }

    @Override
    public Bundle addAccount(
            AccountAuthenticatorResponse response,
            String accountType,
            String authTokenType,
            String[] requiredFeatures,
            Bundle options)
    {
        if (BuildConfig.DEBUG) {
            Log.d( TAG, "add account: " + accountType + "#" + authTokenType);
        }

        Bundle reply = new Bundle();

        String at = ctxt.getString(R.string.account_type);
        reply.putString(AccountManager.KEY_ACCOUNT_TYPE, at);

        if (!at.equals(accountType)) {
            reply.putInt(AccountManager.KEY_ERROR_CODE, -1);
            reply.putString(
                    AccountManager.KEY_ERROR_MESSAGE,
                    "Unrecognized account type");
            return reply;
        }

        Account account = new Account(ctxt.getString(R.string.app_name), accountType);
        if (!AccountManager.get(ctxt).addAccountExplicitly(account, null, null)) {
            reply.putInt(AccountManager.KEY_ERROR_CODE, -1);
            reply.putString(
                    AccountManager.KEY_ERROR_MESSAGE,
                    "Unable to create account");
            return reply;
        }
        reply.putString(AccountManager.KEY_ACCOUNT_NAME, account.name);

        String provider = ctxt.getString(R.string.contacts_authority);
        ContentResolver.setIsSyncable(account, provider, 1);
        ContentResolver.setSyncAutomatically(account, provider, true);

        String token = obtainToken(authTokenType);
        if (null == token) {
            reply.putInt(AccountManager.KEY_ERROR_CODE, -1);
            reply.putString(
                    AccountManager.KEY_ERROR_MESSAGE,
                    "Unrecognized token type");
            return reply;
        }
        reply.putString(AccountManager.KEY_AUTHTOKEN, token);

        return reply;
    }

    @Override
    public Bundle getAuthToken(
            AccountAuthenticatorResponse response,
            Account account,
            String authTokenType,
            Bundle options)
    {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "token request @" + acctStr(account));
        }

        Bundle reply = new Bundle();
        reply.putString(AccountManager.KEY_ACCOUNT_TYPE, account.type);
        reply.putString(AccountManager.KEY_ACCOUNT_NAME, account.name);
        reply.putString(KEY_TOKEN_TYPE, authTokenType);

        if (!ctxt.getString(R.string.account_type).equals(account.type)
                || !ctxt.getString(R.string.app_name).equals(account.name))
        {
            reply.putInt(AccountManager.KEY_ERROR_CODE, -1);
            reply.putString(AccountManager.KEY_ERROR_MESSAGE, "Unrecognized account");
            return reply;
        }

        String token = obtainToken(authTokenType);
        if (null == token) {
            reply.putInt(AccountManager.KEY_ERROR_CODE, -1);
            reply.putString(AccountManager.KEY_ERROR_MESSAGE, "Unrecognized token type");
            return reply;
        }

        reply.putString(AccountManager.KEY_AUTHTOKEN, token);
        return reply;
    }

    @Override
    public Bundle updateCredentials(
            AccountAuthenticatorResponse response,
            Account account,
            String authTokenType,
            Bundle options)
    {
        throw new UnsupportedOperationException("Update credentials not supported.");
    }

    @Override
    public Bundle hasFeatures(
            AccountAuthenticatorResponse response,
            Account account,
            String[] features)
    {
        throw new UnsupportedOperationException("Update credentials not supported.");
    }

    @Override
    public Bundle confirmCredentials(
            AccountAuthenticatorResponse response,
            Account account,
            Bundle options)
    {
        throw new UnsupportedOperationException("Update credentials not supported.");
    }

    @Override
    public Bundle editProperties(
            AccountAuthenticatorResponse response,
            String accountType)
    {
        throw new UnsupportedOperationException("Update credentials not supported.");
    }

    @Override
    public String getAuthTokenLabel(String authTokenType) {
        throw new UnsupportedOperationException("Update credentials not supported.");
    }

    // incidentally, we use the installation id as the authentication token
    private String obtainToken(String tt) {
        String token = (!ctxt.getString(R.string.token_type).equals(tt))
                ? null
                : id.getInstallationId();
        if (BuildConfig.DEBUG) { Log.d(TAG, "token @" + tt + ": " + token); }
        return token;
   }
}
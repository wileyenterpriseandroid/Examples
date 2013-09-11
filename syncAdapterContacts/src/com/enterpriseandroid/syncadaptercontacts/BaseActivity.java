package com.enterpriseandroid.syncadaptercontacts;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.enterpriseandroid.syncadaptercontacts.data.ContactsContract;
import com.enterpriseandroid.syncadaptercontacts.sync.AccountMgr;


@SuppressLint("Registered")
public class BaseActivity extends Activity {
    private static final String TAG = "BASE";

    private static final SparseIntArray STATUS_COLOR_MAP;
    static {
        SparseIntArray a = new SparseIntArray();
        a.put(ContactsContract.STATUS_OK, Color.GREEN);
        a.put(ContactsContract.STATUS_SYNC, Color.YELLOW);
        a.put(ContactsContract.STATUS_DIRTY, Color.RED);
        STATUS_COLOR_MAP = a;
    }

    protected static void setStatusBackground(int status, View view) {
        int color = STATUS_COLOR_MAP.get(status);
        view.setBackgroundColor((0 != color) ? color : Color.BLACK);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item_prefs:
                startActivity(new Intent(this, PrefsActivity.class));
                break;

            case R.id.item_sync1:
                requestSync();
                break;

            case R.id.item_sync2:
                if (BuildConfig.DEBUG) { Log.d(TAG, "requesting sync @" + ContactsContract.URI); }
                getContentResolver().notifyChange(ContactsContract.URI, null, true);
                break;

            case R.id.item_account:
                if (BuildConfig.DEBUG) { Log.d(TAG, "add account"); }
                AccountManager.get(this).addAccount(
                        getString(R.string.account_type),
                        getString(R.string.token_type),
                        null,
                        null,
                        null,
                        null,
                        null);
                break;

            default:
                Log.i(TAG, "Unrecognized menu item: " + item);
                return false;
        }

        return true;
    }

    private void requestSync() {
        Account[] accounts = AccountManager.get(this).getAccountsByType(getString(R.string.account_type));
        if ((null == accounts) || (0 >= accounts.length)) {
            Toast.makeText(this, R.string.msg_no_account, Toast.LENGTH_SHORT).show();
            return;
        }

        // Just use the first account of our type.
        // This works because there should be at most one.
        // If there were more, we'd have to choose an account in prefs or something.
        Account account = accounts[0];

        if (BuildConfig.DEBUG) {
            Log.d(
                TAG,
                "requesting sync @" + AccountMgr.acctStr(account)
                    + ": " + ContactsContract.AUTHORITY);
        }
        ContentResolver.requestSync(account, ContactsContract.AUTHORITY, new Bundle());
    }
}

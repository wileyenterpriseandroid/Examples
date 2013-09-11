package com.enterpriseandroid.migratecontacts;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;


@SuppressLint("Registered")
public class BaseActivity extends Activity {
    private static final String TAG = "BASE";

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

            default:
                Log.i(TAG, "Unrecognized menu item: " + item);
                return false;
        }

        return true;
    }
}

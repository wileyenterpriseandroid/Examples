package com.enterpriseandroid.database.keyval;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import com.enterpriseandroid.database.keyval.data.KeyValContract;


public class KeyValActivity extends Activity
    implements LoaderManager.LoaderCallbacks<Cursor>
{
    public static final String INTENT_CLIENT
        = "com.enterpriseandroid.database.keyval.CLIENT";

    private static final int KEYVAL_LOADER = 6;

    private static final String[] FROM = new String[] {
        KeyValContract.Columns.ID,
        KeyValContract.Columns.KEY,
        KeyValContract.Columns.VAL,
        KeyValContract.Columns.EXTRA
    };

    private static final int[] TO = new int[] {
        R.id.listview_id,
        R.id.listview_key,
        R.id.listview_val,
        R.id.listview_extra
    };

    private static class AsyncInsert extends AsyncTask<Void, Void, Void> {
        private final Context ctxt;
        private final String key;
        private final String val;

        public AsyncInsert(Context ctxt, String key, String val) {
            this.ctxt = ctxt;
            this.key = key;
            this.val = val;
        }

        @Override
        protected Void doInBackground(Void... params) {
            ContentValues values = new ContentValues();
            values.put(KeyValContract.Columns.KEY, key);
            values.put(KeyValContract.Columns.VAL, val);

            try {
                ctxt.getContentResolver()
                    .insert(KeyValContract.URI_KEYVAL, values);
            }
            catch (Exception e) { Log.w("INSERT", "Insert failed", e); }

            return null;
        }
    }

    class ExtrasBinder implements SimpleCursorAdapter.ViewBinder {
        @Override
        public boolean setViewValue(View v, Cursor cur, int idx)
        {
            if (R.id.listview_extra != v.getId()) { return false; }

            ((ImageView) v).setImageDrawable(
                (cur.isNull(idx)) ? iconX : iconCheck);

            return true;
        }
    }

    Drawable iconCheck;
    Drawable iconX;

    private ListView listView;
    private EditText keyView;
    private EditText valView;

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
        return new CursorLoader(
            this,
            KeyValContract.URI_KEYVAL,
            FROM,
            null,
            null,
            KeyValContract.Columns.KEY + " ASC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor c) {
        ((SimpleCursorAdapter) listView.getAdapter()).swapCursor(c);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        ((SimpleCursorAdapter) listView.getAdapter()).swapCursor(null);
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
             case R.id.item_test_client:
                 launchClient(2L);
                 break;

             default:
                 Log.w("MENU", "Unrecognized menu item: " + item);
                 return false;
         }

         return true;
     }

    @SuppressLint("ShowToast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Resources rez = getResources();
        iconCheck = rez.getDrawable(R.drawable.ic_check);
        iconX = rez.getDrawable(R.drawable.ic_x);

        getLoaderManager().initLoader(KEYVAL_LOADER, null, this);

        setContentView(R.layout.activity_keyval);

        keyView = (EditText) findViewById(R.id.key);
        valView = (EditText) findViewById(R.id.val);

        listView = (ListView) findViewById(R.id.list);
        listView.setOnItemClickListener(new ListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View v, int p, long id)
            {
                launchContent((Cursor) parent.getItemAtPosition(p));
            } });

        SimpleCursorAdapter adapter = new SimpleCursorAdapter(
            this,
            R.layout.keyval_row,
            null,
            FROM,
            TO,
            0);
        adapter.setViewBinder(new ExtrasBinder());
        listView.setAdapter(adapter);

        ((Button) findViewById(R.id.button_add)).setOnClickListener(
            new Button.OnClickListener() {
                @Override public void onClick(View arg0) { doInsert(); }
            });

        final Toast toast = Toast.makeText(
            this,
            R.string.toast_update,
            Toast.LENGTH_SHORT);

        getContentResolver().registerContentObserver(
            KeyValContract.URI_KEYVAL,
            true,
            new ContentObserver(null) {
                @Override
                public void onChange(boolean selfChange) { toast.show(); }
            });
    }

    void doInsert() {
         new AsyncInsert(
            getApplicationContext(),
            keyView.getText().toString(),
            valView.getText().toString())
        .execute();

        keyView.setText(null);
        valView.setText(null);
    }

    void launchContent(Cursor cursor) {
        int idx = cursor.getColumnIndex(KeyValContract.Columns.EXTRA);
        if (cursor.isNull(idx)) { return; }

        long extra = cursor.getLong(idx);
        Intent i = new Intent(this, ExtrasActivity.class);
        i.putExtra(ExtrasActivity.EXTRA_KEY, extra);
        i.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(i);
    }

    private void launchClient(long id) {
        Intent i = new Intent(INTENT_CLIENT);
        i.setData(
            KeyValContract.URI_KEYVAL.buildUpon()
                .appendPath(String.valueOf(id)).build());
        i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(i);
    }
}


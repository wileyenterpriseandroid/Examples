package com.enterpriseandroid.database.keyval.simple;

import android.app.ListActivity;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.widget.SimpleCursorAdapter;

import com.enterpriseandroid.database.keyval.simple.data.KeyValContract;

public class KeyValActivity extends ListActivity
implements LoaderManager.LoaderCallbacks<Cursor>
{
    private static final int LOADER_ID = 6;

    private static final String[] FROM = new String[] {
        BaseColumns._ID,
        KeyValContract.Columns.KEY,
        KeyValContract.Columns.VAL
    };

    private static final int[] TO = new int[] {
        R.id.listview_id,
        R.id.listview_key,
        R.id.listview_val
    };

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
        return new CursorLoader(
            this,
            KeyValContract.URI,
            null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor c) {
        ((SimpleCursorAdapter) getListAdapter()).swapCursor(c);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        ((SimpleCursorAdapter) getListAdapter()).swapCursor(null);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getLoaderManager().initLoader(LOADER_ID, null, this);

        setListAdapter(
            new SimpleCursorAdapter(this, R.layout.keyval_row, null, FROM, TO, 0));
    }
}

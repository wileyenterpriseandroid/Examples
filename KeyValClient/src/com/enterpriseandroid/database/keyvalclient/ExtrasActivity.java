package com.enterpriseandroid.database.keyvalclient;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.Loader;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.widget.TextView;

import com.enterpriseandroid.database.keyval.data.KeyValContract;


public class ExtrasActivity extends Activity
    implements LoaderManager.LoaderCallbacks<String>
{
    public static final String EXTRA_KEY = "keyval.EXTRA";

    private static final int EXTRAS_LOADER = 9;

    private static class ExtrasLoader extends AsyncTaskLoader<String> {
        private volatile boolean loaded;
        private final long fid;

        public ExtrasLoader(Context context, long fid) {
            super(context);
            this.fid = fid;
        }

        @Override
        public String loadInBackground() {
            loaded = true;

            InputStream in = null;
            try {
                in = getContext().getContentResolver().openInputStream(
                    KeyValContract.URI_VALS.buildUpon()
                        .appendPath(String.valueOf(fid))
                    .build());

                // This probably over-simplifies the process
                // of managing a large data object..
                StringBuilder extra = new StringBuilder();
                byte[] buf = new byte[256];
                while (0 < in.read(buf)) { extra.append(new String(buf)); }

                return extra.toString();
            }
            catch (FileNotFoundException e) {
                Log.w("CONTENT", "File not found: " + fid, e);
            }
            catch (IOException e) {
                Log.w("CONTENT", "Failed reading: " + fid, e);
            }
            finally {
                if (null != in) { try { in.close(); } catch (IOException e) { } }
            }

            return null;
        }

        // see bug: http://code.google.com/p/android/issues/detail?id=14944
        @Override
        protected void onStartLoading() {
            if (!loaded) { forceLoad(); }
        }
    }

    TextView extrasView;

    @Override
    public Loader<String> onCreateLoader(int id, Bundle args) {
        return new ExtrasLoader(this, args.getLong(EXTRA_KEY));
    }

    @Override
    public void onLoadFinished(Loader<String> l, String extra) {
        extrasView.setText(extra);
    }

    @Override
    public void onLoaderReset(Loader<String> l) { }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getLoaderManager().initLoader(EXTRAS_LOADER, getIntent().getExtras(), this);

        setContentView(R.layout.activity_extras);
        extrasView = (TextView) findViewById(R.id.extra);
        extrasView.setMovementMethod(new ScrollingMovementMethod());
    }
}

package com.enterpriseandroid.database.keyval.simple.data;

import android.net.Uri;
import android.provider.BaseColumns;

public final class KeyValContract {
    private KeyValContract() {}

    public static final String TABLE_KEYVAL = "keyval";
    public static final String AUTHORITY
        = "com.enterpriseandroid.database.keyval.simple";
    public static final Uri URI = Uri.parse(
            "content://" + AUTHORITY + "/" + KeyValContract.TABLE_KEYVAL);

    public static final String TYPE_KEYVAL
    	= "vnd.android.cursor.dir/vnd.com.enterpriseandroid.database.keyval";

    public static final class Columns implements BaseColumns {
        private Columns() {}

        public static final String KEY = "key";
        public static final String VAL = "val";
    }
}

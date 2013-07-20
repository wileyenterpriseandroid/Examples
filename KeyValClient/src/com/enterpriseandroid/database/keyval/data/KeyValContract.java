package com.enterpriseandroid.database.keyval.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;


public final class KeyValContract {
    private KeyValContract() {}
    public static final int VERSION = 1;

    public static final String AUTHORITY
        = "com.enterpriseandroid.database.keyval";

    private static final Uri URI_BASE
        = new Uri.Builder()
            .scheme(ContentResolver.SCHEME_CONTENT)
            .authority(AUTHORITY)
            .build();

    public static final String TABLE_VALS = "vals";
    public static final Uri URI_VALS
        = URI_BASE.buildUpon().appendPath(TABLE_VALS).build();
    public static final String TYPE_VALS
        = ContentResolver.CURSOR_DIR_BASE_TYPE
            + "/vnd.com.enterpriseandroid.database.val";
    public static final String TYPE_VAL
        = ContentResolver.CURSOR_ITEM_BASE_TYPE
            + "/vnd.com.enterpriseandroid.database.val";

    public static final String TABLE_KEYVAL = "keyval";
    public static final Uri URI_KEYVAL
        = URI_BASE.buildUpon().appendPath(TABLE_KEYVAL).build();
    public static final String TYPE_KEYVALS
        = ContentResolver.CURSOR_DIR_BASE_TYPE
            + "/vnd.com.enterpriseandroid.database.keyval";
    public static final String TYPE_KEYVAL
        = ContentResolver.CURSOR_ITEM_BASE_TYPE
            + "/vnd.com.enterpriseandroid.database.keyval";

    public static final class Columns {
        private Columns() {}

        // columns in both tables
        public static final String ID = BaseColumns._ID;
        public static final String VAL = "val";
        public static final String EXTRA = "extra";

        // columns only in the keyval table
        public static final String KEY = "key";
    }

    /*
    <permission-group
        android:name="com.enterpriseandroid.database.keyval"
        android:description="@string/content_group_desc"
        android:label="@string/content_group_label" />

    <permission
        android:name="com.enterpriseandroid.database.keyval.READ"
        android:description="@string/content_read_desc"
        android:permissionGroup="com.enterpriseandroid.database.keyval"
        android:protectionLevel="dangerous" />
    <permission
        android:name="com.enterpriseandroid.database.keyval.WRITE"
        android:description="@string/content_write_desc"
        android:permissionGroup="com.enterpriseandroid.database.keyval"
        android:protectionLevel="signature" />

    <provider
        android:name=".data.KeyValContentProvider"
        android:authorities="com.enterpriseandroid.database.keyval"
        android:grantUriPermissions="true"
        android:readPermission="com.enterpriseandroid.database.keyval.READ"
        android:writePermission="com.enterpriseandroid.database.keyval.WRITE" />
     */
    public static final class Permission {
        private Permission() {}

        public static final String READ
            = "com.enterpriseandroid.database.keyval.READ";
        public static final String WRITE
            = "com.enterpriseandroid.database.keyval.WRITE";
    }
}

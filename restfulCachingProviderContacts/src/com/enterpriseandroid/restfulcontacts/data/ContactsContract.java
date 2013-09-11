package com.enterpriseandroid.restfulcontacts.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;


public final class ContactsContract {
    private ContactsContract() {}

    public static final String TABLE = "contacts";
    public static final String AUTHORITY = "com.enterpriseandroid.restfulcontacts.CONTACTS";

    public static final Uri URI
        = new Uri.Builder()
            .scheme(ContentResolver.SCHEME_CONTENT)
            .authority(AUTHORITY)
            .appendPath(TABLE)
            .build();

    /** Contacts DIR type */
    public static final String CONTENT_TYPE_DIR
        = ContentResolver.CURSOR_DIR_BASE_TYPE
            + " + /vnd.com.enterpriseandroid.restfulcontacts.contacts";

    /** Contacts ITEM type */
    public static final String CONTENT_TYPE_ITEM
        = ContentResolver.CURSOR_ITEM_BASE_TYPE
            + "/vnd.com.enterpriseandroid.restfulcontacts.contacts";

    public static final int STATUS_OK = 0;
    public static final int STATUS_SYNC = 1;
    public static final int STATUS_DIRTY = 2;

    public static final class Columns implements BaseColumns {
        private Columns() {}

        public static final String ID = BaseColumns._ID;  // long
        public static final String FNAME = "firstName";   // string
        public static final String LNAME = "lastName";    // string
        public static final String PHONE = "phone";       // string
        public static final String EMAIL = "email";       // string
        public static final String STATUS = "status";     // int

        // !!!
        // These columns really should not be exposed.
        public static final String SYNC = "sync";         // string!
        public static final String DIRTY = "dirty";       // int!
        public static final String REMOTE_ID = "rem";     // int!
    }
}

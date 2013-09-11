/* Generated Source Code - Do not Edit! */
package com.enterpriseandroid.migrateclinic.data;

import android.net.Uri;
import android.provider.BaseColumns;
import net.migrate.api.Webdata;

public final class XRayContract {
    private XRayContract() {}

    public static final String SCHEMA_ID = XRay.class.getName();

    public static final Uri SCHEMA_XRAY_URI = Webdata.Schema.schemaUri(SCHEMA_ID);
    public static final Uri OBJECT_XRAY_URI = Webdata.Object.objectUri(SCHEMA_ID);

    public static final class Columns {
        private Columns() {}

        public static final String ID = BaseColumns._ID;
        public static final String SSN = "ssn";
        public static final String TIMESTAMP = "timestamp";
        public static final String DESCRIPTION = "description";
        public static final String NOTES = "notes";
        public static final String XRAY = "xray";
    }
}

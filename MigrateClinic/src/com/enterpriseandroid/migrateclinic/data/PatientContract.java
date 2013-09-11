/* Generated Source Code - Do not Edit! */
package com.enterpriseandroid.migrateclinic.data;

import android.net.Uri;
import android.provider.BaseColumns;
import net.migrate.api.Webdata;

public final class PatientContract {
    private PatientContract() {}

    public static final String SCHEMA_ID = Patient.class.getName();

    public static final Uri SCHEMA_PATIENT_URI = Webdata.Schema.schemaUri(SCHEMA_ID);
    public static final Uri OBJECT_PATIENT_URI = Webdata.Object.objectUri(SCHEMA_ID);

    public static final class Columns {
        private Columns() {}

        public static final String ID = BaseColumns._ID;
        public static final String SSN = "ssn";
        public static final String FIRSTNAME = "firstname";
        public static final String LASTNAME = "lastname";
        public static final String INSURER = "insurer";
    }
}

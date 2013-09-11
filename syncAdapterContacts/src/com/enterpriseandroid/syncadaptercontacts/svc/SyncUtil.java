package com.enterpriseandroid.syncadaptercontacts.svc;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.enterpriseandroid.syncadaptercontacts.BuildConfig;
import com.enterpriseandroid.syncadaptercontacts.data.ContactsContract;
import com.enterpriseandroid.syncadaptercontacts.data.ContactsHelper;
import com.enterpriseandroid.syncadaptercontacts.data.ContactsProvider;
import com.google.gson.reflect.TypeToken;


public class SyncUtil {
    private static final String TAG = "SyncUtil";

    // JSON field names
    // == doc
    public static final String SYNC_TIME = "syncTime";
    public static final String CONFLICTS = "confilcts";
    public static final String MODIFIED = "modified";
    public static final String ACCOUNT = "account";
    public static final String CLIENT = "client";
    public static final String AUTH = "auth";

    // == record
    public static final String ID = "id";
    public static final String FNAME = "firstName";
    public static final String LNAME = "lastName";
    public static final String PHONE = "phone";
    public static final String EMAIL = "email";
    public static final String VERSION = "version";
    public static final String DELETED = "deleted";

    public static final Type MAP_TYPE = new TypeToken<Map<String, Object>>(){ }.getType();

    private static final Uri CONTENT_URI = ContactsContract.URI.buildUpon()
            .appendQueryParameter(ContactsProvider.SYNC_UPDATE, "true")
            .build();

    private static final String[] FULL_PROJECTION = new String[] {
        ContactsContract.Columns.ID,
        ContactsContract.Columns.FNAME,
        ContactsContract.Columns.LNAME,
        ContactsContract.Columns.PHONE,
        ContactsContract.Columns.EMAIL,
        // ContactsContract.Columns.STATUS,
        ContactsContract.Columns.REMOTE_ID,
        ContactsContract.Columns.VERSION,
        ContactsContract.Columns.DELETED,
        // ContactsContract.Columns.DIRTY,
        // ContactsContract.Columns.SYNC,
    };

    private static final String[] VERSION_PROJECTION = {
        ContactsContract.Columns.REMOTE_ID,
        ContactsContract.Columns.VERSION,
        ContactsContract.Columns.DIRTY,
    };


    public void beginUpdate(ContentResolver cr, String xactId) {
        ContentValues vals = new ContentValues();
        vals.put(ContactsContract.Columns.SYNC, xactId);
        cr.update(CONTENT_URI, vals, ContactsProvider.DIRTY_CONSTRAINT, null);
    }

    public List<Map<String, Object>> getLocalUpdates(ContentResolver cr, String xactId) {
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();

        Cursor c = cr.query(
                CONTENT_URI,
                FULL_PROJECTION,
                ContactsProvider.SYNC_CONSTRAINT,
                new String[] { xactId },
                null);
        if (BuildConfig.DEBUG) { Log.d(TAG, "local updates @" + xactId + ": " + c.getCount()); }

        try {
            while (c.moveToNext()) { list.add(cursor2ContactMap(c)); }
        }
        finally { c.close(); }

        return list;
    }

    public Map<String, Object> createSyncRequest(
            List<Map<String, Object>> modified,
            String account,
            String authToken,
            String clientId,
            long lastUpdate)
    {
        Map<String, Object> syncRequestMap = new HashMap<String, Object>();
        syncRequestMap.put(MODIFIED, modified);
        syncRequestMap.put(SYNC_TIME, Long.valueOf(lastUpdate));
        return syncRequestMap;
    }

    @SuppressWarnings("unchecked")
    public long processUpdates(ContentResolver cr, Map<String, Object> syncResultMap) {
        processServerUpdates(cr, (List<Map<String, Object>>) syncResultMap.get(SyncUtil.MODIFIED));

        resolveConflicts(cr, (List<Map<String, Object>>) syncResultMap.get(SyncUtil.CONFLICTS));

        return (long) ((Double) syncResultMap.get(SyncUtil.SYNC_TIME)).doubleValue();
    }

    /**
     * For each contact in the list
     *    1. remove the dirty flag
     *    2. remove transaction id (sync)
     *    3. increment version
     *
     * @param cr
     * @param contactMapList
     */
    public void finishUpdate(ContentResolver cr, List<Map<String, Object>> contactMapList) {
        if (BuildConfig.DEBUG) { Log.d(TAG, "finish update"); }
        for (Map<String, Object> contactMap: contactMapList) {
            String[] args = new String[] { (String) contactMap.get(ID) };

            if (getMapBoolValue(contactMap, DELETED)) {
                cr.delete(CONTENT_URI, ContactsProvider.REM_ID_CONSTRAINT, args);
                continue;
            }

            ContentValues vals = new ContentValues();

            vals.putNull(ContactsContract.Columns.DIRTY);

            Long version = getMapLongValue(contactMap, VERSION);
            if (null != version) {
                vals.put(ContactsContract.Columns.VERSION, Long.valueOf(version.longValue() + 1));
            }

            cr.update(CONTENT_URI, vals, ContactsProvider.REM_ID_CONSTRAINT, args);
        }
    }

    public void endUpdate(ContentResolver cr, String xactId) {
        ContentValues vals = new ContentValues();
        vals.putNull(ContactsContract.Columns.SYNC);
        cr.update(CONTENT_URI, vals, ContactsProvider.SYNC_CONSTRAINT, new String[] { xactId });
    }

    /**
     * For each contact do the following:
     * 1. Get the version and dirty flag in local db using the remoteId
     * 2. if the dirty flag is not set, update the contact to local db
     * 3. if the dirty flag is set and the version is the same, then discard the contact
     * 4. if the dirty flag is set and the version is not the same, then we have a conflict.
     *    we should write the conflict to a conflict table, then ask the user to resolve it.
     *    For now, we can just resolve to the server version.
     */
    private void processServerUpdates(ContentResolver cr, List<Map<String, Object>> contactMapList) {
        if ((null == contactMapList) || (0 >= contactMapList.size())) { return; }

        for (Map<String, Object> contactMap: contactMapList) {
            boolean deleted = getMapBoolValue(contactMap, DELETED);

            ContentValues vals = contactMap2ContentValues(contactMap);

            String remoteId = (String) contactMap.get(ID);
            String[] args = new String[] { remoteId };

            Cursor cursor = cr.query(
                    ContactsContract.URI,
                    VERSION_PROJECTION,
                    ContactsProvider.REM_ID_CONSTRAINT,
                    args,
                    null);
            try {
                // new from remote
                if (!cursor.moveToFirst()) {
                    if (!deleted) {
                        vals.putNull(ContactsContract.Columns.DIRTY);
                        if (BuildConfig.DEBUG) { Log.d(TAG, "remote insert {" + vals + "}"); }
                        cr.insert(CONTENT_URI, vals);
                    }
                    continue;
                }

                // change initiated on remote
                if (!getCursorBoolValue(cursor, ContactsContract.Columns.DIRTY)) {
                    if (BuildConfig.DEBUG) {
                        Log.d(TAG, "remote change: " + contactMap.get(DELETED) + " {" + vals + "}");
                    }

                    // server has deleted the record
                    if (deleted) { cr.delete(CONTENT_URI, ContactsProvider.REM_ID_CONSTRAINT, args); }
                    else { cr.update(CONTENT_URI, vals, ContactsProvider.REM_ID_CONSTRAINT, args); }

                    continue;
                }

                // change initiated locally
                if (getCursorLongValue(cursor, ContactsContract.Columns.VERSION)
                        .equals(contactMap.get(VERSION)))
                {
                    // no conflict: server and local data must be the same
                    continue;
                }

                // conflict: just accept server version for now.
                if (BuildConfig.DEBUG) { Log.d(TAG, "conflict {" + vals + "}"); }
                cr.update(CONTENT_URI, vals, ContactsProvider.REM_ID_CONSTRAINT, args);
            }
            finally {
                cursor.close();
            }
        }
    }

    // Just use the server version, for now.
    private void resolveConflicts(ContentResolver cr, List<Map<String, Object>> contactMapList) {
        if ((contactMapList == null) || (contactMapList.size() == 0)) { return; }

        for (Map<String, Object> contactMap: contactMapList) {
            ContentValues vals = contactMap2ContentValues(contactMap);

            String[] args = new String[] { vals.getAsString(ContactsContract.Columns.REMOTE_ID) };

            if (BuildConfig.DEBUG) { Log.d(TAG, "resolving {" + vals + "}"); }
            cr.update(CONTENT_URI, vals, ContactsProvider.REM_ID_CONSTRAINT, args);
        }
    }

    private ContentValues contactMap2ContentValues(Map<String, Object> map) {
        ContentValues vals = new ContentValues();
        putStringContentValues(vals, FNAME, ContactsContract.Columns.FNAME, map);
        putStringContentValues(vals, LNAME, ContactsContract.Columns.LNAME, map);
        putStringContentValues(vals, PHONE, ContactsContract.Columns.PHONE, map);
        putStringContentValues(vals, EMAIL, ContactsContract.Columns.EMAIL, map);
        putStringContentValues(vals, ID, ContactsContract.Columns.REMOTE_ID, map);
        putLongContentValues(vals, VERSION, ContactsContract.Columns.VERSION, map);
        putBooleanContentValues(vals, DELETED, ContactsContract.Columns.DELETED, map);
        return vals;
    }

    private Map<String, Object> cursor2ContactMap(Cursor cursor) {
        Map<String, Object> contactMap = new HashMap<String, Object>();
        putMapStringValue(contactMap, ContactsContract.Columns.REMOTE_ID, ID, cursor);
        putMapStringValue(contactMap, ContactsContract.Columns.FNAME, FNAME, cursor);
        putMapStringValue(contactMap, ContactsContract.Columns.LNAME, LNAME, cursor);
        putMapStringValue(contactMap, ContactsContract.Columns.EMAIL, EMAIL, cursor);
        putMapStringValue(contactMap, ContactsContract.Columns.PHONE, PHONE, cursor);
        putMapLongValue(contactMap, ContactsContract.Columns.VERSION, VERSION, cursor);
        putMapBooleanValue(contactMap, ContactsContract.Columns.DELETED, DELETED, cursor);
        return contactMap;
    }

    private void putStringContentValues(ContentValues vals, String src, String dst, Map<String, Object> map) {
        vals.put(dst, getMapStringValue(map, src));
    }

    private void putLongContentValues(ContentValues vals, String src, String dst, Map<String, Object> map) {
        Long l = getMapLongValue(map, src);
        if (null != l) { vals.put(dst, l); }
    }

    private void putBooleanContentValues(ContentValues vals, String src, String dst, Map<String, Object> map) {
        if (!getMapBoolValue(map, src)) { vals.putNull(dst); }
        else { vals.put(dst, ContactsHelper.MARKED); }
    }

    private void putMapStringValue(Map<String, Object> map, String src, String dst, Cursor cursor) {
        map.put(dst, getCursorStringValue(cursor, src));
    }

    private void putMapLongValue(Map<String, Object> map, String src, String dst, Cursor cursor) {
        map.put(dst, getCursorLongValue(cursor, src));
    }

    private void putMapBooleanValue(Map<String, Object> map, String src, String dst, Cursor cursor) {
        map.put(dst, Boolean.valueOf(getCursorBoolValue(cursor, src)));
    }

    private String getMapStringValue(Map<String, Object> map, String key) {
        return (String) map.get(key);
    }

    @SuppressLint("UseValueOf")
    private Long getMapLongValue(Map<String, Object> map, String key) {
        Long ret = null;

        Object o = map.get(key);
        if (o instanceof Long) { ret = (Long) o; }

        // WTF???
        else if (o instanceof Double) { ret = new Long((long) ((Double) o).doubleValue()); }

        return ret;
    }

    private boolean getMapBoolValue(Map<String, Object> map, String key) {
        return ((Boolean) map.get(key)).booleanValue();
    }

    private String getCursorStringValue(Cursor cursor, String columnName) {
        return cursor.getString(cursor.getColumnIndex(columnName));
    }

    private Long getCursorLongValue(Cursor cursor, String columnName) {
        return Long.valueOf(cursor.getLong(cursor.getColumnIndex(columnName)));
    }

    private boolean getCursorBoolValue(Cursor cursor, String columnName) {
        return !cursor.isNull(cursor.getColumnIndex(columnName));
    }
}

package com.enterpriseandroid.database.sql;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;


public final class SQLSamples {
    public static final long HISTORY = 1000 * 60 * 60 * 24 * 365 * 20;

    static final String TAB_KEYS = "keys";
    static final String COL_KEY = "key";
    static final String COL_FK = "fk";
    static final String TAB_VALS = "vals";
    static final String COL_ID = "id";
    static final String COL_VAL = "val";

    public static void raw(SQLiteDatabase db, String tableName, String person) {

        // SQL exec example
        db.execSQL("create table pets(name text, age integer)");

        // Raw query example
        Cursor c = db.rawQuery("pragam table_info(" + tableName + ")", null);
        c.close();

        // Use query arguments instead!!
        db.execSQL("delete from people where name=" + person
            + " and added_date < "
                + String.valueOf(System.currentTimeMillis() - HISTORY));
    }

    // Uses the Sample pets database
    // CREATE TABLE pets(name text, age integer);
    // INSERT INTO pets VALUES("linus", 14);
    // INSERT INTO pets VALUES("fellini", 15);
    // INSERT INTO pets VALUES("totoro", 8);
    public static void delete(SQLiteDatabase db) {
        db.delete("pets", "age > 10 AND age < 20", null);
        db.delete("pets", "age = ? OR name = ?", new String[] {"15", "linus"});
        db.delete("pets", "id = ?2 AND val = ?1", new String[] {"linus", "15"});
    }

    // Uses the Sample pets database
    // CREATE TABLE pets(name text, age integer);
    // INSERT INTO pets VALUES("linus", 14);
    // INSERT INTO pets VALUES("fellini", 15);
    // INSERT INTO pets VALUES("totoro", 8);
    public static void update(SQLiteDatabase db) {
        ContentValues newAges = new ContentValues();
        newAges.put("age", Integer.valueOf(99));
        db.update("pets",
            newAges,
            "name = ? OR name = ?",
            new String[] {"linus", "fellini"});

    }

    // Uses the Sample pets database
    // CREATE TABLE pets(name text, age integer);
    // INSERT INTO pets VALUES("linus", 14);
    // INSERT INTO pets VALUES("fellini", 15);
    // INSERT INTO pets VALUES("totoro", 8);
    public static void insert(SQLiteDatabase db) {
        ContentValues newPet = new ContentValues();
        newPet.put("name", "luna");
        newPet.put("age", Integer.valueOf(99));
        db.insert("pets", null, newPet);
    }

    // Uses a test database
    // CREATE TABLE test(id integer primary key, key text unique, val text unique);
    // INSERT INTO test VALUES(1, "foo", "foo")
    // INSERT INTO test VALUES(2, "bar", "bar")
    public static void replace(SQLiteDatabase db) {
        ContentValues newTest = new ContentValues();
        newTest.put("key", "bar");
        newTest.put("val", "foo");
        db.replace("test", null, newTest);
        // SELECT * FROM test;
        // 3|foo|bar
    }

    // Uses the Sample pets database
    // CREATE TABLE pets(name text, age integer);
    // INSERT INTO pets VALUES("linus", 14);
    // INSERT INTO pets VALUES("fellini", 15);
    // INSERT INTO pets VALUES("totoro", 8);
    public static void simpleQuery(SQLiteDatabase db) {
        db.query(
            "pets",
            new String[] { "name", "age" },
            "age > ?",
            new String[] { "50" },
            null, // group by
            null, // having
            "name ASC");
    }

    // Use a multi-table key/value database
    // CREATE TABLE vals(id integer PRIMARY KEY, val text)
    // CREATE TABLE keys(key text, fk integer references ref(id))
    // INSERT INTO vals VALUES(1, 'bar')
    // INSERT INTO vals VALUES(2, 'baz')
    // INSERT INTO vals VALUES(3, 'zqx3')
    // INSERT INTO vals VALUES(4, 'quux')
    // INSERT INTO keys VALUES('one', 1)
    // INSERT INTO keys VALUES('one', 4)
    // INSERT INTO keys VALUES('two', 2)
    public static void queryBuilder(SQLiteDatabase db) {
        Cursor c;

        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables("keys k INNER JOIN vals v ON k.fk = v.id");
        c = qb.query(db,
            new String[] {"k.key AS kk", "v.val AS vv"},
            "kk = ?",
            new String[] { "two" },
            null,
            null,
            "vv DESC",
            null);
        c.close();

        qb.setTables("keys k, vals v");
        c = qb.query(db,
            new String[] {"k.key as kk", "v.val as vv"},
            "k.fk = v.id AND kk = ?",
            new String[] { "two" },
            null,
            null,
            "vv DESC",
            null);
        c.close();
    }

    public static void transactionTemplate(SQLiteDatabase db) {
        db.beginTransaction();
        try {
            // sql...
            db.setTransactionSuccessful();
        }
        finally {
            db.endTransaction();
        }
    }

    // Example Cursor processing
    @SuppressWarnings("unused")
    private void getValues(SQLiteDatabase db) {
        Cursor c = db.query(
            "keyval",
            new String[] { "_id", "key" },
            null, null, null, null, null);
        try {
        int idIdx = c.getColumnIndex("_id");
        int keyIdx = c.getColumnIndex("key");
        while (c.moveToNext()) {
            Integer id = getInt(c, idIdx);
            String key = c.getString(keyIdx);
            // É process the extracted value
        }
        }
        finally {
            c.close();
        }
    }

    private Integer getInt(Cursor c, int idx) {
        if (c.isNull(idx)) { return null; }
        long n = c.getLong(idx);
        if ((Integer.MAX_VALUE < n) || (Integer.MIN_VALUE > n)) {
            throw new RuntimeException("Not an integer: " + n);
        }
        return Integer.valueOf((int) n);
    }
}
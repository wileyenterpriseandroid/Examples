package com.enterpriseandroid.database.keyval.data;

import android.content.ContentValues;


public class ColumnDef {
    public static enum Type {
        BOOLEAN, BYTE, BYTEARRAY, DOUBLE, FLOAT, INTEGER, LONG, SHORT, STRING
    };

    private final String name;
    private final Type type;

    public ColumnDef(String name, Type type) {
        this.name = name;
        this.type = type;
    }

    public void copy(String srcCol, ContentValues src, ContentValues dst) {
        switch (type) {
            case BOOLEAN:
                dst.put(name, src.getAsBoolean(srcCol));
                break;
            case BYTE:
                dst.put(name, src.getAsByte(srcCol));
                break;
            case BYTEARRAY:
                dst.put(name, src.getAsByteArray(srcCol));
                break;
            case DOUBLE:
                dst.put(name, src.getAsDouble(srcCol));
                break;
            case FLOAT:
                dst.put(name, src.getAsFloat(srcCol));
                break;
            case INTEGER:
                dst.put(name, src.getAsInteger(srcCol));
                break;
            case LONG:
                dst.put(name, src.getAsLong(srcCol));
                break;
            case SHORT:
                dst.put(name, src.getAsShort(srcCol));
                break;
            case STRING:
                dst.put(name, src.getAsString(srcCol));
                break;
        }
    }
}
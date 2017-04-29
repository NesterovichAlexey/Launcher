package com.example.alexey.mylauncher.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import java.util.ArrayList;

public class UriDatabaseHelper extends SQLiteOpenHelper {
    public static final int VERSION = 1;
    public static final String DB_NAME = "uri.dp";
    public static final String TABLE_NAME = "uri";

    public static interface Columns extends BaseColumns {
        String FIELD_URI_NAME = "uri_name";
    }
    public static final String CREATE_TABLE_SCRIPT =
            "CREATE TABLE IF NOT EXISTS " + TABLE_NAME +
                    "(" +
                    Columns.FIELD_URI_NAME + " TEXT" +
                    ")";
    public static final String DROP_TABLE_SCRIPT =
            "DROP TABLE IF EXISTS " + TABLE_NAME;

    public UriDatabaseHelper(Context context) {
        super(context, DB_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_SCRIPT);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(DROP_TABLE_SCRIPT);
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    public ArrayList<String> readRecords() {
        ArrayList<String> list = new ArrayList<>();
        try {
            SQLiteDatabase db = getReadableDatabase();
            Cursor cursor = db.query(
                    TABLE_NAME,
                    new String[]{Columns.FIELD_URI_NAME},
                    null, null,
                    null, null, null, null
            );
            int idColumnUri = cursor.getColumnIndex(Columns.FIELD_URI_NAME);
            while (cursor.moveToNext()) {
                list.add(cursor.getString(idColumnUri));
            }
            cursor.close();
        } catch (SQLiteException ignored) {}
        return list;
    }

    public void writeRecords(ArrayList<String> uriList) {
        try {
            SQLiteDatabase db = getWritableDatabase();
            db.delete(TABLE_NAME, null, null);
            for (String uri : uriList) {
                ContentValues contentValues = new ContentValues();
                contentValues.put(Columns.FIELD_URI_NAME, uri);
                db.insert(TABLE_NAME, null, contentValues);
            }
            db.close();
        } catch (SQLiteException ignored) {}
    }
}

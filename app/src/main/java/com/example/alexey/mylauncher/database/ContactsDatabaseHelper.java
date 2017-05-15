package com.example.alexey.mylauncher.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import com.example.alexey.mylauncher.recyclerview.ElementInfo;

import java.util.ArrayList;


public class ContactsDatabaseHelper extends SQLiteOpenHelper {
    public static final int VERSION = 1;
    public static final String DB_NAME = "contacts.dp";
    public static final String TABLE_NAME = "contacts";

    public static interface Columns extends BaseColumns {
        String FIELD_NAME = "name";
        String FIELD_PHONE_NUMBER = "phone_number";
        String FIELD_CONTACT_ID = "contact_id";
    }
    public static final String CREATE_TABLE_SCRIPT =
            "CREATE TABLE IF NOT EXISTS " + TABLE_NAME +
                    "(" +
                    Columns.FIELD_NAME + " TEXT, " +
                    Columns.FIELD_PHONE_NUMBER + " TEXT, " +
                    Columns.FIELD_CONTACT_ID + " TEXT " +
                    ")";
    public static final String DROP_TABLE_SCRIPT =
            "DROP TABLE IF EXISTS " + TABLE_NAME;

    public ContactsDatabaseHelper(Context context) {
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

    public ArrayList<ElementInfo> readRecords() {
        ArrayList<ElementInfo> map = new ArrayList<>();
        try {
            SQLiteDatabase db = getReadableDatabase();
            Cursor cursor = db.query(
                    TABLE_NAME,
                    new String[]{Columns.FIELD_NAME, Columns.FIELD_PHONE_NUMBER, Columns.FIELD_CONTACT_ID},
                    null, null,
                    null, null, null, null
            );
            int idColumnName = cursor.getColumnIndex(Columns.FIELD_NAME);
            int idColumnPhoneNumber = cursor.getColumnIndex(Columns.FIELD_PHONE_NUMBER);
            int idColumnContactId = cursor.getColumnIndex(Columns.FIELD_CONTACT_ID);
            while (cursor.moveToNext()) {
                String name = cursor.getString(idColumnName);
                String phoneNumber = cursor.getString(idColumnPhoneNumber);
                ElementInfo elementInfo = new ElementInfo(name, phoneNumber, null, false);
                elementInfo.contactId = cursor.getString(idColumnContactId);
                map.add(elementInfo);
            }
            cursor.close();
        } catch (SQLiteException ignored) {}
        return map;
    }

    public void writeRecords(ArrayList<ElementInfo> contactsList) {
        try {
            SQLiteDatabase db = getWritableDatabase();
            db.delete(TABLE_NAME, null, null);
            for (ElementInfo elementInfo : contactsList) {
                ContentValues contentValues = new ContentValues();
                contentValues.put(Columns.FIELD_NAME, (String) elementInfo.appName);
                contentValues.put(Columns.FIELD_PHONE_NUMBER, elementInfo.packageName);
                contentValues.put(Columns.FIELD_CONTACT_ID, elementInfo.contactId);
                db.insert(TABLE_NAME, null, contentValues);
            }
            db.close();
        } catch (SQLiteException ignored) {}
    }
}

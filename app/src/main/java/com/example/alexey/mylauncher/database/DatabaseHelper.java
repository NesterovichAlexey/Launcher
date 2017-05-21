package com.example.alexey.mylauncher.database;

import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.provider.BaseColumns;
import android.support.annotation.NonNull;

import com.example.alexey.mylauncher.R;
import com.example.alexey.mylauncher.recyclerview.items.App;
import com.example.alexey.mylauncher.recyclerview.items.Contact;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final int MILLIS_IN_DAY = 1000 * 60 * 60 * 24;
    private static final int VERSION = 1;
    private static final String DB_NAME = "launcher.db";
    //App---------------------------------------------------------------//
    private static final String APP_TABLE_NAME = "app";
    private interface AppColumns extends BaseColumns {
        String FIELD_NAME = "name";
        String FIELD_PACKAGE_NAME = "package_name";
        String FIELD_CLICK_COUNT = "click_count";
        String FIELD_FAVORITES = "favorites";
    }
    private static final String CREATE_APP_TABLE =
            "CREATE TABLE IF NOT EXISTS " + APP_TABLE_NAME +
                    "(" +
                    AppColumns.FIELD_NAME + " TEXT, " +
                    AppColumns.FIELD_PACKAGE_NAME + " TEXT, " +
                    AppColumns.FIELD_CLICK_COUNT + " NUMBER, " +
                    AppColumns.FIELD_FAVORITES + " NUMBER" +
                    ")";
    private static final String DROP_APP_TABLE =
            "DROP TABLE IF EXISTS " + APP_TABLE_NAME;
    //Contact-----------------------------------------------------------//
    private static final String CONTACT_TABLE_NAME = "contact";
    private interface ContactColumns extends BaseColumns {
        String FIELD_NAME = "name";
        String FIELD_PHONE_NUMBER = "phone_number";
        String FIELD_CONTACT_ID = "contact_id";
    }
    private static final String CREATE_CONTACT_TABLE =
            "CREATE TABLE IF NOT EXISTS " + CONTACT_TABLE_NAME +
                    "(" +
                    ContactColumns.FIELD_NAME + " TEXT, " +
                    ContactColumns.FIELD_PHONE_NUMBER + " TEXT, " +
                    ContactColumns.FIELD_CONTACT_ID + " TEXT" +
                    ")";
    private static final String DROP_CONTACT_TABLE =
            "DROP TABLE IF EXISTS " + CONTACT_TABLE_NAME;
    //Uri---------------------------------------------------------------//
    private static final String URI_TABLE_NAME = "uri";
    private interface UriColumns extends BaseColumns {
        String FIELD_NAME = "name";
        String FIELD_TIME = "time";
    }
    private static final String CREATE_URI_TABLE =
            "CREATE TABLE IF NOT EXISTS " + URI_TABLE_NAME +
                    "(" +
                    UriColumns.FIELD_NAME + " TEXT, " +
                    UriColumns.FIELD_TIME + " NUMBER " +
                    ")";
    private static final String DROP_URI_TABLE =
            "DROP TABLE IF EXISTS " + URI_TABLE_NAME;
    //------------------------------------------------------------------//

    private Context context;
    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_APP_TABLE);
        db.execSQL(CREATE_CONTACT_TABLE);
        db.execSQL(CREATE_URI_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(DROP_APP_TABLE);
        db.execSQL(DROP_CONTACT_TABLE);
        db.execSQL(DROP_URI_TABLE);
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    //App---------------------------------------------------------------//
    public ArrayList<App> loadApp() {
        ArrayList<App> appList = new ArrayList<>();
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = getReadableDatabase();
            cursor = db.query(
                    APP_TABLE_NAME,
                    new String[]{AppColumns.FIELD_NAME,
                                AppColumns.FIELD_PACKAGE_NAME,
                                AppColumns.FIELD_CLICK_COUNT,
                                AppColumns.FIELD_FAVORITES},
                    null, null,
                    null, null, null, null
            );
            int idColumnName = cursor.getColumnIndex(AppColumns.FIELD_NAME);
            int idColumnPackageName = cursor.getColumnIndex(AppColumns.FIELD_PACKAGE_NAME);
            int idColumnClickCount = cursor.getColumnIndex(AppColumns.FIELD_CLICK_COUNT);
            int idColumnFavorites = cursor.getColumnIndex(AppColumns.FIELD_FAVORITES);
            while (cursor.moveToNext()) {
                String name = cursor.getString(idColumnName);
                String packageName = cursor.getString(idColumnPackageName);
                int clickCount = cursor.getInt(idColumnClickCount);
                boolean favorites = cursor.getInt(idColumnFavorites) == 1;
                App app = new App(name, packageName, null, -1);
                app.setClickCount(clickCount);
                app.setFavorites(favorites);
                appList.add(app);
            }
        } catch (Exception ignored) {
        } finally {
            if (db != null)
                db.close();
            if (cursor != null)
                cursor.close();
        }
        return appList;
    }

    public void saveApp(@NonNull ArrayList<App> appList) {
        SQLiteDatabase db = null;
        try {
            db = getWritableDatabase();
            db.delete(APP_TABLE_NAME, null, null);
            for (App app : appList) {
                ContentValues contentValues = new ContentValues();
                contentValues.put(AppColumns.FIELD_NAME, app.getName());
                contentValues.put(AppColumns.FIELD_PACKAGE_NAME, app.getPackageName());
                contentValues.put(AppColumns.FIELD_CLICK_COUNT, app.getClickCount());
                contentValues.put(AppColumns.FIELD_FAVORITES, app.isFavorites() ? 1 : 0);
                db.insert(APP_TABLE_NAME, null, contentValues);
            }
        } catch (Exception ignored) {
        } finally {
            if (db != null)
                db.close();
        }
    }
    //Contact-----------------------------------------------------------//
    public ArrayList<Contact> loadContact() {
        ArrayList<Contact> contactList = new ArrayList<>();
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = getReadableDatabase();
            cursor = db.query(
                    CONTACT_TABLE_NAME,
                    new String[]{ContactColumns.FIELD_NAME,
                                ContactColumns.FIELD_PHONE_NUMBER,
                                ContactColumns.FIELD_CONTACT_ID},
                    null, null,
                    null, null, null, null
            );
            int idColumnName = cursor.getColumnIndex(ContactColumns.FIELD_NAME);
            int idColumnPhoneNumber = cursor.getColumnIndex(ContactColumns.FIELD_PHONE_NUMBER);
            int idColumnContactId = cursor.getColumnIndex(ContactColumns.FIELD_CONTACT_ID);
            while (cursor.moveToNext()) {
                String name = cursor.getString(idColumnName);
                String phoneNumber = cursor.getString(idColumnPhoneNumber);
                String contactId = cursor.getString(idColumnContactId);
                Contact contact = new Contact(name, phoneNumber, (BitmapDrawable) context.getResources().getDrawable(R.drawable.phone), contactId);
                contactList.add(contact);
            }
        } catch (Exception ignored) {
        } finally {
            if (db != null)
                db.close();
            if (cursor != null)
                cursor.close();
        }
        return contactList;
    }

    public void saveContact(@NonNull ArrayList<Contact> contactList) {
        SQLiteDatabase db = null;
        try {
            db = getWritableDatabase();
            db.delete(CONTACT_TABLE_NAME, null, null);
            for (Contact contact : contactList) {
                ContentValues contentValues = new ContentValues();
                contentValues.put(ContactColumns.FIELD_NAME, contact.getName());
                contentValues.put(ContactColumns.FIELD_PHONE_NUMBER, contact.getPhoneNumber());
                contentValues.put(ContactColumns.FIELD_CONTACT_ID, contact.getContactId());
                db.insert(CONTACT_TABLE_NAME, null, contentValues);
            }
        } catch (Exception ignored) {
        } finally {
            if (db != null)
                db.close();
        }
    }
    //Uri---------------------------------------------------------------//
    public void deleteUri() {
        SQLiteDatabase db = null;
        try {
            db = getWritableDatabase();
            db.delete(URI_TABLE_NAME, null, null);
        } catch (Exception ignore) {
        } finally {
            if (db != null)
                db.close();
        }
    }

    public long insertUri(@NonNull String name) {
        SQLiteDatabase db = null;
        try {
            db = getWritableDatabase();
            db.delete(URI_TABLE_NAME, UriColumns.FIELD_NAME + " = ?", new String[]{name});
            ContentValues contentValues = new ContentValues();
            contentValues.put(UriColumns.FIELD_NAME, name);
            contentValues.put(UriColumns.FIELD_TIME, System.currentTimeMillis());
            return db.insert(URI_TABLE_NAME, null, contentValues);
        } catch (Exception ignore) {
            return -1;
        } finally {
            if (db != null)
                db.close();
        }
    }

    public Cursor getAllUri() {
        return getReadableDatabase().query(URI_TABLE_NAME,
                new String[]{UriColumns.FIELD_NAME},
                null, null,
                null, null,
                UriColumns.FIELD_TIME + " DESC",
                null);
    }

    public Cursor getLastUri() {
        return getReadableDatabase().query(URI_TABLE_NAME,
                new String[]{UriColumns.FIELD_NAME},
                null, null,
                null, null,
                UriColumns.FIELD_TIME + " DESC",
                "1");
    }

    public Cursor getTodayUri() {
        return getReadableDatabase().query(URI_TABLE_NAME,
                new String[]{UriColumns.FIELD_NAME},
                UriColumns.FIELD_TIME + " >= ?", new String[]{String.valueOf(System.currentTimeMillis() / MILLIS_IN_DAY * MILLIS_IN_DAY)},
                null, null,
                UriColumns.FIELD_TIME + " DESC",
                "1");
    }
    //------------------------------------------------------------------//
}

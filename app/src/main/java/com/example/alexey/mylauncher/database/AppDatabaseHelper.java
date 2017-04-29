package com.example.alexey.mylauncher.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Pair;

import com.example.alexey.mylauncher.recyclerview.AppInfo;

import java.util.ArrayList;
import java.util.HashMap;

public class AppDatabaseHelper extends SQLiteOpenHelper {
    public static final int VERSION = 1;
    public static final String DB_NAME = "app.dp";
    public static final String TABLE_NAME = "app";

    public static interface Columns extends BaseColumns {
        String FIELD_PACKAGE_NAME = "package_name";
        String FIELD_CLICK_COUNT = "click_count";
        String FIELD_IS_FAVORITES = "is_favorites";
    }
    public static final String CREATE_TABLE_SCRIPT =
            "CREATE TABLE IF NOT EXISTS " + TABLE_NAME +
                    "(" +
                    Columns.FIELD_PACKAGE_NAME + " TEXT, " +
                    Columns.FIELD_CLICK_COUNT + " NUMBER, " +
                    Columns.FIELD_IS_FAVORITES + " NUMBER" +
                    ")";
    public static final String DROP_TABLE_SCRIPT =
            "DROP TABLE IF EXISTS " + TABLE_NAME;

    public AppDatabaseHelper(Context context) {
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

    public HashMap<String, Pair<Integer, Boolean>> readRecords() {
        HashMap<String, Pair<Integer, Boolean>> map = new HashMap<>();
        try {
            SQLiteDatabase db = getReadableDatabase();
            Cursor cursor = db.query(
                    TABLE_NAME,
                    new String[]{Columns.FIELD_PACKAGE_NAME, Columns.FIELD_CLICK_COUNT, Columns.FIELD_IS_FAVORITES},
                    null, null,
                    null, null, null, null
            );
            int idColumnPackageName = cursor.getColumnIndex(Columns.FIELD_PACKAGE_NAME);
            int idColumnClickCount = cursor.getColumnIndex(Columns.FIELD_CLICK_COUNT);
            int idColumnIsFavorites = cursor.getColumnIndex(Columns.FIELD_IS_FAVORITES);
            while (cursor.moveToNext()) {
                String packageName = cursor.getString(idColumnPackageName);
                int clickCount = cursor.getInt(idColumnClickCount);
                boolean isFavorites = cursor.getInt(idColumnIsFavorites) == 1;
                map.put(packageName, new Pair<>(clickCount, isFavorites));
            }
            cursor.close();
        } catch (SQLiteException ignored) {}
        return map;
    }

    public void writeRecords(ArrayList<AppInfo> appList) {
        try {
            SQLiteDatabase db = getWritableDatabase();
            db.delete(TABLE_NAME, null, null);
            for (AppInfo appInfo : appList) {
                ContentValues contentValues = new ContentValues();
                contentValues.put(Columns.FIELD_PACKAGE_NAME, appInfo.packageName);
                contentValues.put(Columns.FIELD_CLICK_COUNT, appInfo.clickCount);
                contentValues.put(Columns.FIELD_IS_FAVORITES, appInfo.isFavorites ? 1 : 0);
                db.insert(TABLE_NAME, null, contentValues);
            }
            db.close();
        } catch (SQLiteException ignored) {}
    }
}

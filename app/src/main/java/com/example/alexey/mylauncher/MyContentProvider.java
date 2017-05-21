package com.example.alexey.mylauncher;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Binder;
import android.os.Process;
import android.support.annotation.NonNull;
import android.support.v4.content.PermissionChecker;

import com.example.alexey.mylauncher.database.DatabaseHelper;

public class MyContentProvider extends ContentProvider {
    private static final String AUTHORITY = "com.example.alexey.mylauncher";
    private static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/uri");

    public static final String URI_NAME = "name";

    private static final String ALL_URI_PATH = "uri";
    private static final int ALL_URI_CODE = 1;
    private static final String LAST_URI_PATH = "uri/last";
    private static final int LAST_URI_CODE = 2;
    private static final String TODAY_URI_PATH = "uri/today";
    private static final int TODAY_URI_CODE = 3;
    private static final String INSERT_URI_PATH = "uri/insert";
    private static final int INSERT_URI_CODE = 4;
    private static final String CLEAR_URI_PATH = "uri/clear";
    private static final int CLEAR_URI_CODE = 5;

    private static final UriMatcher uriMatcher;
    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(AUTHORITY, ALL_URI_PATH, ALL_URI_CODE);
        uriMatcher.addURI(AUTHORITY, LAST_URI_PATH, LAST_URI_CODE);
        uriMatcher.addURI(AUTHORITY, TODAY_URI_PATH, TODAY_URI_CODE);
        uriMatcher.addURI(AUTHORITY, INSERT_URI_PATH, INSERT_URI_CODE);
        uriMatcher.addURI(AUTHORITY, CLEAR_URI_PATH, CLEAR_URI_CODE);
    }

    private DatabaseHelper dbHelper;

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        if (uriMatcher.match(uri) != CLEAR_URI_CODE)
            throw new IllegalArgumentException("Wrong URI: " + uri);
        dbHelper.deleteUri();
        return 0;
    }

    @Override
    public String getType(Uri uri) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        if (uriMatcher.match(uri) != INSERT_URI_CODE)
            throw new IllegalArgumentException("Wrong URI: " + uri);
        if (values.getAsString(URI_NAME) == null)
            throw new IllegalArgumentException("Wrong values");
        long id = dbHelper.insertUri(values.getAsString("name"));
        return ContentUris.withAppendedId(CONTENT_URI, id);
    }

    @Override
    public boolean onCreate() {
        dbHelper = new DatabaseHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        switch (uriMatcher.match(uri)) {
            case ALL_URI_CODE:
                if (getContext() == null || checkCallingPermission(getContext(), Manifest.permission.READ_ALL, null) != PermissionChecker.PERMISSION_GRANTED)
                    throw new SecurityException("Permission Denial");
                return dbHelper.getAllUri();
            case LAST_URI_CODE:
                return dbHelper.getLastUri();
            case TODAY_URI_CODE:
                return dbHelper.getTodayUri();
            default:
                throw new IllegalArgumentException("Wrong URI: " + uri);
        }
    }

    private int checkCallingPermission(@NonNull Context context, @NonNull String permission, String packageName) {
        if (Binder.getCallingPid() == Process.myPid()) {
            return PackageManager.PERMISSION_GRANTED;
        }
        return PermissionChecker.checkCallingPermission(context, permission, packageName);
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}

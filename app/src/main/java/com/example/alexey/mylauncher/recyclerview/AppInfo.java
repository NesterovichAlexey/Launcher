package com.example.alexey.mylauncher.recyclerview;

import android.graphics.drawable.Drawable;


public class AppInfo {
    public CharSequence appName;
    public String packageName;
    public Drawable icon;
    public long clickCount;
    public long timeInstalled;
    public boolean isFavorites;

    public AppInfo(CharSequence appName, String packageName, Drawable icon) {
        this.appName = appName;
        this.packageName = packageName;
        this.icon = icon;
    }
}

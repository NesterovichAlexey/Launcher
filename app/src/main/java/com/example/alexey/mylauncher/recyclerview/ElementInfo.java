package com.example.alexey.mylauncher.recyclerview;

import android.graphics.drawable.Drawable;


public class ElementInfo {
    public CharSequence appName;
    public String packageName;
    public Drawable icon;
    public boolean isApp;
    public long clickCount;
    public long timeInstalled;
    public boolean isFavorites;
    public String contactId;

    public ElementInfo(CharSequence appName, String packageName, Drawable icon, boolean isApp) {
        this.appName = appName;
        this.packageName = packageName;
        this.icon = icon;
        this.isApp = isApp;
    }

    @Override
    public boolean equals(Object obj) {
        try {
            ElementInfo o = (ElementInfo) obj;
            boolean f = true;
            if (appName == null) {
                f &= o.appName == null;
            } else {
                f &= appName.equals(o.appName);
            }
            if (packageName == null) {
                f &= o.packageName == null;
            } else {
                f &= packageName.equals(o.packageName);
            }
            return f;
        } catch (Exception e) {
            return false;
        }
    }
}

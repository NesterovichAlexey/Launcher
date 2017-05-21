package com.example.alexey.mylauncher.recyclerview.items;

import android.graphics.drawable.BitmapDrawable;
import android.support.annotation.NonNull;

public class App extends Item {
    private String packageName;
    private BitmapDrawable icon;
    private int clickCount;
    private long timeInstalled;
    private boolean favorites;

    public App(@NonNull String name, @NonNull String packageName, BitmapDrawable icon, long timeInstalled) {
        super(ItemType.APP.getId(), name);
        this.packageName = packageName;
        this.icon = icon;
        this.timeInstalled = timeInstalled;
        clickCount = 0;
        favorites = false;
    }

    public BitmapDrawable getIcon() {
        return icon;
    }

    public void setIcon(BitmapDrawable icon) {
        this.icon = icon;
    }

    public int getClickCount() {
        return clickCount;
    }

    public void setClickCount(int clickCount) {
        this.clickCount = clickCount;
    }

    public void click() {
        ++clickCount;
    }

    public long getTimeInstalled() {
        return timeInstalled;
    }

    public void setTimeInstalled(long timeInstalled) {
        this.timeInstalled = timeInstalled;
    }

    public boolean isFavorites() {
        return favorites;
    }

    public void setFavorites(boolean favorites) {
        this.favorites = favorites;
    }

    public String getPackageName() {
        return packageName;
    }
}

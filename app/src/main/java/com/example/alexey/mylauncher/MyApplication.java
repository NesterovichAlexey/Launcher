package com.example.alexey.mylauncher;

import android.app.Application;

import com.yandex.metrica.YandexMetrica;

public class MyApplication extends Application {
    private static final String API_KEY = "35ea4d97-9015-40f1-9231-18b25f28766c";

    @Override
    public void onCreate() {
        super.onCreate();

        YandexMetrica.activate(getApplicationContext(), API_KEY);
        YandexMetrica.enableActivityAutoTracking(this);
    }
}

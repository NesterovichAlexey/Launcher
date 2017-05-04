package com.example.alexey.mylauncher;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.alexey.mylauncher.main.MainActivity;
import com.example.alexey.mylauncher.main.MainFragment;


public class MyReceiver extends BroadcastReceiver {
    private static MainActivity activity;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_PACKAGE_REMOVED)) {
            if (activity != null)
                activity.uninstallApp(intent.getData().getSchemeSpecificPart());
        } else if (intent.getAction().equals(Intent.ACTION_PACKAGE_ADDED)) {
            if (activity != null)
                activity.installApp(intent.getData().getSchemeSpecificPart());
        }
    }

    public void setActivity(MainActivity activity) {
        this.activity = activity;
    }
}

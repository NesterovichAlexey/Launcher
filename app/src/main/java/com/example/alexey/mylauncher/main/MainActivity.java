package com.example.alexey.mylauncher.main;

import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Pair;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;

import com.example.alexey.mylauncher.SettingsActivity;
import com.example.alexey.mylauncher.database.AppDatabaseHelper;
import com.example.alexey.mylauncher.MyReceiver;
import com.example.alexey.mylauncher.R;
import com.example.alexey.mylauncher.database.UriDatabaseHelper;
import com.example.alexey.mylauncher.recyclerview.AppInfo;
import com.example.alexey.mylauncher.recyclerview.AppRecyclerViewAdapter;
import com.example.alexey.mylauncher.welcome.WelcomeActivity;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

public class MainActivity extends AppCompatActivity {
    private SharedPreferences preferences;
    private ViewPager pager;
    private int columnCount;
    private AppDatabaseHelper appDatabaseHelper;
    private UriDatabaseHelper uriDatabaseHelper;
    private ArrayList<AppInfo> app;
    private TreeSet<AppInfo> newApp, popularApp;
    private MyReceiver receiver;
    private final ArrayList<AppInfo> appList = new ArrayList<>();
    private final ArrayList<AppInfo> favoritesAppList = new ArrayList<>();
    private final ArrayList<String> uriList = new ArrayList<>();
    private AppRecyclerViewAdapter appAdapter, favoritesAppAdapter;
    private ArrayAdapter<String> uriAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (preferences.getString("theme", "light").equals("light"))
            setTheme(R.style.LightTheme);
        else
            setTheme(R.style.DarkTheme);
        super.onCreate(savedInstanceState);
        if (!preferences.getBoolean("hide_welcome_activity", false)) {
            Intent intent = new Intent(this, WelcomeActivity.class);
            startActivity(intent);
            finish();
            return;
        }
        setContentView(R.layout.activity_main);

        init();
        pager = (ViewPager) findViewById(R.id.main_pager);
        pager.setAdapter(new MainPagerAdapter(getSupportFragmentManager(), !preferences.getBoolean("hide_favorites", true)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (preferences.getBoolean("clear_fav", false)) {
            preferences.edit().putBoolean("clear_fav", false).apply();
            while (favoritesAppList.size() > 1) {
                changeFavorites(favoritesAppList.get(1), false);
            }
        }
        if (preferences.getBoolean("clear_uri", false)) {
            preferences.edit().putBoolean("clear_uri", false).apply();
            uriList.clear();
            uriAdapter.clear();
        }
        if (preferences.getBoolean("restart", false)) {
            preferences.edit().putBoolean("restart", false).apply();
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private void init() {
        if (preferences.getString("column_count", "1").equals("1"))
            columnCount = getResources().getInteger(R.integer.column_count_46);
        else
            columnCount = getResources().getInteger(R.integer.column_count_57);
        appDatabaseHelper = new AppDatabaseHelper(this);
        uriDatabaseHelper = new UriDatabaseHelper(this);
        app = new ArrayList<>();
        newApp = new TreeSet<>(new Comparator<AppInfo>() {
            @Override
            public int compare(AppInfo app1, AppInfo app2) {
                long x = -app1.timeInstalled;
                long y = -app2.timeInstalled;
                if (x == y) {
                    return app1.packageName.compareTo(app2.packageName);
                } else
                    return x < y ? -1 : 1;
            }
        });
        popularApp = new TreeSet<>(new Comparator<AppInfo>() {
            @Override
            public int compare(AppInfo app1, AppInfo app2) {
                long x = -app1.clickCount;
                long y = -app2.clickCount;
                if (x == y) {
                    return app1.packageName.compareTo(app2.packageName);
                } else
                    return x < y ? -1 : 1;
            }
        });
        initApp();
        initAppList();
        appAdapter = new AppRecyclerViewAdapter(this, appList, new AppRecyclerViewAdapter.CreateContextMenuListener() {
            @Override
            public void create(ContextMenu menu, final AppInfo appInfo) {
                menu.add("Инфо").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        intent.setData(Uri.parse("package:" + appInfo.packageName));
                        startActivity(intent);
                        return true;
                    }
                });
                menu.add("Удалить").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        Intent intent = new Intent(Intent.ACTION_DELETE);
                        intent.setData(Uri.parse("package:" + appInfo.packageName));
                        startActivity(intent);
                        return true;
                    }
                });
                menu.add("Добавить в избранное").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        changeFavorites(appInfo, true);
                        return true;
                    }
                });
            }
        });
        initFavoritesAppList();
        favoritesAppAdapter = new AppRecyclerViewAdapter(this, favoritesAppList, new AppRecyclerViewAdapter.CreateContextMenuListener() {
            @Override
            public void create(ContextMenu menu, final AppInfo appInfo) {
                menu.add("Удалить из избранного").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        changeFavorites(appInfo, false);
                        return true;
                    }
                });
            }
        });
        initUri();
        uriAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, uriList);
        initReceiver();
    }

    private void initApp() {
        HashMap<String, Pair<Integer, Boolean>> map = appDatabaseHelper.readRecords();
        PackageManager pm = getPackageManager();
        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> app = pm.queryIntentActivities(intent, 0);
        for (ResolveInfo ri : app) {
            String packageName = ri.activityInfo.packageName;
            if (getPackageName().equals(packageName))
                continue;
            CharSequence appName = ri.loadLabel(pm);
            Drawable icon = ri.loadIcon(pm);
            AppInfo appInfo = new AppInfo(appName, packageName, icon);
            if (map.containsKey(packageName)) {
                appInfo.clickCount = map.get(packageName).first;
                appInfo.isFavorites = map.get(packageName).second;
            } else {
                appInfo.clickCount = 0;
                appInfo.isFavorites = false;
            }
            try {
                appInfo.timeInstalled = createPackageContext(ri.activityInfo.packageName, 0)
                        .getPackageManager()
                        .getPackageInfo(ri.activityInfo.packageName, 0)
                        .lastUpdateTime;
            } catch (PackageManager.NameNotFoundException ignored) {
                appInfo.timeInstalled = 0;
            }
            this.app.add(appInfo);
            newApp.add(appInfo);
            popularApp.add(appInfo);
        }
    }

    private void initAppList() {
        appList.clear();
        appList.add(new AppInfo("Popular", null, null));
        appList.addAll(getPopularApp(columnCount));
        appList.add(new AppInfo("New", null, null));
        appList.addAll(getNewApp(columnCount));
        appList.add(new AppInfo("All", null, null));
        appList.addAll(app);
    }

    private void initFavoritesAppList() {
        favoritesAppList.clear();
        favoritesAppList.add(new AppInfo("Favorites", null, null));
        favoritesAppList.addAll(getFavoritesApp());
    }

    private ArrayList<AppInfo> getNewApp(int count) {
        ArrayList<AppInfo> list = new ArrayList<>();
        Iterator<AppInfo> it = newApp.iterator();
        while (it.hasNext() && count-- != 0) {
            list.add(it.next());
        }
        return list;
    }

    private ArrayList<AppInfo> getPopularApp(int count) {
        ArrayList<AppInfo> list = new ArrayList<>();
        Iterator<AppInfo> it = popularApp.iterator();
        while (it.hasNext() && count-- != 0) {
            list.add(it.next());
        }
        return list;
    }

    private ArrayList<AppInfo> getFavoritesApp() {
        ArrayList<AppInfo> list = new ArrayList<>();
        for (AppInfo appInfo : app) {
            if (appInfo.isFavorites)
                list.add(appInfo);
        }
        return list;
    }

    public void installApp(String packageName) {
        Log.d("install", packageName);
        int pos = indexOfApp(packageName);
        if (pos == -1) {
            PackageManager pm = getPackageManager();
            Intent intent = new Intent(Intent.ACTION_MAIN, null);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            List<ResolveInfo> app = pm.queryIntentActivities(intent, 0);
            int position = -1;
            for (ResolveInfo ri : app) {
                ++position;
                if (!ri.activityInfo.packageName.equals(packageName))
                    continue;
                CharSequence appName = ri.loadLabel(pm);
                Drawable icon = ri.loadIcon(pm);
                AppInfo appInfo = new AppInfo(appName, packageName, icon);
                appInfo.clickCount = 0;
                appInfo.isFavorites = false;
                this.app.add(position, appInfo);
                pos = position;
                break;
            }
        } else {
            newApp.remove(app.get(pos));
            popularApp.remove(app.get(pos));
        }
        try {
            app.get(pos).timeInstalled = createPackageContext(app.get(pos).packageName, 0)
                    .getPackageManager()
                    .getPackageInfo(app.get(pos).packageName, 0)
                    .lastUpdateTime;
        } catch (PackageManager.NameNotFoundException ignored) {
            app.get(pos).timeInstalled = System.currentTimeMillis();
        }
        newApp.add(app.get(pos));
        popularApp.add(app.get(pos));
        initAppList();
        appAdapter.notifyDataSetChanged();
    }

    public void uninstallApp(String packageName) {
        Log.d("uninstall", packageName);
        int position = indexOfApp(packageName);
        if (position < 0 || position >= app.size())
            return;
        newApp.remove(app.get(position));
        popularApp.remove(app.get(position));
        favoritesAppList.remove(app.get(position));
        app.remove(position);
        initAppList();
        appAdapter.notifyDataSetChanged();
        favoritesAppAdapter.notifyDataSetChanged();
    }

    private int indexOfApp(String packageName) {
        int pos = 0;
        while (pos < app.size() && !app.get(pos).packageName.equals(packageName))
            ++pos;
        return pos < app.size() ? pos : -1;
    }

    private void initUri() {
        uriList.clear();
        int uriCount = Integer.parseInt(preferences.getString("uri_count", "10"));
        ArrayList<String> list = uriDatabaseHelper.readRecords();
        for (String uri : list) {
            uriList.add(uri);
            if (--uriCount == 0)
                break;
        }
    }

    private void initReceiver() {
        receiver = new MyReceiver();
        receiver.setActivity(this);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
        intentFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        registerReceiver(receiver, intentFilter);
    }

    public void click(AppInfo appInfo) {
        newApp.remove(appInfo);
        popularApp.remove(appInfo);
        ++appInfo.clickCount;
        newApp.add(appInfo);
        popularApp.add(appInfo);
        initAppList();
        appAdapter.notifyDataSetChanged();
    }

    private void changeFavorites(AppInfo appInfo, boolean newValue) {
        newApp.remove(appInfo);
        popularApp.remove(appInfo);
        appInfo.isFavorites = newValue;
        newApp.add(appInfo);
        popularApp.add(appInfo);
        initFavoritesAppList();
        favoritesAppAdapter.notifyDataSetChanged();
    }

    public AppRecyclerViewAdapter getAppAdapter() {
        return appAdapter;
    }

    public AppRecyclerViewAdapter getFavoritesAppAdapter() {
        return favoritesAppAdapter;
    }

    public ArrayList<String> getUriList() {
        return uriList;
    }

    public ArrayAdapter<String> getUriAdapter() {
        return uriAdapter;
    }

    @Override
    protected void onPause() {
        appDatabaseHelper.writeRecords(app);
        uriDatabaseHelper.writeRecords(uriList);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (receiver != null)
            unregisterReceiver(receiver);
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add("Preferences").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
                return true;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }
}

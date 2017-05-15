package com.example.alexey.mylauncher.main;

import android.content.ContentValues;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Pair;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.example.alexey.mylauncher.SettingsActivity;
import com.example.alexey.mylauncher.database.AppDatabaseHelper;
import com.example.alexey.mylauncher.MyReceiver;
import com.example.alexey.mylauncher.R;
import com.example.alexey.mylauncher.database.ContactsDatabaseHelper;
import com.example.alexey.mylauncher.database.UriDatabaseHelper;
import com.example.alexey.mylauncher.recyclerview.ElementInfo;
import com.example.alexey.mylauncher.recyclerview.AppRecyclerViewAdapter;
import com.example.alexey.mylauncher.welcome.WelcomeActivity;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final int CONTACTS_MAX = 10;
    private static final int CONTACT_REQUEST = 1;

    private static final Uri DELETE_ALL_URI = Uri.parse("content://com.example.alexey.mylauncher/uri/clear");
    private static final Uri ALL_URI = Uri.parse("content://com.example.alexey.mylauncher/uri");

    private SharedPreferences preferences;
    private ViewPager pager;
    private int columnCount;
    private AppDatabaseHelper appDatabaseHelper;
    private ContactsDatabaseHelper contactsDatabaseHelper;
    private ArrayList<ElementInfo> app;
    private TreeSet<ElementInfo> newApp, popularApp;
    private ArrayList<ElementInfo> contacts;
    private MyReceiver receiver;
    private final ArrayList<ElementInfo> appList = new ArrayList<>();
    private final ArrayList<ElementInfo> favoritesAppList = new ArrayList<>();
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
        pager.setAdapter(new MainPagerAdapter(getSupportFragmentManager(), !preferences.getBoolean("hide_favorites", false)));
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
            getContentResolver().delete(DELETE_ALL_URI, null, null);
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
        contactsDatabaseHelper = new ContactsDatabaseHelper(this);
        app = new ArrayList<>();
        newApp = new TreeSet<>(new Comparator<ElementInfo>() {
            @Override
            public int compare(ElementInfo app1, ElementInfo app2) {
                long x = -app1.timeInstalled;
                long y = -app2.timeInstalled;
                if (x == y) {
                    return app1.packageName.compareTo(app2.packageName);
                } else
                    return x < y ? -1 : 1;
            }
        });
        popularApp = new TreeSet<>(new Comparator<ElementInfo>() {
            @Override
            public int compare(ElementInfo app1, ElementInfo app2) {
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
            public void create(ContextMenu menu, final ElementInfo elementInfo) {
                menu.add("Инфо").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        intent.setData(Uri.parse("package:" + elementInfo.packageName));
                        startActivity(intent);
                        return true;
                    }
                });
                menu.add("Удалить").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        Intent intent = new Intent(Intent.ACTION_DELETE);
                        intent.setData(Uri.parse("package:" + elementInfo.packageName));
                        startActivity(intent);
                        return true;
                    }
                });
                menu.add("Добавить в избранное").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        changeFavorites(elementInfo, true);
                        return true;
                    }
                });
            }
        });
        contacts = new ArrayList<>();
        initContacts();
        initFavoritesAppList();
        favoritesAppAdapter = new AppRecyclerViewAdapter(this, favoritesAppList, new AppRecyclerViewAdapter.CreateContextMenuListener() {
            @Override
            public void create(ContextMenu menu, final ElementInfo elementInfo) {
                menu.add("Удалить из избранного").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        changeFavorites(elementInfo, false);
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
            ElementInfo elementInfo = new ElementInfo(appName, packageName, icon, true);
            if (map.containsKey(packageName)) {
                elementInfo.clickCount = map.get(packageName).first;
                elementInfo.isFavorites = map.get(packageName).second;
            } else {
                elementInfo.clickCount = 0;
                elementInfo.isFavorites = false;
            }
            try {
                elementInfo.timeInstalled = createPackageContext(ri.activityInfo.packageName, 0)
                        .getPackageManager()
                        .getPackageInfo(ri.activityInfo.packageName, 0)
                        .lastUpdateTime;
            } catch (PackageManager.NameNotFoundException ignored) {
                elementInfo.timeInstalled = 0;
            }
            this.app.add(elementInfo);
            newApp.add(elementInfo);
            popularApp.add(elementInfo);
        }
    }

    private void initAppList() {
        appList.clear();
        appList.add(new ElementInfo("Popular", null, null, false));
        appList.addAll(getPopularApp(columnCount));
        appList.add(new ElementInfo("New", null, null, false));
        appList.addAll(getNewApp(columnCount));
        appList.add(new ElementInfo("All", null, null, false));
        appList.addAll(app);
    }

    private void initContacts() {
        contacts.clear();
        int contactCount = CONTACTS_MAX;
        ArrayList<ElementInfo> list = contactsDatabaseHelper.readRecords();
        for (ElementInfo el : list) {
            contacts.add(el);
            if (--contactCount == 0)
                break;
        }
        Cursor cursor = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
        while (cursor.moveToNext()) {
            String name = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            String phoneNumber = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            ElementInfo app = new ElementInfo(name, phoneNumber, null, false);
            int f = contacts.indexOf(app);
            if (f != -1) {
                String photoUri = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_THUMBNAIL_URI));
                Drawable icon = null;
                try {
                    icon = new BitmapDrawable(getResources(), MediaStore.Images.Media.getBitmap(getContentResolver(), Uri.parse(photoUri)));
                } catch (Exception ignored) {}
                contacts.get(f).icon = icon;
            }
        }
        cursor.close();
    }

    private void initFavoritesAppList() {
        favoritesAppList.clear();
        favoritesAppList.add(new ElementInfo("Favorites", null, null, false));
        favoritesAppList.addAll(getFavoritesApp());
        favoritesAppList.add(new ElementInfo("Contacts", null, null, false));
        favoritesAppList.addAll(contacts);
    }

    private ArrayList<ElementInfo> getNewApp(int count) {
        ArrayList<ElementInfo> list = new ArrayList<>();
        Iterator<ElementInfo> it = newApp.iterator();
        while (it.hasNext() && count-- != 0) {
            list.add(it.next());
        }
        return list;
    }

    private ArrayList<ElementInfo> getPopularApp(int count) {
        ArrayList<ElementInfo> list = new ArrayList<>();
        Iterator<ElementInfo> it = popularApp.iterator();
        while (it.hasNext() && count-- != 0) {
            list.add(it.next());
        }
        return list;
    }

    private ArrayList<ElementInfo> getFavoritesApp() {
        ArrayList<ElementInfo> list = new ArrayList<>();
        for (ElementInfo elementInfo : app) {
            if (elementInfo.isFavorites)
                list.add(elementInfo);
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
                ElementInfo elementInfo = new ElementInfo(appName, packageName, icon, false);
                elementInfo.clickCount = 0;
                elementInfo.isFavorites = false;
                if (position < 0 || position >= this.app.size())
                    position = this.app.size() - 1;
                this.app.add(position, elementInfo);
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
        Cursor cursor = getContentResolver().query(ALL_URI, null, null, null, null);
        if (cursor == null)
            return;
        while (cursor.moveToNext()) {
            uriList.add(cursor.getString(cursor.getColumnIndex(UriDatabaseHelper.Columns.FIELD_URI_NAME)));
            if (--uriCount == 0)
                break;
        }
        cursor.close();
    }

    private void initReceiver() {
        receiver = new MyReceiver();
        receiver.setActivity(this);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
        intentFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        registerReceiver(receiver, intentFilter);
    }

    public void click(ElementInfo elementInfo) {
        newApp.remove(elementInfo);
        popularApp.remove(elementInfo);
        ++elementInfo.clickCount;
        newApp.add(elementInfo);
        popularApp.add(elementInfo);
        initAppList();
        appAdapter.notifyDataSetChanged();
    }

    private void changeFavorites(ElementInfo elementInfo, boolean newValue) {
        newApp.remove(elementInfo);
        popularApp.remove(elementInfo);
        elementInfo.isFavorites = newValue;
        newApp.add(elementInfo);
        popularApp.add(elementInfo);
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
        contactsDatabaseHelper.writeRecords(contacts);
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CONTACT_REQUEST && resultCode == RESULT_OK) {
            if (contacts.size() >= CONTACTS_MAX) {
                Toast.makeText(this, "Больше нельзя добавлять контакты", Toast.LENGTH_SHORT).show();
                return;
            }
            try {
                Cursor cursor = getContentResolver().query(data.getData(), null, null, null, null);
                cursor.moveToFirst();
                String name = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                String phoneNumber = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                String contactId = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID));
                String photoUri = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_THUMBNAIL_URI));
                Drawable icon = null;
                try {
                    icon = new BitmapDrawable(getResources(), MediaStore.Images.Media.getBitmap(getContentResolver(), Uri.parse(photoUri)));
                } catch (Exception ignored) {}
                ElementInfo elementInfo = new ElementInfo(name, phoneNumber, icon, false);
                elementInfo.contactId = contactId;
                if (contacts.contains(elementInfo))
                    return;
                contacts.add(elementInfo);
                initFavoritesAppList();
                favoritesAppAdapter.notifyDataSetChanged();
                cursor.close();
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }
    }

    public void deleteContact(ElementInfo contact) {
        contacts.remove(contact);
        initFavoritesAppList();
        favoritesAppAdapter.notifyDataSetChanged();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_add_contact:
                Intent intent = new Intent(Intent.ACTION_PICK, Uri.parse("content://contacts"));
                intent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
                startActivityForResult(intent, CONTACT_REQUEST);
                break;
        }
    }
}

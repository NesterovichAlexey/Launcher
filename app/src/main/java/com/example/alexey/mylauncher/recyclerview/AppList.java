package com.example.alexey.mylauncher.recyclerview;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.util.Pair;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.alexey.mylauncher.database.DatabaseHelper;
import com.example.alexey.mylauncher.recyclerview.items.App;
import com.example.alexey.mylauncher.recyclerview.items.Item;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.TreeSet;

public class AppList {
    private Context context;
    private DatabaseHelper dbHelper;
    private int columnCount;
    private HashMap<String, App> packageNames;
    private TreeSet<Pair<Long, String>> newApps;
    private TreeSet<Pair<Integer, String>> popularApps;
    private HashSet<String> favorites;
    private final ArrayList<App> appsList, newAppsList, popularAppsList, favoritesAppsList;
    private ArrayList<Listener> appListeners, newAppListeners, popularAppListeners, favoritesAppListeners;

    //App-Listeners-----------------------------------------------------//
    private Item.ClickListener appClickListener = new Item.ClickListener() {
        @Override
        public void onClick(View v, Item item) {
            App app = (App) item;
            setClickCount(app.getPackageName(), app.getClickCount() + 1);
            Toast.makeText(v.getContext(), app.getName(), Toast.LENGTH_SHORT).show();
            v.getContext().startActivity(v.getContext().getPackageManager().getLaunchIntentForPackage(app.getPackageName()));
        }
    };
    private Item.LongClickListener appLongClickListener = new Item.LongClickListener() {
        @Override
        public boolean onLongClick(View v, Item item) {
            v.showContextMenu();
            return true;
        }
    };
    private Item.CreateContextMenu appCreateContextMenuListener = new Item.CreateContextMenu() {
        @Override
        public void onCreateContextMenu(ContextMenu menu, final View v, ContextMenu.ContextMenuInfo menuInfo, Item item) {
            final App app = (App) item;
            menu.add("Инфо").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    intent.setData(Uri.parse("package:" + app.getPackageName()));
                    v.getContext().startActivity(intent);
                    return true;
                }
            });
            menu.add("Удалить").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    Intent intent = new Intent(Intent.ACTION_DELETE);
                    intent.setData(Uri.parse("package:" + app.getPackageName()));
                    v.getContext().startActivity(intent);
                    return true;
                }
            });
            menu.add("Добавить в избранное").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    setFavorites(app.getPackageName(), true);
                    return true;
                }
            });
        }
    };
    //Favorites-App-Listeners-------------------------------------------//
    private Item.ClickListener favoritesClickListener = new Item.ClickListener() {
        @Override
        public void onClick(View v, Item item) {
            App app = (App) item;
            setClickCount(app.getPackageName(), app.getClickCount() + 1);
            Toast.makeText(v.getContext(), app.getName(), Toast.LENGTH_SHORT).show();
            v.getContext().startActivity(v.getContext().getPackageManager().getLaunchIntentForPackage(app.getPackageName()));
        }
    };
    private Item.LongClickListener favoritesLongClickListaner = new Item.LongClickListener() {
        @Override
        public boolean onLongClick(View v, Item item) {
            v.showContextMenu();
            return true;
        }
    };
    private Item.CreateContextMenu favoritesCreateContextMenuListener = new Item.CreateContextMenu() {
        @Override
        public void onCreateContextMenu(ContextMenu menu, final View v, ContextMenu.ContextMenuInfo menuInfo, Item item) {
            final App app = (App) item;
            menu.add("Инфо").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    intent.setData(Uri.parse("package:" + app.getPackageName()));
                    v.getContext().startActivity(intent);
                    return true;
                }
            });
            menu.add("Удалить").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    Intent intent = new Intent(Intent.ACTION_DELETE);
                    intent.setData(Uri.parse("package:" + app.getPackageName()));
                    v.getContext().startActivity(intent);
                    return true;
                }
            });
            menu.add("Удалить из избранного").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    setFavorites(app.getPackageName(), false);
                    return true;
                }
            });
        }
    };
    //------------------------------------------------------------------//

    public AppList(@NonNull Context context, @NonNull DatabaseHelper dbHelper, int columnCount) {
        this.context = context;
        this.dbHelper = dbHelper;
        this.columnCount = columnCount;
        appsList = new ArrayList<>();
        newAppsList = new ArrayList<>();
        popularAppsList = new ArrayList<>();
        favoritesAppsList = new ArrayList<>();
        appListeners = new ArrayList<>();
        newAppListeners = new ArrayList<>();
        popularAppListeners = new ArrayList<>();
        favoritesAppListeners = new ArrayList<>();
        packageNames = new HashMap<>();
        newApps = new TreeSet<>(new Comparator<Pair<Long, String>>() {
            @Override
            public int compare(Pair<Long, String> o1, Pair<Long, String> o2) {
                if (o1.first > o2.first)
                    return -1;
                else if (o1.first < o2.first)
                    return 1;
                else
                    return o1.second.compareTo(o2.second);
            }
        });
        popularApps = new TreeSet<>(new Comparator<Pair<Integer, String>>() {
            @Override
            public int compare(Pair<Integer, String> o1, Pair<Integer, String> o2) {
                if (o1.first > o2.first)
                    return -1;
                else if (o1.first < o2.first)
                    return 1;
                else
                    return o1.second.compareTo(o2.second);
            }
        });
        favorites = new HashSet<>();
        initApp();
    }

    private void initApp() {
        for (App app : dbHelper.loadApp()) {
            addApp(app);
        }
        PackageManager pm = context.getPackageManager();
        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> app = pm.queryIntentActivities(intent, 0);
        for (ResolveInfo ri : app) {
            String packageName = ri.activityInfo.packageName;
            if (context.getPackageName().equals(packageName))
                continue;
            BitmapDrawable icon = (BitmapDrawable) ri.loadIcon(pm);
            long timeInstalled;
            try {
                timeInstalled = context.createPackageContext(ri.activityInfo.packageName, 0)
                        .getPackageManager()
                        .getPackageInfo(ri.activityInfo.packageName, 0)
                        .lastUpdateTime;
            } catch (PackageManager.NameNotFoundException ignored) {
                timeInstalled = System.currentTimeMillis();
            }
            if (getApp(packageName) != null) {
                getApp(packageName).setIcon(icon);
                setTimeInstalled(packageName, timeInstalled);
                continue;
            }
            String name = (String) ri.loadLabel(pm);
            App newApp = new App(name, packageName, icon, timeInstalled);
            addApp(newApp);
        }
    }

    public void addApp(@NonNull App app) {
        app.setClickListener(appClickListener);
        app.setLongClickListener(appLongClickListener);
        app.setCreateContextMenuListener(appCreateContextMenuListener);
        packageNames.put(app.getPackageName(), app);
        newApps.add(new Pair<>(app.getTimeInstalled(), app.getPackageName()));
        popularApps.add(new Pair<>(app.getClickCount(), app.getPackageName()));
        updateAppsList();
        updateNewAppsList();
        updateNewAppsList();
        if (app.isFavorites()) {
            favorites.add(app.getPackageName());
            updateFavoritesAppsList();
        }
    }

    public void removeApp(@NonNull String packageName) {
        App app = getApp(packageName);
        if (app == null)
            return;
        packageNames.remove(packageName);
        newApps.remove(new Pair<>(app.getTimeInstalled(), app.getPackageName()));
        popularApps.remove(new Pair<>(app.getClickCount(), app.getPackageName()));
        favorites.remove(app.getPackageName());
        updateAppsList();
        updateNewAppsList();
        updatePopularAppsList();
        updateFavoritesAppsList();
    }

    public App getApp(@NonNull String packageName) {
        return packageNames.get(packageName);
    }

    public void setTimeInstalled(@NonNull String packageName, long newTimeInstalled) {
        App app = getApp(packageName);
        if (app == null)
            return;
        removeApp(packageName);
        app.setTimeInstalled(newTimeInstalled);
        addApp(app);
        updateNewAppsList();
    }

    public void setClickCount(@NonNull String packageName, int newClickCount) {
        App app = getApp(packageName);
        if (app == null)
            return;
        removeApp(packageName);
        app.setClickCount(newClickCount);
        addApp(app);
        updatePopularAppsList();
    }

    public void setFavorites(@NonNull String packageName, boolean newFavorites) {
        App app = getApp(packageName);
        if (app == null)
            return;
        app.setFavorites(newFavorites);
        if (newFavorites)
            favorites.add(packageName);
        else
            favorites.remove(packageName);
        updateFavoritesAppsList();
    }

    public void clearFavorites() {
        for (String app : favorites) {
            getApp(app).setFavorites(false);
        }
        favorites.clear();
        updateFavoritesAppsList();
    }

    public void updateAppsList() {
        appsList.clear();
        for (App app : packageNames.values()) {
            appsList.add(app);
        }
        for (Listener listener : appListeners) {
            listener.update();
        }
    }

    public void updateNewAppsList() {
        newAppsList.clear();
        int count = columnCount;
        for (Pair<Long, String> app : newApps) {
            newAppsList.add(getApp(app.second));
            if (--count == 0)
                break;
        }
        for (Listener listener : newAppListeners) {
            listener.update();
        }
    }

    public void updatePopularAppsList() {
        popularAppsList.clear();
        int count = columnCount;
        for (Pair<Integer, String> app : popularApps) {
            popularAppsList.add(getApp(app.second));
            if (--count == 0)
                break;
        }
        for (Listener listener : popularAppListeners) {
            listener.update();
        }
    }

    public void updateFavoritesAppsList() {
        favoritesAppsList.clear();
        for (String app : favorites) {
            App app1 = getApp(app);
            App app2 = new App(app1.getName(), app1.getPackageName(), app1.getIcon(), app1.getTimeInstalled());
            app2.setClickCount(app1.getClickCount());
            app2.setFavorites(true);
            app2.setClickListener(favoritesClickListener);
            app2.setLongClickListener(favoritesLongClickListaner);
            app2.setCreateContextMenuListener(favoritesCreateContextMenuListener);
            favoritesAppsList.add(app2);
        }
        for (Listener listener : favoritesAppListeners) {
            listener.update();
        }
    }

    public ArrayList<App> getAppsList() {
        return appsList;
    }

    public ArrayList<App> getNewAppsList() {
        return newAppsList;
    }

    public ArrayList<App> getPopularAppsList() {
        return popularAppsList;
    }

    public ArrayList<App> getFavoritesAppsList() {
        return favoritesAppsList;
    }

    public void addAppListener(Listener listener) {
        appListeners.add(listener);
    }

    public void addNewAppListener(Listener listener) {
        newAppListeners.add(listener);
    }

    public void addPopularAppListener(Listener listener) {
        popularAppListeners.add(listener);
    }

    public void addFavoritesAppListener(Listener listener) {
        favoritesAppListeners.add(listener);
    }

    public static interface Listener {
        public void update();
    }
}

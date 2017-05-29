package com.example.alexey.mylauncher.recyclerview;

import com.example.alexey.mylauncher.recyclerview.items.App;

import org.junit.Test;

import static org.junit.Assert.*;

public class AppListTest {

    @Test
    public void createAppListTest() throws Exception {
        AppList appList = new AppList(null, null,4);
        assertTrue(appList.getAppsList().size() == 0);
        appList.addApp(new App("name", "com.package", null, 1));
        appList.addApp(new App("name1", "com.package1", null, 2));
        appList.addApp(new App("name2", "com.package1", null, 3));
        assertTrue(appList.getAppsList().size() == 2);
    }

    @Test
    public void setFavoritesAppListTest() throws Exception {
        AppList appList = new AppList(null, null,4);
        appList.addApp(new App("name", "com.package", null, 1));
        appList.addApp(new App("name2", "com.package1", null, 2));
        appList.setFavorites("com.package", true);
        assertTrue(appList.getApp("com.package").isFavorites());
        appList.setFavorites("com.package", false);
        assertFalse(appList.getApp("com.package").isFavorites());
    }

    @Test
    public void clearFavoritesAppListTest() throws Exception {
        AppList appList = new AppList(null, null,4);
        appList.addApp(new App("name", "com.package", null, 1));
        appList.addApp(new App("name1", "com.package1", null, 2));
        appList.addApp(new App("name2", "com.package2", null, 3));
        appList.addApp(new App("name3", "com.package3", null, 4));
        assertTrue(appList.getFavoritesAppsList().size() == 0);
        appList.setFavorites("com.package", true);
        appList.setFavorites("com.package1", true);
        appList.setFavorites("com.package3", true);
        assertTrue(appList.getFavoritesAppsList().size() == 3);
        appList.clearFavorites();
        assertTrue(appList.getFavoritesAppsList().size() == 0);
    }

    @Test
    public void setClickCountAppListTest() throws Exception {
        AppList appList = new AppList(null, null,4);
        appList.addApp(new App("name", "com.package", null, 1));
        appList.addApp(new App("name1", "com.package1", null, 2));
        appList.addApp(new App("name2", "com.package2", null, 3));
        appList.addApp(new App("name3", "com.package3", null, 4));
        assertNotNull(appList.getPopularAppsList());
        appList.setClickCount("com.package", 3);
        assertTrue(appList.getPopularAppsList().size() != 0);
        assertTrue("com.package".equals(appList.getPopularAppsList().get(0).getPackageName()));
        appList.setClickCount("com.package2", 1);
        appList.setClickCount("com.package3", 10);
        assertTrue(appList.getPopularAppsList().size() != 0);
        assertTrue("com.package3".equals(appList.getPopularAppsList().get(0).getPackageName()));
    }

    @Test
    public void setTimeInstalledAppListTest() throws Exception {
        AppList appList = new AppList(null, null,4);
        appList.addApp(new App("name", "com.package", null, 1));
        appList.addApp(new App("name1", "com.package1", null, 2));
        appList.addApp(new App("name2", "com.package2", null, 3));
        appList.addApp(new App("name3", "com.package3", null, 4));
        assertNotNull(appList.getNewAppsList());
        assertTrue("com.package3".equals(appList.getNewAppsList().get(0).getPackageName()));
        appList.setTimeInstalled("com.package2", 5);
        appList.setTimeInstalled("com.package", 6);
        assertTrue("com.package".equals(appList.getNewAppsList().get(0).getPackageName()));
        assertTrue("com.package2".equals(appList.getNewAppsList().get(1).getPackageName()));
    }

    @Test
    public void removeAppListTest() throws Exception {
        AppList appList = new AppList(null, null,4);
        appList.addApp(new App("name", "com.package", null, 1));
        appList.addApp(new App("name1", "com.package1", null, 2));
        appList.addApp(new App("name2", "com.package2", null, 3));
        appList.addApp(new App("name3", "com.package3", null, 4));
        int size = appList.getAppsList().size();
        appList.removeApp("com.package2");
        appList.removeApp("com.package22");
        assertTrue(appList.getAppsList().size() == size - 1);
    }

    boolean isApp = false;
    boolean isFavorites = false;
    boolean isPopular = false;
    boolean isNew = false;
    @Test
    public void checkListenersAppListTest() throws Exception {
        AppList appList = new AppList(null, null,4);
        isApp = isFavorites = isPopular = isNew = false;
        appList.addAppListener(new AppList.Listener() {
            @Override
            public void update() {
                isApp = true;
            }
        });
        appList.addFavoritesAppListener(new AppList.Listener() {
            @Override
            public void update() {
                isFavorites = true;
            }
        });
        appList.addPopularAppListener(new AppList.Listener() {
            @Override
            public void update() {
                isPopular = true;
            }
        });
        appList.addNewAppListener(new AppList.Listener() {
            @Override
            public void update() {
                isNew = true;
            }
        });

        appList.addApp(new App("name", "com.package", null, 1));
        assertTrue(isApp && !isFavorites && !isPopular && isNew);
        isApp = isFavorites = isPopular = isNew = false;

        appList.setFavorites("com.package", true);
        assertTrue(!isApp && isFavorites && !isPopular && !isNew);
        isApp = isFavorites = isPopular = isNew = false;

        appList.setTimeInstalled("com.package", 2);
        assertTrue(isApp && isFavorites && isPopular && isNew);
        isApp = isFavorites = isPopular = isNew = false;

        appList.setClickCount("com.package", 12);
        assertTrue(isApp && isFavorites && isPopular && isNew);
        isApp = isFavorites = isPopular = isNew = false;
    }
}
package com.example.alexey.mylauncher.recyclerview.items;

import org.junit.Test;

import static org.junit.Assert.*;

public class AppTest {
    @Test
    public void clickTest() throws Exception {
        App app = new App("name", "com.package", null, 0);
        assertTrue(app.getClickCount() == 0);
        app.click();
        assertTrue(app.getClickCount() == 1);
        app.setClickCount(100);
        assertTrue(app.getClickCount() == 100);
    }

    @Test
    public void favoritesTest() throws Exception {
        App app = new App("name", "com.package", null, 0);
        assertFalse(app.isFavorites());
        app.setFavorites(true);
        assertTrue(app.isFavorites());
        app.setFavorites(false);
        assertFalse(app.isFavorites());
    }

    @Test
    public void appTest() throws Exception {
        App app = new App("name", "com.package", null, 1234);
        assertTrue("name".equals(app.getName()));
        assertTrue("com.package".equals(app.getPackageName()));
        assertTrue(app.getTimeInstalled() == 1234);
    }

    @Test
    public void timeInstalledTest() throws Exception {
        App app = new App("name", "com.package", null, 1234);
        assertTrue(app.getTimeInstalled() == 1234);
        long time = System.currentTimeMillis();
        app.setTimeInstalled(time);
        assertTrue(app.getTimeInstalled() == time);
    }

}
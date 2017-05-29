package com.example.alexey.mylauncher;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.IBinder;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MyService extends Service {

    private ExecutorService es;
    private MyRun run;
    private PendingIntent pi;
    private SharedPreferences preference;

    @Override
    public void onCreate() {
        System.out.println("onCreate");
        super.onCreate();
        preference = PreferenceManager.getDefaultSharedPreferences(this);
        es = Executors.newFixedThreadPool(1);
    }

    @Override
    public void onDestroy() {
        System.out.println("onDestroy");
        if (run != null)
            run.stop();
        es.shutdown();
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null)
            this.pi = intent.getParcelableExtra("pendingIntent");
        if (run != null)
            run.stop();
        es.execute(run = new MyRun());
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private class MyRun implements Runnable {
        private static final String CUR = "cur";
        private static final String SIZE = "size";
        private static final String LAST_LOAD_IMAGE = "lastLoadImage";
        private static final String LAST_SEND_IMAGE = "lastSendImage";
        private static final long SLEEP = 15 * 60 * 1000;
        private long lastLoadImage;
        private long lastSendImage;
        private int size;
        private int cur;

        private boolean running;

        public MyRun() {
            running = false;
            cur = preference.getInt(CUR, 0);
            lastLoadImage = preference.getLong(LAST_LOAD_IMAGE, -1);
            lastSendImage = preference.getLong(LAST_SEND_IMAGE, -1);
            size = preference.getInt(SIZE, 0);
        }

        private void sendImage() {
            Intent intent = new Intent();
            if (size != 0)
                intent.putExtra("imageFileName", (cur % size) + ".png");
            try {
                if (pi == null)
                    return;
                pi.send(MyService.this, 0, intent);
                lastSendImage = System.currentTimeMillis();
                preference.edit().putLong(LAST_SEND_IMAGE, lastSendImage).apply();
                System.out.println("SEND " + cur);
            } catch (PendingIntent.CanceledException e) {
                e.printStackTrace();
            }
        }

        private Bitmap loadBitmap(String stringUrl) {
            try {
                URL url = new URL(stringUrl);
                URLConnection urlConnection = url.openConnection();
                InputStream is = urlConnection.getInputStream();
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();

                int nRead;
                byte[] data = new byte[16384];

                while ((nRead = is.read(data, 0, data.length)) != -1) {
                    buffer.write(data, 0, nRead);
                }
                buffer.flush();
                byte [] bitmap = buffer.toByteArray();
                return BitmapFactory.decodeByteArray(bitmap, 0, bitmap.length);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        private void loadImage(String stringUrl) {
            int id = 0;
            try {
                URL url = new URL(stringUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    InputStream stream = connection.getInputStream();
                    XmlPullParser parser = Xml.newPullParser();
                    parser.setInput(stream, null);
                    while (parser.next() != XmlPullParser.END_DOCUMENT) {
                        if (parser.getEventType() != XmlPullParser.START_TAG) {
                            continue;
                        }
                        String name = parser.getName();
                        String prefix = parser.getPrefix();
                        // Starts by looking for the entry tag
                        if ("f".equals(prefix) && "img".equals(name)) {
                            for (int i = 0; i < parser.getAttributeCount(); i++) {
                                if ("size".equals(parser.getAttributeName(i))) {
                                    if ("L".equals(parser.getAttributeValue(i))) {
                                        saveImage(loadBitmap(parser.getAttributeValue(i-1)), id++);
                                        if (id == 1) {
                                            cur = 0;
                                            sendImage();
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                lastLoadImage = System.currentTimeMillis();
                preference.edit().putLong(LAST_LOAD_IMAGE, lastLoadImage).apply();
            } catch (IOException | XmlPullParserException e) {
                e.printStackTrace();
            }
            preference.edit().putInt(SIZE, id).apply();
            size = id;
            System.out.println("loadImage");
        }

        private void saveImage(Bitmap bitmap, int id) {
            try {
                FileOutputStream stream = openFileOutput(id + ".png", Context.MODE_PRIVATE);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                stream.close();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            if (lastLoadImage < getStartDay() || size < 100) {
                loadImage("http://api-fotki.yandex.ru/api/podhistory/poddate/?limit=100");
                cur = 0;
            }
            if (lastSendImage == -1) {
                cur = 0;
            }
            int delta = (int) ((System.currentTimeMillis() - lastSendImage) / SLEEP);
            if (delta != 0) {
                cur += delta;
            }
            preference.edit().putInt(CUR, cur).apply();
            running = true;
            while (running) {
                sendImage();
                try {
                    Thread.sleep(getSleep());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                ++cur;
                preference.edit().putInt(CUR, cur).apply();
            }
            running = false;
        }

        private static final long DAY = 24L * 60 * 60 * 1000;
        private static final long HOUR_12 = 9 * 60 * 60 * 1000;
        private long getStartDay() {
            long time = System.currentTimeMillis();
            return time / DAY * DAY + HOUR_12;
        }

        public void stop() {
            running = false;
            System.out.println("stop");
        }

        public long getSleep() {
            return SLEEP - (System.currentTimeMillis() % SLEEP);
        }
    }
}

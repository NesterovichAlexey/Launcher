package com.example.alexey.mylauncher.main;

import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.example.alexey.mylauncher.MyContentProvider;
import com.example.alexey.mylauncher.MyService;
import com.example.alexey.mylauncher.SettingsActivity;
import com.example.alexey.mylauncher.MyReceiver;
import com.example.alexey.mylauncher.R;
import com.example.alexey.mylauncher.database.DatabaseHelper;
import com.example.alexey.mylauncher.recyclerview.AppList;
import com.example.alexey.mylauncher.recyclerview.MyRecyclerViewAdapter;
import com.example.alexey.mylauncher.recyclerview.items.App;
import com.example.alexey.mylauncher.recyclerview.items.Contact;
import com.example.alexey.mylauncher.recyclerview.items.Header;
import com.example.alexey.mylauncher.recyclerview.items.Item;
import com.example.alexey.mylauncher.welcome.WelcomeActivity;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final int CONTACTS_MAX = 10;
    private static final int CONTACT_REQUEST = 1;
    private static final int SERVICE_REQUEST = 2;

    private static final Uri DELETE_ALL_URI = Uri.parse("content://com.example.alexey.mylauncher/uri/clear");
    private static final Uri ALL_URI = Uri.parse("content://com.example.alexey.mylauncher/uri");

    private SharedPreferences preferences;
    private int columnCount;
    private MyReceiver receiver;
    private DatabaseHelper dbHelper;
    private AppList apps;
    private final ArrayList<Contact> contacts = new ArrayList<>();
    private final ArrayList<Item> appsList = new ArrayList<>();
    private final ArrayList<Item> favoritesList = new ArrayList<>();
    private final ArrayList<String> uriList = new ArrayList<>();
    private MyRecyclerViewAdapter appAdapter, favoritesAdapter;
    private ArrayAdapter<String> uriAdapter;
    private boolean contactsAllowed;
    private ImageView background;

    private boolean bound = false;

    private Item.ClickListener contactClickListener = new Item.ClickListener() {
        @Override
        public void onClick(View v, Item item) {
            Contact contact = (Contact) item;
            if (((ToggleButton)findViewById(R.id.btn_delete_contact)).isChecked()) {
                contacts.remove(contact);
                initFavoritesList();
                favoritesAdapter.notifyDataSetChanged();
            } else {
                v.getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("tel:" + contact.getPhoneNumber())));
            }
        }
    };
    private Item.LongClickListener contactLongClickListener = new Item.LongClickListener() {
        @Override
        public boolean onLongClick(View v, Item item) {
            Contact contact = (Contact) item;
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_URI, contact.getContactId()));
            v.getContext().startActivity(intent);
            return true;
        }
    };

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

        background = (ImageView) findViewById(R.id.background);

        init();
        ViewPager pager = (ViewPager) findViewById(R.id.main_pager);
        pager.setAdapter(new MainPagerAdapter(getSupportFragmentManager(), !preferences.getBoolean("hide_favorites", false)));

        Intent intent = new Intent(this, MyService.class);
        PendingIntent pendingIntent = createPendingResult(SERVICE_REQUEST, new Intent(), 0);
        intent.putExtra("pendingIntent", pendingIntent);
        startService(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (preferences.getBoolean("clear_fav", false)) {
            preferences.edit().putBoolean("clear_fav", false).apply();
            apps.clearFavorites();
        }
        if (preferences.getBoolean("clear_uri", false)) {
            preferences.edit().putBoolean("clear_uri", false).apply();
            uriList.clear();
            uriAdapter.notifyDataSetChanged();
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
        dbHelper = new DatabaseHelper(this);
        apps = new AppList(this, dbHelper, columnCount);
        initAppList();
        appAdapter = new MyRecyclerViewAdapter(appsList);
        initContacts();
        initFavoritesList();
        favoritesAdapter = new MyRecyclerViewAdapter(favoritesList);
        apps.addAppListener(new AppList.Listener() {
            @Override
            public void update() {
                initAppList();
                appAdapter.notifyDataSetChanged();
            }
        });
        apps.addNewAppListener(new AppList.Listener() {
            @Override
            public void update() {
                initAppList();
                appAdapter.notifyDataSetChanged();
            }
        });
        apps.addPopularAppListener(new AppList.Listener() {
            @Override
            public void update() {
                initAppList();
                appAdapter.notifyDataSetChanged();
            }
        });
        apps.addFavoritesAppListener(new AppList.Listener() {
            @Override
            public void update() {
                initFavoritesList();
                favoritesAdapter.notifyDataSetChanged();
            }
        });
        initUri();
        uriAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, uriList);
        initReceiver();
    }

    private void initAppList() {
        appsList.clear();
        appsList.add(new Header("Popular"));
        appsList.addAll(apps.getPopularAppsList());
        appsList.add(new Header("New"));
        appsList.addAll(apps.getNewAppsList());
        appsList.add(new Header("All"));
        appsList.addAll(apps.getAppsList());
    }

    private void initFavoritesList() {
        favoritesList.clear();
        favoritesList.add(new Header("Favorites"));
        favoritesList.addAll(apps.getFavoritesAppsList());
        favoritesList.add(new Header("Contacts"));
        favoritesList.addAll(contacts);
    }

    private void initContacts() {
        contacts.clear();
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {
            contactsAllowed = false;
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.READ_CONTACTS}, 1);
        } else {
            contactsAllowed = true;
        }
        if (!contactsAllowed)
            return;
        int contactCount = CONTACTS_MAX;
        ArrayList<Contact> list = dbHelper.loadContact();
        HashMap<String, Integer> contactIds = new HashMap<>();
        for (Contact contact : list) {
            contacts.add(contact);
            contact.setClickListener(contactClickListener);
            contact.setLongClickListener(contactLongClickListener);
            contactIds.put(contact.getContactId(), contacts.size() - 1);
            if (--contactCount == 0)
                break;
        }
        Cursor cursor = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
        while (cursor.moveToNext()) {
            String contactId = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID));
            if (contactIds.containsKey(contactId)) {
                String photoUri = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_THUMBNAIL_URI));
                BitmapDrawable photo = null;
                try {
                    photo = new BitmapDrawable(getResources(), MediaStore.Images.Media.getBitmap(getContentResolver(), Uri.parse(photoUri)));
                } catch (Exception ignored) {}
                if (photo != null)
                    contacts.get(contactIds.get(contactId)).setPhoto(photo);
            }
        }
        cursor.close();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                contactsAllowed = grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED;
                if (contactsAllowed) {
                    initContacts();
                    initFavoritesList();
                }
        }
    }

    public void installApp(String packageName) {
        PackageManager pm = getPackageManager();
        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> app = pm.queryIntentActivities(intent, 0);
        for (ResolveInfo ri : app) {
            if (!ri.activityInfo.packageName.equals(packageName))
                continue;
            String name = (String) ri.loadLabel(pm);
            BitmapDrawable icon = (BitmapDrawable) ri.loadIcon(pm);
            long timeInstalled;
            try {
                timeInstalled = createPackageContext(ri.activityInfo.packageName, 0)
                        .getPackageManager()
                        .getPackageInfo(ri.activityInfo.packageName, 0)
                        .lastUpdateTime;
            } catch (PackageManager.NameNotFoundException ignored) {
                timeInstalled = System.currentTimeMillis();
            }
            if (apps.getApp(packageName) == null) {
                App newApp = new App(name, packageName, icon, timeInstalled);
                apps.addApp(newApp);
            } else {
                apps.setTimeInstalled(packageName, timeInstalled);
            }
            break;
        }
    }

    public void uninstallApp(String packageName) {
        apps.removeApp(packageName);
    }

    private void initUri() {
        uriList.clear();
        int uriCount = Integer.parseInt(preferences.getString("uri_count", "10"));
        Cursor cursor = getContentResolver().query(ALL_URI, null, null, null, null);
        if (cursor == null)
            return;
        while (cursor.moveToNext()) {
            uriList.add(cursor.getString(cursor.getColumnIndex(MyContentProvider.URI_NAME)));
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

    public MyRecyclerViewAdapter getAppAdapter() {
        return appAdapter;
    }

    public MyRecyclerViewAdapter getFavoritesAdapter() {
        return favoritesAdapter;
    }

    public ArrayList<String> getUriList() {
        return uriList;
    }

    public ArrayAdapter<String> getUriAdapter() {
        return uriAdapter;
    }

    @Override
    protected void onPause() {
        dbHelper.saveApp(apps.getAppsList());
        dbHelper.saveContact(contacts);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (receiver != null)
            unregisterReceiver(receiver);
        stopService(new Intent(this, MyService.class));
        bound = false;
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
        if (requestCode == SERVICE_REQUEST) {
            if (data.getStringExtra("imageFileName") == null) {
                background.setImageBitmap(null);
                return;
            }
            try {
                FileInputStream is = openFileInput(data.getStringExtra("imageFileName"));
                Bitmap bitmap = BitmapFactory.decodeStream(is);
                is.close();
                background.setScaleType(ImageView.ScaleType.CENTER_CROP);
                background.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
                background.setImageBitmap(null);
            }
        }
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
                BitmapDrawable photo;
                try {
                    photo = new BitmapDrawable(getResources(), MediaStore.Images.Media.getBitmap(getContentResolver(), Uri.parse(photoUri)));
                } catch (Exception ignored) {
                    photo = (BitmapDrawable) getResources().getDrawable(R.drawable.phone);
                }
                Contact contact = new Contact(name, phoneNumber, photo, contactId);
                if (contacts.contains(contact))
                    return;
                contact.setClickListener(contactClickListener);
                contact.setLongClickListener(contactLongClickListener);
                contacts.add(contact);
                initFavoritesList();
                favoritesAdapter.notifyDataSetChanged();
                cursor.close();
            } catch (NullPointerException ignored) {}
        }
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

package com.example.alexey.mylauncher.welcome;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;

import com.example.alexey.mylauncher.main.MainActivity;
import com.example.alexey.mylauncher.R;

public class WelcomeActivity extends AppCompatActivity {
    private ViewPager pager;
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        pager = (ViewPager) findViewById(R.id.welcome_pager);
        pager.setAdapter(new WelcomePagerAdapter(getSupportFragmentManager()));
    }

    public void onClick(View view) {
        if (view.getId() == R.id.btn_next) {
            switch (pager.getCurrentItem()) {
                case 0:
                    pager.setCurrentItem(1);
                    break;
                case 1:
                    boolean isStandardSize = ((RadioButton)findViewById(R.id.rb_column_count_46)).isChecked();
                    preferences.edit().putString("column_count", isStandardSize ? "1" : "2").apply();
                    pager.setCurrentItem(2);
                    break;
                case 2:
                    boolean isLight = ((RadioButton)findViewById(R.id.rb_light_theme)).isChecked();
                    preferences.edit().putString("theme", isLight ? "light" : "dark").apply();
                    preferences.edit().putBoolean("hide_welcome_activity", true).apply();
                    preferences.edit().putString("uri_count", "10").apply();
                    preferences.edit().putBoolean("hide_favorites", false).apply();
                    Intent intent = new Intent(this, MainActivity.class);
                    startActivity(intent);
                    finish();
                    break;
            }
        }
    }
}

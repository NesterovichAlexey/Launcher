package com.example.alexey.mylauncher;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.support.annotation.Nullable;

public class SettingsActivity extends PreferenceActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction().replace(android.R.id.content, new SettingsFragment()).commit();
    }

    public static class SettingsFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener, Preference.OnPreferenceClickListener {

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref);

            findPreference("hide_favorites").getSharedPreferences().edit().putBoolean("restart", false).apply();
            findPreference("hide_favorites").getSharedPreferences().edit().putBoolean("clear_fav", false).apply();
            findPreference("hide_favorites").getSharedPreferences().edit().putBoolean("clear_uri", false).apply();
            findPreference("hide_favorites").setOnPreferenceChangeListener(this);
            findPreference("theme").setOnPreferenceChangeListener(this);
            findPreference("column_count").setOnPreferenceChangeListener(this);
            findPreference("uri_count").setOnPreferenceChangeListener(this);
            findPreference("clear_favorites").setOnPreferenceClickListener(this);
            findPreference("clear_uri_history").setOnPreferenceClickListener(this);
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            findPreference("hide_favorites").getSharedPreferences().edit().putBoolean("restart", true).apply();
            return true;
        }

        @Override
        public boolean onPreferenceClick(Preference preference) {
            switch (preference.getKey()) {
                case "clear_favorites":
                    findPreference("hide_favorites").getSharedPreferences().edit().putBoolean("clear_fav", true).apply();
                    break;
                case "clear_uri_history":
                    findPreference("hide_favorites").getSharedPreferences().edit().putBoolean("clear_uri", true).apply();
                    break;
            }
            findPreference("hide_favorites").getSharedPreferences().edit().putBoolean("restart", true).apply();
            return true;
        }
    }
}

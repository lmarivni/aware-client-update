package com.aware.plugin.smokeregistration;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

import com.aware.Aware;

public class Settings  extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    /**
     * Activate/deactivate plugin
     */
    public static final String STATUS_PLUGIN_SMOKE_REGISTRATION = "status_plugin_smokeregistration";

    private static CheckBoxPreference active;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences_smokeregistration);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        active = (CheckBoxPreference) findPreference(STATUS_PLUGIN_SMOKE_REGISTRATION);
        if (Aware.getSetting(this, STATUS_PLUGIN_SMOKE_REGISTRATION).length() == 0) {
            Aware.setSetting(this, STATUS_PLUGIN_SMOKE_REGISTRATION, true);
        }
        active.setChecked(Aware.getSetting(getApplicationContext(), STATUS_PLUGIN_SMOKE_REGISTRATION).equals("true"));
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Preference preference = findPreference(key);
        if (preference.getKey().equals(STATUS_PLUGIN_SMOKE_REGISTRATION)) {
            Aware.setSetting(this, key, sharedPreferences.getBoolean(key, false));
            active.setChecked(sharedPreferences.getBoolean(key, false));
        }
        if (Aware.getSetting(this, STATUS_PLUGIN_SMOKE_REGISTRATION).equals("true")) {
            Aware.startPlugin(getApplicationContext(), "com.aware.plugin.smokeregistration");
        } else {
            Aware.stopPlugin(getApplicationContext(), "com.aware.plugin.smokeregistration");
        }
    }
}

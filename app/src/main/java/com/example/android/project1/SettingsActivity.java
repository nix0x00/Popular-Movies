package com.example.android.project1;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

public class SettingsActivity extends PreferenceActivity implements Preference.OnPreferenceChangeListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_settings);

        addPreferencesFromResource(R.xml.pref_sync);
        bindToPreference(findPreference(getString(R.string.pref_sync_key)));
    }

    private void bindToPreference(Preference preference) {
        preference.setOnPreferenceChangeListener(this);
        onPreferenceChange(
                preference,
                PreferenceManager
                        .getDefaultSharedPreferences(getApplicationContext())
                        .getString(preference.getKey(), "")
        );
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object value) {
        String strValue = value.toString();
        ListPreference listPreference = (ListPreference) preference;
        int index = listPreference.findIndexOfValue(strValue);
        if (index >= 0) {
            listPreference.setValueIndex(index);
            preference.setSummary(listPreference.getEntries()[index]);
        }

        return true;
    }
}

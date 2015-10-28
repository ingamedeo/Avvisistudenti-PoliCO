package com.ingamedeo.avvisistudenti_polico;

import android.os.Bundle;
import android.preference.ListPreference;
import android.view.MenuItem;

import com.example.android.supportv7.app.AppCompatPreferenceActivity;

/**
 * Created by ingamedeo on 10/10/15.
 */

public class SettingsActivity extends AppCompatPreferenceActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        ListPreference langPref = (ListPreference) findPreference(getResources().getString(R.string.preference_lang));
        if (langPref.getValue()==null) {
            langPref.setValueIndex(Constants.getDefaultLang(this).ordinal());
        }

        ListPreference syncIntervalPref = (ListPreference) findPreference(getResources().getString(R.string.preference_syncinterval));
        if (syncIntervalPref.getValue()==null) {
            syncIntervalPref.setValueIndex(Constants.defaultInterval.ordinal());
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}


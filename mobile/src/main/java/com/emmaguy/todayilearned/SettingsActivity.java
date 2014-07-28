package com.emmaguy.todayilearned;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;

import de.psdev.licensesdialog.LicensesDialog;

public class SettingsActivity extends Activity {

    public static final String PREFS_REFRESH_FREQUENCY = "sync_frequency";
    public static final String PREFS_BEFORE_ID = "before_id";
    public static final String PREFS_NUMBER_TO_RETRIEVE = "number_to_retrieve";
    public static final String PREFS_SORT_ORDER = "sort_order";

    public static final String OPEN_SOURCE = "open_source";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getFragmentManager().beginTransaction().replace(android.R.id.content, new SettingsFragment()).commit();
    }

    public static class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener, Preference.OnPreferenceClickListener {

        public SettingsFragment() {
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.prefs);

            Utils.setRecurringAlarm(getActivity().getApplicationContext());

            initSummary();
            initialiseClickListener(OPEN_SOURCE);
        }

        private void initialiseClickListener(String key) {
            Preference resetPref = findPreference(key);
            if (resetPref != null) {
                resetPref.setOnPreferenceClickListener(this);
            }
        }

        @Override
        public void onResume() {
            super.onResume();

            getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onPause() {
            super.onPause();

            getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        }

        @Override
        public boolean onPreferenceClick(Preference preference) {
            if (preference.getKey().equals(OPEN_SOURCE)) {
                new LicensesDialog(getActivity(), R.raw.open_source_notices, false, true).show();
                return true;
            }
            return false;
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            updatePrefsSummary(sharedPreferences, findPreference(key));

            if (key.equals(PREFS_REFRESH_FREQUENCY)) {
                Utils.setRecurringAlarm(getActivity().getApplicationContext());
            } else if(key.equals(PREFS_SORT_ORDER)) {
                getPreferenceManager().getSharedPreferences().edit().putString(SettingsActivity.PREFS_BEFORE_ID, "").apply();
            }
        }

        protected void initSummary() {
            for (int i = 0; i < getPreferenceScreen().getPreferenceCount(); i++) {
                initPrefsSummary(getPreferenceManager().getSharedPreferences(), getPreferenceScreen().getPreference(i));
            }
        }

        protected void initPrefsSummary(SharedPreferences sharedPreferences, Preference p) {
            if (p instanceof PreferenceCategory) {
                PreferenceCategory cat = (PreferenceCategory) p;
                for (int i = 0; i < cat.getPreferenceCount(); i++) {
                    initPrefsSummary(sharedPreferences, cat.getPreference(i));
                }
            } else {
                updatePrefsSummary(sharedPreferences, p);
            }
        }

        protected void updatePrefsSummary(final SharedPreferences sharedPreferences, Preference pref) {
            if (pref == null) {
                return;
            }

            if (pref instanceof ListPreference) {
                ListPreference lst = (ListPreference) pref;
                String currentValue = lst.getValue();

                int index = lst.findIndexOfValue(currentValue);
                CharSequence[] entries = lst.getEntries();
                if (index >= 0 && index < entries.length) {
                    pref.setSummary(entries[index]);
                }
            }
        }
    }
}
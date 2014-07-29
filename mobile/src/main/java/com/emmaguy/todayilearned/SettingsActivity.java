package com.emmaguy.todayilearned;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.List;

import de.psdev.licensesdialog.LicensesDialog;

public class SettingsActivity extends Activity {

    public static final String PREFS_REFRESH_FREQUENCY = "sync_frequency";
    public static final String PREFS_BEFORE_ID = "before_id";
    public static final String PREFS_NUMBER_TO_RETRIEVE = "number_to_retrieve";
    public static final String PREFS_SORT_ORDER = "sort_order";
    public static final String PREFS_SUBREDDITS = "subreddits";

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
            initialiseClickListener(PREFS_SUBREDDITS);
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
            if (preference.getKey().equals(PREFS_SUBREDDITS)) {
                showSelectSubredditsDialog();
                return true;
            } else if (preference.getKey().equals(OPEN_SOURCE)) {
                new LicensesDialog(getActivity(), R.raw.open_source_notices, false, true).show();
                return true;
            }
            return false;
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            updatePrefsSummary(findPreference(key));

            if (key.equals(PREFS_REFRESH_FREQUENCY)) {
                Utils.setRecurringAlarm(getActivity().getApplicationContext());
            } else if (key.equals(PREFS_SORT_ORDER) || key.equals(PREFS_SUBREDDITS)) {
                clearSavedPosition();
            }
        }

        private void clearSavedPosition() {
            getPreferenceManager().getSharedPreferences().edit().putString(SettingsActivity.PREFS_BEFORE_ID, "").apply();
        }

        protected void initSummary() {
            for (int i = 0; i < getPreferenceScreen().getPreferenceCount(); i++) {
                initPrefsSummary(getPreferenceScreen().getPreference(i));
            }
        }

        protected void initPrefsSummary(Preference p) {
            if (p instanceof PreferenceCategory) {
                PreferenceCategory cat = (PreferenceCategory) p;
                for (int i = 0; i < cat.getPreferenceCount(); i++) {
                    initPrefsSummary(cat.getPreference(i));
                }
            } else {
                updatePrefsSummary(p);
            }
        }

        protected void updatePrefsSummary(Preference pref) {
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

        private void showSelectSubredditsDialog() {
            final List<String> savedSubreddits = Utils.allSubReddits(getActivity().getApplicationContext());
            final List<String> selectedSubreddits = Utils.selectedSubReddits(getActivity().getApplicationContext());
            final boolean[] selected = new boolean[savedSubreddits.size()];

            int i = 0;
            for (String s : savedSubreddits) {
                selected[i] = selectedSubreddits.contains(s);

                i++;
            }

            new AlertDialog.Builder(getActivity())
                    .setTitle(getString(R.string.select_subreddits))
                    .setMultiChoiceItems(savedSubreddits.toArray(new String[savedSubreddits.size()]), selected, new DialogInterface.OnMultiChoiceClickListener() {

                        @Override
                        public void onClick(DialogInterface dialogInterface, int i, boolean b) {
                            selected[i] = b;
                            ((AlertDialog) dialogInterface).getListView().setItemChecked(i, b);
                        }
                    })
                    .setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            List<String> selectedSrs = new ArrayList<String>();

                            int index = 0;
                            for (boolean b : selected) {
                                if (b) {
                                    selectedSrs.add(savedSubreddits.get(index));
                                }
                                index++;
                            }
                            Utils.saveSubreddits(getActivity().getApplicationContext(), savedSubreddits);
                            Utils.saveSelectedSubreddits(getActivity().getApplicationContext(), selectedSrs);
                        }
                    })
                    .setNeutralButton(R.string.add_subreddit, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            showAddSubredditDialog();
                        }
                    })
                    .setNegativeButton(R.string.cancel, null)
                    .create()
                    .show();
        }

        private void showAddSubredditDialog() {
            final EditText input = new EditText(getActivity());

            new AlertDialog.Builder(getActivity())
                    .setNegativeButton(R.string.cancel, null)
                    .setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            String subreddit = input.getText().toString().trim();
                            if (subreddit.length() > 0) {
                                Utils.addSubreddit(getActivity().getApplicationContext(), subreddit);
                            }
                            showSelectSubredditsDialog();
                        }
                    })
                    .setTitle(R.string.add_subreddit)
                    .setMessage(R.string.enter_subreddit_name)
                    .setView(input)
                    .create()
                    .show();
        }
    }
}
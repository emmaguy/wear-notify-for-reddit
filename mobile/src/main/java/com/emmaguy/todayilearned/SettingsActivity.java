package com.emmaguy.todayilearned;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.commonsware.cwac.wakeful.WakefulIntentService;
import com.emmaguy.todayilearned.background.AppListener;
import com.emmaguy.todayilearned.data.LoginResponse;
import com.emmaguy.todayilearned.data.Reddit;
import com.emmaguy.todayilearned.data.SubscriptionResponse;
import com.emmaguy.todayilearned.sharedlib.Constants;
import com.google.gson.GsonBuilder;

import java.util.List;

import de.psdev.licensesdialog.LicensesDialog;
import retrofit.RestAdapter;
import retrofit.converter.GsonConverter;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class SettingsActivity extends Activity {

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

            WakefulIntentService.scheduleAlarms(new AppListener(), getActivity().getApplicationContext());

            initSummary();

            initialiseClickListener(getString(R.string.prefs_key_open_source));
            initialiseClickListener(getString(R.string.prefs_key_account_info));
            initialiseClickListener(getString(R.string.prefs_key_sync_subreddits));
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
            getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);

            super.onPause();
        }

        @Override
        public boolean onPreferenceClick(Preference preference) {
            if (preference.getKey().equals(getString(R.string.prefs_key_open_source))) {
                new LicensesDialog(getActivity(), R.raw.open_source_notices, false, true).show();
                return true;
            } else if (preference.getKey().equals(getString(R.string.prefs_key_account_info))) {
                showLoginDialog();
            } else if (preference.getKey().equals(getString(R.string.prefs_key_sync_subreddits))) {
                if (isLoggedIn()) {
                    syncSubreddits();
                } else {
                    Toast.makeText(getActivity(), R.string.you_need_to_sign_in_to_sync_subreddits, Toast.LENGTH_SHORT).show();
                }
            }
            return false;
        }

        private void syncSubreddits() {
            final ProgressDialog spinner = ProgressDialog.show(getActivity(), "", getString(R.string.syncing_subreddits));

            final RestAdapter restAdapter = new RestAdapter.Builder()
                    .setEndpoint("https://www.reddit.com/")
                    .setRequestInterceptor(new RedditRequestInterceptor(getCookie(), getModhash()))
                    .setConverter(new GsonConverter(new GsonBuilder().registerTypeAdapter(SubscriptionResponse.class, new SubscriptionResponse.SubscriptionResponseJsonDeserializer()).create()))
                    .build();

            final Reddit redditEndpoint = restAdapter.create(Reddit.class);
            redditEndpoint.subredditSubscriptions()
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<SubscriptionResponse>() {
                        @Override
                        public void onNext(SubscriptionResponse response) {
                            if (response.hasErrors()) {
                                throw new RuntimeException("Failed to sync subreddits: " + response);
                            }
                            List<String> subreddits = response.getSubreddits();

                            SubredditPreference pref = (SubredditPreference) findPreference(getString(R.string.prefs_key_subreddits));

                            pref.saveSubreddits(subreddits);
                            pref.saveSelectedSubreddits(subreddits);
                        }

                        @Override
                        public void onCompleted() {
                            spinner.dismiss();
                            Logger.sendEvent(getActivity(), Logger.LOG_EVENT_SYNC_SUBREDDITS, Logger.LOG_EVENT_SUCCESS);
                            Toast.makeText(getActivity(), R.string.successfully_synced_subreddits, Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onError(Throwable e) {
                            Logger.sendEvent(getActivity(), Logger.LOG_EVENT_SYNC_SUBREDDITS, Logger.LOG_EVENT_FAILURE);
                            Logger.Log(getActivity().getApplicationContext(), e.getMessage(), e);
                            spinner.dismiss();
                            Toast.makeText(getActivity(), R.string.failed_to_sync_subreddits, Toast.LENGTH_SHORT).show();
                        }
                    });
        }

        private void showLoginDialog() {
            View layout = LayoutInflater.from(getActivity()).inflate(R.layout.login_dialog, null);

            final EditText username = (EditText) layout.findViewById(R.id.username_edittext);
            final EditText password = (EditText) layout.findViewById(R.id.password_edittext);

            new AlertDialog.Builder(getActivity())
                    .setNegativeButton(R.string.cancel, null)
                    .setPositiveButton(R.string.login, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            loginToReddit(username.getText().toString(), password.getText().toString());
                        }
                    })
                    .setTitle(R.string.login_to_reddit)
                    .setView(layout)
                    .create()
                    .show();
        }

        private void loginToReddit(final String username, String password) {
            final ProgressDialog spinner = ProgressDialog.show(getActivity(), "", getString(R.string.logging_in));

            final RestAdapter restAdapter = new RestAdapter.Builder()
                    .setEndpoint(Constants.ENDPOINT_URL_REDDIT)
                    .setConverter(new GsonConverter(new GsonBuilder().registerTypeAdapter(LoginResponse.class, new LoginResponse.LoginResponseJsonDeserializer()).create()))
                    .build();

            final Reddit redditEndpoint = restAdapter.create(Reddit.class);
            redditEndpoint.login(username, password)
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<LoginResponse>() {
                        @Override
                        public void onNext(LoginResponse response) {
                            if (response.hasErrors()) {
                                throw new RuntimeException("Failed to login: " + response);
                            }
                            updateLoginInformation(response.getModhash(), response.getCookie(), username);
                        }

                        @Override
                        public void onCompleted() {
                            Logger.sendEvent(getActivity().getApplicationContext(), Logger.LOG_EVENT_LOGIN, Logger.LOG_EVENT_SUCCESS);
                            spinner.dismiss();
                        }

                        @Override
                        public void onError(Throwable e) {
                            Logger.sendEvent(getActivity().getApplicationContext(), Logger.LOG_EVENT_LOGIN, Logger.LOG_EVENT_FAILURE);
                            Logger.Log(getActivity().getApplicationContext(), e.getMessage(), e);
                            spinner.dismiss();
                            Toast.makeText(getActivity(), R.string.failed_to_login, Toast.LENGTH_SHORT).show();
                        }
                    });
        }

        private void updateLoginInformation(String modhash, String cookie, String username) {
            getPreferenceManager().getSharedPreferences()
                    .edit()
                    .putString(getString(R.string.prefs_key_username), username)
                    .putString(getString(R.string.prefs_key_modhash), modhash)
                    .putString(getString(R.string.prefs_key_cookie), cookie)
                    .apply();

            updatePrefsSummary(findPreference(getString(R.string.prefs_key_account_info)));
        }

        private boolean isLoggedIn() {
            return !TextUtils.isEmpty(getCookie());
        }

        private String getModhash() {
            return getPreferenceManager().getSharedPreferences().getString(getString(R.string.prefs_key_modhash), "");
        }

        private String getCookie() {
            return getPreferenceManager().getSharedPreferences().getString(getString(R.string.prefs_key_cookie), "");
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            updatePrefsSummary(findPreference(key));

            SubredditPreference subredditPreference = (SubredditPreference) findPreference(getString(R.string.prefs_key_subreddits));

            if (key.equals(getString(R.string.prefs_key_sync_frequency))) {
                Logger.sendEvent(getActivity().getApplicationContext(), Logger.LOG_EVENT_UPDATE_INTERVAL, sharedPreferences.getString(getString(R.string.prefs_key_sync_frequency), ""));
                WakefulIntentService.scheduleAlarms(new AppListener(), getActivity().getApplicationContext());
            } else if (key.equals(getString(R.string.prefs_key_sort_order)) || key.equals(subredditPreference.getKey()) || key.equals(subredditPreference.getSelectedSubredditsKey())) {
                clearSavedUtcTime();
            }
        }

        private void clearSavedUtcTime() {
            getPreferenceManager().getSharedPreferences().edit().putLong(getString(R.string.prefs_key_created_utc), 0).apply();
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
            } else if (pref instanceof PreferenceScreen) {
                PreferenceScreen screen = (PreferenceScreen) pref;

                if (screen.getKey().equals(getString(R.string.prefs_key_account_info))) {
                    if (isLoggedIn()) {
                        screen.setSummary(getString(R.string.logged_in_as_x, getUsername()));
                    }
                }
            }
        }

        private String getUsername() {
            return getPreferenceManager().getSharedPreferences().getString(getString(R.string.prefs_key_username), "");
        }
    }
}

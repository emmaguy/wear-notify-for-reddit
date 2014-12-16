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
import com.emmaguy.todayilearned.sharedlib.Logger;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.List;

import de.psdev.licensesdialog.LicensesDialog;
import retrofit.RestAdapter;
import retrofit.converter.GsonConverter;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class SettingsActivity extends Activity {
    public static final String PREFS_REFRESH_FREQUENCY = "sync_frequency";
    public static final String PREFS_NUMBER_TO_RETRIEVE = "number_to_retrieve";
    public static final String PREFS_SORT_ORDER = "sort_order";
    public static final String PREFS_CREATED_UTC = "created_utc";
    public static final String PREFS_MESSAGES_ENABLED = "messages_enabled";

    public static final String PREFS_ACCOUNT_INFO = "account_info";
    public static final String PREFS_SYNC_SUBREDDITS = "sync_subreddits";

    public static final String PREFS_OPEN_SOURCE = "open_source";

    public static final String PREFS_KEY_COOKIE = "cookie";
    public static final String PREFS_KEY_MODHASH = "modhash";
    private static final String PREFS_KEY_USERNAME = "username";

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

            initialiseClickListener(PREFS_OPEN_SOURCE);
            initialiseClickListener(PREFS_ACCOUNT_INFO);
            initialiseClickListener(PREFS_SYNC_SUBREDDITS);
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
            if (preference.getKey().equals(PREFS_OPEN_SOURCE)) {
                new LicensesDialog(getActivity(), R.raw.open_source_notices, false, true).show();
                return true;
            } else if (preference.getKey().equals(PREFS_ACCOUNT_INFO)) {
                showLoginDialog();
            } else if (preference.getKey().equals(PREFS_SYNC_SUBREDDITS)) {
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
                            List<String> subreddits = response.getSubreddits();

                            SubredditPreference pref = (SubredditPreference) findPreference(SubredditPreference.PREFS_SUBREDDITS);

                            pref.saveSubreddits(subreddits);
                            pref.saveSelectedSubreddits(subreddits);
                        }

                        @Override
                        public void onCompleted() {
                            spinner.dismiss();
                            Toast.makeText(getActivity(), R.string.successfully_synced_subreddits, Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onError(Throwable e) {
                            Logger.Log("Error syncing subreddits", e);
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
                            updateLoginInformation(response.getModhash(), response.getCookie(), username);
                        }

                        @Override
                        public void onCompleted() {
                            spinner.dismiss();
                        }

                        @Override
                        public void onError(Throwable e) {
                            Logger.Log("Error logging in to reddit", e);
                            spinner.dismiss();
                            Toast.makeText(getActivity(), R.string.failed_to_login, Toast.LENGTH_SHORT).show();
                        }
                    });
        }

        private void updateLoginInformation(String modhash, String cookie, String username) {
            getPreferenceManager().getSharedPreferences()
                    .edit()
                    .putString(SettingsActivity.PREFS_KEY_USERNAME, username)
                    .putString(SettingsActivity.PREFS_KEY_MODHASH, modhash)
                    .putString(SettingsActivity.PREFS_KEY_COOKIE, cookie)
                    .apply();

            updatePrefsSummary(findPreference(PREFS_ACCOUNT_INFO));
        }

        private boolean isLoggedIn() {
            return !TextUtils.isEmpty(getCookie());
        }

        private String getModhash() {
            return getPreferenceManager().getSharedPreferences().getString(SettingsActivity.PREFS_KEY_MODHASH, "");
        }

        private String getCookie() {
            return getPreferenceManager().getSharedPreferences().getString(SettingsActivity.PREFS_KEY_COOKIE, "");
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            updatePrefsSummary(findPreference(key));

            Logger.Log("onSharedPreferenceChanged: " + key);

            if (key.equals(PREFS_REFRESH_FREQUENCY)) {
                WakefulIntentService.scheduleAlarms(new AppListener(), getActivity().getApplicationContext());
            } else if (key.equals(PREFS_SORT_ORDER) || key.equals(SubredditPreference.PREFS_SUBREDDITS) || key.equals(SubredditPreference.PREFS_SELECTED_SUBREDDITS)) {
                clearSavedUtcTime();
            }
        }

        private void clearSavedUtcTime() {
            Logger.Log("clearSavedUtcTime");

            getPreferenceManager().getSharedPreferences().edit().putLong(SettingsActivity.PREFS_CREATED_UTC, 0).apply();
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

                if (screen.getKey().equals(PREFS_ACCOUNT_INFO)) {
                    if (isLoggedIn()) {
                        screen.setSummary(getString(R.string.logged_in_as_x, getUsername()));
                    }
                }
            }
        }

        private String getUsername() {
            return getPreferenceManager().getSharedPreferences().getString(SettingsActivity.PREFS_KEY_USERNAME, "");
        }
    }
}

package com.emmaguy.todayilearned.ui;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.commonsware.cwac.wakeful.WakefulIntentService;
import com.emmaguy.todayilearned.App;
import com.emmaguy.todayilearned.Logger;
import com.emmaguy.todayilearned.R;
import com.emmaguy.todayilearned.Utils;
import com.emmaguy.todayilearned.background.AppListener;
import com.emmaguy.todayilearned.data.auth.RedditAccessTokenRequester;
import com.emmaguy.todayilearned.data.auth.RedditRequestTokenUriParser;
import com.emmaguy.todayilearned.data.model.Token;
import com.emmaguy.todayilearned.data.response.SubscriptionResponse;
import com.emmaguy.todayilearned.data.retrofit.AuthenticatedRedditService;
import com.emmaguy.todayilearned.data.retrofit.UnauthenticatedRedditService;
import com.emmaguy.todayilearned.data.storage.TokenStorage;
import com.emmaguy.todayilearned.data.storage.UserStorage;
import com.emmaguy.todayilearned.sharedlib.Constants;
import com.google.gson.GsonBuilder;

import java.util.List;

import javax.inject.Inject;

import de.psdev.licensesdialog.LicensesDialog;
import retrofit.RequestInterceptor;
import retrofit.converter.Converter;
import retrofit.converter.GsonConverter;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().setTitle(R.string.app_name);
        getFragmentManager().beginTransaction().replace(android.R.id.content, new SettingsFragment()).commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_feedback, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_feedback) {
            startActivity(Utils.getFeedbackEmailIntent(this));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public static class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener, Preference.OnPreferenceClickListener {
        @Inject UnauthenticatedRedditService mUnauthenticatedRedditService;
        @Inject AuthenticatedRedditService mAuthenticatedRedditService;
        @Inject RedditAccessTokenRequester mRedditAccessTokenRequester;
        @Inject RedditRequestTokenUriParser mRequestTokenUriParser;
        @Inject RequestInterceptor mRequestInterceptor;
        @Inject Converter mTokenConverter;
        @Inject TokenStorage mTokenStorage;
        @Inject UserStorage mUserStorage;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            App.with(getActivity()).getAppComponent().inject(this);

            addPreferencesFromResource(R.xml.prefs);

            WakefulIntentService.scheduleAlarms(new AppListener(), getActivity().getApplicationContext());

            initSummary();

            if (Utils.sIsDebug) {
                initialiseClickListener(getString(R.string.prefs_force_expire_token));
                initialiseClickListener(getString(R.string.prefs_force_refresh_now));
            } else {
                getPreferenceScreen().removePreference(findPreference(getString(R.string.prefs_force_expire_token)));
                getPreferenceScreen().removePreference(findPreference(getString(R.string.prefs_force_refresh_now)));
            }

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

            Uri uri = getActivity().getIntent().getData();
            mRequestTokenUriParser.setUri(uri);

            if (mRequestTokenUriParser.hasValidCode()) {
                getAccessToken(mRequestTokenUriParser.getCode());
            } else if (mRequestTokenUriParser.showError()) {
                new AlertDialog.Builder(getActivity())
                        .setPositiveButton(android.R.string.ok, null)
                        .setTitle(R.string.login_to_reddit)
                        .setMessage(R.string.error_whilst_trying_to_login)
                        .create()
                        .show();
            }
        }

        private void getAccessToken(String code) {
            final ProgressDialog spinner = ProgressDialog.show(getActivity(), "", getString(R.string.logging_in));
            final String redirectUri = getString(R.string.redirect_url_scheme) + getString(R.string.redirect_url_callback);

            mUnauthenticatedRedditService
                    .getRedditService(mTokenConverter, mRequestInterceptor)
                    .loginToken(Constants.GRANT_TYPE_AUTHORISATION_CODE, redirectUri, code)
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<Token>() {
                        @Override
                        public void onCompleted() {
                            spinner.dismiss();
                            initPrefsSummary(findPreference(getString(R.string.prefs_key_account_info)));
                            Logger.sendEvent(getActivity().getApplicationContext(), Logger.LOG_EVENT_LOGIN, Logger.LOG_EVENT_SUCCESS);
                        }

                        @Override
                        public void onError(Throwable e) {
                            Logger.sendEvent(getActivity().getApplicationContext(), Logger.LOG_EVENT_LOGIN, Logger.LOG_EVENT_FAILURE);
                            Logger.sendThrowable(getActivity().getApplicationContext(), e.getMessage(), e);
                            spinner.dismiss();
                            Toast.makeText(getActivity(), R.string.failed_to_login, Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onNext(Token tokenResponse) {
                            mTokenStorage.saveToken(tokenResponse);
                        }
                    });
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
                mRedditAccessTokenRequester.request();
            } else if (preference.getKey().equals(getString(R.string.prefs_key_sync_subreddits))) {
                if (mTokenStorage.isLoggedIn()) {
                    syncSubreddits();
                } else {
                    Toast.makeText(getActivity(), R.string.you_need_to_sign_in_to_sync_subreddits, Toast.LENGTH_SHORT).show();
                }
            } else if (preference.getKey().equals(getString(R.string.prefs_force_expire_token))) {
                mTokenStorage.forceExpireToken();
            } else if (preference.getKey().equals(getString(R.string.prefs_force_refresh_now))) {
                mUserStorage.setRetrievedPostCreatedUtc(0);
                WakefulIntentService.scheduleAlarms(new AppListener(), getActivity().getApplicationContext());
            }
            return false;
        }

        private void syncSubreddits() {
            final ProgressDialog spinner = ProgressDialog.show(getActivity(), "", getString(R.string.syncing_subreddits));

            final GsonConverter converter = new GsonConverter(new GsonBuilder().registerTypeAdapter(SubscriptionResponse.class, new SubscriptionResponse.SubscriptionResponseJsonDeserializer()).create());
            mAuthenticatedRedditService
                    .getRedditService(converter)
                    .subredditSubscriptions()
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
                            Logger.sendThrowable(getActivity().getApplicationContext(), e.getMessage(), e);
                            spinner.dismiss();
                            Toast.makeText(getActivity(), R.string.failed_to_sync_subreddits, Toast.LENGTH_SHORT).show();
                        }
                    });
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

            sendEvents(sharedPreferences, key);
        }

        private void sendEvents(SharedPreferences sharedPreferences, String key) {
            if (key.equals(getString(R.string.prefs_key_sort_order))) {
                Logger.sendEvent(getActivity(), Logger.LOG_EVENT_SORT_ORDER, sharedPreferences.getString(key, ""));
            } else if (key.equals(getString(R.string.prefs_key_open_on_phone_dismisses))) {
                Logger.sendEvent(getActivity(), Logger.LOG_EVENT_OPEN_ON_PHONE_DISMISSES, sharedPreferences.getBoolean(key, false) + "");
            } else if (key.equals(getString(R.string.prefs_key_full_image))) {
                Logger.sendEvent(getActivity(), Logger.LOG_EVENT_HIGH_RES_IMAGE, sharedPreferences.getBoolean(key, false) + "");
            }
        }

        private void clearSavedUtcTime() {
            mUserStorage.setRetrievedPostCreatedUtc(0);
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
                    if (mTokenStorage.isLoggedIn()) {
                        screen.setSummary(getString(R.string.logged_in));
                    }
                }
            } else if (pref instanceof DragReorderActionsPreference) {
                pref.setSummary(pref.getSummary());
            }
        }
    }
}

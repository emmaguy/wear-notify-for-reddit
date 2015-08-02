package com.emmaguy.todayilearned.settings;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
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
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.commonsware.cwac.wakeful.WakefulIntentService;
import com.emmaguy.todayilearned.App;
import com.emmaguy.todayilearned.BuildConfig;
import com.emmaguy.todayilearned.R;
import com.emmaguy.todayilearned.common.Logger;
import com.emmaguy.todayilearned.common.StringUtils;
import com.emmaguy.todayilearned.common.Utils;
import com.emmaguy.todayilearned.refresh.BackgroundAlarmListener;
import com.emmaguy.todayilearned.refresh.RedditAuthenticationService;
import com.emmaguy.todayilearned.refresh.RedditService;
import com.emmaguy.todayilearned.refresh.Token;
import com.emmaguy.todayilearned.sharedlib.Constants;
import com.emmaguy.todayilearned.storage.TokenStorage;
import com.emmaguy.todayilearned.storage.UserStorage;

import javax.inject.Inject;

import de.psdev.licensesdialog.LicensesDialog;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;

import static com.emmaguy.todayilearned.sharedlib.Constants.ACTION_ORDER_OPEN_ON_PHONE;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().setTitle(R.string.app_name);
        getFragmentManager().beginTransaction().replace(android.R.id.content, new SettingsFragment()).commit();
    }

    public static class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener, Preference.OnPreferenceClickListener {
        @Inject RedditAccessTokenRequester mRedditAccessTokenRequester;
        @Inject RedditAuthenticationService mRedditAuthenticationService;
        @Inject RedditRequestTokenUriParser mRequestTokenUriParser;
        @Inject BackgroundAlarmListener mAlarmListener;
        @Inject WearableActionStorage mWearableActionStorage;
        @Inject RedditService mRedditService;
        @Inject TokenStorage mTokenStorage;
        @Inject UserStorage mUserStorage;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            App.with(getActivity()).getAppComponent().inject(this);

            WakefulIntentService.scheduleAlarms(mAlarmListener, getActivity().getApplicationContext());

            addPreferencesFromResource(R.xml.preferences);
            initSummary();

            if (App.with(getActivity()).isDebug()) {
                initialiseClickListener(getString(R.string.prefs_force_expire_token));
                initialiseClickListener(getString(R.string.prefs_force_refresh_now));
            } else {
                final PreferenceScreen screen = (PreferenceScreen) findPreference(getString(R.string.prefs_key_parent));
                screen.removePreference(findPreference(getString(R.string.prefs_key_debug)));
            }

            initialiseClickListener(getString(R.string.prefs_key_open_source));
            initialiseClickListener(getString(R.string.prefs_key_account_info));
            initialiseClickListener(getString(R.string.prefs_key_sync_subreddits));

            toggleRedditSettings();
            toggleOpenOnPhoneAction();

            setHasOptionsMenu(true);

            showDialogIfStillUsingOldAuth();
        }

        private void showDialogIfStillUsingOldAuth() {
            final String usernameKey = getString(R.string.prefs_key_username);
            final String username = getPreferenceManager().getSharedPreferences().getString(usernameKey, "");
            if (!StringUtils.isEmpty(username)) {
                new AlertDialog.Builder(getActivity())
                        .setCancelable(false)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override public void onClick(DialogInterface dialog, int which) {
                                getPreferenceManager().getSharedPreferences().edit().remove(usernameKey).apply();
                            }
                        })
                        .setTitle(getString(R.string.app_name) + " v" + BuildConfig.VERSION_NAME)
                        .setMessage(R.string.auth_change_message)
                        .create()
                        .show();
            }
        }

        private void toggleRedditSettings() {
            findPreference(getString(R.string.prefs_key_sync_subreddits)).setEnabled(mTokenStorage.isLoggedIn());
            findPreference(getString(R.string.prefs_key_messages_enabled)).setEnabled(mTokenStorage.isLoggedIn());
        }

        private void initialiseClickListener(String key) {
            Preference preference = findPreference(key);
            if (preference != null) {
                preference.setOnPreferenceClickListener(this);
            }
        }

        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            inflater.inflate(R.menu.menu_feedback, menu);
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            if (item.getItemId() == R.id.action_feedback) {
                startActivity(Utils.getFeedbackEmailIntent(getActivity(), buildExtraInformation()));
                return true;
            }

            return super.onOptionsItemSelected(item);
        }

        private String buildExtraInformation() {
            StringBuilder sb = new StringBuilder();

            sb.append(getString(R.string.debug_information_explanation) + "\n\n");
            sb.append("Number of posts: " + mUserStorage.getNumberToRequest() + "\n");
            sb.append("Sort order: " + mUserStorage.getSortType() + "\n");
            sb.append("Refresh interval: " + mUserStorage.getRefreshInterval() + "\n");
            sb.append("Selected subreddits: " + mUserStorage.getSubreddits() + "\n");
            sb.append("Stored timestamp: " + mUserStorage.getTimestamp() + "\n");
            sb.append("Is logged in: " + mTokenStorage.isLoggedIn() + "\n");
            sb.append("Has token expired: " + mTokenStorage.hasTokenExpired() + "\n");
            sb.append("\n\n");

            return sb.toString();
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

            // TODO: unit test this chain, verify retrieving an empty token errors
            mRedditAuthenticationService.loginToken(Constants.GRANT_TYPE_AUTHORISATION_CODE, redirectUri, code)
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<Token>() {
                        @Override
                        public void onCompleted() {
                            spinner.dismiss();
                        }

                        @Override
                        public void onError(Throwable e) {
                            App.with(getActivity()).sendEvent(Logger.LOG_EVENT_LOGIN, Logger.LOG_EVENT_FAILURE);
                            Timber.e(e, "Failed to get access token");
                            spinner.dismiss();
                            Toast.makeText(getActivity(), R.string.failed_to_login, Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onNext(Token tokenResponse) {
                            mTokenStorage.saveToken(tokenResponse);
                            if (mTokenStorage.isLoggedIn()) {
                                toggleRedditSettings();
                                initPrefsSummary(findPreference(getString(R.string.prefs_key_account_info)));
                                App.with(getActivity()).sendEvent(Logger.LOG_EVENT_LOGIN, Logger.LOG_EVENT_SUCCESS);
                                Toast.makeText(getActivity(), R.string.successfully_logged_in, Toast.LENGTH_SHORT).show();
                            } else {
                                throw new RuntimeException("Failed to login " + tokenResponse);
                            }
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
            final String preferenceKey = preference.getKey();
            if (preferenceKey.equals(getString(R.string.prefs_key_open_source))) {
                new LicensesDialog(getActivity(), R.raw.open_source_notices, false, true).show();
                return true;
            } else if (preferenceKey.equals(getString(R.string.prefs_key_account_info))) {
                if (mTokenStorage.isLoggedIn()) {
                    mTokenStorage.clearToken();
                    initSummary();
                    toggleRedditSettings();
                    Toast.makeText(getActivity(), R.string.logged_out, Toast.LENGTH_SHORT).show();
                } else {
                    mRedditAccessTokenRequester.request();
                }
            } else if (preferenceKey.equals(getString(R.string.prefs_key_sync_subreddits))) {
                if (mTokenStorage.isLoggedIn()) {
                    syncSubreddits();
                } else {
                    Toast.makeText(getActivity(), R.string.you_need_to_sign_in_to_sync_subreddits, Toast.LENGTH_SHORT).show();
                }
            } else if (preferenceKey.equals(getString(R.string.prefs_force_expire_token))) {
                mTokenStorage.forceExpireToken();
            } else if (preferenceKey.equals(getString(R.string.prefs_force_refresh_now))) {
                mUserStorage.clearTimestamp();
                mAlarmListener.sendWakefulWork(getActivity());
            } else if (preferenceKey.equals(getString(R.string.prefs_key_actions_order))) {
                App.with(getActivity()).sendEvent(Logger.LOG_EVENT_CUSTOMISE_ACTIONS, "");
            }
            return false;
        }

        private void syncSubreddits() {
            final ProgressDialog spinner = ProgressDialog.show(getActivity(), "", getString(R.string.syncing_subreddits));

            mRedditService
                    .subredditSubscriptions()
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<SubscriptionResponse>() {
                        @Override
                        public void onNext(SubscriptionResponse response) {
                            if (response.hasErrors()) {
                                throw new RuntimeException("Failed to sync subreddits: " + response);
                            }

                            SubredditPreference pref = (SubredditPreference) findPreference(getString(R.string.prefs_key_subreddits));
                            pref.saveSubreddits(response.getSubreddits());
                        }

                        @Override
                        public void onCompleted() {
                            spinner.dismiss();
                            App.with(getActivity()).sendEvent(Logger.LOG_EVENT_SYNC_SUBREDDITS, Logger.LOG_EVENT_SUCCESS);
                            Toast.makeText(getActivity(), R.string.successfully_synced_subreddits, Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onError(Throwable e) {
                            App.with(getActivity()).sendEvent(Logger.LOG_EVENT_SYNC_SUBREDDITS, Logger.LOG_EVENT_FAILURE);
                            Timber.e(e, "Failed to sync subreddits");
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
                App.with(getActivity()).sendEvent(Logger.LOG_EVENT_UPDATE_INTERVAL, sharedPreferences.getString(getString(R.string.prefs_key_sync_frequency), ""));
                WakefulIntentService.scheduleAlarms(mAlarmListener, getActivity().getApplicationContext());
            } else if (key.equals(getString(R.string.prefs_key_sort_order)) || key.equals(subredditPreference.getKey()) || key.equals(subredditPreference.getSelectedSubredditsKey())) {
                clearSavedUtcTime();
            } else if (key.equals(getString(R.string.prefs_key_actions_order)) || key.equals(getString(R.string.prefs_key_actions_order_ordered))) {
                toggleOpenOnPhoneAction();
            }

            sendEvents(sharedPreferences, key);
        }

        private void toggleOpenOnPhoneAction() {
            boolean enableOpenOnPhoneOption = false;
            for (Integer i : mWearableActionStorage.getSelectedActionIds()) {
                if (i == ACTION_ORDER_OPEN_ON_PHONE) {
                    enableOpenOnPhoneOption = true;
                    break;
                }
            }
            findPreference(getString(R.string.prefs_key_open_on_phone_dismisses)).setEnabled(enableOpenOnPhoneOption);
        }

        private void sendEvents(SharedPreferences sharedPreferences, String key) {
            if (key.equals(getString(R.string.prefs_key_sort_order))) {
                App.with(getActivity()).sendEvent(Logger.LOG_EVENT_SORT_ORDER, sharedPreferences.getString(key, ""));
            } else if (key.equals(getString(R.string.prefs_key_open_on_phone_dismisses))) {
                App.with(getActivity()).sendEvent(Logger.LOG_EVENT_OPEN_ON_PHONE_DISMISSES, sharedPreferences.getBoolean(key, false) + "");
            } else if (key.equals(getString(R.string.prefs_key_full_image))) {
                App.with(getActivity()).sendEvent(Logger.LOG_EVENT_HIGH_RES_IMAGE, sharedPreferences.getBoolean(key, false) + "");
            }
        }

        private void clearSavedUtcTime() {
            mUserStorage.clearTimestamp();
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
                    } else {
                        screen.setSummary(R.string.tap_to_sign_in);
                    }
                }
            } else if (pref instanceof DragReorderActionsPreference) {
                pref.setSummary(pref.getSummary());
            }
        }
    }
}

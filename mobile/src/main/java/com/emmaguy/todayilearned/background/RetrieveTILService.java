package com.emmaguy.todayilearned.background;

import android.app.IntentService;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.text.TextUtils;
import android.util.Log;

import com.emmaguy.todayilearned.R;
import com.emmaguy.todayilearned.SettingsActivity;
import com.emmaguy.todayilearned.data.Listing;
import com.emmaguy.todayilearned.data.RedditTIL;
import com.emmaguy.todayilearned.data.TIL;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.List;

import retrofit.RestAdapter;
import retrofit.android.AndroidLog;
import retrofit.converter.GsonConverter;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class RetrieveTILService extends IntentService {
    private static final int NOTIFICATION_ID = 1;

    private final RestAdapter restAdapter = new RestAdapter.Builder()
            .setEndpoint("http://www.reddit.com/")
            .setConverter(new GsonConverter(new GsonBuilder().registerTypeAdapter(Listing.class, new Listing.ListingJsonDeserializer()).create()))
            .build();

    private final RedditTIL mRedditTILEndpoint = restAdapter.create(RedditTIL.class);
    private final List mNotificationPages = new ArrayList();

    public RetrieveTILService() {
        super("RetrieveTILService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        retrieveLatestTILsFromReddit();
    }

    private void retrieveLatestTILsFromReddit() {
        mRedditTILEndpoint.latestTILs(getSortType(), getNumberToRequest(), getBeforeId())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .flatMap(new Func1<Listing, Observable<TIL>>() {
                    @Override
                    public Observable<TIL> call(Listing listing) {
                        storeNewBeforeId(listing.before);

                        return Observable.from(listing.getTodayILearneds());
                    }
                })
                .subscribe(new Action1<TIL>() {
                    @Override
                    public void call(TIL til) {
                        NotificationCompat.BigTextStyle extraPageStyle = new NotificationCompat.BigTextStyle();
                        extraPageStyle.bigText(til.getTitle());
                        Notification extraPageNotification = new NotificationCompat.Builder(RetrieveTILService.this)
                                .setStyle(extraPageStyle)
                                .build();
                        mNotificationPages.add(extraPageNotification);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Log.e("TIL", "failed: ", throwable);
                    }
                }, new Action0() {
                    @Override
                    public void call() {
                        sendNewTILsNotification();
                    }
                });
    }

    private void storeNewBeforeId(String before) {
        if (!TextUtils.isEmpty(before)) {
            getSharedPreferences().edit().putString(SettingsActivity.PREFS_BEFORE_ID, before).apply();
        }
    }

    private void sendNewTILsNotification() {
        if (mNotificationPages.size() <= 0) {
            return;
        }

        Intent dismissIntent = new Intent(this, DismissNotificationsReceiver.class);
        dismissIntent.putExtra(DismissNotificationsReceiver.NOTIFICATION_ID_EXTRA, NOTIFICATION_ID);
        dismissIntent.setAction(DismissNotificationsReceiver.DISMISS_ACTION);

        PendingIntent dismissPendingIntent = PendingIntent.getBroadcast(this, 0, dismissIntent, 0);

        NotificationCompat.Builder builder1 = new NotificationCompat.Builder(this)
                .addAction(R.drawable.ic_action_done, getString(R.string.dismiss_all), dismissPendingIntent)
                .setContentTitle(getResources().getQuantityString(R.plurals.x_new_today_i_learned, mNotificationPages.size(), mNotificationPages.size()))
                .setSmallIcon(R.drawable.ic_launcher);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(NOTIFICATION_ID,
                new NotificationCompat.WearableExtender()
                        .addPages(mNotificationPages)
                        .extend(builder1)
                        .build());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mNotificationPages.clear();
    }

    private SharedPreferences getSharedPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    }

    private int getNumberToRequest() {
        return Integer.parseInt(getSharedPreferences().getString(SettingsActivity.PREFS_NUMBER_TO_RETRIEVE, "5"));
    }

    private String getBeforeId() {
        return getSharedPreferences().getString(SettingsActivity.PREFS_BEFORE_ID, "");
    }

    private String getSortType() {
        return getSharedPreferences().getString(SettingsActivity.PREFS_SORT_ORDER, "hot");
    }
}

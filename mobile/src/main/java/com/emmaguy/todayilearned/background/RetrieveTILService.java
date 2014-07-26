package com.emmaguy.todayilearned.background;

import android.app.IntentService;
import android.app.Notification;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.emmaguy.todayilearned.R;
import com.emmaguy.todayilearned.data.Listing;
import com.emmaguy.todayilearned.data.RedditTIL;
import com.emmaguy.todayilearned.data.TIL;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

import retrofit.RestAdapter;
import retrofit.client.OkClient;
import retrofit.converter.GsonConverter;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class RetrieveTILService extends IntentService {

    private static final String PREFS_BEFORE_ID = "before_id";
    private static final String PREFS_NUMBER_TO_RETRIEVE = "number_to_retrieve";

    private final RestAdapter restAdapter = new RestAdapter.Builder()
            .setEndpoint("http://www.reddit.com/")
            .setConverter(new GsonConverter(new GsonBuilder().registerTypeAdapter(Listing.class, new Listing.ListingJsonDeserializer()).create()))
            .build();

    private final RedditTIL mRedditTILEndpoint = restAdapter.create(RedditTIL.class);

    public RetrieveTILService() {
        super("RetrieveTILService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d("TIL", "retrieve service, onHandleIntent");

        mRedditTILEndpoint.latestTILs(getNumberToRequest(), getAfterId())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .flatMap(new Func1<Listing, Observable<TIL>>() {
                    @Override
                    public Observable<TIL> call(Listing listing) {
                        Log.d("TIL", "before: " + listing.before);

                        SharedPreferences prefs = getSharedPreferences();
                        prefs.edit().putString(PREFS_BEFORE_ID, listing.before).apply();

                        return Observable.from(listing.getTodayILearneds());
                    }
                })
                .subscribe(new Action1<TIL>() {
                    @Override
                    public void call(TIL til) {
                        Log.d("TIL", "til: " + til.getTitle());
                        sendNotification(til.getTitle());
                    }
                });
    }

    private void sendNotification(String message) {
        NotificationCompat.WearableExtender wearableExtender = new NotificationCompat.WearableExtender().setHintHideIcon(true);
        Notification notification = new NotificationCompat.Builder(this)
                .setContentTitle(message)
                .setContentText(message)
                .setSmallIcon(R.drawable.ic_launcher)
                .extend(wearableExtender)
                .build();

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(1, notification);
    }

    private SharedPreferences getSharedPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    }

    private int getNumberToRequest() {
        return Integer.parseInt(getSharedPreferences().getString(PREFS_NUMBER_TO_RETRIEVE, "25"));
    }

    private String getAfterId() {
        return getSharedPreferences().getString(PREFS_BEFORE_ID, "");
    }
}

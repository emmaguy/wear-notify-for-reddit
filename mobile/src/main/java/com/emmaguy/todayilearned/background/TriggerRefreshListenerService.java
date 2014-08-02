package com.emmaguy.todayilearned.background;

import com.commonsware.cwac.wakeful.WakefulIntentService;
import com.emmaguy.todayilearned.sharedlib.Constants;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

public class TriggerRefreshListenerService extends WearableListenerService {
    private GoogleApiClient mGoogleApiClient;

    @Override
    public void onCreate() {
        super.onCreate();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .build();

        mGoogleApiClient.connect();
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        if (messageEvent.getPath().equals(Constants.PATH_REFRESH)) {
            WakefulIntentService.sendWakefulWork(this, RetrieveService.class);
        }
    }
}

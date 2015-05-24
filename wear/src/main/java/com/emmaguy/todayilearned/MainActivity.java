package com.emmaguy.todayilearned;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.emmaguy.todayilearned.sharedlib.Constants;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Wearable;

import java.util.concurrent.TimeUnit;

public class MainActivity extends Activity implements GoogleApiClient.ConnectionCallbacks {
    public static final int REQUEST_NEW_POSTS_TIMEOUT = 30;

    private GoogleApiClient mGoogleApiClient;

    private final BroadcastReceiver mForceFinishMainActivity = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            finish();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        ViewFlipper flipper = (ViewFlipper) findViewById(R.id.main_flipper_benefits);
        flipper.startFlipping();

        registerReceiver(mForceFinishMainActivity, new IntentFilter(getString(R.string.force_finish_main_activity)));
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addApi(Wearable.API)
                .build();

        mGoogleApiClient.connect();
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(mForceFinishMainActivity);

        super.onDestroy();
    }

    @Override
    public void onConnected(Bundle bundle) {
        Wearable.MessageApi.sendMessage(mGoogleApiClient, "", Constants.PATH_REFRESH, null).setResultCallback(new ResultCallback<MessageApi.SendMessageResult>() {
            @Override
            public void onResult(MessageApi.SendMessageResult result) {
                Logger.Log("Requested a refresh, result: " + result.getStatus());
                finishActivity();
            }
        });
    }

    private void finishActivity() {
        // the activity will be finished by the broadcast receiver in most cases, but as a backup, end it after 30 secs
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                finish();
            }
        }, TimeUnit.SECONDS.toMillis(REQUEST_NEW_POSTS_TIMEOUT));
    }

    @Override
    public void onConnectionSuspended(int i) {

    }
}

package com.emmaguy.todayilearned;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import com.emmaguy.todayilearned.sharedlib.Constants;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Wearable;

import java.util.concurrent.TimeUnit;

public class MainActivity extends Activity implements GoogleApiClient.ConnectionCallbacks {
    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addApi(Wearable.API)
                .build();

        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(Bundle bundle) {
        Wearable.MessageApi.sendMessage(mGoogleApiClient, "", Constants.PATH_REFRESH, null).setResultCallback(new ResultCallback<MessageApi.SendMessageResult>() {
            @Override
            public void onResult(MessageApi.SendMessageResult result) {
                if (!result.getStatus().isSuccess()) {
                    Toast.makeText(MainActivity.this, "Failed to retrieve posts " + result.getStatus(), Toast.LENGTH_LONG).show();
                }
                finishActivity();
            }
        });
    }

    private void finishActivity() {
        // ensure the user gets a chance to see that we've done a refresh
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                finish();
            }
        }, TimeUnit.SECONDS.toMillis(3));
    }

    @Override
    public void onConnectionSuspended(int i) {

    }
}

package com.emmaguy.todayilearned;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.wearable.activity.ConfirmationActivity;
import android.util.Log;

import com.emmaguy.todayilearned.sharedlib.Constants;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Wearable;

import java.util.concurrent.TimeUnit;

public class OpenOnPhoneReceiver extends BroadcastReceiver {
    private GoogleApiClient mGoogleApiClient;

    public OpenOnPhoneReceiver() {
    }

    @Override
    public void onReceive(final Context context, Intent intent) {
        mGoogleApiClient = new GoogleApiClient.Builder(context)
                .addApi(Wearable.API)
                .build();

        new ConnectTask(context).execute();
    }

    private void showConfirmation(Context context) {
        Intent confirmationActivity = new Intent(context, ConfirmationActivity.class)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION)
                .putExtra(ConfirmationActivity.EXTRA_ANIMATION_TYPE, ConfirmationActivity.OPEN_ON_PHONE_ANIMATION)
                .putExtra(ConfirmationActivity.EXTRA_MESSAGE, context.getString(R.string.open_on_phone));
        context.startActivity(confirmationActivity);
    }

    private class ConnectTask extends AsyncTask<Void, Void, Void> {
        private final Context mContext;

        public ConnectTask(Context context) {
            mContext = context.getApplicationContext();
        }

        @Override
        protected Void doInBackground(Void... params) {
            ConnectionResult connectionResult = mGoogleApiClient.blockingConnect(30, TimeUnit.SECONDS);
            if (!connectionResult.isSuccess()) {
                Log.e("RedditWear", "Service failed to connect");
                return null;
            }

            Wearable.MessageApi.sendMessage(mGoogleApiClient, "", Constants.PATH_OPEN_ON_PHONE, null).setResultCallback(new ResultCallback<MessageApi.SendMessageResult>() {
                @Override
                public void onResult(MessageApi.SendMessageResult sendMessageResult) {
                    if (sendMessageResult.getStatus().isSuccess()) {
                        showConfirmation(mContext);
                    }
                }
            });

            return null;
        }
    }
}

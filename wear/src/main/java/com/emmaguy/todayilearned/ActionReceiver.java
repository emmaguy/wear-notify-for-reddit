package com.emmaguy.todayilearned;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.wearable.activity.ConfirmationActivity;

import com.emmaguy.todayilearned.sharedlib.Constants;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.Wearable;

import java.util.concurrent.TimeUnit;

public class ActionReceiver extends BroadcastReceiver {
    private GoogleApiClient mGoogleApiClient;

    public ActionReceiver() {
    }

    @Override
    public void onReceive(final Context context, Intent intent) {
        mGoogleApiClient = new GoogleApiClient.Builder(context)
                .addApi(Wearable.API)
                .build();

        new ConnectTask(context, intent.getExtras()).execute();
    }

    private void showConfirmation(Context context, String message, int animation) {
        Intent confirmationActivity = new Intent(context, ConfirmationActivity.class)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION)
                .putExtra(ConfirmationActivity.EXTRA_ANIMATION_TYPE, animation)
                .putExtra(ConfirmationActivity.EXTRA_MESSAGE, message);
        context.startActivity(confirmationActivity);
    }

    private class ConnectTask extends AsyncTask<Void, Void, Void> {
        private final Context mContext;
        private final Bundle mBundle;

        public ConnectTask(Context context, Bundle bundle) {
            mContext = context;
            mBundle = bundle;
        }

        @Override
        protected Void doInBackground(Void... params) {
            ConnectionResult connectionResult = mGoogleApiClient.blockingConnect(30, TimeUnit.SECONDS);
            if (!connectionResult.isSuccess()) {
                Logger.Log("Action receiver, service failed to connect: " + connectionResult);
                return null;
            }

            final String path = getStringAndRemoveKey(Constants.KEY_PATH);
            final String message = getStringAndRemoveKey(Constants.KEY_CONFIRMATION_MESSAGE);
            final int animation = getIntAndRemoveKey(Constants.KEY_CONFIRMATION_ANIMATION);
            final int notificationId = getIntAndRemoveKey(Constants.KEY_NOTIFICATION_ID);
            final boolean dismissAfterAction = getBooleanAndRemoveKey(Constants.KEY_DISMISS_AFTER_ACTION);

            Logger.Log("Path: " + path);

            PutDataMapRequest putDataMapRequest = PutDataMapRequest.create(path);

            for (String key : mBundle.keySet()) {
                Object value = mBundle.get(key);
                if (value instanceof Integer) {
                    Logger.Log("Putting int: " + key + " value: " + value);
                    Integer i = (Integer) value;
                    putDataMapRequest.getDataMap().putInt(key, i);
                } else { // assume String
                    Logger.Log("Putting String: " + key + " value: " + value);
                    putDataMapRequest.getDataMap().putString(key, value.toString());
                }
            }

            putDataMapRequest.getDataMap().putLong("timestamp", System.currentTimeMillis());

            Wearable.DataApi.putDataItem(mGoogleApiClient, putDataMapRequest.asPutDataRequest())
                    .setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
                        @Override
                        public void onResult(DataApi.DataItemResult dataItemResult) {
                            Logger.Log("Action receiver '" + message + "' putDataItem status: " + dataItemResult.getStatus().toString());
                            if (dataItemResult.getStatus().isSuccess()) {
                                showConfirmation(mContext, message, animation);

                                Logger.Log("DismissAfterAction: " + dismissAfterAction + " notificationId " + notificationId);
                                if (dismissAfterAction) {
                                    NotificationManager manager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
                                    manager.cancel(notificationId);
                                }
                            }
                        }
                    });

            return null;
        }

        private int getIntAndRemoveKey(String key) {
            int i = mBundle.getInt(key);
            mBundle.remove(key);
            return i;
        }

        private String getStringAndRemoveKey(String key) {
            String s = mBundle.getString(key);
            mBundle.remove(key);
            return s;
        }

        private boolean getBooleanAndRemoveKey(String key) {
            boolean b = mBundle.getBoolean(key);
            mBundle.remove(key);
            return b;
        }
    }
}

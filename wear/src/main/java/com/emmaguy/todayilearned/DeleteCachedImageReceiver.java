package com.emmaguy.todayilearned;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.emmaguy.todayilearned.sharedlib.Constants;

import java.io.File;

public class DeleteCachedImageReceiver extends BroadcastReceiver {
    public DeleteCachedImageReceiver() {
    }

    @Override public void onReceive(Context context, Intent intent) {
        String cachedImageName = intent.getExtras().getString(Constants.KEY_HIGHRES_IMAGE_NAME);

        File localCache = new File(context.getCacheDir(), cachedImageName);
        boolean success = localCache.delete();

        Logger.log("Deleting " + success);
    }
}

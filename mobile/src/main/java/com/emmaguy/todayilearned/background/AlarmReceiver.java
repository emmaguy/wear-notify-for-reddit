package com.emmaguy.todayilearned.background;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AlarmReceiver extends BroadcastReceiver {
    public AlarmReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        context.startService(new Intent(context, RetrieveTILService.class));
    }
}

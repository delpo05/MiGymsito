package com.example.migymsito;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.migymsito.utils.NotificationHelper;

public class TimerReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (!MainActivity.isAppInForeground) {
            NotificationHelper.showTimerFinishedNotification(context);
        }
    }
}

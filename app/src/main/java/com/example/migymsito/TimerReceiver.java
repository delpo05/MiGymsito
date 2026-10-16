package com.example.migymsito;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.migymsito.utils.NotificationHelper;

public class TimerReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        boolean isCardio = intent != null && intent.getBooleanExtra("IS_CARDIO", false);
        if (isCardio) {
            NotificationHelper.showCardioTimerFinishedNotification(context);
        } else {
            NotificationHelper.showTimerFinishedNotification(context);
        }
    }
}

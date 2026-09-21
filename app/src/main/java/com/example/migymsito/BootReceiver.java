package com.example.migymsito;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.example.migymsito.utils.NotificationHelper;

public class BootReceiver extends BroadcastReceiver {

    private static final String PREFS_TIMER = "prefs_timer";
    private static final String KEY_END_TIME = "key_end_time";
    private static final String KEY_TIMER_RUNNING = "key_timer_running";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            NotificationHelper.createNotificationChannel(context);

            SharedPreferences prefs = context.getSharedPreferences(PREFS_TIMER, Context.MODE_PRIVATE);
            boolean timerRunning = prefs.getBoolean(KEY_TIMER_RUNNING, false);
            long endTime = prefs.getLong(KEY_END_TIME, 0);

            if (timerRunning) {
                long now = System.currentTimeMillis();
                if (now >= endTime) {
                    // Timer expired while device was powered off
                    NotificationHelper.showTimerFinishedNotification(context);
                    prefs.edit().putBoolean(KEY_TIMER_RUNNING, false).apply();
                } else {
                    // Reschedule alarm
                    AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                    if (alarmManager != null) {
                        Intent timerIntent = new Intent(context, TimerReceiver.class);
                        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                                context,
                                1001,
                                timerIntent,
                                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
                        );
                        Intent showIntent = new Intent(context, MainActivity.class);
                        PendingIntent showPendingIntent = PendingIntent.getActivity(
                                context,
                                0,
                                showIntent,
                                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
                        );
                        AlarmManager.AlarmClockInfo clockInfo = new AlarmManager.AlarmClockInfo(endTime, showPendingIntent);
                        alarmManager.setAlarmClock(clockInfo, pendingIntent);
                    }
                }
            }
        }
    }
}

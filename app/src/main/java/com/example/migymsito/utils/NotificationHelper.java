package com.example.migymsito.utils;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.example.migymsito.MainActivity;
import com.example.migymsito.R;

public class NotificationHelper {

    public static final String CHANNEL_ID = "TimerChannelV2";
    private static final String CHANNEL_NAME = "Descanso MiGymsito";
    public static final String CHANNEL_ID_ENTRENAMIENTO = "EntrenamientoChannel";
    private static final String CHANNEL_NAME_ENTRENAMIENTO = "Entrenamiento MiGymsito";
    public static final String CHANNEL_ID_CARDIO = "CardioTimerChannelV1";
    private static final String CHANNEL_NAME_CARDIO = "Cardio MiGymsito";

    private static final int NOTIFICATION_ID = 101;
    private static final int NOTIFICATION_ID_ENTRENAMIENTO = 102;
    private static final int NOTIFICATION_ID_CARDIO = 103;

    private static final long NOTIFICATION_TIMEOUT_MS = 5 * 60 * 1000L; // 5 minutos

    public static void cancelAllNotifications(Context context) {
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.cancelAll();
        }
    }

    private static int getCardioSoundResId(Context context) {
        int audioResId = context.getResources().getIdentifier("silbato", "raw", context.getPackageName());
        if (audioResId == 0) {
            audioResId = context.getResources().getIdentifier("silbato_cardio", "raw", context.getPackageName());
        }
        if (audioResId == 0) {
            audioResId = R.raw.sonido1;
        }
        return audioResId;
    }

    public static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .build();

            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Canal para notificaciones de fin de descanso");
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{0, 500, 200, 500});
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);

            Uri soundUri = Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.sonido1);
            channel.setSound(soundUri, audioAttributes);

            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);

                NotificationChannel trainingChannel = new NotificationChannel(
                        CHANNEL_ID_ENTRENAMIENTO,
                        CHANNEL_NAME_ENTRENAMIENTO,
                        NotificationManager.IMPORTANCE_HIGH
                );
                trainingChannel.setDescription("Notificaciones sobre el estado de tu entrenamiento");
                trainingChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
                manager.createNotificationChannel(trainingChannel);

                NotificationChannel cardioChannel = new NotificationChannel(
                        CHANNEL_ID_CARDIO,
                        CHANNEL_NAME_CARDIO,
                        NotificationManager.IMPORTANCE_HIGH
                );
                cardioChannel.setDescription("Canal para notificaciones de fin de ejercicio cardio");
                cardioChannel.enableVibration(true);
                cardioChannel.setVibrationPattern(new long[]{0, 500, 200, 500});
                cardioChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
                Uri cardioSoundUri = Uri.parse("android.resource://" + context.getPackageName() + "/" + getCardioSoundResId(context));
                cardioChannel.setSound(cardioSoundUri, audioAttributes);
                manager.createNotificationChannel(cardioChannel);
            }
        }
    }

    public static void showTimerFinishedNotification(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                101,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Uri soundUri = Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.sonido1);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.baseline_fitness_center)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.isotipo_white))
                .setContentTitle("Descanso terminado")
                .setContentText("¡Es hora de la siguiente serie!")
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setSound(soundUri)
                .setAutoCancel(true)
                .setTimeoutAfter(NOTIFICATION_TIMEOUT_MS)
                .setContentIntent(pendingIntent);

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.notify(NOTIFICATION_ID, builder.build());
        }
    }

    public static void showCardioTimerFinishedNotification(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                103,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Uri soundUri = Uri.parse("android.resource://" + context.getPackageName() + "/" + getCardioSoundResId(context));

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID_CARDIO)
                .setSmallIcon(R.drawable.baseline_fitness_center)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.isotipo_white))
                .setContentTitle("Ejercicio Cardio Finalizado")
                .setContentText("¡Has completado tu tiempo de cardio!")
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setSound(soundUri)
                .setAutoCancel(true)
                .setTimeoutAfter(NOTIFICATION_TIMEOUT_MS)
                .setContentIntent(pendingIntent);

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.notify(NOTIFICATION_ID_CARDIO, builder.build());
        }
    }

    public static void showInactivityNotification(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                102,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID_ENTRENAMIENTO)
                .setSmallIcon(R.drawable.baseline_fitness_center)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.isotipo_white))
                .setContentTitle("¿Sigues entrenando?")
                .setContentText("Llevas 1 hora sin registrar ejercicios. No olvides finalizar tu sesión.")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_EVENT)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setAutoCancel(true)
                .setTimeoutAfter(NOTIFICATION_TIMEOUT_MS)
                .setContentIntent(pendingIntent);

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.notify(NOTIFICATION_ID_ENTRENAMIENTO, builder.build());
        }
    }
}

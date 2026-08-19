package com.example.migymsito.utils;

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
    private static final int NOTIFICATION_ID = 101;
    private static final int NOTIFICATION_ID_ENTRENAMIENTO = 102;

    public static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Canal para notificaciones de fin de descanso");
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{0, 500, 200, 500});

            Uri soundUri = Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.sonido1);
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .build();
            channel.setSound(soundUri, audioAttributes);

            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);

                NotificationChannel trainingChannel = new NotificationChannel(
                        CHANNEL_ID_ENTRENAMIENTO,
                        CHANNEL_NAME_ENTRENAMIENTO,
                        NotificationManager.IMPORTANCE_DEFAULT
                );
                trainingChannel.setDescription("Notificaciones sobre el estado de tu entrenamiento");
                manager.createNotificationChannel(trainingChannel);
            }
        }
    }

    public static void showTimerFinishedNotification(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        Uri soundUri = Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.sonido1);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.baseline_fitness_center)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.isotipo_white))
                .setContentTitle("¡Descanso Terminado!")
                .setContentText("Es hora de tu siguiente serie.")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setSound(soundUri)
                .setVibrate(new long[]{0, 500, 200, 500})
                .setContentIntent(pendingIntent);

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.notify(NOTIFICATION_ID, builder.build());
        }
    }

    public static void showInactivityNotification(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 1, intent, PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID_ENTRENAMIENTO)
                .setSmallIcon(R.drawable.baseline_fitness_center)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.isotipo_white))
                .setContentTitle("¿Sigues entrenando?")
                .setContentText("Llevas 1 hora sin registrar ejercicios. No olvides finalizar tu sesión.")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.notify(NOTIFICATION_ID_ENTRENAMIENTO, builder.build());
        }
    }
}

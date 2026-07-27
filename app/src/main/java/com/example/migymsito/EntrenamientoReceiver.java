package com.example.migymsito;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.migymsito.data.Entrenamiento;
import com.example.migymsito.dataDao.EntrenamientoDao;
import com.example.migymsito.dataDataBase.AppDatabase;
import com.example.migymsito.utils.NotificationHelper;

import java.util.List;
import java.util.concurrent.Executors;

public class EntrenamientoReceiver extends BroadcastReceiver {
    public static final String ACTION_INACTIVITY_CHECK = "com.example.migymsito.ACTION_INACTIVITY_CHECK";
    public static final String ACTION_AUTO_FINALIZE = "com.example.migymsito.ACTION_AUTO_FINALIZE";
    public static final String EXTRA_ID_USUARIO = "idUsuario";
    public static final String EXTRA_ID_SECCION = "idSeccion";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        int idUsuario = intent.getIntExtra(EXTRA_ID_USUARIO, -1);
        int idSeccion = intent.getIntExtra(EXTRA_ID_SECCION, -1);

        if (idUsuario == -1 || idSeccion == -1) return;

        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.getDatabase(context);
            EntrenamientoDao dao = db.entrenamientoDao();
            Entrenamiento activo = dao.getEntrenamientoActivoPorSeccion(idUsuario, idSeccion);

            if (activo != null) {
                if (ACTION_INACTIVITY_CHECK.equals(action)) {
                    // Solo notificar si la app NO está en primer plano
                    if (!MainActivity.isAppInForeground) {
                        NotificationHelper.showInactivityNotification(context);
                    }
                } else if (ACTION_AUTO_FINALIZE.equals(action)) {
                    // Finalizar automáticamente
                    activo.FechaFin = System.currentTimeMillis();
                    dao.update(activo);
                    Log.d("EntrenamientoReceiver", "Entrenamiento finalizado automáticamente por inactividad (5h)");
                }
            }
        });
    }
}

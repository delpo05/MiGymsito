package com.example.migymsito.dataRepository;

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;

import com.example.migymsito.EntrenamientoReceiver;
import com.example.migymsito.data.Entrenamiento;
import com.example.migymsito.data.Registro;
import com.example.migymsito.data.RegistroCardio;
import com.example.migymsito.dataDao.EntrenamientoDao;
import com.example.migymsito.dataDao.RegistroCardioDao;
import com.example.migymsito.dataDao.RegistroDao;
import com.example.migymsito.dataDataBase.AppDatabase;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EntrenamientoRepository {

    private final EntrenamientoDao entrenamientoDao;
    private final RegistroDao registroDao;
    private final RegistroCardioDao registroCardioDao;
    private final ExecutorService executorService;
    private final Context context;

    public EntrenamientoRepository(Application application) {
        this.context = application.getApplicationContext();
        AppDatabase db = AppDatabase.getDatabase(application);
        entrenamientoDao = db.entrenamientoDao();
        registroDao = db.registroDao();
        registroCardioDao = db.registroCardioDao();
        executorService = Executors.newFixedThreadPool(4);
    }

    public void obtenerEntrenamientosPorUsuario(int idUsuario, RepositoryCallback<List<Entrenamiento>> callback) {
        executorService.execute(() -> {
            List<Entrenamiento> lista = entrenamientoDao.getEntrenamientosByUsuario(idUsuario);
            notificar(callback, lista);
        });
    }

    public void insertarEntrenamiento(Entrenamiento entrenamiento, RepositoryCallback<Long> callback) {
        executorService.execute(() -> {
            long id = entrenamientoDao.insert(entrenamiento);
            notificar(callback, id);
        });
    }

    public void obtenerEntrenamientoActivo(int idUsuario, RepositoryCallback<Entrenamiento> callback) {
        executorService.execute(() -> {
            Entrenamiento e = entrenamientoDao.getEntrenamientoActivo(idUsuario);
            notificar(callback, e);
        });
    }

    public void obtenerEntrenamientoActivoPorSeccion(int idUsuario, int idSeccion, RepositoryCallback<Entrenamiento> callback) {
        obtenerEntrenamientoActivo(idUsuario, callback);
    }

    public void finalizarEntrenamiento(int idEntrenamiento) {
        executorService.execute(() -> {
            Entrenamiento e = entrenamientoDao.getEntrenamientoById(idEntrenamiento);
            if (e != null) {
                e.FechaFin = System.currentTimeMillis();
                entrenamientoDao.update(e);
                cancelarAlarmasInactividad(e.IdUsuario);
            }
        });
    }

    public void finalizarEntrenamientoActivo(int idUsuario, RepositoryCallback<Boolean> callback) {
        executorService.execute(() -> {
            Entrenamiento e = entrenamientoDao.getEntrenamientoActivo(idUsuario);
            if (e != null) {
                e.FechaFin = System.currentTimeMillis();
                entrenamientoDao.update(e);
                cancelarAlarmasInactividad(e.IdUsuario);
                notificar(callback, true);
            } else {
                notificar(callback, false);
            }
        });
    }

    public void finalizarEntrenamientoActivoPorSeccion(int idUsuario, int idSeccion, RepositoryCallback<Boolean> callback) {
        finalizarEntrenamientoActivo(idUsuario, callback);
    }

    public void finalizarEntrenamientosActivosDeRutina(int idUsuario, int idRutina, RepositoryCallback<Boolean> callback) {
        executorService.execute(() -> {
            List<Entrenamiento> activos = entrenamientoDao.getEntrenamientosActivosPorRutina(idUsuario, idRutina);
            if (activos != null && !activos.isEmpty()) {
                long now = System.currentTimeMillis();
                for (Entrenamiento e : activos) {
                    e.FechaFin = now;
                    entrenamientoDao.update(e);
                    cancelarAlarmasInactividad(e.IdUsuario);
                }
                notificar(callback, true);
            } else {
                notificar(callback, false);
            }
        });
    }

    /**
     * Finaliza el entrenamiento activo del usuario si tiene registros (fuerza o cardio).
     * Si no tiene registros, lo elimina de la base de datos.
     * El callback devuelve true si se finalizó (con registros), false si se eliminó (sin registros).
     */
    public void finalizarOEliminarSiVacio(int idUsuario, RepositoryCallback<Boolean> callback) {
        executorService.execute(() -> {
            Entrenamiento e = entrenamientoDao.getEntrenamientoActivo(idUsuario);
            if (e != null) {
                List<Registro> registros = registroDao.obtenerRegistrosPorEntrenamiento(e.IdEntrenamiento);
                List<RegistroCardio> registrosCardio = registroCardioDao.obtenerPorEntrenamiento(e.IdEntrenamiento);
                
                boolean tieneRegistros = (registros != null && !registros.isEmpty()) || (registrosCardio != null && !registrosCardio.isEmpty());

                if (tieneRegistros) {
                    e.FechaFin = System.currentTimeMillis();
                    entrenamientoDao.update(e);
                    cancelarAlarmasInactividad(e.IdUsuario);
                    notificar(callback, true);
                } else {
                    entrenamientoDao.delete(e);
                    cancelarAlarmasInactividad(e.IdUsuario);
                    notificar(callback, false);
                }
            } else {
                notificar(callback, null);
            }
        });
    }

    public void finalizarOEliminarSiVacio(int idUsuario, int idSeccion, RepositoryCallback<Boolean> callback) {
        finalizarOEliminarSiVacio(idUsuario, callback);
    }

    public void iniciarNuevoEntrenamiento(int idUsuario, Integer idSeccion, RepositoryCallback<Entrenamiento> callback) {
        executorService.execute(() -> {
            // Verificar si ya existe uno activo para el usuario
            Entrenamiento activo = entrenamientoDao.getEntrenamientoActivo(idUsuario);
            if (activo != null) {
                notificar(callback, activo);
                return;
            }

            Entrenamiento nuevo = new Entrenamiento();
            nuevo.IdUsuario = idUsuario;
            nuevo.IdSeccion = idSeccion;
            nuevo.FechaInicio = System.currentTimeMillis();

            List<Entrenamiento> todos = entrenamientoDao.getEntrenamientosByUsuario(idUsuario);
            nuevo.NumeroEntrenamiento = todos.size() + 1;

            long id = entrenamientoDao.insert(nuevo);
            nuevo.IdEntrenamiento = (int) id;
            programarAlarmasInactividad(nuevo.IdUsuario);
            notificar(callback, nuevo);
        });
    }

    public void iniciarNuevoEntrenamiento(int idUsuario, int idSeccion, RepositoryCallback<Entrenamiento> callback) {
        iniciarNuevoEntrenamiento(idUsuario, Integer.valueOf(idSeccion), callback);
    }

    public void programarAlarmasInactividad(int idUsuario) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        Intent intentCheck = new Intent(context, EntrenamientoReceiver.class);
        intentCheck.setAction(EntrenamientoReceiver.ACTION_INACTIVITY_CHECK);
        intentCheck.putExtra(EntrenamientoReceiver.EXTRA_ID_USUARIO, idUsuario);
        PendingIntent piCheck = PendingIntent.getBroadcast(
                context,
                10000 + idUsuario,
                intentCheck,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Intent intentAuto = new Intent(context, EntrenamientoReceiver.class);
        intentAuto.setAction(EntrenamientoReceiver.ACTION_AUTO_FINALIZE);
        intentAuto.putExtra(EntrenamientoReceiver.EXTRA_ID_USUARIO, idUsuario);
        PendingIntent piAuto = PendingIntent.getBroadcast(
                context,
                20000 + idUsuario,
                intentAuto,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        long now = System.currentTimeMillis();
        long oneHour = 3600 * 1000;
        long fiveHours = 5 * 3600 * 1000;

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, now + oneHour, piCheck);
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, now + fiveHours, piAuto);
                } else {
                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, now + oneHour, piCheck);
                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, now + fiveHours, piAuto);
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, now + oneHour, piCheck);
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, now + fiveHours, piAuto);
            }
        } catch (SecurityException e) {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, now + oneHour, piCheck);
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, now + fiveHours, piAuto);
        }
    }

    public void programarAlarmasInactividad(int idUsuario, int idSeccion) {
        programarAlarmasInactividad(idUsuario);
    }

    public void cancelarAlarmasInactividad(int idUsuario) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        Intent intentCheck = new Intent(context, EntrenamientoReceiver.class);
        intentCheck.setAction(EntrenamientoReceiver.ACTION_INACTIVITY_CHECK);
        PendingIntent piCheck = PendingIntent.getBroadcast(
                context,
                10000 + idUsuario,
                intentCheck,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        alarmManager.cancel(piCheck);

        Intent intentAuto = new Intent(context, EntrenamientoReceiver.class);
        intentAuto.setAction(EntrenamientoReceiver.ACTION_AUTO_FINALIZE);
        PendingIntent piAuto = PendingIntent.getBroadcast(
                context,
                20000 + idUsuario,
                intentAuto,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        alarmManager.cancel(piAuto);
    }

    public void cancelarAlarmasInactividad(int idUsuario, int idSeccion) {
        cancelarAlarmasInactividad(idUsuario);
    }

    public void refrescarAlarmasInactividad(int idUsuario) {
        programarAlarmasInactividad(idUsuario);
    }

    public void refrescarAlarmasInactividad(int idUsuario, int idSeccion) {
        refrescarAlarmasInactividad(idUsuario);
    }

    private <T> void notificar(RepositoryCallback<T> callback, T resultado) {
        if (callback != null) {
            new Handler(Looper.getMainLooper()).post(() -> callback.onResult(resultado));
        }
    }

    public interface RepositoryCallback<T> {
        void onResult(T result);
    }
}

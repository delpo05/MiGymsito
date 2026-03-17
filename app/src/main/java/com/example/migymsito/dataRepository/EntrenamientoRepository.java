package com.example.migymsito.dataRepository;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;

import com.example.migymsito.data.Entrenamiento;
import com.example.migymsito.dataDao.EntrenamientoDao;
import com.example.migymsito.dataDataBase.AppDatabase;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EntrenamientoRepository {

    private final EntrenamientoDao entrenamientoDao;
    private final ExecutorService executorService;

    public EntrenamientoRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        entrenamientoDao = db.entrenamientoDao();
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

    public void finalizarEntrenamiento(int idEntrenamiento) {
        executorService.execute(() -> {
            Entrenamiento e = entrenamientoDao.getEntrenamientoById(idEntrenamiento);
            if (e != null) {
                e.FechaFin = System.currentTimeMillis();
                entrenamientoDao.update(e);
            }
        });
    }

    public void finalizarEntrenamientosActivosDeRutina(int idUsuario, int idRutina, RepositoryCallback<Boolean> callback) {
        executorService.execute(() -> {
            List<Entrenamiento> activos = entrenamientoDao.getEntrenamientosActivosPorRutina(idUsuario, idRutina);
            if (activos != null && !activos.isEmpty()) {
                long now = System.currentTimeMillis();
                for (Entrenamiento e : activos) {
                    e.FechaFin = now;
                    entrenamientoDao.update(e);
                }
                notificar(callback, true);
            } else {
                notificar(callback, false);
            }
        });
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

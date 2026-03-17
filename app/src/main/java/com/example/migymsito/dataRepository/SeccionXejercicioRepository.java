package com.example.migymsito.dataRepository;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;

import com.example.migymsito.data.SeccionXejercicio;
import com.example.migymsito.dataDao.SeccionXejercicioDao;
import com.example.migymsito.dataDataBase.AppDatabase;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SeccionXejercicioRepository {

    private final SeccionXejercicioDao seccionXejercicioDao;
    private final ExecutorService executorService;

    public SeccionXejercicioRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        seccionXejercicioDao = db.seccionXejercicioDao();
        executorService = Executors.newFixedThreadPool(4);
    }

    public void insertar(SeccionXejercicio seccionXejercicio, RepositoryCallback<Long> callback) {
        executorService.execute(() -> {
            long id = seccionXejercicioDao.insert(seccionXejercicio);
            notificar(callback, id);
        });
    }

    public void obtenerRelacion(int idSeccion, int idEjercicio, RepositoryCallback<SeccionXejercicio> callback) {
        executorService.execute(() -> {
            SeccionXejercicio relacion = seccionXejercicioDao.getRelacion(idSeccion, idEjercicio);
            notificar(callback, relacion);
        });
    }

    public void obtenerEjerciciosPorSeccion(int idSeccion, RepositoryCallback<List<SeccionXejercicio>> callback) {
        executorService.execute(() -> {
            List<SeccionXejercicio> lista = seccionXejercicioDao.getEjerciciosBySeccion(idSeccion);
            notificar(callback, lista);
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

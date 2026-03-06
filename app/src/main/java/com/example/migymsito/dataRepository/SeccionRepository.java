package com.example.migymsito.dataRepository;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;

import com.example.migymsito.data.Seccion;
import com.example.migymsito.dataDao.SeccionDao;
import com.example.migymsito.dataDataBase.AppDatabase;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SeccionRepository {

    private final SeccionDao seccionDao;
    private final ExecutorService executorService;

    public SeccionRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        seccionDao = db.seccionDao();
        executorService = Executors.newFixedThreadPool(4);
    }

    public void insertarSeccion(Seccion seccion) {
        executorService.execute(() -> seccionDao.insertarSeccion(seccion));
    }

    public void actualizarSeccion(Seccion seccion) {
        executorService.execute(() -> seccionDao.actualizarSeccion(seccion));
    }

    public void eliminarSeccion(Seccion seccion) {
        executorService.execute(() -> seccionDao.eliminarSeccion(seccion));
    }

    public void obtenerSeccionesDeRutina(int idRutina, RepositoryCallback<List<Seccion>> callback) {
        executorService.execute(() -> {
            List<Seccion> lista = seccionDao.obtenerSeccionesPorRutina(idRutina);
            notificar(callback, lista);
        });
    }

    // Mensajero para volver al hilo principal de la UI
    private <T> void notificar(RepositoryCallback<T> callback, T resultado) {
        new Handler(Looper.getMainLooper()).post(() -> callback.onResult(resultado));
    }

    public interface RepositoryCallback<T> {
        void onResult(T result);
    }
}

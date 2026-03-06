package com.example.migymsito.dataRepository;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;

import com.example.migymsito.data.Historial;
import com.example.migymsito.dataDao.HistorialDao;
import com.example.migymsito.dataDataBase.AppDatabase;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HistorialRepository {

    private final HistorialDao historialDao;
    private final ExecutorService executorService;

    public HistorialRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        historialDao = db.historialDao();
        executorService = Executors.newFixedThreadPool(4);
    }

    public void insertarHistorial(Historial historial) {
        executorService.execute(() -> historialDao.insertarHistorial(historial));
    }

    public void actualizarHistorial(Historial historial) {
        executorService.execute(() -> historialDao.actualizarHistorial(historial));
    }

    public void eliminarHistorial(Historial historial) {
        executorService.execute(() -> historialDao.eliminarHistorial(historial));
    }

    public void obtenerHistorialPorUsuario(int idUsuario, RepositoryCallback<List<Historial>> callback) {
        executorService.execute(() -> {
            List<Historial> lista = historialDao.obtenerHistorialPorUsuario(idUsuario);
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

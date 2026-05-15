package com.example.migymsito.dataRepository;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;

import com.example.migymsito.data.Rutina;
import com.example.migymsito.dataDao.RutinaDao;
import com.example.migymsito.dataDataBase.AppDatabase;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RutinaRepository {

    private final RutinaDao rutinaDao;
    private final ExecutorService executorService;

    public RutinaRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        // Usamos el DAO directamente del AppDatabase
        this.rutinaDao = db.rutinaDao();
        this.executorService = Executors.newFixedThreadPool(4);
    }

    public void insertarRutina(Rutina rutina) {
        executorService.execute(() -> rutinaDao.insertarRutina(rutina));
    }

    public void actualizarRutina(Rutina rutina) {
        executorService.execute(() -> rutinaDao.actualizarRutina(rutina));
    }

    public void eliminarRutina(Rutina rutina) {
        executorService.execute(() -> rutinaDao.eliminarRutina(rutina));
    }

    public void obtenerRutinasDeUsuario(int idUsuario, RepositoryCallback<List<Rutina>> callback) {
        executorService.execute(() -> {
            List<Rutina> lista = rutinaDao.obtenerRutinasPorUsuario(idUsuario);
            notificar(callback, lista);
        });
    }

    private <T> void notificar(RepositoryCallback<T> callback, T resultado) {
        new Handler(Looper.getMainLooper()).post(() -> callback.onResult(resultado));
    }

    public interface RepositoryCallback<T> {
        void onResult(T result);
    }
}

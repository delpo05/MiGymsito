package com.example.migymsito.dataRepository;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;

import com.example.migymsito.data.Registro;
import com.example.migymsito.dataDao.RegistroDao;
import com.example.migymsito.dataDataBase.AppDatabase;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RegistroRepository {

    private final RegistroDao registroDao;
    private final ExecutorService executorService;

    public RegistroRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        registroDao = db.registroDao();
        executorService = Executors.newFixedThreadPool(4);
    }

    public void insertarRegistro(Registro registro) {
        executorService.execute(() -> registroDao.insertarRegistro(registro));
    }

    public void actualizarRegistro(Registro registro) {
        executorService.execute(() -> registroDao.actualizarRegistro(registro));
    }

    public void eliminarRegistro(Registro registro) {
        executorService.execute(() -> registroDao.eliminarRegistro(registro));
    }
    
    public void obtenerHistorialPorEjercicio(int idUsuario, int idEjercicio, RepositoryCallback<List<Registro>> callback) {
        executorService.execute(() -> {
            List<Registro> lista = registroDao.obtenerHistorialPorEjercicio(idUsuario, idEjercicio);
            notificar(callback, lista);
        });
    }

    public void obtenerTodosLosRegistrosDelUsuario(int idUsuario, RepositoryCallback<List<Registro>> callback) {
        executorService.execute(() -> {
            List<Registro> lista = registroDao.obtenerTodosLosRegistrosDelUsuario(idUsuario);
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

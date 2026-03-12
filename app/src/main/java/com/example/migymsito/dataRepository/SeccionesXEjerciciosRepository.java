package com.example.migymsito.dataRepository;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;

import com.example.migymsito.data.Ejercicio;
import com.example.migymsito.data.SeccionesXEjercicios;
import com.example.migymsito.dataDao.SeccionesXEjerciciosDao;
import com.example.migymsito.dataDataBase.AppDatabase;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// Repositorio para gestionar las operaciones de la tabla intermedia SeccionesXEjercicios
public class SeccionesXEjerciciosRepository {

    private final SeccionesXEjerciciosDao dao;
    private final ExecutorService executorService;

    public SeccionesXEjerciciosRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        dao = db.seccionesXEjerciciosDao();
        executorService = Executors.newFixedThreadPool(4);
    }

    public void insertar(SeccionesXEjercicios item) {
        executorService.execute(() -> dao.insertar(item));
    }

    public void eliminar(SeccionesXEjercicios item) {
        executorService.execute(() -> dao.eliminar(item));
    }

    // El callback ahora espera una List<Ejercicio>, ya que el DAO devuelve los ejercicios vinculados a la sección.
    public void obtenerEjerciciosPorSeccion(int idSeccion, RepositoryCallback<List<Ejercicio>> callback) {
        executorService.execute(() -> {
            // El DAO devuelve List<Ejercicio> a través del INNER JOIN
            List<Ejercicio> lista = dao.obtenerEjerciciosPorSeccion(idSeccion);
            notificar(callback, lista);
        });
    }

    // Método genérico para notificar resultados al hilo principal de la UI
    private <T> void notificar(RepositoryCallback<T> callback, T resultado) {
        new Handler(Looper.getMainLooper()).post(() -> callback.onResult(resultado));
    }

    public interface RepositoryCallback<T> {
        void onResult(T result);
    }
}

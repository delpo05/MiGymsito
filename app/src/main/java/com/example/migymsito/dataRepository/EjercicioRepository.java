package com.example.migymsito.dataRepository;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;

import com.example.migymsito.data.Ejercicio;
import com.example.migymsito.data.SeccionesXEjercicios;
import com.example.migymsito.dataDao.EjercicioDao;
import com.example.migymsito.dataDao.SeccionesXEjerciciosDao;
import com.example.migymsito.dataDataBase.AppDatabase;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EjercicioRepository {

    private final EjercicioDao ejercicioDao;
    private final SeccionesXEjerciciosDao seccionXEjercicioDao;
    private final ExecutorService executorService;

    public EjercicioRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        ejercicioDao = db.ejercicioDao();
        seccionXEjercicioDao = db.seccionesXEjerciciosDao();
        executorService = Executors.newFixedThreadPool(4);
    }

    // Inserta el ejercicio en su tabla y luego crea la vinculación en la tabla intermedia
    public void insertarEjercicioEnSeccion(Ejercicio ejercicio, int idSeccion) {
        executorService.execute(() -> {
            // 1. Insertamos el ejercicio en la tabla Ejercicio
            long idGenerado = ejercicioDao.insertarEjercicio(ejercicio);
            
            // 2. Creamos la relación en SeccionesXEjercicios (la tabla intermedia)
            SeccionesXEjercicios relacion = new SeccionesXEjercicios();
            relacion.idSeccion = idSeccion;
            relacion.idEjercicio = (int) idGenerado;
            seccionXEjercicioDao.insertar(relacion);
        });
    }

    public void actualizarEjercicio(Ejercicio ejercicio) {
        executorService.execute(() -> ejercicioDao.actualizarEjercicio(ejercicio));
    }

    public void eliminarEjercicio(Ejercicio ejercicio) {
        executorService.execute(() -> ejercicioDao.eliminarEjercicio(ejercicio));
    }

    // Ahora este método utiliza correctamente el DAO de la tabla intermedia para obtener los ejercicios
    public void obtenerEjerciciosPorSeccion(int idSeccion, RepositoryCallback<List<Ejercicio>> callback) {
        executorService.execute(() -> {
            List<Ejercicio> lista = seccionXEjercicioDao.obtenerEjerciciosPorSeccion(idSeccion);
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

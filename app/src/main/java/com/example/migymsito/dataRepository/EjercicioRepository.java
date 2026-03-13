package com.example.migymsito.dataRepository;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;

import com.example.migymsito.data.Ejercicio;
import com.example.migymsito.data.SeccionXejercicio;
import com.example.migymsito.dataDao.EjercicioDao;
import com.example.migymsito.dataDao.SeccionXejercicioDao;
import com.example.migymsito.dataDataBase.AppDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EjercicioRepository {

    private final EjercicioDao ejercicioDao;
    private final SeccionXejercicioDao seccionXejercicioDao;
    private final ExecutorService executorService;

    public EjercicioRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        ejercicioDao = db.ejercicioDao();
        seccionXejercicioDao = db.seccionXejercicioDao();
        executorService = Executors.newFixedThreadPool(4);
    }

    public void insertarEjercicio(Ejercicio ejercicio) {
        executorService.execute(() -> ejercicioDao.insertarEjercicio(ejercicio));
    }

    public void insertarEjercicioConSeccion(Ejercicio ejercicio, int idSeccion) {
        executorService.execute(() -> {
            long idEjercicio = ejercicioDao.insertarEjercicio(ejercicio);
            
            SeccionXejercicio relacion = new SeccionXejercicio();
            relacion.IdSeccion = idSeccion;
            relacion.IdEjercicio = (int) idEjercicio;
            
            seccionXejercicioDao.insert(relacion);
        });
    }

    public void actualizarEjercicio(Ejercicio ejercicio) {
        executorService.execute(() -> ejercicioDao.actualizarEjercicio(ejercicio));
    }

    public void eliminarEjercicio(Ejercicio ejercicio) {
        executorService.execute(() -> ejercicioDao.eliminarEjercicio(ejercicio));
    }

    public void obtenerEjerciciosPorSeccion(int idSeccion, RepositoryCallback<List<Ejercicio>> callback) {
        executorService.execute(() -> {
            List<SeccionXejercicio> relaciones = seccionXejercicioDao.getEjerciciosBySeccion(idSeccion);
            List<Ejercicio> ejercicios = new ArrayList<>();
            for (SeccionXejercicio rel : relaciones) {
                Ejercicio ej = ejercicioDao.obtenerEjercicioPorId(rel.IdEjercicio);
                if (ej != null) {
                    ejercicios.add(ej);
                }
            }
            notificar(callback, ejercicios);
        });
    }

    private <T> void notificar(RepositoryCallback<T> callback, T resultado) {
        new Handler(Looper.getMainLooper()).post(() -> callback.onResult(resultado));
    }

    public interface RepositoryCallback<T> {
        void onResult(T result);
    }
}

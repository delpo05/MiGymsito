package com.example.migymsito.dataRepository;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

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
    private final Handler mainThreadHandler;
    private final String TAG = "EjercicioRepository";

    public EjercicioRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        ejercicioDao = db.ejercicioDao();
        seccionXejercicioDao = db.seccionXejercicioDao();
        executorService = Executors.newFixedThreadPool(4);
        mainThreadHandler = new Handler(Looper.getMainLooper());
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

    // NUEVO: Modifica el ejercicio creando una copia para no afectar a otras secciones
    public void actualizarEjercicioIndependiente(Ejercicio ejercicioEditado, int idSeccion) {
        executorService.execute(() -> {
            // 1. Crear una copia del ejercicio con los nuevos datos (Nombre/Imagen)
            Ejercicio nuevoEj = new Ejercicio();
            nuevoEj.NombreEjercicio = ejercicioEditado.NombreEjercicio;
            nuevoEj.ImagenEjercicio = ejercicioEditado.ImagenEjercicio;
            nuevoEj.TipoEjercicio = ejercicioEditado.TipoEjercicio;
            nuevoEj.PesoCorporalEjercicio = ejercicioEditado.PesoCorporalEjercicio;

            // 2. Insertarlo como un ejercicio nuevo
            long nuevoIdEjercicio = ejercicioDao.insertarEjercicio(nuevoEj);

            // 3. Buscar la relación actual de esta sección
            SeccionXejercicio relacion = seccionXejercicioDao.getRelacion(idSeccion, ejercicioEditado.IdEjercicio);

            if (relacion != null) {
                // 4. Actualizar la relación para que apunte al nuevo ID
                relacion.IdEjercicio = (int) nuevoIdEjercicio;
                seccionXejercicioDao.update(relacion);
            }
        });
    }

    public void eliminarEjercicio(Ejercicio ejercicio) {
        executorService.execute(() -> ejercicioDao.eliminarEjercicio(ejercicio));
    }

    // NUEVO: Elimina solo el vínculo entre el ejercicio y la sección actual
    public void eliminarEjercicioDeSeccion(int idEjercicio, int idSeccion) {
        executorService.execute(() -> {
            SeccionXejercicio relacion = seccionXejercicioDao.getRelacion(idSeccion, idEjercicio);
            if (relacion != null) {
                seccionXejercicioDao.delete(relacion);
            }
        });
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
        if (callback != null) {
            new Handler(Looper.getMainLooper()).post(() -> callback.onResult(resultado));
        }
    }

    public interface RepositoryCallback<T> {
        void onResult(T result);
    }
}
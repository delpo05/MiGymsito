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

    // Inserta un ejercicio en la base de datos
    public void insertarEjercicio(Ejercicio ejercicio) {
        executorService.execute(() -> ejercicioDao.insertarEjercicio(ejercicio));
    }

    // Inserta un ejercicio y crea su relación con una sección específica
    public void insertarEjercicioConSeccion(Ejercicio ejercicio, int idSeccion) {
        executorService.execute(() -> {
            long idEjercicio = ejercicioDao.insertarEjercicio(ejercicio);
            
            SeccionXejercicio relacion = new SeccionXejercicio();
            relacion.IdSeccion = idSeccion;
            relacion.IdEjercicio = (int) idEjercicio;
            
            seccionXejercicioDao.insert(relacion);
        });
    }

    // Inserta una relación entre un ejercicio existente y una sección
    public void insertarRelacionSeccionEjercicio(int idEjercicio, int idSeccion) {
        executorService.execute(() -> {
            SeccionXejercicio relacion = new SeccionXejercicio();
            relacion.IdSeccion = idSeccion;
            relacion.IdEjercicio = idEjercicio;
            seccionXejercicioDao.insert(relacion);
        });
    }

    // Actualiza los datos de un ejercicio
    public void actualizarEjercicio(Ejercicio ejercicio) {
        executorService.execute(() -> ejercicioDao.actualizarEjercicio(ejercicio));
    }

    // Modifica el ejercicio creando una copia para no afectar a otras secciones que lo utilicen
    public void actualizarEjercicioIndependiente(Ejercicio ejercicioEditado, int idSeccion) {
        executorService.execute(() -> {
            Ejercicio nuevoEj = new Ejercicio();
            nuevoEj.NombreEjercicio = ejercicioEditado.NombreEjercicio;
            nuevoEj.ImagenEjercicio = ejercicioEditado.ImagenEjercicio;
            nuevoEj.TipoEjercicio = ejercicioEditado.TipoEjercicio;
            nuevoEj.PesoCorporalEjercicio = ejercicioEditado.PesoCorporalEjercicio;
            nuevoEj.EsPreestablecido = false; // Al editarlo, deja de ser preestablecido para este usuario
            
            long nuevoIdEjercicio = ejercicioDao.insertarEjercicio(nuevoEj);
            
            SeccionXejercicio relacion = seccionXejercicioDao.getRelacion(idSeccion, ejercicioEditado.IdEjercicio);
            
            if (relacion != null) {
                relacion.IdEjercicio = (int) nuevoIdEjercicio;
                seccionXejercicioDao.update(relacion);
            }
        });
    }

    // Elimina un ejercicio de la base de datos
    public void eliminarEjercicio(Ejercicio ejercicio) {
        executorService.execute(() -> ejercicioDao.eliminarEjercicio(ejercicio));
    }

    // Elimina solo el vínculo entre el ejercicio y la sección actual
    public void eliminarEjercicioDeSeccion(int idEjercicio, int idSeccion) {
        executorService.execute(() -> {
            SeccionXejercicio relacion = seccionXejercicioDao.getRelacion(idSeccion, idEjercicio);
            if (relacion != null) {
                seccionXejercicioDao.delete(relacion);
            }
        });
    }

    // Obtiene los ejercicios que pertenecen a una sección
    public void obtenerEjerciciosPorSeccion(int idSeccion, RepositoryCallback<List<Ejercicio>> callback) {
        executorService.execute(() -> {
            List<Ejercicio> ejercicios = ejercicioDao.obtenerEjerciciosPorSeccion(idSeccion);
            notificar(callback, ejercicios);
        });
    }

    // Obtiene todos los ejercicios registrados
    public void obtenerTodosLosEjercicios(RepositoryCallback<List<Ejercicio>> callback) {
        executorService.execute(() -> {
            List<Ejercicio> ejercicios = ejercicioDao.obtenerTodosLosEjercicios();
            notificar(callback, ejercicios);
        });
    }

    // Obtiene solo los ejercicios que son preestablecidos por el sistema
    public void obtenerEjerciciosPreestablecidos(RepositoryCallback<List<Ejercicio>> callback) {
        executorService.execute(() -> {
            List<Ejercicio> ejercicios = ejercicioDao.obtenerEjerciciosPreestablecidos();
            notificar(callback, ejercicios);
        });
    }

    // Notifica el resultado en el hilo principal
    private <T> void notificar(RepositoryCallback<T> callback, T resultado) {
        if (callback != null) {
            new Handler(Looper.getMainLooper()).post(() -> callback.onResult(resultado));
        }
    }

    public interface RepositoryCallback<T> {
        void onResult(T result);
    }
}

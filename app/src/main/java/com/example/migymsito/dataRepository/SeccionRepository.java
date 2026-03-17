package com.example.migymsito.dataRepository;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;

import com.example.migymsito.data.Seccion;
import com.example.migymsito.data.SeccionXejercicio;
import com.example.migymsito.dataDao.SeccionDao;
import com.example.migymsito.dataDao.SeccionXejercicioDao;
import com.example.migymsito.dataDataBase.AppDatabase;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SeccionRepository {

    private final SeccionDao seccionDao;
    private final SeccionXejercicioDao seccionXejercicioDao;
    private final ExecutorService executorService;

    public SeccionRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        seccionDao = db.seccionDao();
        seccionXejercicioDao = db.seccionXejercicioDao();
        executorService = Executors.newFixedThreadPool(4);
    }

    public void insertarSeccion(Seccion seccion) {
        executorService.execute(() -> seccionDao.insertarSeccion(seccion));
    }

    // Participa en SeccionesActivity para clonar una sección previa con sus ejercicios
    public void clonarSeccion(Seccion seccionOriginal, int idNuevaRutina, RepositoryCallback<Void> callback) {
        clonarSeccionConNombre(seccionOriginal, idNuevaRutina, seccionOriginal.NombreSeccion, callback);
    }

    // Nueva versión que permite especificar el nombre al clonar
    public void clonarSeccionConNombre(Seccion seccionOriginal, int idNuevaRutina, String nuevoNombre, RepositoryCallback<Void> callback) {
        executorService.execute(() -> {
            // 1. Insertar la nueva sección con el nuevo nombre
            Seccion nuevaSeccion = new Seccion();
            nuevaSeccion.NombreSeccion = nuevoNombre;
            nuevaSeccion.IdRutinaSeccion = idNuevaRutina;
            long nuevaSeccionId = seccionDao.insertarSeccion(nuevaSeccion);

            // 2. Obtener los ejercicios de la sección original
            List<SeccionXejercicio> ejercicios = seccionXejercicioDao.getEjerciciosBySeccion(seccionOriginal.IdSeccion);

            // 3. Insertar los mismos ejercicios en la nueva sección
            for (SeccionXejercicio sxe : ejercicios) {
                SeccionXejercicio nuevoSxe = new SeccionXejercicio();
                nuevoSxe.IdSeccion = (int) nuevaSeccionId;
                nuevoSxe.IdEjercicio = sxe.IdEjercicio;
                seccionXejercicioDao.insert(nuevoSxe);
            }
            notificar(callback, null);
        });
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

    // Participa en SeccionesActivity para obtener todas las secciones de la base para el popup de secciones previas
    public void obtenerTodasLasSecciones(RepositoryCallback<List<Seccion>> callback) {
        executorService.execute(() -> {
            List<Seccion> lista = seccionDao.obtenerTodasLasSeccionesConRutina();
            notificar(callback, lista);
        });
    }

    // Mensajero para volver al hilo principal de la UI
    private <T> void notificar(RepositoryCallback<T> callback, T resultado) {
        if (callback != null) {
            new Handler(Looper.getMainLooper()).post(() -> callback.onResult(resultado));
        }
    }

    public interface RepositoryCallback<T> {
        void onResult(T result);
    }
}

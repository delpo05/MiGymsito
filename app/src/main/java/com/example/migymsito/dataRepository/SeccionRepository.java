package com.example.migymsito.dataRepository;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;

import com.example.migymsito.data.Rutina;
import com.example.migymsito.data.Seccion;
import com.example.migymsito.data.SeccionXejercicio;
import com.example.migymsito.dataDao.SeccionDao;
import com.example.migymsito.dataDao.SeccionXejercicioDao;
import com.example.migymsito.dataDataBase.AppDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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

    public interface RepositoryCallback<T> {
        void onResult(T result);
    }

    public void insertarSeccion(Seccion seccion) {
        executorService.execute(() -> seccionDao.insertarSeccion(seccion));
    }

    // --- MÉTODOS DE CLONACIÓN ---
    public void clonarSeccion(Seccion seccionOriginal, int idNuevaRutina, RepositoryCallback<Void> callback) {
        clonarSeccionConNombre(seccionOriginal, idNuevaRutina, seccionOriginal.NombreSeccion, callback);
    }

    public void clonarSeccionConNombre(Seccion seccionOriginal, int idNuevaRutina, String nuevoNombre, RepositoryCallback<Void> callback) {
        executorService.execute(() -> {
            Seccion nuevaSeccion = new Seccion();
            nuevaSeccion.NombreSeccion = nuevoNombre;
            nuevaSeccion.IdRutinaSeccion = idNuevaRutina;
            long nuevaSeccionId = seccionDao.insertarSeccion(nuevaSeccion);

            List<SeccionXejercicio> ejercicios = seccionXejercicioDao.getEjerciciosBySeccion(seccionOriginal.IdSeccion);

            for (SeccionXejercicio sxe : ejercicios) {
                SeccionXejercicio nuevoSxe = new SeccionXejercicio();
                nuevoSxe.IdSeccion = (int) nuevaSeccionId;
                nuevoSxe.IdEjercicio = sxe.IdEjercicio;
                seccionXejercicioDao.insert(nuevoSxe);
            }
            notificar(callback, null);
        });
    }

    // --- MÉTODOS DE OBTENCIÓN ---
    public void obtenerSeccionesPorUsuario(int idUsuario, RepositoryCallback<List<Seccion>> callback) {
        executorService.execute(() -> {
            List<Seccion> lista = seccionDao.obtenerSeccionesPorUsuario(idUsuario);
            notificar(callback, lista);
        });
    }

    public void obtenerSeccionesDeRutina(int idRutina, RepositoryCallback<List<Seccion>> callback) {
        executorService.execute(() -> {
            List<Seccion> lista = seccionDao.obtenerSeccionesPorRutina(idRutina);
            notificar(callback, lista);
        });
    }

    public void obtenerTodasLasSecciones(RepositoryCallback<List<Seccion>> callback) {
        executorService.execute(() -> {
            Map<Seccion, Rutina> mapa = seccionDao.obtenerTodasLasSeccionesConRutina();
            List<Seccion> listaCompleta = new ArrayList<>();
            if (mapa != null) {
                for (Map.Entry<Seccion, Rutina> entry : mapa.entrySet()) {
                    Seccion seccion = entry.getKey();
                    Rutina rutina = entry.getValue();
                    if (rutina != null) {
                        seccion.nombreRutina = rutina.NombreRutina;
                    }
                    listaCompleta.add(seccion);
                }
            }
            notificar(callback, listaCompleta);
        });
    }

    public void obtenerSeccionesPreestablecidas(RepositoryCallback<List<Seccion>> callback) {
        executorService.execute(() -> {
            List<Seccion> lista = seccionDao.obtenerSeccionesPreestablecidas();
            notificar(callback, lista);
        });
    }

    public void obtenerSeccionesPersonalizadas(RepositoryCallback<List<Seccion>> callback) {
        executorService.execute(() -> {
            List<Seccion> lista = seccionDao.obtenerSeccionesPersonalizadas();
            notificar(callback, lista);
        });
    }

    // --- MÉTODOS DE ACTUALIZACIÓN Y ELIMINACIÓN ---
    public void actualizarSeccion(Seccion seccion) {
        executorService.execute(() -> seccionDao.actualizarSeccion(seccion));
    }

    public void eliminarSeccion(Seccion seccion) {
        executorService.execute(() -> seccionDao.eliminarSeccion(seccion));
    }

    private <T> void notificar(RepositoryCallback<T> callback, T resultado) {
        if (callback != null) {
            new Handler(Looper.getMainLooper()).post(() -> callback.onResult(resultado));
        }
    }
}
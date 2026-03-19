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

    public interface RepositoryCallback<T> {
        void onResult(T result);
    }

    public void insertarEjercicioConSeccion(Ejercicio ejercicio, int idSeccion, RepositoryCallback<Boolean> callback) {
        executorService.execute(() -> {
            try {
                long idEjercicio = ejercicioDao.insertarEjercicio(ejercicio);
                
                SeccionXejercicio relacion = new SeccionXejercicio();
                relacion.IdSeccion = idSeccion;
                relacion.IdEjercicio = (int) idEjercicio;
                
                seccionXejercicioDao.insert(relacion);
                
                if (callback != null) {
                    mainThreadHandler.post(() -> callback.onResult(true));
                }
            } catch (Exception e) {
                Log.e(TAG, "ERROR AL CREAR EJERCICIO: ", e);
                if (callback != null) {
                    mainThreadHandler.post(() -> callback.onResult(false));
                }
            }
        });
    }

    public void insertarRelacionSeccionEjercicio(int idEjercicio, int idSeccion) {
        executorService.execute(() -> {
            try {
                SeccionXejercicio relacion = new SeccionXejercicio();
                relacion.IdSeccion = idSeccion;
                relacion.IdEjercicio = idEjercicio;
                seccionXejercicioDao.insert(relacion);
            } catch (Exception e) {
                Log.e(TAG, "Error al insertar relación: " + e.getMessage());
            }
        });
    }

    public void actualizarEjercicio(Ejercicio ejercicio) {
        executorService.execute(() -> {
            try {
                ejercicioDao.actualizarEjercicio(ejercicio);
            } catch (Exception e) {
                Log.e(TAG, "Error al actualizar ejercicio: " + e.getMessage());
            }
        });
    }

    // ACTUALIZADO: Ahora acepta un callback para avisar cuando termina
    public void actualizarEjercicioIndependiente(Ejercicio ejercicioEditado, int idSeccion, RepositoryCallback<Boolean> callback) {
        executorService.execute(() -> {
            try {
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

                if (callback != null) {
                    mainThreadHandler.post(() -> callback.onResult(true));
                }
            } catch (Exception e) {
                Log.e(TAG, "Error en actualizarEjercicioIndependiente: " + e.getMessage());
                if (callback != null) {
                    mainThreadHandler.post(() -> callback.onResult(false));
                }
            }
        });
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
            try {
                List<SeccionXejercicio> relaciones = seccionXejercicioDao.getEjerciciosBySeccion(idSeccion);
                List<Ejercicio> ejercicios = new ArrayList<>();
                for (SeccionXejercicio rel : relaciones) {
                    Ejercicio ej = ejercicioDao.obtenerEjercicioPorId(rel.IdEjercicio);
                    if (ej != null) {
                        ejercicios.add(ej);
                    }
                }
                mainThreadHandler.post(() -> callback.onResult(ejercicios));
            } catch (Exception e) {
                Log.e(TAG, "Error al obtener ejercicios: " + e.getMessage());
                mainThreadHandler.post(() -> callback.onResult(new ArrayList<>()));
            }
        });
    }

    public void obtenerTodosLosEjercicios(RepositoryCallback<List<Ejercicio>> callback) {
        executorService.execute(() -> {
            try {
                List<Ejercicio> ejercicios = ejercicioDao.obtenerTodosLosEjercicios();
                mainThreadHandler.post(() -> callback.onResult(ejercicios));
            } catch (Exception e) {
                Log.e(TAG, "Error al obtener todos los ejercicios: " + e.getMessage());
                mainThreadHandler.post(() -> callback.onResult(new ArrayList<>()));
            }
        });
    }
}
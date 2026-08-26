package com.example.migymsito.dataRepository;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.migymsito.data.EjercicioPeso;
import com.example.migymsito.data.SeccionXejercicio;
import com.example.migymsito.dataDao.EjercicioPesoDao;
import com.example.migymsito.dataDao.SeccionXejercicioDao;
import com.example.migymsito.dataDataBase.AppDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EjercicioPesoRepository {

    private final EjercicioPesoDao ejercicioDao;
    private final SeccionXejercicioDao seccionXejercicioDao;
    private final ExecutorService executorService;
    private final Handler mainThreadHandler;
    private final String TAG = "EjercicioPesoRepository";

    public EjercicioPesoRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        ejercicioDao = db.ejercicioPesoDao();
        seccionXejercicioDao = db.seccionXejercicioDao();
        executorService = Executors.newFixedThreadPool(4);
        mainThreadHandler = new Handler(Looper.getMainLooper());
    }

    public interface RepositoryCallback<T> {
        void onResult(T result);
    }

    public void insertarEjercicioConSeccion(EjercicioPeso ejercicio, int idSeccion, RepositoryCallback<Boolean> callback) {
        executorService.execute(() -> {
            try {
                long idEjercicio = ejercicioDao.insertarEjercicioPeso(ejercicio);
                
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

    public void actualizarEjercicio(EjercicioPeso ejercicio) {
        executorService.execute(() -> {
            try {
                ejercicioDao.actualizarEjercicioPeso(ejercicio);
            } catch (Exception e) {
                Log.e(TAG, "Error al actualizar ejercicio: " + e.getMessage());
            }
        });
    }

    public void actualizarEjercicioIndependiente(EjercicioPeso ejercicioEditado, int idSeccion, RepositoryCallback<Boolean> callback) {
        executorService.execute(() -> {
            try {
                EjercicioPeso nuevoEj = new EjercicioPeso();
                nuevoEj.NombreEjercicio = ejercicioEditado.NombreEjercicio;
                nuevoEj.ImagenEjercicio = ejercicioEditado.ImagenEjercicio;
                nuevoEj.TipoEjercicio = ejercicioEditado.TipoEjercicio;
                nuevoEj.PesoCorporalEjercicio = ejercicioEditado.PesoCorporalEjercicio;
                nuevoEj.PesoPorLado = ejercicioEditado.PesoPorLado;
                nuevoEj.TipoDeBarra = ejercicioEditado.TipoDeBarra;
                nuevoEj.PesoBarra = ejercicioEditado.PesoBarra;

                long nuevoIdEjercicio = ejercicioDao.insertarEjercicioPeso(nuevoEj);
                SeccionXejercicio relacion = seccionXejercicioDao.getRelacion(idSeccion, ejercicioEditado.IdEjercicio);

                if (relacion != null) {
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

    public void eliminarEjercicioDeSeccion(int idEjercicio, int idSeccion) {
        executorService.execute(() -> {
            SeccionXejercicio relacion = seccionXejercicioDao.getRelacion(idSeccion, idEjercicio);
            if (relacion != null) {
                seccionXejercicioDao.delete(relacion);
            }
        });
    }

    public void obtenerEjerciciosPorSeccion(int idSeccion, RepositoryCallback<List<EjercicioPeso>> callback) {
        executorService.execute(() -> {
            try {
                List<SeccionXejercicio> relaciones = seccionXejercicioDao.getEjerciciosBySeccion(idSeccion);
                List<EjercicioPeso> ejercicios = new ArrayList<>();
                for (SeccionXejercicio rel : relaciones) {
                    EjercicioPeso ej = ejercicioDao.obtenerEjercicioPorId(rel.IdEjercicio);
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

    public void obtenerEjerciciosEnUso(int idUsuario, RepositoryCallback<List<EjercicioPeso>> callback) {
        executorService.execute(() -> {
            try {
                List<EjercicioPeso> ejercicios = ejercicioDao.obtenerEjerciciosEnUsoPorUsuario(idUsuario);
                mainThreadHandler.post(() -> callback.onResult(ejercicios));
            } catch (Exception e) {
                Log.e(TAG, "Error al obtener ejercicios en uso: " + e.getMessage());
                mainThreadHandler.post(() -> callback.onResult(new ArrayList<>()));
            }
        });
    }

    /**
     * NUEVO: Obtiene solo los nombres de los ejercicios de una sección.
     * Ideal para llenar dropdowns en Estadísticas.
     */
    public void obtenerNombresEjerciciosPorSeccion(int idSeccion, RepositoryCallback<List<String>> callback) {
        executorService.execute(() -> {
            try {
                List<SeccionXejercicio> relaciones = seccionXejercicioDao.getEjerciciosBySeccion(idSeccion);
                List<String> nombres = new ArrayList<>();
                for (SeccionXejercicio rel : relaciones) {
                    EjercicioPeso ej = ejercicioDao.obtenerEjercicioPorId(rel.IdEjercicio);
                    if (ej != null) {
                        nombres.add(ej.NombreEjercicio);
                    }
                }
                mainThreadHandler.post(() -> callback.onResult(nombres));
            } catch (Exception e) {
                Log.e(TAG, "Error al obtener nombres de ejercicios: " + e.getMessage());
                mainThreadHandler.post(() -> callback.onResult(new ArrayList<>()));
            }
        });
    }

    public void obtenerTodosLosEjercicios(RepositoryCallback<List<EjercicioPeso>> callback) {
        executorService.execute(() -> {
            try {
                List<EjercicioPeso> ejercicios = ejercicioDao.obtenerTodosLosEjercicios();
                mainThreadHandler.post(() -> callback.onResult(ejercicios));
            } catch (Exception e) {
                Log.e(TAG, "Error al obtener todos los ejercicios: " + e.getMessage());
                mainThreadHandler.post(() -> callback.onResult(new ArrayList<>()));
            }
        });
    }
}

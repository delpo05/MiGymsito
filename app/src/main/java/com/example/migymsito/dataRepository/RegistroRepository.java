package com.example.migymsito.dataRepository;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;

import com.example.migymsito.data.Entrenamiento;
import com.example.migymsito.data.Registro;
import com.example.migymsito.data.SeccionXejercicio;
import com.example.migymsito.dataDao.EntrenamientoDao;
import com.example.migymsito.dataDao.RegistroDao;
import com.example.migymsito.dataDao.SeccionXejercicioDao;
import com.example.migymsito.dataDataBase.AppDatabase;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RegistroRepository {

    private final RegistroDao registroDao;
    private final EntrenamientoDao entrenamientoDao;
    private final SeccionXejercicioDao seccionXejercicioDao;
    private final ExecutorService executorService;

    public RegistroRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        registroDao = db.registroDao();
        entrenamientoDao = db.entrenamientoDao();
        seccionXejercicioDao = db.seccionXejercicioDao();
        executorService = Executors.newFixedThreadPool(4);
    }

    public void insertarRegistro(Registro registro) {
        executorService.execute(() -> registroDao.insertarRegistro(registro));
    }

    /**
     * Guarda un registro vinculándolo a un entrenamiento activo o creando uno nuevo.
     */
    public void guardarRegistroCompleto(int idUsuario, int idSeccion, int idEjercicio, double peso, int series, int reps, RepositoryCallback<Registro> callback) {
        executorService.execute(() -> {
            // 1. Obtener Entrenamiento activo específicamente para esta sección
            Entrenamiento entrenamientoActivo = entrenamientoDao.getEntrenamientoActivoPorSeccion(idUsuario, idSeccion);

            if (entrenamientoActivo == null) {
                entrenamientoActivo = new Entrenamiento();
                entrenamientoActivo.IdUsuario = idUsuario;
                entrenamientoActivo.IdSeccion = idSeccion;
                entrenamientoActivo.FechaInicio = System.currentTimeMillis();
                
                // Calcular el número correlativo de entrenamientos totales del usuario
                List<Entrenamiento> todos = entrenamientoDao.getEntrenamientosByUsuario(idUsuario);
                entrenamientoActivo.NumeroEntrenamiento = todos.size() + 1;
                
                long idEnt = entrenamientoDao.insert(entrenamientoActivo);
                entrenamientoActivo.IdEntrenamiento = (int) idEnt;
            }

            // 2. Obtener Relación SeccionXejercicio
            SeccionXejercicio relacion = seccionXejercicioDao.getRelacion(idSeccion, idEjercicio);
            if (relacion == null) {
                relacion = new SeccionXejercicio();
                relacion.IdSeccion = idSeccion;
                relacion.IdEjercicio = idEjercicio;
                long idRel = seccionXejercicioDao.insert(relacion);
                relacion.IdSeccionXejercicio = (int) idRel;
            }

            // 3. Crear Registro
            Registro nuevo = new Registro();
            nuevo.IdEntrenamiento = entrenamientoActivo.IdEntrenamiento;
            nuevo.IdSeccionXejercicio = relacion.IdSeccionXejercicio;
            nuevo.PesoRegistro = peso;
            nuevo.NumSeriesRegistro = series;
            nuevo.Repeticiones = reps;
            nuevo.FechaRegistro = System.currentTimeMillis();

            registroDao.insertarRegistro(nuevo);
            
            notificar(callback, nuevo);
        });
    }

    public void actualizarRegistro(Registro registro) {
        executorService.execute(() -> registroDao.actualizarRegistro(registro));
    }

    public void eliminarRegistro(Registro registro) {
        executorService.execute(() -> registroDao.eliminarRegistro(registro));
    }
    
    public void obtenerHistorialPorEjercicio(int idUsuario, int idEjercicio, RepositoryCallback<List<Registro>> callback) {
        executorService.execute(() -> {
            List<Registro> lista = registroDao.obtenerHistorialPorEjercicioYUsuario(idUsuario, idEjercicio);
            notificar(callback, lista);
        });
    }

    /**
     * Obtiene el historial del ejercicio pero filtrado por el entrenamiento activo.
     * Esto permite que el contador de series se reinicie en cada nuevo entrenamiento.
     */
    public void obtenerRegistrosEntrenamientoActivo(int idUsuario, int idSeccion, int idEjercicio, RepositoryCallback<List<Registro>> callback) {
        executorService.execute(() -> {
            Entrenamiento activo = entrenamientoDao.getEntrenamientoActivoPorSeccion(idUsuario, idSeccion);
            if (activo != null) {
                List<Registro> lista = registroDao.obtenerHistorialPorEjercicioYUsuario(idUsuario, idEjercicio);
                // Filtrar solo los que pertenecen a este entrenamiento
                java.util.List<Registro> filtrados = new java.util.ArrayList<>();
                for (Registro r : lista) {
                    if (r.IdEntrenamiento == activo.IdEntrenamiento) {
                        filtrados.add(r);
                    }
                }
                notificar(callback, filtrados);
            } else {
                notificar(callback, new java.util.ArrayList<>());
            }
        });
    }

    public void obtenerProgresoCargas(int idEjercicio, RepositoryCallback<List<Registro>> callback) {
        executorService.execute(() -> {
            List<Registro> lista = registroDao.obtenerProgresoCargas(idEjercicio);
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
        if (callback != null) {
            new Handler(Looper.getMainLooper()).post(() -> callback.onResult(resultado));
        }
    }

    public interface RepositoryCallback<T> {
        void onResult(T result);
    }
}

package com.example.migymsito.dataRepository;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;

import com.example.migymsito.data.Entrenamiento;
import com.example.migymsito.data.RegistroCardio;
import com.example.migymsito.data.SeccionXejercicio;
import com.example.migymsito.dataDao.EntrenamientoDao;
import com.example.migymsito.dataDao.RegistroCardioDao;
import com.example.migymsito.dataDao.SeccionXejercicioDao;
import com.example.migymsito.dataDataBase.AppDatabase;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RegistroCardioRepository {

    private final RegistroCardioDao registroCardioDao;
    private final EntrenamientoDao entrenamientoDao;
    private final SeccionXejercicioDao seccionXejercicioDao;
    private final ExecutorService executorService;
    private final EntrenamientoRepository entrenamientoRepository;

    public RegistroCardioRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        registroCardioDao = db.registroCardioDao();
        entrenamientoDao = db.entrenamientoDao();
        seccionXejercicioDao = db.seccionXejercicioDao();
        executorService = Executors.newFixedThreadPool(4);
        entrenamientoRepository = new EntrenamientoRepository(application);
    }

    public void guardarRegistro(int idUsuario, int idSeccion, int idEjercicio, RegistroCardio registro, RepositoryCallback<RegistroCardio> callback) {
        executorService.execute(() -> {
            // 1. Obtener/Crear Entrenamiento
            Entrenamiento ent = entrenamientoDao.getEntrenamientoActivoPorSeccion(idUsuario, idSeccion);
            if (ent == null) {
                ent = new Entrenamiento();
                ent.IdUsuario = idUsuario;
                ent.IdSeccion = idSeccion;
                ent.FechaInicio = System.currentTimeMillis();
                List<Entrenamiento> todos = entrenamientoDao.getEntrenamientosByUsuario(idUsuario);
                ent.NumeroEntrenamiento = todos.size() + 1;
                long idEnt = entrenamientoDao.insert(ent);
                ent.IdEntrenamiento = (int) idEnt;
            }

            // 2. Obtener/Crear Relación
            SeccionXejercicio sxe = seccionXejercicioDao.getRelacion(idSeccion, idEjercicio);
            if (sxe == null) {
                sxe = new SeccionXejercicio();
                sxe.IdSeccion = idSeccion;
                sxe.IdEjercicio = idEjercicio;
                long idRel = seccionXejercicioDao.insert(sxe);
                sxe.IdSeccionXejercicio = (int) idRel;
            }

            // 3. Vincular y Guardar
            registro.IdEntrenamiento = ent.IdEntrenamiento;
            registro.IdSeccionXejercicio = sxe.IdSeccionXejercicio;
            if (registro.FechaRegistro == null) registro.FechaRegistro = System.currentTimeMillis();
            
            long id = registroCardioDao.insertar(registro);
            registro.IdRegistroCardio = (int) id;
            
            entrenamientoRepository.refrescarAlarmasInactividad(idUsuario, idSeccion);
            
            notificar(callback, registro);
        });
    }

    public void obtenerHistorial(int idSeccion, int idEjercicio, RepositoryCallback<List<RegistroCardio>> callback) {
        executorService.execute(() -> {
            SeccionXejercicio sxe = seccionXejercicioDao.getRelacion(idSeccion, idEjercicio);
            if (sxe != null) {
                List<RegistroCardio> lista = registroCardioDao.obtenerPorSeccionXejercicio(sxe.IdSeccionXejercicio);
                notificar(callback, lista);
            } else {
                notificar(callback, new java.util.ArrayList<>());
            }
        });
    }

    public void eliminarUltimo(RegistroCardio registro) {
        executorService.execute(() -> registroCardioDao.eliminar(registro));
    }

    private <T> void notificar(RepositoryCallback<T> callback, T result) {
        if (callback != null) {
            new Handler(Looper.getMainLooper()).post(() -> callback.onResult(result));
        }
    }

    public interface RepositoryCallback<T> {
        void onResult(T result);
    }
}

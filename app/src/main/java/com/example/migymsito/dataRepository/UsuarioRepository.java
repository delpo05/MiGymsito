package com.example.migymsito.dataRepository;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.migymsito.data.Historial;
import com.example.migymsito.data.Usuario;
import com.example.migymsito.dataDao.UsuarioDao;
import com.example.migymsito.dataDataBase.AppDatabase;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UsuarioRepository {

    private final UsuarioDao usuarioDao;
    private final ExecutorService executorService;
    private final Handler mainThreadHandler;
    private final Application application;
    private static final String PREFS_NAME = "SesionUsuario";
    private static final String KEY_USER_ID = "idUsuario";
    private static final String KEY_RUTINA_ID = "idRutinaSeleccionada";

    public interface RepositoryCallback<T> {
        void onResult(T result);
    }

    public interface DebugCallback {
        void onResult(boolean success, String errorMessage);
    }

    public UsuarioRepository(Application application) {
        this.application = application;
        AppDatabase db = AppDatabase.getDatabase(application);
        usuarioDao = db.usuarioDao();
        executorService = Executors.newSingleThreadExecutor();
        mainThreadHandler = new Handler(Looper.getMainLooper());
    }

    // --- MÉTODOS DE SESIÓN (SharedPreferences) ---

    public void guardarIdSesion(int idUsuario) {
        SharedPreferences prefs = application.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putInt(KEY_USER_ID, idUsuario).apply();
    }

    public int obtenerIdSesion() {
        SharedPreferences prefs = application.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getInt(KEY_USER_ID, -1);
    }

    public void eliminarSesion() {
        SharedPreferences prefs = application.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().remove(KEY_USER_ID).remove(KEY_RUTINA_ID).apply();
    }

    // --- MÉTODOS DE RUTINA SELECCIONADA ---

    public void guardarIdRutina(int idRutina) {
        SharedPreferences prefs = application.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putInt(KEY_RUTINA_ID, idRutina).apply();
    }

    public int obtenerIdRutina() {
        SharedPreferences prefs = application.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getInt(KEY_RUTINA_ID, -1);
    }

    public void eliminarRutinaSeleccionada() {
        SharedPreferences prefs = application.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().remove(KEY_RUTINA_ID).apply();
    }

    // --- MÉTODOS DE BASE DE DATOS ---

    public void obtenerUsuarioPorId(int id, RepositoryCallback<Usuario> callback) {
        executorService.execute(() -> {
            Usuario usuario = usuarioDao.obtenerUsuarioPorId(id);
            mainThreadHandler.post(() -> callback.onResult(usuario));
        });
    }

    public void validarLogin(String correo, String password, RepositoryCallback<Usuario> callback) {
        executorService.execute(() -> {
            Usuario usuario = usuarioDao.login(correo, password);
            mainThreadHandler.post(() -> callback.onResult(usuario));
        });
    }

    public void registrarUsuarioConHistorial(Usuario usuario, Historial historial, RepositoryCallback<Integer> callback) {
        executorService.execute(() -> {
            try {
                AppDatabase db = AppDatabase.getDatabase(application);
                long idGenerado = usuarioDao.registrarUsuario(usuario);
                historial.IdUsuarioHistorial = (int) idGenerado;
                db.historialDao().insertarHistorial(historial);
                mainThreadHandler.post(() -> callback.onResult((int) idGenerado));
            } catch (Exception e) {
                Log.e("UsuarioRepository", "Error en registro: " + e.getMessage());
                mainThreadHandler.post(() -> callback.onResult(-1));
            }
        });
    }

    public void actualizarPerfilUsuario(Usuario usuario, Historial nuevoHistorial, DebugCallback callback) {
        executorService.execute(() -> {
            try {
                AppDatabase db = AppDatabase.getDatabase(application);
                usuarioDao.actualizarUsuario(usuario);
                if (nuevoHistorial != null) {
                    db.historialDao().insertarHistorial(nuevoHistorial);
                }
                mainThreadHandler.post(() -> callback.onResult(true, null));
            } catch (Exception e) {
                Log.e("UsuarioRepository", "ERROR REAL DE BASE DE DATOS: ", e);
                mainThreadHandler.post(() -> callback.onResult(false, e.getMessage()));
            }
        });
    }

    public void validarCorreoExistente(String correo, RepositoryCallback<Usuario> callback) {
        executorService.execute(() -> {
            Usuario usuario = usuarioDao.validarCorreoUsuario(correo);
            mainThreadHandler.post(() -> callback.onResult(usuario));
        });
    }

    public void obtenerUltimoHistorial(int idUsuario, RepositoryCallback<Historial> callback) {
        executorService.execute(() -> {
            AppDatabase db = AppDatabase.getDatabase(application);
            Historial historial = db.historialDao().obtenerUltimoHistorial(idUsuario);
            mainThreadHandler.post(() -> callback.onResult(historial));
        });
    }

    public void obtenerTodosLosUsuarios(RepositoryCallback<List<Usuario>> callback) {
        executorService.execute(() -> {
            List<Usuario> usuarios = usuarioDao.obtenerTodosLosUsuarios();
            mainThreadHandler.post(() -> callback.onResult(usuarios));
        });
    }

    public void borrarTodosLosUsuarios(RepositoryCallback<Boolean> callback) {
        executorService.execute(() -> {
            try {
                usuarioDao.deleteAll();
                mainThreadHandler.post(() -> callback.onResult(true));
            } catch (Exception e) {
                mainThreadHandler.post(() -> callback.onResult(false));
            }
        });
    }

    public void borrarTodaLaBaseDeDatos(RepositoryCallback<Boolean> callback) {
        executorService.execute(() -> {
            try {
                AppDatabase db = AppDatabase.getDatabase(application);
                db.clearAllTables();
                mainThreadHandler.post(() -> callback.onResult(true));
            } catch (Exception e) {
                mainThreadHandler.post(() -> callback.onResult(false));
            }
        });
    }
}
package com.example.migymsito.dataRepository;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;

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

    public interface RepositoryCallback<T> {
        void onResult(T result);
    }

    public UsuarioRepository(Application application) {
        this.application = application;
        AppDatabase db = AppDatabase.getDatabase(application);
        usuarioDao = db.usuarioDao();
        executorService = Executors.newSingleThreadExecutor();
        mainThreadHandler = new Handler(Looper.getMainLooper());
    }

    public void validarLogin(String correo, String password, RepositoryCallback<Usuario> callback) {
        executorService.execute(() -> {
            Usuario usuario = usuarioDao.login(correo, password);
            mainThreadHandler.post(() -> callback.onResult(usuario));
        });
    }

    public void registrarUsuarioConHistorial(Usuario usuario, Historial historial, RepositoryCallback<Boolean> callback) {
        executorService.execute(() -> {
            try {
                AppDatabase db = AppDatabase.getDatabase(application);
                long idGenerado = usuarioDao.registrarUsuario(usuario);
                historial.IdUsuarioHistorial = (int) idGenerado;
                db.historialDao().insertarHistorial(historial);
                mainThreadHandler.post(() -> callback.onResult(true));
            } catch (Exception e) {
                mainThreadHandler.post(() -> callback.onResult(false));
            }
        });
    }

    public void validarCorreoExistente(String correo, RepositoryCallback<Usuario> callback) {
        executorService.execute(() -> {
            Usuario usuario = usuarioDao.validarCorreoUsuario(correo);
            mainThreadHandler.post(() -> callback.onResult(usuario));
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
    //Code para debug

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

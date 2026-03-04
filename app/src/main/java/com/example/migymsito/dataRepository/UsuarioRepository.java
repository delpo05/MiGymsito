package com.example.migymsito.dataRepository;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;

import com.example.migymsito.data.Historial;
import com.example.migymsito.data.Usuario;
import com.example.migymsito.dataDao.UsuarioDao;
import com.example.migymsito.dataDataBase.AppDatabase;

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
                // 1. Insertar usuario y obtener su ID generado
                long idGenerado = usuarioDao.registrarUsuario(usuario);

                // 2. Asignar el ID al historial e insertarlo
                historial.IdUsuario = (int) idGenerado;
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
}
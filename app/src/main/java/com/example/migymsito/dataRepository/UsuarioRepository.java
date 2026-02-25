package com.example.migymsito.dataRepository;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;

// imports para que encuentre los otros archivos

import com.example.migymsito.data.Usuario;
import com.example.migymsito.dataDao.UsuarioDao;
import com.example.migymsito.dataDataBase.AppDatabase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

//CONTROLARRRRRRRRRRRRRRRRRRRRRRR!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

public class UsuarioRepository {

    private final UsuarioDao usuarioDao;
    private final ExecutorService executorService;

    public UsuarioRepository(Application application) {
        // Conectamos con la Database usando tu nueva carpeta dataDataBase
        AppDatabase db = AppDatabase.getDatabase(application);
        usuarioDao = db.usuarioDao();
        this.executorService = Executors.newSingleThreadExecutor();
    }

    // --- LÓGICA DE NEGOCIO: REGISTRO ---
    public void registrarNuevoUsuario(Usuario usuario, RepositoryCallback<String> callback) {
        executorService.execute(() -> {
            // Validaciones antes de guardar
            if (usuarioDao.validarNombreUsuario(usuario.nombreUsuario) != null) {
                notificar(callback, "NOMBRE_REPETIDO");
                return;
            }
            if (usuarioDao.validarCorreoUsuario(usuario.correoElectronicoUsuario) != null) {
                notificar(callback, "CORREO_REPETIDO");
                return;
            }

            try {
                usuarioDao.registrarUsuario(usuario);
                notificar(callback, "EXITO");
            } catch (Exception e) {
                notificar(callback, "ERROR_DATABASE");
            }
        });
    }

    // --- LÓGICA DE NEGOCIO: LOGIN ---
    public void validarLogin(String usuario, String pass, RepositoryCallback<Usuario> callback) {
        executorService.execute(() -> {
            Usuario user = usuarioDao.login(usuario, pass);
            notificar(callback, user);
        });
    }

    // Herramienta para avisar a la pantalla (FRONT) cuando termina la DB
    private <T> void notificar(RepositoryCallback<T> callback, T resultado) {
        new Handler(Looper.getMainLooper()).post(() -> callback.onResult(resultado));
    }

    // Interfaz para recibir la respuesta en la Activity
    public interface RepositoryCallback<T> {
        void onResult(T result);
    }
}
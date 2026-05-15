package com.example.migymsito;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.migymsito.data.Rutina;
import com.example.migymsito.data.Usuario;
import com.example.migymsito.dataDataBase.AppDatabase;
import com.example.migymsito.dataRepository.UsuarioRepository;

import java.util.concurrent.Executors;

public class InicioSesionActivity extends AppCompatActivity {

    private UsuarioRepository usuarioRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.inicio_sesion_activity);

        usuarioRepository = new UsuarioRepository(getApplication());

        // Simulamos un pequeño retraso para el splash
        new Handler(Looper.getMainLooper()).postDelayed(this::verificarEstadoSesion, 1500);
    }

    private void verificarEstadoSesion() {
        int idSesion = usuarioRepository.obtenerIdSesion();
        
        if (idSesion != -1) {
            continuarComoUsuarioLogueado(idSesion);
        } else {
            // Si no hay sesión, verificamos si existe algún usuario en la DB
            usuarioRepository.obtenerPrimerUsuario(usuario -> {
                if (usuario != null) {
                    // Si existe un usuario, auto-login
                    usuarioRepository.guardarIdSesion(usuario.IdUsuario);
                    continuarComoUsuarioLogueado(usuario.IdUsuario);
                } else {
                    // Si no hay usuarios, ir a Registro
                    irARegistro();
                }
            });
        }
    }

    private void continuarComoUsuarioLogueado(int idUsuario) {
        int idRutina = usuarioRepository.obtenerIdRutina();
        
        usuarioRepository.obtenerUsuarioPorId(idUsuario, usuario -> {
            if (usuario != null) {
                HeaderActivity.usuarioLogueado = usuario;
                
                if (idRutina != -1) {
                    saltarDirectoASecciones(idRutina);
                } else {
                    irARutinas();
                }
            } else {
                // Si el usuario guardado no existe, limpiamos y vamos a Registro
                usuarioRepository.eliminarSesion();
                irARegistro();
            }
        });
    }

    private void saltarDirectoASecciones(int idRutina) {
        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.getDatabase(getApplicationContext());
            Rutina rutina = db.rutinaDao().obtenerRutinaPorId(idRutina);
            runOnUiThread(() -> {
                if (rutina != null) {
                    Intent intent = new Intent(InicioSesionActivity.this, SeccionesActivity.class);
                    intent.putExtra("rutina", rutina);
                    startActivity(intent);
                    finish();
                } else {
                    usuarioRepository.eliminarRutinaSeleccionada();
                    irARutinas();
                }
            });
        });
    }

    private void irARutinas() {
        Intent intent = new Intent(InicioSesionActivity.this, RutinasActivity.class);
        startActivity(intent);
        finish();
    }

    private void irARegistro() {
        Intent intent = new Intent(InicioSesionActivity.this, RegistroSesionActivity.class);
        startActivity(intent);
        finish();
    }
}

package com.example.migymsito;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.migymsito.data.Historial;
import com.example.migymsito.data.Rutina;
import com.example.migymsito.data.Usuario;
import com.example.migymsito.dataDataBase.AppDatabase;
import com.example.migymsito.dataRepository.UsuarioRepository;

import java.util.concurrent.Executors;

public class InicioSesionActivity extends AppCompatActivity implements UsuarioRepository.RepositoryCallback<Usuario> {

    private EditText etUsuario, etPassword;
    private UsuarioRepository usuarioRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        usuarioRepository = new UsuarioRepository(getApplication());

        // --- LÓGICA DE AUTO-LOGIN Y SALTO DIRECTO ---
        int idSesion = usuarioRepository.obtenerIdSesion();
        if (idSesion != -1) {
            // Si hay sesión, verificamos si hay rutina guardada ANTES de mostrar el Login
            int idRutina = usuarioRepository.obtenerIdRutina();
            
            usuarioRepository.obtenerUsuarioPorId(idSesion, usuario -> {
                if (usuario != null) {
                    HeaderActivity.usuarioLogueado = usuario;
                    
                    if (idRutina != -1) {
                        // Si hay rutina, vamos directo a Secciones sin pasar por Rutinas visualmente
                        saltarDirectoASecciones(idRutina);
                    } else {
                        // Si no hay rutina, vamos a Rutinas
                        irARutinas();
                    }
                } else {
                    // Si por algún motivo el usuario no existe, limpiamos y mostramos login
                    usuarioRepository.eliminarSesion();
                    cargarInterfazLogin();
                }
            });
            // No llamamos a cargarInterfazLogin() todavía para evitar el parpadeo
            return; 
        }

        cargarInterfazLogin();
    }

    private void cargarInterfazLogin() {
        EdgeToEdge.enable(this);
        setContentView(R.layout.inicio_sesion_activity);

        etUsuario = findViewById(R.id.etUsuario);
        etPassword = findViewById(R.id.etPassword);
        
        configurarWindowInsets(R.id.main);
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
                    // Si la rutina ya no existe, limpiamos y vamos a la lista de Rutinas
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

    private void configurarWindowInsets(int layoutId) {
        View layout = findViewById(layoutId);
        if (layout != null) {
            ViewCompat.setOnApplyWindowInsetsListener(layout, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }
    }

    public void EventoBotonContinuar(View view) {
        String usuario = etUsuario.getText().toString();
        String password = etPassword.getText().toString();

        if (usuario.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Por favor complete todos los campos", Toast.LENGTH_SHORT).show();
        } else {
            usuarioRepository.validarLogin(usuario, password, this);
        }
    }

    public void EventoRegistrarse(View view) {
        Intent intent = new Intent(this, RegistroSesionActivity.class);
        startActivity(intent);
    }

    @Override
    public void onResult(Usuario result) {
        if (result != null) {
            usuarioRepository.guardarIdSesion(result.IdUsuario);
            HeaderActivity.usuarioLogueado = result;
            irARutinas();
        } else {
            Toast.makeText(this, "Usuario o contraseña incorrectos", Toast.LENGTH_SHORT).show();
        }
    }
}

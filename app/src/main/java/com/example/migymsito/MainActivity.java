package com.example.migymsito;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.migymsito.adapter.RutinasAdapter;
import com.example.migymsito.data.Usuario;
import com.example.migymsito.dataRepository.UsuarioRepository;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements UsuarioRepository.RepositoryCallback<Usuario> {

    private EditText etUsuario, etPassword;
    private UsuarioRepository usuarioRepository;
    private GridView gvRutinas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        // Iniciamos directamente en la pantalla de rutinas para ver los cambios
        mostrarSeccionesRutinas(null);
    }

    private void mostrarLogin() {
        setContentView(R.layout.activity_main);
        etUsuario = findViewById(R.id.etUsuario);
        etPassword = findViewById(R.id.etPassword);
        usuarioRepository = new UsuarioRepository(getApplication());
        configurarWindowInsets(R.id.main);
    }

    private void mostrarRegistro() {
        setContentView(R.layout.registro_sesion);
        configurarWindowInsets(R.id.registro);
    }

    private void mostrarSeccionesRutinas(Usuario usuario) {
        setContentView(R.layout.secciones_rutinas);
        gvRutinas = findViewById(R.id.gvRutinas);
        
        TextView tvUsername = findViewById(R.id.toolbar_username);
        if (tvUsername != null) {
            tvUsername.setText(usuario != null ? usuario.nombreUsuario : "Invitado");
        }

        // Usamos el RutinasAdapter con el GridView
        List<String> rutinasDummy = new ArrayList<>(); 
        RutinasAdapter adapter = new RutinasAdapter(rutinasDummy);
        gvRutinas.setAdapter(adapter);

        configurarWindowInsets(R.id.layout_secciones);
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

    // Métodos de navegación y eventos
    public void EventoBoton(View view) {
        if (etUsuario == null || etPassword == null) return;
        String usuario = etUsuario.getText().toString();
        String password = etPassword.getText().toString();

        if (usuario.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Por favor complete todos los campos", Toast.LENGTH_SHORT).show();
        } else {
            usuarioRepository.validarLogin(usuario, password, this);
        }
    }

    public void EventoRegistrarse(View view) {
        mostrarRegistro();
    }

    public void EventoBotonVolver(View view) {
        mostrarSeccionesRutinas(null);
    }
    
    public void EventoBotonRegistrar(View view) {
        Toast.makeText(this, "Registro simulado", Toast.LENGTH_SHORT).show();
        mostrarLogin();
    }

    @Override
    public void onResult(Usuario result) {
        if (result != null) {
            mostrarSeccionesRutinas(result);
        } else {
            Toast.makeText(this, "Usuario o contraseña incorrectos", Toast.LENGTH_SHORT).show();
        }
    }
}
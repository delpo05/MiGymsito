package com.example.migymsito;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.migymsito.data.Historial;
import com.example.migymsito.data.Usuario;
import com.example.migymsito.dataRepository.UsuarioRepository;

public class InicioSesionActivity extends AppCompatActivity implements UsuarioRepository.RepositoryCallback<Usuario> {

    private EditText etUsuario, etPassword;
    private UsuarioRepository usuarioRepository;
    private int logoClickCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        usuarioRepository = new UsuarioRepository(getApplication());

        // --- LÓGICA DE AUTO-LOGIN ---
        int idSesion = usuarioRepository.obtenerIdSesion();
        if (idSesion != -1) {
            usuarioRepository.obtenerUsuarioPorId(idSesion, usuario -> {
                if (usuario != null) {
                    HeaderActivity.usuarioLogueado = usuario;
                    irARutinas();
                }
            });
        }

        EdgeToEdge.enable(this);
        setContentView(R.layout.inicio_sesion_activity);

        etUsuario = findViewById(R.id.etUsuario);
        etPassword = findViewById(R.id.etPassword);
        
        configurarWindowInsets(R.id.main);

        View btnVerUsuarios = findViewById(R.id.btnVerUsuarios);
        View btnTestLogin = findViewById(R.id.btnTestLogin);
        ImageView ivLogo = findViewById(R.id.ivLogo);

        btnVerUsuarios.setOnClickListener(v -> {
            Intent intent = new Intent(InicioSesionActivity.this, DebugUsuariosRegistradosActivity.class);
            startActivity(intent);
        });

        // Truco para desarrolladores: Tocar el logo 10 veces para mostrar botones de debug
        ivLogo.setOnClickListener(v -> {
            logoClickCount++;
            if (logoClickCount == 10) {
                btnVerUsuarios.setVisibility(View.VISIBLE);
                btnTestLogin.setVisibility(View.VISIBLE);
                Toast.makeText(this, "Modo desarrollador activado", Toast.LENGTH_SHORT).show();
            }
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

    public void EventoLoginTest(View view) {
        String testEmail = "test@gmail.com";
        String testPass = "test123";

        // Intentar login directamente primero
        usuarioRepository.validarLogin(testEmail, testPass, usuario -> {
            if (usuario != null) {
                // Si existe, logueamos
                onResult(usuario);
            } else {
                // Si no existe, lo creamos
                crearUsuarioTest(testEmail, testPass);
            }
        });
    }

    private void crearUsuarioTest(String email, String pass) {
        Usuario testUser = new Usuario();
        testUser.CorreoElectronicoUsuario = email;
        testUser.ContraseniaUsuario = pass;
        testUser.NombreUsuario = "test_";
        testUser.FechaNacimientoUsuario = System.currentTimeMillis(); // Default
        testUser.GeneroUsuario = "Otro"; // Default

        Historial testHistorial = new Historial();
        testHistorial.PesoHistorial = 70.0;
        testHistorial.AlturaHistorial = 170.0;
        testHistorial.FechaHistorial = System.currentTimeMillis();

        usuarioRepository.registrarUsuarioConHistorial(testUser, testHistorial, idGenerado -> {
            if (idGenerado != -1) {
                testUser.IdUsuario = idGenerado;
                onResult(testUser);
            } else {
                Toast.makeText(this, "Error al crear usuario de prueba", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onResult(Usuario result) {
        if (result != null) {
            // Guardamos el ID en SharedPreferences para la próxima vez
            usuarioRepository.guardarIdSesion(result.IdUsuario);
            
            HeaderActivity.usuarioLogueado = result;
            irARutinas();
        } else {
            Toast.makeText(this, "Usuario o contraseña incorrectos", Toast.LENGTH_SHORT).show();
        }
    }
}
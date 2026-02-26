package com.example.migymsito;

import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.migymsito.data.Usuario;
import com.example.migymsito.dataRepository.UsuarioRepository;

public class MainActivity extends AppCompatActivity implements UsuarioRepository.RepositoryCallback<Usuario> {

    private EditText etUsuario, etPassword; // Login
    private EditText etRegNombre, etRegCorreo, etRegFechaNac, etRegPeso, etRegAltura, etRegGenero; // Registro
    private UsuarioRepository usuarioRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        mostrarRegistro();
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
        etRegNombre = findViewById(R.id.etRegNombre);
        etRegCorreo = findViewById(R.id.etRegCorreo);
        etRegFechaNac = findViewById(R.id.etRegFechaNac);
        etRegPeso = findViewById(R.id.etRegPeso);
        etRegAltura = findViewById(R.id.etRegAltura);
        etRegGenero = findViewById(R.id.etRegGenero);
        configurarWindowInsets(R.id.registro);
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

    public void EventoBoton(View view) {
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

    public void EventoBotonRegistrar(View view) {
        validacionesRegistrarUsuario(view);
    }

    public void validacionesRegistrarUsuario(View view) {
        boolean estado = true;

        if (etRegNombre.getText().toString().isEmpty()) {
            etRegNombre.setError("Campo requerido");
            estado = false;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(etRegCorreo.getText().toString()).matches()) {
            etRegCorreo.setError("Correo inválido");
            estado = false;
        }

        if (etRegFechaNac.getText().toString().isEmpty()) {
            etRegFechaNac.setError("Campo requerido");
            estado = false;
        }

        if (etRegPeso.getText().toString().isEmpty()) {
            etRegPeso.setError("Campo requerido");
            estado = false;
        }

        if (etRegAltura.getText().toString().isEmpty()) {
            etRegAltura.setError("Campo requerido");
            estado = false;
        }

        if (etRegGenero.getText().toString().isEmpty()) {
            etRegGenero.setError("Campo requerido");
            estado = false;
        }

        if (estado) {
            Toast.makeText(this, "Registro exitoso", Toast.LENGTH_SHORT).show();
            mostrarLogin();
        }
    }

    @Override
    public void onResult(Usuario result) {
        if (result != null) {
            Toast.makeText(this, "Bienvenido " + result.nombreUsuario, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Usuario o contraseña incorrectos", Toast.LENGTH_SHORT).show();
        }
    }
}
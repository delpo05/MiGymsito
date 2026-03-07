package com.example.migymsito;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
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

    private EditText etUsuario, etPassword;
    private UsuarioRepository usuarioRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        etUsuario = findViewById(R.id.etUsuario);
        etPassword = findViewById(R.id.etPassword);
        usuarioRepository = new UsuarioRepository(getApplication());
        
        configurarWindowInsets(R.id.main);

        findViewById(R.id.btnVerUsuarios).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, UsuariosRegistradosActivity.class);
            startActivity(intent);
        });
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
        Intent intent = new Intent(this, RegistroActivity.class);
        startActivity(intent);
    }

    @Override
    public void onResult(Usuario result) {
        if (result != null) {
            Intent intent = new Intent(MainActivity.this, HomeActivity.class);
            intent.putExtra("usuario",  result); // Pasar el objeto usuario a la Home
            startActivity(intent);
            finish(); // Cerramos el login para que no se pueda volver atrás con el botón back
        } else {
            Toast.makeText(this, "Usuario o contraseña incorrectos", Toast.LENGTH_SHORT).show();
        }
    }
}

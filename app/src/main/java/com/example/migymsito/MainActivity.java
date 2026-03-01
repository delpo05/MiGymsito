package com.example.migymsito;

import android.os.Bundle;
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

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
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

    @Override
    public void onResult(Usuario result) {
        if (result != null) {
            Toast.makeText(this, "Bienvenido " + result.nombreUsuario, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Usuario o contraseña incorrectos", Toast.LENGTH_SHORT).show();
        }
    }



}
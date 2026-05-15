package com.example.migymsito;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.migymsito.adapter.UsuariosAdapter;
import com.example.migymsito.dataRepository.UsuarioRepository;
import java.util.ArrayList;

public class DebugUsuariosRegistradosActivity extends AppCompatActivity {

    private RecyclerView rvUsuarios;
    private UsuariosAdapter adapter;
    private UsuarioRepository usuarioRepository;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.debug_usuarios_registrados_activity);

        rvUsuarios = findViewById(R.id.rvUsuarios);
        rvUsuarios.setLayoutManager(new LinearLayoutManager(this));

        usuarioRepository = new UsuarioRepository(getApplication());
        findViewById(R.id.btnVolverAdmin).setOnClickListener(v -> finish());

        // Botón para borrar solo usuarios (btnBorrarTodo según tu layout)
        findViewById(R.id.btnBorrarTodo).setOnClickListener(v -> {
            usuarioRepository.borrarTodosLosUsuarios(exito -> {
                if (exito) {
                    Toast.makeText(this, "Usuarios borrados", Toast.LENGTH_SHORT).show();
                    cargarUsuarios();
                } else {
                    Toast.makeText(this, "Error al borrar usuarios", Toast.LENGTH_SHORT).show();
                }
            });
        });

        // Botón para borrar toda la base de datos
        findViewById(R.id.btnBorrarBaseDeDatos).setOnClickListener(v -> {
            usuarioRepository.borrarTodaLaBaseDeDatos(exito -> {
                if (exito) {
                    Toast.makeText(this, "Base de datos reiniciada", Toast.LENGTH_SHORT).show();
                    cargarUsuarios();
                } else {
                    Toast.makeText(this, "Error al limpiar base de datos", Toast.LENGTH_SHORT).show();
                }
            });
        });

        cargarUsuarios();
    }

    private void cargarUsuarios() {
        usuarioRepository.obtenerTodosLosUsuarios(usuarios -> {
            adapter = new UsuariosAdapter(usuarios != null ? usuarios : new ArrayList<>());
            rvUsuarios.setAdapter(adapter);
        });
    }
}

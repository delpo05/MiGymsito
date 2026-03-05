package com.example.migymsito;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.migymsito.adapter.UsuariosAdapter;
import com.example.migymsito.dataRepository.UsuarioRepository;
import java.util.ArrayList;

public class UsuariosRegistradosActivity extends AppCompatActivity {

    private RecyclerView rvUsuarios;
    private UsuariosAdapter adapter;
    private UsuarioRepository usuarioRepository;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usuarios_registrados);

        rvUsuarios = findViewById(R.id.rvUsuarios);
        rvUsuarios.setLayoutManager(new LinearLayoutManager(this));

        usuarioRepository = new UsuarioRepository(getApplication());
        findViewById(R.id.btnVolverAdmin).setOnClickListener(v -> finish());

        // Configurar botón para borrar todos los registros
        findViewById(R.id.btnBorrarTodo).setOnClickListener(v -> {
            usuarioRepository.borrarTodosLosUsuarios(exito -> {
                if (exito) {
                    Toast.makeText(this, "Todos los registros han sido borrados", Toast.LENGTH_SHORT).show();
                    cargarUsuarios(); // Recargar lista (estará vacía)
                } else {
                    Toast.makeText(this, "Error al borrar registros", Toast.LENGTH_SHORT).show();
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
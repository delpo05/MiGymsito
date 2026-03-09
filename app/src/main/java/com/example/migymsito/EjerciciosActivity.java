package com.example.migymsito;

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.GridView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.migymsito.adapter.EjerciciosAdapter;
import com.example.migymsito.data.Ejercicio;
import com.example.migymsito.data.Seccion;
import com.example.migymsito.data.Usuario;
import com.example.migymsito.dataRepository.EjercicioRepository;

import java.util.ArrayList;

public class EjerciciosActivity extends AppCompatActivity {

    private Usuario usuarioActual;
    private Seccion seccionActual;
    private TextView tvMensajeVacio;
    private GridView gvEjercicios;
    private EjercicioRepository ejercicioRepository;
    private EjerciciosAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.secciones_ejercicios_activity);

        // Recuperar objetos enviados desde SeccionesActivity
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            usuarioActual = getIntent().getSerializableExtra("usuario", Usuario.class);
            seccionActual = getIntent().getSerializableExtra("seccion", Seccion.class);
        } else {
            usuarioActual = (Usuario) getIntent().getSerializableExtra("usuario");
            seccionActual = (Seccion) getIntent().getSerializableExtra("seccion");
        }

        // Configurar Header
        TextView tvUsername = findViewById(R.id.toolbar_username);
        if (tvUsername != null && usuarioActual != null) {
            tvUsername.setText(usuarioActual.nombreUsuario);
        }

        tvMensajeVacio = findViewById(R.id.tvMensajeVacio);
        gvEjercicios = findViewById(R.id.gvEjercicios);
        ejercicioRepository = new EjercicioRepository(getApplication());

        configurarGridView();
    }

    private void configurarGridView() {
        // Adaptador configurado sin el método onAddClick que eliminamos
        adapter = new EjerciciosAdapter(new ArrayList<>(), new EjerciciosAdapter.OnEjercicioClickListener() {
            @Override
            public void onEjercicioClick(Ejercicio ejercicio) {
                // Acción al tocar un ejercicio
            }

            @Override
            public void onOptionsClick(View view, Ejercicio ejercicio) {
                // Acción al tocar los tres puntitos
            }
        });

        gvEjercicios.setAdapter(adapter);
        cargarEjerciciosDesdeDB();
    }

    private void cargarEjerciciosDesdeDB() {
        if (seccionActual != null) {
            ejercicioRepository.obtenerEjerciciosPorSeccion(seccionActual.idSeccion, ejercicios -> {
                adapter.setEjercicios(ejercicios);
                
                // Control de visibilidad del mensaje de bienvenida
                if (ejercicios == null || ejercicios.isEmpty()) {
                    tvMensajeVacio.setVisibility(View.VISIBLE);
                } else {
                    tvMensajeVacio.setVisibility(View.GONE);
                }
            });
        }
    }
}
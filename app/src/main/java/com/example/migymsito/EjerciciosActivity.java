package com.example.migymsito;

import android.app.AlertDialog;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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
        adapter = new EjerciciosAdapter(new ArrayList<>(), new EjerciciosAdapter.OnEjercicioClickListener() {
            @Override
            public void onAddClick() {
                mostrarPopUpAnadirEjercicio();
            }

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

    private void mostrarPopUpAnadirEjercicio() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.pop_up_anadir_ejercicio_o_seleccionar_anterior, null);
        builder.setView(view);

        AlertDialog dialog = builder.create();

        LinearLayout btnPreestablecido = view.findViewById(R.id.btn_ejercicio_preestablecido);
        LinearLayout btnPersonalizado = view.findViewById(R.id.btn_ejercicio_personalizado);
        View btnCancelar = view.findViewById(R.id.btn_cancelar_ejercicio);

        if (btnPreestablecido != null) {
            btnPreestablecido.setOnClickListener(v -> {
                Toast.makeText(this, "Ejercicios Preestablecidos (Próximamente)", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            });
        }

        if (btnPersonalizado != null) {
            btnPersonalizado.setOnClickListener(v -> {
                Toast.makeText(this, "Ejercicio Personalizado (Próximamente)", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            });
        }

        if (btnCancelar != null) {
            btnCancelar.setOnClickListener(v -> dialog.dismiss());
        }

        dialog.show();
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
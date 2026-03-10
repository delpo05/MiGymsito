package com.example.migymsito;

import android.app.Dialog;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.migymsito.adapter.EjerciciosAdapter;
import com.example.migymsito.data.Ejercicio;
import com.example.migymsito.data.Seccion;
import com.example.migymsito.data.Usuario;
import com.example.migymsito.dataRepository.EjercicioRepository;

import java.util.ArrayList;

public class EjerciciosActivity extends AppCompatActivity {

    private Usuario usuarioActual;
    private Seccion seccionActual;
    private TextView tvTituloGrid;
    private GridView gvEjercicios;
    private EjercicioRepository ejercicioRepository;
    private EjerciciosAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.secciones_rutinas_activity);

        // Recuperar objetos enviados desde SeccionesActivity
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            usuarioActual = getIntent().getSerializableExtra("usuario", Usuario.class);
            seccionActual = getIntent().getSerializableExtra("seccion", Seccion.class);
        } else {
            usuarioActual = (Usuario) getIntent().getSerializableExtra("usuario");
            seccionActual = (Seccion) getIntent().getSerializableExtra("seccion");
        }

        gvEjercicios = findViewById(R.id.gvGenerico);
        tvTituloGrid = findViewById(R.id.tvTituloGrid);
        
        // Configurar Header
        TextView tvUsername = findViewById(R.id.toolbar_username);
        if (tvUsername != null && usuarioActual != null) {
            tvUsername.setText(usuarioActual.nombreUsuario);
        }

        configurarGridView();
        configurarWindowInsets(R.id.layout_contenedor_grid);
    }

    private void configurarGridView() {
        if (seccionActual != null) {
            tvTituloGrid.setText("Ejercicios de " + seccionActual.NombreSeccion);
        } else {
            tvTituloGrid.setText("Mis Ejercicios");
        }

        ejercicioRepository = new EjercicioRepository(getApplication());
        
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
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.pop_up_dos_opciones);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        }

        // Configurar Textos
        TextView tvTitulo = dialog.findViewById(R.id.tvTituloPopUp);
        TextView tvOpcionIzq = dialog.findViewById(R.id.tvTextoIzquierda);
        TextView tvOpcionDer = dialog.findViewById(R.id.tvTextoDerecha);

        tvTitulo.setText("Añadir Ejercicio");
        tvOpcionIzq.setText("Ejercicio\nPreestablecido");
        tvOpcionDer.setText("Ejercicio\nPersonalizado");

        // Configurar Clicks
        dialog.findViewById(R.id.btnCancelar).setOnClickListener(v -> dialog.dismiss());

        dialog.findViewById(R.id.btnOpcionIzquierda).setOnClickListener(v -> {
            Toast.makeText(this, "Ejercicios Preestablecidos (Próximamente)", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        dialog.findViewById(R.id.btnOpcionDerecha).setOnClickListener(v -> {
            Toast.makeText(this, "Ejercicio Personalizado (Próximamente)", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        dialog.show();
    }

    private void cargarEjerciciosDesdeDB() {
        if (seccionActual != null) {
            ejercicioRepository.obtenerEjerciciosPorSeccion(seccionActual.idSeccion, ejercicios -> {
                adapter.setEjercicios(ejercicios);
                
                // Control de visibilidad del mensaje de bienvenida si está vacío
                if (ejercicios == null || ejercicios.isEmpty()) {
                    tvTituloGrid.setText("Añade tu primer ejercicio");
                }
            });
        }
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
}

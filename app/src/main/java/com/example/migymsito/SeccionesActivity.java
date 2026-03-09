package com.example.migymsito;

import android.app.Dialog;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.migymsito.adapter.SeccionesAdapter;
import com.example.migymsito.data.Rutina;
import com.example.migymsito.data.Seccion;
import com.example.migymsito.data.Usuario;
import com.example.migymsito.dataRepository.SeccionRepository;

import java.util.ArrayList;

public class SeccionesActivity extends AppCompatActivity {

    private GridView gvSecciones;
    private Rutina rutinaActual;
    private Usuario usuarioActual;
    private SeccionRepository seccionRepository;
    private SeccionesAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.secciones_rutinas_activity);

        rutinaActual = (Rutina) getIntent().getSerializableExtra("rutina");
        usuarioActual = (Usuario) getIntent().getSerializableExtra("usuario");

        gvSecciones = findViewById(R.id.gvRutinas);
        TextView tvUsername = findViewById(R.id.toolbar_username);
        if (tvUsername != null && usuarioActual != null) {
            tvUsername.setText(usuarioActual.nombreUsuario);
        }

        configurarGridView();
        configurarWindowInsets(R.id.layout_secciones);
    }

    private void configurarGridView() {
        TextView tituloGv = findViewById(R.id.tvGvTitulo);
        tituloGv.setText("Mis Secciones");
        seccionRepository = new SeccionRepository(getApplication());
        
        adapter = new SeccionesAdapter(new ArrayList<>(), new SeccionesAdapter.OnSeccionClickListener() {
            @Override
            public void onAddClick() {
                mostrarPopUpSeccion(null);
            }

            @Override
            public void onSeccionClick(Seccion seccion) {
                // Acción futura para ejercicios
            }

            @Override
            public void onOptionsClick(View view, Seccion seccion) {
                mostrarMenuOpciones(view, seccion);
            }
        });
        
        gvSecciones.setAdapter(adapter);
        cargarSeccionesDesdeDB();
    }

    private void mostrarMenuOpciones(View view, Seccion seccion) {
        PopupMenu popup = new PopupMenu(this, view);
        popup.getMenu().add("Editar");
        popup.getMenu().add("Eliminar");
        
        popup.setOnMenuItemClickListener(item -> {
            if (item.getTitle().equals("Editar")) {
                mostrarPopUpSeccion(seccion);
            } else if (item.getTitle().equals("Eliminar")) {
                seccionRepository.eliminarSeccion(seccion);
                new Handler().postDelayed(this::cargarSeccionesDesdeDB, 200);
            }
            return true;
        });
        popup.show();
    }

    private void cargarSeccionesDesdeDB() {
        if (rutinaActual != null) {
            seccionRepository.obtenerSeccionesDeRutina(rutinaActual.IdRutina, secciones -> {
                adapter.setSecciones(secciones);
            });
        }
    }

    private void mostrarPopUpSeccion(Seccion seccionExistente) {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.secciones_rutinas_pop_up_add);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        }

        TextView tvTitulo = dialog.findViewById(R.id.tvTituloPopUp);
        EditText etNombre = dialog.findViewById(R.id.etNombreGenerico);
        Button btnCancelar = dialog.findViewById(R.id.btnCancelarGenerico);
        Button btnAceptar = dialog.findViewById(R.id.btnConfirmarGenerico);

        if (seccionExistente == null) {
            tvTitulo.setText("Crear Sección");
            btnAceptar.setText("Crear");
        } else {
            tvTitulo.setText("Editar Sección");
            etNombre.setText(seccionExistente.NombreSeccion);
            btnAceptar.setText("Guardar");
        }

        btnCancelar.setOnClickListener(v -> dialog.dismiss());

        btnAceptar.setOnClickListener(v -> {
            String nombre = etNombre.getText().toString().trim();
            if (!nombre.isEmpty()) {
                if (seccionExistente == null) {
                    Seccion nueva = new Seccion();
                    nueva.NombreSeccion = nombre;
                    nueva.IdRutinaSeccion = rutinaActual.IdRutina;
                    seccionRepository.insertarSeccion(nueva);
                } else {
                    seccionExistente.NombreSeccion = nombre;
                    seccionRepository.actualizarSeccion(seccionExistente);
                }
                dialog.dismiss();
                new Handler().postDelayed(this::cargarSeccionesDesdeDB, 300);
            }
        });
        dialog.show();
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
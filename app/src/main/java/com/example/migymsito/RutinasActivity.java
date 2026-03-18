package com.example.migymsito;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.TextView;

import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.graphics.Insets;

import com.example.migymsito.adapter.RutinasAdapter;
import com.example.migymsito.data.Rutina;
import com.example.migymsito.dataRepository.RutinaRepository;

import java.util.ArrayList;

public class RutinasActivity extends HeaderActivity {

    private GridView gvRutinas;
    private RutinaRepository rutinaRepository;
    private RutinasAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.secciones_rutinas_activity);

        // Ya no cargamos usuarioActual aquí, usamos usuarioLogueado del HeaderActivity

        gvRutinas = findViewById(R.id.gvGenerico);
        
        // El nombre en el toolbar lo maneja automáticamente el HeaderActivity en onResume
        
        // Ocultar el botón en esta pantalla ya que solo debe aparecer en Ejercicios
        View btnFinalizar = findViewById(R.id.btnFinalizarEntrenamiento);
        if (btnFinalizar != null) {
            btnFinalizar.setVisibility(View.GONE);
        }

        TextView tvUsername = findViewById(R.id.toolbar_username);

        if (tvUsername != null && usuarioActual != null) {
            tvUsername.setText(usuarioActual.NombreUsuario);
        } else if (tvUsername != null) {
            tvUsername.setText("Invitado");
        }

        configurarGridView();
        configurarWindowInsets(R.id.layout_contenedor_grid);
    }

    private void configurarGridView() {
        TextView tituloGv = findViewById(R.id.tvTituloGrid);
        if (tituloGv != null) tituloGv.setText("Mis Rutinas");

        rutinaRepository = new RutinaRepository(getApplication());
        
        adapter = new RutinasAdapter(new ArrayList<>(), new RutinasAdapter.OnRutinaClickListener() {
            @Override
            public void onAddClick() {
                mostrarPopUpRutina(null);
            }

            @Override
            public void onRutinaClick(Rutina rutina) {
                Intent intent = new Intent(RutinasActivity.this, SeccionesActivity.class);
                intent.putExtra("rutina", rutina);
                // No es necesario pasar el usuario, ya es estático en HeaderActivity
                startActivity(intent);
            }

            @Override
            public void onOptionsClick(View view, Rutina rutina) {
                mostrarMenuOpciones(view, rutina);
            }
        });
        
        gvRutinas.setAdapter(adapter);
        cargarRutinasDesdeDB();
    }

    private void mostrarMenuOpciones(View view, Rutina rutina) {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.pop_up_modificar_eliminar);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        }

        TextView tvNombre = dialog.findViewById(R.id.tvNombrePopUp);
        if (tvNombre != null) {
            tvNombre.setText(rutina.NombreRutina);
        }

        View btnEliminar = dialog.findViewById(R.id.btnEliminarPopUp);
        if (btnEliminar != null) {
            btnEliminar.setOnClickListener(v -> {
                rutinaRepository.eliminarRutina(rutina);
                dialog.dismiss();
                new Handler().postDelayed(this::cargarRutinasDesdeDB, 200);
            });
        }

        View btnModificar = dialog.findViewById(R.id.btnModificarPopUp);
        if (btnModificar != null) {
            btnModificar.setOnClickListener(v -> {
                dialog.dismiss();
                mostrarPopUpRutina(rutina);
            });
        }

        View btnCancelar = dialog.findViewById(R.id.btnCancelarPopUp);
        if (btnCancelar != null) {
            btnCancelar.setOnClickListener(v -> dialog.dismiss());
        }

        dialog.show();
    }

    private void cargarRutinasDesdeDB() {
        if (usuarioLogueado != null) {
            rutinaRepository.obtenerRutinasDeUsuario(usuarioLogueado.id, rutinas -> {
                adapter.setRutinas(rutinas);
            });
        }
    }

    private void mostrarPopUpRutina(Rutina rutinaExistente) {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.secciones_rutinas_pop_up_add);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        }

        TextView tvTitulo = dialog.findViewById(R.id.tvTituloPopUp);
        EditText etNombre = dialog.findViewById(R.id.etNombreGenerico);
        Button btnCancelar = dialog.findViewById(R.id.btnCancelarGenerico);
        Button btnAceptar = dialog.findViewById(R.id.btnConfirmarGenerico);

        if (rutinaExistente == null) {
            tvTitulo.setText("Crear Rutina");
            btnAceptar.setText("Crear");
        } else {
            tvTitulo.setText("Editar Rutina");
            etNombre.setText(rutinaExistente.NombreRutina);
            btnAceptar.setText("Guardar");
        }

        btnCancelar.setOnClickListener(v -> dialog.dismiss());

        btnAceptar.setOnClickListener(v -> {
            String nombre = etNombre.getText().toString().trim();
            if (!nombre.isEmpty()) {
                if (rutinaExistente == null) {
                    com.example.migymsito.data.Rutina nueva = new com.example.migymsito.data.Rutina();
                    nueva.NombreRutina = nombre;
                    if (usuarioLogueado != null) {
                        nueva.IdUsuarioRutina = usuarioLogueado.id;
                        rutinaRepository.insertarRutina(nueva);
                    }
                } else {
                    rutinaExistente.NombreRutina = nombre;
                    rutinaRepository.actualizarRutina(rutinaExistente);
                }
                dialog.dismiss();
                new Handler().postDelayed(this::cargarRutinasDesdeDB, 300);
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

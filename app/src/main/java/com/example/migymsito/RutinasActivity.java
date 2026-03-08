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
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.migymsito.adapter.RutinasAdapter;
import com.example.migymsito.data.Rutina;
import com.example.migymsito.data.Usuario;
import com.example.migymsito.dataRepository.RutinaRepository;

import java.util.ArrayList;

public class RutinasActivity extends AppCompatActivity {

    private GridView gvRutinas;
    private Usuario usuarioActual;
    private RutinaRepository rutinaRepository;
    private RutinasAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.secciones_rutinas_activity);

        usuarioActual = (Usuario) getIntent().getSerializableExtra("usuario");

        gvRutinas = findViewById(R.id.gvRutinas);
        TextView tvUsername = findViewById(R.id.toolbar_username);

        if (tvUsername != null && usuarioActual != null) {
            tvUsername.setText(usuarioActual.nombreUsuario);
        } else if (tvUsername != null) {
            tvUsername.setText("Invitado");
        }

        configurarGridView();
        configurarWindowInsets(R.id.layout_secciones);
    }

    private void configurarGridView() {
        TextView tituloGv = findViewById(R.id.tvGvTitulo);
        tituloGv.setText("Mis Rutinas");

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
                intent.putExtra("usuario", usuarioActual);
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
        PopupMenu popup = new PopupMenu(this, view);
        popup.getMenu().add("Editar");
        popup.getMenu().add("Eliminar");
        
        popup.setOnMenuItemClickListener(item -> {
            if (item.getTitle().equals("Editar")) {
                mostrarPopUpRutina(rutina);
            } else if (item.getTitle().equals("Eliminar")) {
                rutinaRepository.eliminarRutina(rutina);
                new Handler().postDelayed(this::cargarRutinasDesdeDB, 200);
            }
            return true;
        });
        popup.show();
    }

    private void cargarRutinasDesdeDB() {
        if (usuarioActual != null) {
            rutinaRepository.obtenerRutinasDeUsuario(usuarioActual.id, rutinas -> {
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
                    Rutina nueva = new Rutina();
                    nueva.NombreRutina = nombre;
                    nueva.IdUsuarioRutina = usuarioActual.id;
                    nueva.ColorRutina = "#FFFFFF";
                    rutinaRepository.insertarRutina(nueva);
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
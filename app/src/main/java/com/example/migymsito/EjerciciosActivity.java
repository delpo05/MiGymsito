package com.example.migymsito;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.PopupMenu;
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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            usuarioActual = getIntent().getSerializableExtra("usuario", Usuario.class);
            seccionActual = getIntent().getSerializableExtra("seccion", Seccion.class);
        } else {
            usuarioActual = (Usuario) getIntent().getSerializableExtra("usuario");
            seccionActual = (Seccion) getIntent().getSerializableExtra("seccion");
        }

        gvEjercicios = findViewById(R.id.gvGenerico);
        tvTituloGrid = findViewById(R.id.tvTituloGrid);
        
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
                // REDIRECCIÓN A CARGAR REGISTRO
                Intent intent = new Intent(EjerciciosActivity.this, CargarRegistroActivity.class);
                intent.putExtra("ejercicio", ejercicio);
                intent.putExtra("usuario", usuarioActual);
                startActivity(intent);
            }

            @Override
            public void onOptionsClick(View view, Ejercicio ejercicio) {
                mostrarMenuOpciones(view, ejercicio);
            }
        });

        gvEjercicios.setAdapter(adapter);
        cargarEjerciciosDesdeDB();
    }

    private void mostrarMenuOpciones(View view, Ejercicio ejercicio) {
        PopupMenu popup = new PopupMenu(this, view);
        popup.getMenu().add("Editar");
        popup.getMenu().add("Eliminar");

        popup.setOnMenuItemClickListener(item -> {
            if (item.getTitle().equals("Editar")) {
                mostrarPopUpCrearEjercicioPersonalizado(ejercicio);
            } else if (item.getTitle().equals("Eliminar")) {
                ejercicioRepository.eliminarEjercicio(ejercicio);
                new Handler().postDelayed(this::cargarEjerciciosDesdeDB, 200);
            }
            return true;
        });
        popup.show();
    }

    private void mostrarPopUpAnadirEjercicio() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.pop_up_dos_opciones);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        }

        TextView tvTitulo = dialog.findViewById(R.id.tvTituloPopUp);
        TextView tvOpcionIzq = dialog.findViewById(R.id.tvTextoIzquierda);
        TextView tvOpcionDer = dialog.findViewById(R.id.tvTextoDerecha);

        tvTitulo.setText("Añadir Ejercicio");
        tvOpcionIzq.setText("Ejercicio\nPreestablecido");
        tvOpcionDer.setText("Ejercicio\nPersonalizado");

        dialog.findViewById(R.id.btnCancelar).setOnClickListener(v -> dialog.dismiss());

        dialog.findViewById(R.id.btnOpcionIzquierda).setOnClickListener(v -> {
            Toast.makeText(this, "Ejercicios Preestablecidos (Próximamente)", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        dialog.findViewById(R.id.btnOpcionDerecha).setOnClickListener(v -> {
            dialog.dismiss(); // Cierra el primer pop-up
            mostrarPopUpCrearEjercicioPersonalizado(null); // Abre el de creación
        });

        dialog.show();
    }

    private void mostrarPopUpCrearEjercicioPersonalizado(Ejercicio ejercicioExistente) {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.pop_up_aniadir_ej_personalizado);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        }

        TextView tvTitulo = dialog.findViewById(R.id.tvTituloPopUpEjercicio);
        EditText etNombre = dialog.findViewById(R.id.etNombreEjercicio);
        ImageView ivImagen = dialog.findViewById(R.id.ivSeleccionarImagen);
        Button btnAceptar = dialog.findViewById(R.id.btnAceptarEjercicio);
        Button btnCancelar = dialog.findViewById(R.id.btnCancelarEjercicio);

        if (ejercicioExistente != null) {
            tvTitulo.setText("Editar ejercicio");
            etNombre.setText(ejercicioExistente.NombreEjercicio);
            btnAceptar.setText("Guardar");
        }

        // Al tocar el cuadro grande de imagen
        ivImagen.setOnClickListener(v -> {
            Toast.makeText(this, "Abriendo Galería...", Toast.LENGTH_SHORT).show();
            // Aquí irá la lógica para elegir la foto
        });

        btnCancelar.setOnClickListener(v -> dialog.dismiss());

        btnAceptar.setOnClickListener(v -> {
            String nombre = etNombre.getText().toString().trim();
            if (nombre.isEmpty()) {
                etNombre.setError("Campo obligatorio");
                return;
            }

            if (ejercicioExistente == null) {
                // Lógica para guardar en la DB (Room)
                Ejercicio nuevo = new Ejercicio();
                nuevo.NombreEjercicio = nombre;
                nuevo.idSeccionEjercicio = seccionActual.idSeccion;
                nuevo.EsCalistenico = false;
                // nuevo.ImagenEjercicio = ... (Aquí guardaremos la URI más adelante)

                ejercicioRepository.insertarEjercicio(nuevo);
                Toast.makeText(this, "Ejercicio '" + nombre + "' creado", Toast.LENGTH_SHORT).show();
            } else {
                ejercicioExistente.NombreEjercicio = nombre;
                ejercicioRepository.actualizarEjercicio(ejercicioExistente);
                Toast.makeText(this, "Ejercicio actualizado", Toast.LENGTH_SHORT).show();
            }
            
            dialog.dismiss();
            new Handler().postDelayed(this::cargarEjerciciosDesdeDB, 300);
        });

        dialog.show();
    }

    private void cargarEjerciciosDesdeDB() {
        if (seccionActual != null) {
            ejercicioRepository.obtenerEjerciciosPorSeccion(seccionActual.idSeccion, ejercicios -> {
                adapter.setEjercicios(ejercicios);
                if (ejercicios == null || ejercicios.isEmpty()) {
                    tvTituloGrid.setText("Añade tu primer ejercicio");
                } else {
                    tvTituloGrid.setText("Ejercicios de " + seccionActual.NombreSeccion);
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
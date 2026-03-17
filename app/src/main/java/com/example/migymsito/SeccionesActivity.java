package com.example.migymsito;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
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
import java.util.List;

public class SeccionesActivity extends HeaderActivity {

    private GridView gvSecciones;
    private Rutina rutinaActual;
    private Usuario usuarioActual;
    private SeccionRepository seccionRepository;
    private SeccionesAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.secciones_rutinas_activity);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            rutinaActual = getIntent().getSerializableExtra("rutina", Rutina.class);
            usuarioActual = getIntent().getSerializableExtra("usuario", Usuario.class);
        } else {
            rutinaActual = (Rutina) getIntent().getSerializableExtra("rutina");
            usuarioActual = (Usuario) getIntent().getSerializableExtra("usuario");
        }

        gvSecciones = findViewById(R.id.gvGenerico);
        TextView tvUsername = findViewById(R.id.toolbar_username);
        if (tvUsername != null && usuarioActual != null) {
            tvUsername.setText(usuarioActual.NombreUsuario);
        }

        configurarGridView();
        configurarWindowInsets(R.id.layout_contenedor_grid);
    }

    private void configurarGridView() {
        TextView tituloGv = findViewById(R.id.tvTituloGrid);
        tituloGv.setText("Mis Secciones");
        seccionRepository = new SeccionRepository(getApplication());
        
        adapter = new SeccionesAdapter(new ArrayList<>(), new SeccionesAdapter.OnSeccionClickListener() {
            @Override
            public void onAddClick() {
                mostrarPopUpAnadirSeccion();
            }

            @Override
            public void onSeccionClick(Seccion seccion) {
                Intent intent = new Intent(SeccionesActivity.this, EjerciciosActivity.class);
                intent.putExtra("seccion", seccion);
                intent.putExtra("usuario", usuarioActual);
                startActivity(intent);
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
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.pop_up_modificar_eliminar);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        }

        TextView tvNombre = dialog.findViewById(R.id.tvNombrePopUp);
        if (tvNombre != null) {
            tvNombre.setText(seccion.NombreSeccion);
        }

        View btnEliminar = dialog.findViewById(R.id.btnEliminarPopUp);
        if (btnEliminar != null) {
            btnEliminar.setOnClickListener(v -> {
                seccionRepository.eliminarSeccion(seccion);
                dialog.dismiss();
                new Handler().postDelayed(this::cargarSeccionesDesdeDB, 200);
            });
        }

        View btnModificar = dialog.findViewById(R.id.btnModificarPopUp);
        if (btnModificar != null) {
            btnModificar.setOnClickListener(v -> {
                dialog.dismiss();
                mostrarPopUpCrearSeccion(seccion, false);
            });
        }

        View btnCancelar = dialog.findViewById(R.id.btnCancelarPopUp);
        if (btnCancelar != null) {
            btnCancelar.setOnClickListener(v -> dialog.dismiss());
        }

        dialog.show();
    }

    private void cargarSeccionesDesdeDB() {
        if (rutinaActual != null) {
            seccionRepository.obtenerSeccionesDeRutina(rutinaActual.IdRutina, secciones -> {
                adapter.setSecciones(secciones);
            });
        }
    }

    private void mostrarPopUpAnadirSeccion() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.pop_up_dos_opciones);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        }

        TextView tvTitulo = dialog.findViewById(R.id.tvTituloPopUp);
        TextView tvOpcionIzq = dialog.findViewById(R.id.tvTextoIzquierda);
        TextView tvOpcionDer = dialog.findViewById(R.id.tvTextoDerecha);

        tvTitulo.setText("Añadir Sección");
        tvOpcionIzq.setText("Sección\nPrevia");
        tvOpcionDer.setText("Nueva\nSección");

        dialog.findViewById(R.id.btnCancelar).setOnClickListener(v -> dialog.dismiss());
        
        dialog.findViewById(R.id.btnOpcionDerecha).setOnClickListener(v -> {
            dialog.dismiss();
            mostrarPopUpCrearSeccion(null, false);
        });

        // Participa en SeccionesActivity para abrir el popup de secciones previas
        dialog.findViewById(R.id.btnOpcionIzquierda).setOnClickListener(v -> {
            dialog.dismiss();
            mostrarPopUpSeccionesPrevias();
        });

        dialog.show();
    }

    // Participa en SeccionesActivity para mostrar todas las secciones previas en un popup blanco estético y profesional
    private void mostrarPopUpSeccionesPrevias() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.pop_up_secciones_previas);
        
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        }

        GridView gvPopup = dialog.findViewById(R.id.gvSeccionesPrevias);
        Button btnCancelar = dialog.findViewById(R.id.btnCancelarPrevias);

        btnCancelar.setOnClickListener(v -> dialog.dismiss());

        // MODIFICACIÓN: La función obtenerTodasLasSecciones ahora utiliza el JOIN en SeccionDao para traer el nombre de la rutina
        seccionRepository.obtenerTodasLasSecciones(secciones -> {
             gvPopup.setAdapter(new BaseAdapter() {
                 @Override public int getCount() { return secciones.size(); }
                 @Override public Object getItem(int i) { return secciones.get(i); }
                 @Override public long getItemId(int i) { return i; }
                 @Override public View getView(int position, View convertView, ViewGroup parent) {
                     if (convertView == null) {
                         convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_seccion_previa, parent, false);
                     }
                     Seccion s = secciones.get(position);
                     TextView tvNombre = convertView.findViewById(R.id.tv_nombre_seccion_previa);
                     TextView tvRutina = convertView.findViewById(R.id.tv_nombre_rutina_previa);
                     View container = convertView.findViewById(R.id.container_item_previa);
                     
                     tvNombre.setText(s.NombreSeccion);
                     // CAMBIO: Se usa el campo nombreRutina obtenido del JOIN SQL
                     tvRutina.setText("Rutina: " + (s.nombreRutina != null ? s.nombreRutina : "Desconocida"));
                     
                     GradientDrawable shape = new GradientDrawable();
                     shape.setCornerRadius(15 * parent.getContext().getResources().getDisplayMetrics().density);
                     shape.setStroke(4, Color.BLACK);
                     shape.setColor(Color.WHITE);
                     container.setBackground(shape);
                     
                     convertView.setOnClickListener(v -> {
                         dialog.dismiss();
                         // Abrir popup de creación/clonación con la info de la sección seleccionada
                         mostrarPopUpCrearSeccion(s, true);
                     });
                     return convertView;
                 }
             });
        });

        dialog.show();
    }

    private void mostrarPopUpCrearSeccion(Seccion seccionBase, boolean esClonacion) {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.secciones_rutinas_pop_up_add);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        }

        TextView tvTitulo = dialog.findViewById(R.id.tvTituloPopUp);
        EditText etNombre = dialog.findViewById(R.id.etNombreGenerico);
        Button btnCancelar = dialog.findViewById(R.id.btnCancelarGenerico);
        Button btnAceptar = dialog.findViewById(R.id.btnConfirmarGenerico);

        if (seccionBase == null) {
            tvTitulo.setText("Crear Sección");
            btnAceptar.setText("Crear");
        } else if (esClonacion) {
            tvTitulo.setText("Clonar Sección");
            etNombre.setText(seccionBase.NombreSeccion);
            btnAceptar.setText("Clonar");
        } else {
            tvTitulo.setText("Editar Sección");
            etNombre.setText(seccionBase.NombreSeccion);
            btnAceptar.setText("Guardar");
        }

        btnCancelar.setOnClickListener(v -> dialog.dismiss());

        btnAceptar.setOnClickListener(v -> {
            String nombre = etNombre.getText().toString().trim();
            if (!nombre.isEmpty()) {
                if (seccionBase == null) {
                    Seccion nueva = new Seccion();
                    nueva.NombreSeccion = nombre;
                    nueva.IdRutinaSeccion = rutinaActual.IdRutina;
                    seccionRepository.insertarSeccion(nueva);
                    dialog.dismiss();
                    new Handler().postDelayed(this::cargarSeccionesDesdeDB, 300);
                } else if (esClonacion) {
                    seccionRepository.clonarSeccionConNombre(seccionBase, rutinaActual.IdRutina, nombre, result -> {
                        cargarSeccionesDesdeDB();
                    });
                    dialog.dismiss();
                } else {
                    seccionBase.NombreSeccion = nombre;
                    seccionRepository.actualizarSeccion(seccionBase);
                    dialog.dismiss();
                    new Handler().postDelayed(this::cargarSeccionesDesdeDB, 300);
                }
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

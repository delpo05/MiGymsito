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

import androidx.activity.OnBackPressedCallback;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.migymsito.adapter.SeccionesAdapter;
import com.example.migymsito.data.Rutina;
import com.example.migymsito.data.Seccion;
import com.example.migymsito.dataRepository.SeccionRepository;

import java.util.ArrayList;
import java.util.List;

public class SeccionesActivity extends HeaderActivity {

    private GridView gvSecciones;
    private Rutina rutinaActual;
    private SeccionRepository seccionRepository;
    private SeccionesAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.secciones_rutinas_activity);

        if (getIntent() != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                rutinaActual = getIntent().getSerializableExtra("rutina", Rutina.class);
            } else {
                rutinaActual = (Rutina) getIntent().getSerializableExtra("rutina");
            }
        }

        gvSecciones = findViewById(R.id.gvGenerico);

        View btnFinalizar = findViewById(R.id.btnFinalizarEntrenamiento);
        if (btnFinalizar != null) {
            btnFinalizar.setVisibility(View.GONE);
        }

        configurarGridView();
        configurarWindowInsets(R.id.layout_contenedor_grid);

        // Volver a RutinasActivity al presionar atrás
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Intent intent = new Intent(SeccionesActivity.this, RutinasActivity.class);
                intent.putExtra("cambiarRutina", true);
                startActivity(intent);
                finish();
            }
        });
    }

    private void configurarGridView() {
        TextView tituloGv = findViewById(R.id.tvTituloGrid);
        if (tituloGv != null) {
            if (rutinaActual != null) {
                tituloGv.setText("Ejercicios de " + rutinaActual.NombreRutina);
            } else {
                tituloGv.setText("Mis Secciones");
            }
        }
        
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

    @Override
    protected void onImportFinished() {
        cargarSeccionesDesdeDB();
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

        dialog.findViewById(R.id.btnOpcionIzquierda).setOnClickListener(v -> {
            dialog.dismiss();
            mostrarPopUpSeccionesPrevias();
        });

        dialog.show();
    }

    private void mostrarPopUpSeccionesPrevias() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.pop_up_listado_generico);
        
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        }

        TextView tvTitulo = dialog.findViewById(R.id.tvTituloPopUpGenerico);
        tvTitulo.setText("Elegir sección previa");

        GridView gvPopup = dialog.findViewById(R.id.gvListadoGenerico);
        Button btnCancelar = dialog.findViewById(R.id.btnCancelarGenerico);

        btnCancelar.setOnClickListener(v -> {
            dialog.dismiss();
            mostrarPopUpAnadirSeccion();
        });

        seccionRepository.obtenerTodasLasSecciones(secciones -> {
             // Filtrar: NO mostrar las secciones que vienen cargadas por sistema
             List<Seccion> filtradas = new ArrayList<>();
             for (Seccion s : secciones) {
                 if ("Personalizado".equals(s.TipoSeccion)) filtradas.add(s);
             }

             gvPopup.setAdapter(new BaseAdapter() {
                 @Override public int getCount() { return filtradas.size(); }
                 @Override public Object getItem(int i) { return filtradas.get(i); }
                 @Override public long getItemId(int i) { return i; }
                 @Override public View getView(int position, View convertView, ViewGroup parent) {
                     if (convertView == null) {
                         convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_seccion_previa, parent, false);
                     }
                     Seccion s = filtradas.get(position);
                     TextView tvNombre = convertView.findViewById(R.id.tv_nombre_seccion_previa);
                     TextView tvRutina = convertView.findViewById(R.id.tv_nombre_rutina_previa);
                     View container = convertView.findViewById(R.id.container_item_previa);
                     
                     tvNombre.setText(s.NombreSeccion);
                     tvRutina.setText(s.nombreRutina != null ? "Rutina: " + s.nombreRutina : "Sistema");
                     
                     GradientDrawable shape = new GradientDrawable();
                     shape.setCornerRadius(15 * parent.getContext().getResources().getDisplayMetrics().density);
                     shape.setStroke(4, Color.BLACK);
                     shape.setColor(Color.WHITE);
                     container.setBackground(shape);
                     
                     convertView.setOnClickListener(v -> {
                         dialog.dismiss();
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

        btnCancelar.setOnClickListener(v -> {
            dialog.dismiss();
            if (esClonacion) {
                mostrarPopUpSeccionesPrevias();
            } else if (seccionBase == null) {
                mostrarPopUpAnadirSeccion();
            }
        });

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
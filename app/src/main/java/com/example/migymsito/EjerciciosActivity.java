package com.example.migymsito;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.migymsito.adapter.EjerciciosAdapter;
import com.example.migymsito.data.Ejercicio;
import com.example.migymsito.data.Seccion;
import com.example.migymsito.data.SeccionXejercicio;
import com.example.migymsito.data.Usuario;
import com.example.migymsito.dataRepository.EjercicioRepository;
import com.example.migymsito.dataRepository.EntrenamientoRepository;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class EjerciciosActivity extends HeaderActivity {

    private Seccion seccionActual;
    private TextView tvTituloGrid;
    private GridView gvEjercicios;
    private EjercicioRepository ejercicioRepository;
    private EntrenamientoRepository entrenamientoRepository;
    private EjerciciosAdapter adapter;
    private Button btnFinalizarEntrenamiento;

    private Uri uriImagenSeleccionada;
    private ImageView ivPreviewImagen;
    private ActivityResultLauncher<String> galleryLauncher;
    private ActivityResultLauncher<Uri> cameraLauncher;
    private Uri uriFotoCamara; 

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.secciones_rutinas_activity);

        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        uriImagenSeleccionada = uri;
                        if (ivPreviewImagen != null) {
                            ivPreviewImagen.setImageURI(uri);
                            getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        }
                    }
                }
        );

        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.TakePicture(),
                success -> {
                    if (success && uriFotoCamara != null) {
                        uriImagenSeleccionada = uriFotoCamara;
                        if (ivPreviewImagen != null) {
                            ivPreviewImagen.setImageURI(uriFotoCamara);
                        }
                    }
                }
        );

        if (getIntent() != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                seccionActual = getIntent().getSerializableExtra("seccion", Seccion.class);
            } else {
                seccionActual = (Seccion) getIntent().getSerializableExtra("seccion");
            }
        }

        gvEjercicios = findViewById(R.id.gvGenerico);
        tvTituloGrid = findViewById(R.id.tvTituloGrid);
        btnFinalizarEntrenamiento = findViewById(R.id.btnFinalizarEntrenamiento);

        TextView tvUsername = findViewById(R.id.toolbar_username);
        if (tvUsername != null && usuarioActual != null) {
            tvUsername.setText(usuarioActual.NombreUsuario);
        }

        entrenamientoRepository = new EntrenamientoRepository(getApplication());

        configurarGridView();
        configurarBotonFinalizar();
        configurarWindowInsets(R.id.layout_contenedor_grid);
    }

    private void configurarBotonFinalizar() {
        if (btnFinalizarEntrenamiento != null) {
            btnFinalizarEntrenamiento.setVisibility(View.VISIBLE);
            btnFinalizarEntrenamiento.setOnClickListener(v -> {
                if (usuarioActual != null && seccionActual != null) {
                    entrenamientoRepository.finalizarEntrenamientoActivoPorSeccion(
                            usuarioActual.IdUsuario,
                            seccionActual.IdSeccion,
                            success -> {
                                if (success) {
                                    Toast.makeText(this, "Entrenamiento de " + seccionActual.NombreSeccion + " finalizado.", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(this, "No hay entrenamiento activo en esta sección.", Toast.LENGTH_SHORT).show();
                                }
                            }
                    );
                }
            });
        }
    }

    private void abrirCamara() {
        File photoFile = null;
        try {
            photoFile = crearArchivoImagen();
        } catch (IOException ex) {
            Toast.makeText(this, "Error al crear el archivo de imagen", Toast.LENGTH_SHORT).show();
        }

        if (photoFile != null) {
            uriFotoCamara = FileProvider.getUriForFile(this,
                    "com.example.migymsito.fileprovider",
                    photoFile);
            cameraLauncher.launch(uriFotoCamara);
        }
    }

    private File crearArchivoImagen() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(imageFileName, ".jpg", storageDir);
    }

    private void mostrarOpcionesImagen(View view) {
        PopupMenu popup = new PopupMenu(this, view);
        popup.getMenu().add("Cámara");
        popup.getMenu().add("Galería");

        popup.setOnMenuItemClickListener(item -> {
            if (item.getTitle().equals("Cámara")) {
                abrirCamara();
            } else if (item.getTitle().equals("Galería")) {
                galleryLauncher.launch("image/*");
            }
            return true;
        });
        popup.show();
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
                Intent intent = new Intent(EjerciciosActivity.this, CargarRegistroActivity.class);
                intent.putExtra("ejercicio", ejercicio);
                // No es necesario pasar el usuario, ya es estático en HeaderActivity
                intent.putExtra("usuario", usuarioActual);
                intent.putExtra("seccion", seccionActual);
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
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.pop_up_modificar_eliminar);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        }

        TextView tvNombre = dialog.findViewById(R.id.tvNombrePopUp);
        if (tvNombre != null) {
            tvNombre.setText(ejercicio.NombreEjercicio);
        }

        View btnEliminar = dialog.findViewById(R.id.btnEliminarPopUp);
        if (btnEliminar != null) {
            btnEliminar.setOnClickListener(v -> {
                // CAMBIO: Solo eliminamos la relación con esta sección
                ejercicioRepository.eliminarEjercicioDeSeccion(ejercicio.IdEjercicio, seccionActual.IdSeccion);
                dialog.dismiss();
                new Handler().postDelayed(this::cargarEjerciciosDesdeDB, 200);
            });
        }

        View btnModificar = dialog.findViewById(R.id.btnModificarPopUp);
        if (btnModificar != null) {
            btnModificar.setOnClickListener(v -> {
                dialog.dismiss();
                mostrarPopUpCrearEjercicioPersonalizado(ejercicio);
            });
        }

        View btnCancelar = dialog.findViewById(R.id.btnCancelarPopUp);
        if (btnCancelar != null) {
            btnCancelar.setOnClickListener(v -> dialog.dismiss());
        }

        dialog.show();
    }

    // Muestra el popup inicial para elegir entre ejercicio preestablecido o personalizado
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

        // Al hacer clic en Preestablecido, abre el clon del popup de secciones previas
        dialog.findViewById(R.id.btnOpcionIzquierda).setOnClickListener(v -> {
            dialog.dismiss();
            mostrarPopUpEjerciciosPreestablecidos();
        });

        dialog.findViewById(R.id.btnOpcionDerecha).setOnClickListener(v -> {
            dialog.dismiss();
            mostrarPopUpCrearEjercicioPersonalizado(null);
        });

        dialog.show();
    }

    // NUEVA FUNCIÓN: Muestra un GridView con todos los ejercicios de la DB para clonar su relación
    private void mostrarPopUpEjerciciosPreestablecidos() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.pop_up_ejercicios_preestablecidos);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        }

        GridView gvPopup = dialog.findViewById(R.id.gvEjerciciosPreestablecidos);
        Button btnCancelar = dialog.findViewById(R.id.btnCancelarEjercicios);

        btnCancelar.setOnClickListener(v -> dialog.dismiss());

        // Obtiene todos los ejercicios existentes a través del repositorio
        ejercicioRepository.obtenerTodosLosEjercicios(ejercicios -> {
            gvPopup.setAdapter(new BaseAdapter() {
                @Override public int getCount() { return ejercicios.size(); }
                @Override public Object getItem(int i) { return ejercicios.get(i); }
                @Override public long getItemId(int i) { return i; }
                @Override public View getView(int position, View convertView, ViewGroup parent) {
                    if (convertView == null) {
                        convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ejercicio_previo, parent, false);
                    }
                    Ejercicio e = ejercicios.get(position);
                    TextView tvNombre = convertView.findViewById(R.id.tv_nombre_ejercicio_previo);
                    TextView tvTipo = convertView.findViewById(R.id.tv_tipo_ejercicio_previo);
                    ImageView ivImagen = convertView.findViewById(R.id.iv_ejercicio_previo);
                    View container = convertView.findViewById(R.id.container_item_ejercicio_previo);

                    tvNombre.setText(e.NombreEjercicio);
                    /// Definir si muestra el tipo de ejercicio o de si muesta si es peso corporal o no
                    tvTipo.setText("Tipo: " + e.TipoEjercicio);

                    if (e.ImagenEjercicio != null && !e.ImagenEjercicio.isEmpty()) {
                        ivImagen.setVisibility(View.VISIBLE);
                        ivImagen.setImageURI(Uri.parse(e.ImagenEjercicio));
                    } else {
                        ivImagen.setVisibility(View.GONE);
                    }

                    GradientDrawable shape = new GradientDrawable();
                    shape.setCornerRadius(15 * parent.getContext().getResources().getDisplayMetrics().density);
                    shape.setStroke(4, Color.BLACK);
                    shape.setColor(Color.WHITE);
                    container.setBackground(shape);

                    convertView.setOnClickListener(v -> {
                        // Al seleccionar un ejercicio, se registra la relación en SeccionXejercicio
                        ejercicioRepository.insertarRelacionSeccionEjercicio(e.IdEjercicio, seccionActual.IdSeccion);
                        dialog.dismiss();
                        new Handler().postDelayed(() -> cargarEjerciciosDesdeDB(), 300);
                    });
                    return convertView;
                }
            });
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
        ivPreviewImagen = dialog.findViewById(R.id.ivSeleccionarImagen);
        Button btnAceptar = dialog.findViewById(R.id.btnAceptarEjercicio);
        Button btnCancelar = dialog.findViewById(R.id.btnCancelarEjercicio);

        uriImagenSeleccionada = null; 

        if (ejercicioExistente != null) {
            tvTitulo.setText("Editar ejercicio");
            etNombre.setText(ejercicioExistente.NombreEjercicio);
            btnAceptar.setText("Guardar");
            if (ejercicioExistente.ImagenEjercicio != null) {
                uriImagenSeleccionada = Uri.parse(ejercicioExistente.ImagenEjercicio);
                ivPreviewImagen.setImageURI(uriImagenSeleccionada);
            }
        }

        ivPreviewImagen.setOnClickListener(v -> mostrarOpcionesImagen(v));

        btnCancelar.setOnClickListener(v -> dialog.dismiss());

        btnAceptar.setOnClickListener(v -> {
            String nombre = etNombre.getText().toString().trim();
            if (nombre.isEmpty()) {
                etNombre.setError("Campo obligatorio");
                return;
            }

            if (ejercicioExistente == null) {
                Ejercicio nuevo = new Ejercicio();
                nuevo.NombreEjercicio = nombre;
                nuevo.TipoEjercicio = "Personalizado";
                nuevo.PesoCorporalEjercicio = false;
                if (uriImagenSeleccionada != null) {
                    nuevo.ImagenEjercicio = uriImagenSeleccionada.toString();
                }

                ejercicioRepository.insertarEjercicioConSeccion(nuevo, seccionActual.IdSeccion);
            } else {
                // MODIFICACIÓN: Usamos actualizarEjercicioIndependiente para no afectar a otras rutinas que usen este ejercicio
                Ejercicio editado = new Ejercicio();
                editado.IdEjercicio = ejercicioExistente.IdEjercicio;
                editado.NombreEjercicio = nombre;
                editado.ImagenEjercicio = (uriImagenSeleccionada != null) ? uriImagenSeleccionada.toString() : ejercicioExistente.ImagenEjercicio;
                editado.TipoEjercicio = ejercicioExistente.TipoEjercicio;
                editado.PesoCorporalEjercicio = ejercicioExistente.PesoCorporalEjercicio;

                ejercicioRepository.actualizarEjercicioIndependiente(editado, seccionActual.IdSeccion);
            }
            
            dialog.dismiss();
            new Handler().postDelayed(this::cargarEjerciciosDesdeDB, 300);
        });

        dialog.show();
    }

    private void cargarEjerciciosDesdeDB() {
        if (seccionActual != null) {
            ejercicioRepository.obtenerEjerciciosPorSeccion(seccionActual.IdSeccion, ejercicios -> {
                adapter.setEjercicios(ejercicios);
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

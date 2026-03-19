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
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
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
import com.example.migymsito.data.Usuario;
import com.example.migymsito.dataRepository.EjercicioRepository;
import com.example.migymsito.dataRepository.SeccionRepository;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class EjerciciosActivity extends HeaderActivity {

    private Seccion seccionActual;
    private TextView tvTituloGrid;
    private GridView gvEjercicios;
    private EjercicioRepository ejercicioRepository;
    private SeccionRepository seccionRepository;
    private EjerciciosAdapter adapter;

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

        configurarGridView();
        configurarWindowInsets(R.id.layout_contenedor_grid);
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
            if ("Cámara".equals(item.getTitle())) {
                abrirCamara();
            } else if ("Galería".equals(item.getTitle())) {
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
        seccionRepository = new SeccionRepository(getApplication());

        adapter = new EjerciciosAdapter(new ArrayList<>(), new EjerciciosAdapter.OnEjercicioClickListener() {
            @Override
            public void onAddClick() {
                mostrarPopUpAnadirEjercicio();
            }

            @Override
            public void onEjercicioClick(Ejercicio ejercicio) {
                Intent intent = new Intent(EjerciciosActivity.this, CargarRegistroActivity.class);
                intent.putExtra("ejercicio", ejercicio);
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
                ejercicioRepository.eliminarEjercicioDeSeccion(ejercicio.IdEjercicio, seccionActual.IdSeccion);
                dialog.dismiss();
                new Handler(Looper.getMainLooper()).postDelayed(this::cargarEjerciciosDesdeDB, 200);
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
        tvOpcionIzq.setText("Ejercicios\nCreados");
        tvOpcionDer.setText("Nuevo\nEjercicio");

        dialog.findViewById(R.id.btnCancelar).setOnClickListener(v -> dialog.dismiss());

        dialog.findViewById(R.id.btnOpcionIzquierda).setOnClickListener(v -> {
            dialog.dismiss();
            mostrarPopUpEleccionTipoEjercicio();
        });

        dialog.findViewById(R.id.btnOpcionDerecha).setOnClickListener(v -> {
            dialog.dismiss();
            mostrarPopUpCrearEjercicioPersonalizado(null);
        });

        dialog.show();
    }

    private void mostrarPopUpEleccionTipoEjercicio() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.pop_up_dos_opciones);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        }

        TextView tvTitulo = dialog.findViewById(R.id.tvTituloPopUp);
        TextView tvOpcionIzq = dialog.findViewById(R.id.tvTextoIzquierda);
        TextView tvOpcionDer = dialog.findViewById(R.id.tvTextoDerecha);

        tvTitulo.setText("Tipo de Ejercicio");
        tvOpcionIzq.setText("Ejercicios\nPrestablecidos");
        tvOpcionDer.setText("Ejercicios\nPersonalizados");

        dialog.findViewById(R.id.btnCancelar).setOnClickListener(v -> {
            dialog.dismiss();
            mostrarPopUpAnadirEjercicio();
        });

        dialog.findViewById(R.id.btnOpcionIzquierda).setOnClickListener(v -> {
            dialog.dismiss();
            mostrarPopUpSeccionesParaSeleccion("Preestablecido");
        });

        dialog.findViewById(R.id.btnOpcionDerecha).setOnClickListener(v -> {
            dialog.dismiss();
            mostrarPopUpSeccionesParaSeleccion("Personalizado");
        });

        dialog.show();
    }

    private void mostrarPopUpSeccionesParaSeleccion(String tipo) {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.pop_up_secciones_previas);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        }

        TextView tvTitulo = dialog.findViewById(R.id.tvTituloPopUpPrevias);
        if (tvTitulo != null) {
            tvTitulo.setText(tipo.equals("Preestablecido") ? "Secciones Prestablecidas" : "Secciones Personalizadas");
        }

        GridView gvPopup = dialog.findViewById(R.id.gvSeccionesPrevias);
        Button btnCancelar = dialog.findViewById(R.id.btnCancelarPrevias);

        btnCancelar.setOnClickListener(v -> {
            dialog.dismiss();
            mostrarPopUpEleccionTipoEjercicio();
        });

        SeccionRepository.RepositoryCallback<List<Seccion>> callback = secciones -> {
            List<Seccion> listaAMostrar = new ArrayList<>();
            for (Seccion s : secciones) {
                if (seccionActual != null && s.IdSeccion == seccionActual.IdSeccion) {
                    continue;
                }
                listaAMostrar.add(s);
            }

            gvPopup.setAdapter(new BaseAdapter() {
                @Override public int getCount() { return listaAMostrar.size(); }
                @Override public Object getItem(int i) { return listaAMostrar.get(i); }
                @Override public long getItemId(int i) { return i; }
                @Override public View getView(int position, View convertView, ViewGroup parent) {
                    if (convertView == null) {
                        convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_seccion_previa, parent, false);
                    }
                    Seccion s = listaAMostrar.get(position);
                    TextView tvNombre = convertView.findViewById(R.id.tv_nombre_seccion_previa);
                    TextView tvDetalle = convertView.findViewById(R.id.tv_nombre_rutina_previa);
                    View container = convertView.findViewById(R.id.container_item_previa);

                    tvNombre.setText(s.NombreSeccion);
                    tvDetalle.setText(s.TipoSeccion);

                    GradientDrawable shape = new GradientDrawable();
                    shape.setCornerRadius(15 * parent.getContext().getResources().getDisplayMetrics().density);
                    shape.setStroke(4, Color.BLACK);
                    shape.setColor(Color.WHITE);
                    container.setBackground(shape);

                    convertView.setOnClickListener(v -> {
                        dialog.dismiss();
                        mostrarPopUpEjerciciosDeSeccionSeleccionada(s);
                    });
                    return convertView;
                }
            });
        };

        if (tipo.equals("Preestablecido")) {
            seccionRepository.obtenerSeccionesPreestablecidas(callback);
        } else {
            seccionRepository.obtenerSeccionesPersonalizadas(callback);
        }

        dialog.show();
    }

    private void mostrarPopUpEjerciciosDeSeccionSeleccionada(Seccion seccionSeleccionada) {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.pop_up_ejercicios_preestablecidos);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        }

        TextView tvTitulo = dialog.findViewById(R.id.tvTituloPopUpEjercicios);
        if (tvTitulo != null) {
            tvTitulo.setText("Ejercicios de " + seccionSeleccionada.NombreSeccion);
        }

        GridView gvPopup = dialog.findViewById(R.id.gvEjerciciosPreestablecidos);
        Button btnCancelar = dialog.findViewById(R.id.btnCancelarEjercicios);

        btnCancelar.setOnClickListener(v -> {
            dialog.dismiss();
            mostrarPopUpSeccionesParaSeleccion(seccionSeleccionada.TipoSeccion);
        });

        ejercicioRepository.obtenerEjerciciosPorSeccion(seccionSeleccionada.IdSeccion, ejercicios -> {
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
                        ejercicioRepository.insertarRelacionSeccionEjercicio(e.IdEjercicio, seccionActual.IdSeccion);
                        dialog.dismiss();
                        new Handler(Looper.getMainLooper()).postDelayed(() -> cargarEjerciciosDesdeDB(), 300);
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
        CheckBox cbPesoCorporal = dialog.findViewById(R.id.cbPesoCorporal);
        ivPreviewImagen = dialog.findViewById(R.id.ivSeleccionarImagen);
        Button btnAceptar = dialog.findViewById(R.id.btnAceptarEjercicio);
        Button btnCancelar = dialog.findViewById(R.id.btnCancelarEjercicio);

        uriImagenSeleccionada = null; 

        if (ejercicioExistente != null) {
            tvTitulo.setText("Editar ejercicio");
            etNombre.setText(ejercicioExistente.NombreEjercicio);
            cbPesoCorporal.setChecked(ejercicioExistente.PesoCorporalEjercicio != null && ejercicioExistente.PesoCorporalEjercicio);
            btnAceptar.setText("Guardar");
            if (ejercicioExistente.ImagenEjercicio != null) {
                uriImagenSeleccionada = Uri.parse(ejercicioExistente.ImagenEjercicio);
                ivPreviewImagen.setImageURI(uriImagenSeleccionada);
            }
        }

        ivPreviewImagen.setOnClickListener(this::mostrarOpcionesImagen);

        btnCancelar.setOnClickListener(v -> {
            dialog.dismiss();
            if (ejercicioExistente == null) {
                mostrarPopUpAnadirEjercicio();
            }
        });

        btnAceptar.setOnClickListener(v -> {
            String nombre = etNombre.getText().toString().trim();
            if (nombre.isEmpty()) {
                etNombre.setError("Campo obligatorio");
                return;
            }

            boolean esPesoCorporal = cbPesoCorporal.isChecked();

            if (ejercicioExistente == null) {
                Ejercicio nuevo = new Ejercicio();
                nuevo.NombreEjercicio = nombre;
                nuevo.TipoEjercicio = "Personalizado";
                nuevo.PesoCorporalEjercicio = esPesoCorporal;
                if (uriImagenSeleccionada != null) {
                    nuevo.ImagenEjercicio = uriImagenSeleccionada.toString();
                }

                ejercicioRepository.insertarEjercicioConSeccion(nuevo, seccionActual.IdSeccion, (Boolean success) -> {
                    if (success != null && success) {
                        dialog.dismiss();
                        new Handler(Looper.getMainLooper()).postDelayed(this::cargarEjerciciosDesdeDB, 300);
                    }
                });
            } else {
                Ejercicio editado = new Ejercicio();
                editado.IdEjercicio = ejercicioExistente.IdEjercicio; 
                editado.NombreEjercicio = nombre;
                editado.ImagenEjercicio = (uriImagenSeleccionada != null) ? uriImagenSeleccionada.toString() : ejercicioExistente.ImagenEjercicio;
                editado.TipoEjercicio = "Personalizado";
                editado.PesoCorporalEjercicio = esPesoCorporal;

                ejercicioRepository.actualizarEjercicioIndependiente(editado, seccionActual.IdSeccion, (Boolean success) -> {
                    if (success != null && success) {
                        dialog.dismiss();
                        new Handler(Looper.getMainLooper()).postDelayed(this::cargarEjerciciosDesdeDB, 300);
                    }
                });
            }
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
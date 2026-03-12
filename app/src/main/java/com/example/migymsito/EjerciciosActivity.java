package com.example.migymsito;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.migymsito.adapter.EjerciciosAdapter;
import com.example.migymsito.data.Ejercicio;
import com.example.migymsito.data.Seccion;
import com.example.migymsito.data.Usuario;
import com.example.migymsito.dataRepository.EjercicioRepository;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class EjerciciosActivity extends HeaderActivity {

    private Usuario usuarioActual;
    private Seccion seccionActual;
    private TextView tvTituloGrid;
    private GridView gvEjercicios;
    private EjercicioRepository ejercicioRepository;
    private EjerciciosAdapter adapter;

    private Uri uriImagenSeleccionada;
    private ImageView ivPreviewImagen;
    private ActivityResultLauncher<String> galleryLauncher;
    private ActivityResultLauncher<Uri> cameraLauncher;
    private Uri uriFotoCamara; // Para guardar la ruta de la foto sacada con la cámara

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.secciones_rutinas_activity);

        // Launcher para Galería
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

        // Launcher para Cámara
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
            dialog.dismiss();
            mostrarPopUpCrearEjercicioPersonalizado(null);
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

        // Ahora muestra opciones: Cámara o Galería
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
                nuevo.EsCalistenico = false;
                if (uriImagenSeleccionada != null) {
                    nuevo.ImagenEjercicio = uriImagenSeleccionada.toString();
                }

                // He modificado esta llamada para usar el nuevo método que guarda tanto el ejercicio como su relación
                ejercicioRepository.insertarEjercicioEnSeccion(nuevo, seccionActual.idSeccion);
                Toast.makeText(this, "Ejercicio '" + nombre + "' creado", Toast.LENGTH_SHORT).show();
            } else {
                ejercicioExistente.NombreEjercicio = nombre;
                if (uriImagenSeleccionada != null) {
                    ejercicioExistente.ImagenEjercicio = uriImagenSeleccionada.toString();
                }
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

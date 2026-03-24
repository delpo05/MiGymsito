package com.example.migymsito;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
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
import com.example.migymsito.dataRepository.EjercicioRepository;
import com.example.migymsito.dataRepository.EntrenamientoRepository;
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

        configurarGridView();
        configurarBotonFinalizar();
        configurarWindowInsets(R.id.layout_contenedor_grid);
        
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
    }

    private void configurarBotonFinalizar() {
        if (btnFinalizarEntrenamiento == null || seccionActual == null || usuarioLogueado == null) {
            if (btnFinalizarEntrenamiento != null) btnFinalizarEntrenamiento.setVisibility(View.GONE);
            return;
        }

        entrenamientoRepository.obtenerEntrenamientoActivoPorSeccion(usuarioLogueado.IdUsuario, seccionActual.IdSeccion, entrenamiento -> {
            if (entrenamiento != null) {
                btnFinalizarEntrenamiento.setVisibility(View.VISIBLE);
                btnFinalizarEntrenamiento.setEnabled(true);
            } else {
                btnFinalizarEntrenamiento.setVisibility(View.GONE);
            }
        });

        btnFinalizarEntrenamiento.setOnClickListener(v -> {
            MediaPlayer mp = MediaPlayer.create(this, R.raw.sonido3);
            if (mp != null) {
                mp.start();
                mp.setOnCompletionListener(MediaPlayer::release); 
            }

            btnFinalizarEntrenamiento.setEnabled(false);

            entrenamientoRepository.finalizarEntrenamientoActivoPorSeccion(
                    usuarioLogueado.IdUsuario, seccionActual.IdSeccion, success -> {
                        if (success != null && success) {
                            Toast.makeText(this, "¡Entrenamiento finalizado!", Toast.LENGTH_SHORT).show();
                            
                            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                                Intent intent = new Intent(this, RutinasActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                finish();
                            }, 500);
                        } else {
                            Toast.makeText(this, "Error al finalizar", Toast.LENGTH_SHORT).show();
                            btnFinalizarEntrenamiento.setEnabled(true);
                        }
                    });
        });
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
            if (ejercicioExistente == null) mostrarPopUpAnadirEjercicio();
        });

        btnAceptar.setOnClickListener(v -> {
            String nombre = etNombre.getText().toString().trim();
            if (nombre.isEmpty()) {
                etNombre.setError("Requerido");
                return;
            }

            if (ejercicioExistente == null) {
                Ejercicio nuevo = new Ejercicio();
                nuevo.NombreEjercicio = nombre;
                nuevo.TipoEjercicio = "Personalizado";
                nuevo.PesoCorporalEjercicio = cbPesoCorporal.isChecked();
                if (uriImagenSeleccionada != null) nuevo.ImagenEjercicio = uriImagenSeleccionada.toString();

                ejercicioRepository.insertarEjercicioConSeccion(nuevo, seccionActual.IdSeccion, success -> {
                    dialog.dismiss();
                    new Handler(Looper.getMainLooper()).postDelayed(this::cargarEjerciciosDesdeDB, 300);
                });
            } else {
                Ejercicio editado = new Ejercicio();
                editado.IdEjercicio = ejercicioExistente.IdEjercicio; 
                editado.NombreEjercicio = nombre;
                editado.ImagenEjercicio = (uriImagenSeleccionada != null) ? uriImagenSeleccionada.toString() : ejercicioExistente.ImagenEjercicio;
                editado.TipoEjercicio = "Personalizado";
                editado.PesoCorporalEjercicio = cbPesoCorporal.isChecked();

                ejercicioRepository.actualizarEjercicioIndependiente(editado, seccionActual.IdSeccion, success -> {
                    dialog.dismiss();
                    new Handler(Looper.getMainLooper()).postDelayed(this::cargarEjerciciosDesdeDB, 300);
                });
            }
        });
        dialog.show();
    }

    private void configurarGridView() {
        if (seccionActual != null) tvTituloGrid.setText("Ejercicios de " + seccionActual.NombreSeccion);
        ejercicioRepository = new EjercicioRepository(getApplication());
        seccionRepository = new SeccionRepository(getApplication());
        entrenamientoRepository = new EntrenamientoRepository(getApplication());
        adapter = new EjerciciosAdapter(new ArrayList<>(), new EjerciciosAdapter.OnEjercicioClickListener() {
            @Override public void onAddClick() { mostrarPopUpAnadirEjercicio(); }
            @Override public void onEjercicioClick(Ejercicio ej) {
                Intent i = new Intent(EjerciciosActivity.this, CargarRegistroActivity.class);
                i.putExtra("ejercicio", ej); i.putExtra("seccion", seccionActual);
                startActivity(i);
            }
            @Override public void onOptionsClick(View v, Ejercicio ej) { mostrarMenuOpciones(v, ej); }
        });
        gvEjercicios.setAdapter(adapter);
        cargarEjerciciosDesdeDB();
    }

    private void cargarEjerciciosDesdeDB() {
        if (seccionActual != null) {
            ejercicioRepository.obtenerEjerciciosPorSeccion(seccionActual.IdSeccion, ejercicios -> adapter.setEjercicios(ejercicios));
        }
    }

    private void abrirCamara() {
        File photoFile = null;
        try { photoFile = crearArchivoImagen(); } catch (IOException ex) {}
        if (photoFile != null) {
            uriFotoCamara = FileProvider.getUriForFile(this, "com.example.migymsito.fileprovider", photoFile);
            cameraLauncher.launch(uriFotoCamara);
        }
    }

    private File crearArchivoImagen() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile("JPEG_" + timeStamp + "_", ".jpg", storageDir);
    }

    private void mostrarOpcionesImagen(View view) {
        PopupMenu popup = new PopupMenu(this, view);
        popup.getMenu().add("Cámara"); popup.getMenu().add("Galería");
        popup.setOnMenuItemClickListener(item -> {
            if ("Cámara".equals(item.getTitle())) abrirCamara();
            else galleryLauncher.launch("image/*");
            return true;
        });
        popup.show();
    }

    private void mostrarMenuOpciones(View view, Ejercicio ejercicio) {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.pop_up_modificar_eliminar);
        if (dialog.getWindow() != null) dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        TextView tv = dialog.findViewById(R.id.tvNombrePopUp);
        if (tv != null) tv.setText(ejercicio.NombreEjercicio);
        dialog.findViewById(R.id.btnEliminarPopUp).setOnClickListener(v -> {
            ejercicioRepository.eliminarEjercicioDeSeccion(ejercicio.IdEjercicio, seccionActual.IdSeccion);
            dialog.dismiss();
            new Handler(Looper.getMainLooper()).postDelayed(this::cargarEjerciciosDesdeDB, 200);
        });
        dialog.findViewById(R.id.btnModificarPopUp).setOnClickListener(v -> {
            dialog.dismiss();
            mostrarPopUpCrearEjercicioPersonalizado(ejercicio);
        });
        dialog.findViewById(R.id.btnCancelarPopUp).setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void mostrarPopUpAnadirEjercicio() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.pop_up_dos_opciones);
        if (dialog.getWindow() != null) dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        TextView tvTitulo = dialog.findViewById(R.id.tvTituloPopUp);
        TextView tvOpcionIzq = dialog.findViewById(R.id.tvTextoIzquierda);
        TextView tvOpcionDer = dialog.findViewById(R.id.tvTextoDerecha);

        tvTitulo.setText("Añadir Ejercicio");
        if (tvOpcionIzq != null) tvOpcionIzq.setText("Elegir\nExistente");
        if (tvOpcionDer != null) tvOpcionDer.setText("Crear\nNuevo");

        dialog.findViewById(R.id.btnOpcionIzquierda).setOnClickListener(v -> {
            dialog.dismiss();
            mostrarPopUpEleccionTipoEjercicio();
        });

        dialog.findViewById(R.id.btnOpcionDerecha).setOnClickListener(v -> {
            dialog.dismiss();
            mostrarPopUpCrearEjercicioPersonalizado(null);
        });

        dialog.findViewById(R.id.btnCancelar).setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void mostrarPopUpEleccionTipoEjercicio() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.pop_up_dos_opciones);
        if (dialog.getWindow() != null) dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        TextView tvTitulo = dialog.findViewById(R.id.tvTituloPopUp);
        TextView tvOpcionIzq = dialog.findViewById(R.id.tvTextoIzquierda);
        TextView tvOpcionDer = dialog.findViewById(R.id.tvTextoDerecha);

        tvTitulo.setText("Tipo de Ejercicio");
        if (tvOpcionIzq != null) tvOpcionIzq.setText("Preestablecido");
        if (tvOpcionDer != null) tvOpcionDer.setText("Personalizado");

        dialog.findViewById(R.id.btnOpcionIzquierda).setOnClickListener(v -> {
            dialog.dismiss();
            mostrarPopUpSeccionesParaSeleccion("Preestablecido");
        });

        dialog.findViewById(R.id.btnOpcionDerecha).setOnClickListener(v -> {
            dialog.dismiss();
            mostrarPopUpSeccionesParaSeleccion("Personalizado");
        });

        dialog.findViewById(R.id.btnCancelar).setOnClickListener(v -> {
            dialog.dismiss();
            mostrarPopUpAnadirEjercicio();
        });
        dialog.show();
    }

    private void mostrarPopUpSeccionesParaSeleccion(String tipo) {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.pop_up_secciones_previas);
        if (dialog.getWindow() != null) dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        GridView gvPopup = dialog.findViewById(R.id.gvSeccionesPrevias);
        SeccionRepository.RepositoryCallback<List<Seccion>> callback = secciones -> {
            List<Seccion> lista = new ArrayList<>();
            for (Seccion s : secciones) if (seccionActual == null || s.IdSeccion != seccionActual.IdSeccion) lista.add(s);
            gvPopup.setAdapter(new BaseAdapter() {
                @Override public int getCount() { return lista.size(); }
                @Override public Object getItem(int i) { return lista.get(i); }
                @Override public long getItemId(int i) { return i; }
                @Override public View getView(int pos, View v, ViewGroup p) {
                    if (v == null) v = LayoutInflater.from(p.getContext()).inflate(R.layout.item_seccion_previa, p, false);
                    Seccion s = lista.get(pos);
                    ((TextView)v.findViewById(R.id.tv_nombre_seccion_previa)).setText(s.NombreSeccion);
                    v.setOnClickListener(view -> { dialog.dismiss(); mostrarPopUpEjerciciosDeSeccionSeleccionada(s); });
                    return v;
                }
            });
        };
        if (tipo.equals("Preestablecido")) seccionRepository.obtenerSeccionesPreestablecidas(callback);
        else seccionRepository.obtenerSeccionesPersonalizadas(callback);
        dialog.findViewById(R.id.btnCancelarPrevias).setOnClickListener(v -> { dialog.dismiss(); mostrarPopUpEleccionTipoEjercicio(); });
        dialog.show();
    }

    private void mostrarPopUpEjerciciosDeSeccionSeleccionada(Seccion seccionSel) {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.pop_up_ejercicios_preestablecidos);
        if (dialog.getWindow() != null) dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        GridView gv = dialog.findViewById(R.id.gvEjerciciosPreestablecidos);
        ejercicioRepository.obtenerEjerciciosPorSeccion(seccionSel.IdSeccion, ejercicios -> {
            gv.setAdapter(new BaseAdapter() {
                @Override public int getCount() { return ejercicios.size(); }
                @Override public Object getItem(int i) { return ejercicios.get(i); }
                @Override public long getItemId(int i) { return i; }
                @Override public View getView(int pos, View v, ViewGroup p) {
                    if (v == null) v = LayoutInflater.from(p.getContext()).inflate(R.layout.item_ejercicio_previo, p, false);
                    Ejercicio e = ejercicios.get(pos);
                    ((TextView)v.findViewById(R.id.tv_nombre_ejercicio_previo)).setText(e.NombreEjercicio);
                    v.setOnClickListener(view -> {
                        ejercicioRepository.insertarRelacionSeccionEjercicio(e.IdEjercicio, seccionActual.IdSeccion);
                        dialog.dismiss();
                        new Handler(Looper.getMainLooper()).postDelayed(() -> cargarEjerciciosDesdeDB(), 300);
                    });
                    return v;
                }
            });
        });
        dialog.findViewById(R.id.btnCancelarEjercicios).setOnClickListener(v -> { dialog.dismiss(); mostrarPopUpSeccionesParaSeleccion(seccionSel.TipoSeccion); });
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

    @Override protected void onResume() { super.onResume(); configurarBotonFinalizar(); }
}

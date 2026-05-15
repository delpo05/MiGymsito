package com.example.migymsito;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
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
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.migymsito.adapter.EjerciciosAdapter;
import com.example.migymsito.data.Ejercicio;
import com.example.migymsito.data.Entrenamiento;
import com.example.migymsito.data.Seccion;
import com.example.migymsito.dataDataBase.AppDatabase;
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

public class EjerciciosFragment extends Fragment {

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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.secciones_rutinas_activity, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        if (getActivity() != null) {
            View toolbarInclude = getActivity().findViewById(R.id.include_toolbar);
            if (toolbarInclude != null) toolbarInclude.setVisibility(View.VISIBLE);
        }

        if (getArguments() != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                seccionActual = getArguments().getSerializable("seccion", Seccion.class);
            } else {
                seccionActual = (Seccion) getArguments().getSerializable("seccion");
            }
        }

        gvEjercicios = view.findViewById(R.id.gvGenerico);
        tvTituloGrid = view.findViewById(R.id.tvTituloGrid);
        btnFinalizarEntrenamiento = view.findViewById(R.id.btnFinalizarEntrenamiento);

        if (gvEjercicios != null) {
            gvEjercicios.setNumColumns(2);
        }

        configurarGridView(view);
        configurarBotonFinalizar();
        
        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        uriImagenSeleccionada = uri;
                        if (ivPreviewImagen != null) {
                            ivPreviewImagen.setImageURI(uri);
                            if (getActivity() != null) {
                                getActivity().getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            }
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
        if (btnFinalizarEntrenamiento == null || seccionActual == null || MainActivity.usuarioLogueado == null) {
            if (btnFinalizarEntrenamiento != null) btnFinalizarEntrenamiento.setVisibility(View.GONE);
            return;
        }

        entrenamientoRepository.obtenerEntrenamientoActivoPorSeccion(MainActivity.usuarioLogueado.IdUsuario, seccionActual.IdSeccion, entrenamiento -> {
            if (entrenamiento != null) {
                btnFinalizarEntrenamiento.setVisibility(View.VISIBLE);
                btnFinalizarEntrenamiento.setEnabled(true);
            } else {
                btnFinalizarEntrenamiento.setVisibility(View.GONE);
            }
        });

        btnFinalizarEntrenamiento.setOnClickListener(v -> {
            MediaPlayer mp = MediaPlayer.create(getContext(), R.raw.sonido3);
            if (mp != null) {
                mp.start();
                mp.setOnCompletionListener(MediaPlayer::release); 
            }

            btnFinalizarEntrenamiento.setEnabled(false);

            entrenamientoRepository.finalizarEntrenamientoActivoPorSeccion(
                    MainActivity.usuarioLogueado.IdUsuario, seccionActual.IdSeccion, success -> {
                        if (success != null && success) {
                            Toast.makeText(getContext(), "¡Entrenamiento finalizado!", Toast.LENGTH_SHORT).show();
                            validarYRedirigir();
                        } else {
                            Toast.makeText(getContext(), "Error al finalizar", Toast.LENGTH_SHORT).show();
                            btnFinalizarEntrenamiento.setEnabled(true);
                        }
                    });
        });
    }

    private void validarYRedirigir() {
        new Thread(() -> {
            AppDatabase db = AppDatabase.getDatabase(getContext());
            List<Entrenamiento> entrenamientos = db.entrenamientoDao().getEntrenamientosFinalizadosPorSeccion(MainActivity.usuarioLogueado.IdUsuario, seccionActual.IdSeccion);
            
            new Handler(Looper.getMainLooper()).post(() -> {
                if (entrenamientos != null && entrenamientos.size() >= 2) {
                    irAComparativaPostFinalizar(entrenamientos);
                } else {
                    if (isAdded()) {
                        Navigation.findNavController(requireView()).navigate(R.id.rutinasFragment, null);
                    }
                }
            });
        }).start();
    }

    private void irAComparativaPostFinalizar(List<Entrenamiento> entrenamientos) {
        if (isAdded()) {
            Bundle bundle = new Bundle();
            bundle.putInt("idEntA", entrenamientos.get(entrenamientos.size() - 1).IdEntrenamiento);
            bundle.putInt("idEntB", entrenamientos.get(entrenamientos.size() - 2).IdEntrenamiento);
            bundle.putInt("idSeccion", seccionActual.IdSeccion);
            Navigation.findNavController(requireView()).navigate(R.id.compararEntrenamientosFragment, bundle);
        }
    }

    private void mostrarPopUpCrearEjercicioPersonalizado(Ejercicio ejercicioExistente) {
        Dialog dialog = new Dialog(requireContext());
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

    private void configurarGridView(View view) {
        if (seccionActual != null) tvTituloGrid.setText(String.format("Ejercicios de %s", seccionActual.NombreSeccion));
        if (getActivity() != null) {
            ejercicioRepository = new EjercicioRepository(getActivity().getApplication());
            seccionRepository = new SeccionRepository(getActivity().getApplication());
            entrenamientoRepository = new EntrenamientoRepository(getActivity().getApplication());
        }
        adapter = new EjerciciosAdapter(new ArrayList<>(), new EjerciciosAdapter.OnEjercicioClickListener() {
            @Override public void onAddClick() { mostrarPopUpAnadirEjercicio(); }
            @Override public void onEjercicioClick(Ejercicio ej) {
                Bundle bundle = new Bundle();
                bundle.putSerializable("ejercicio", ej);
                bundle.putSerializable("seccion", seccionActual);
                Navigation.findNavController(requireView()).navigate(R.id.cargarRegistroFragment, bundle);
            }
            @Override public void onOptionsClick(View v, Ejercicio ej) { mostrarMenuOpciones(v, ej); }
        });
        gvEjercicios.setAdapter(adapter);
        cargarEjerciciosDesdeDB();
    }

    private void cargarEjerciciosDesdeDB() {
        if (seccionActual != null && ejercicioRepository != null) {
            ejercicioRepository.obtenerEjerciciosPorSeccion(seccionActual.IdSeccion, ejercicios -> adapter.setEjercicios(ejercicios));
        }
    }

    private void abrirCamara() {
        File photoFile = null;
        try { photoFile = crearArchivoImagen(); } catch (IOException ignored) {}
        if (photoFile != null) {
            uriFotoCamara = FileProvider.getUriForFile(requireContext(), "com.example.migymsito.fileprovider", photoFile);
            cameraLauncher.launch(uriFotoCamara);
        }
    }

    private File crearArchivoImagen() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        if (getActivity() != null) {
            File storageDir = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            return File.createTempFile("JPEG_" + timeStamp + "_", ".jpg", storageDir);
        }
        return null;
    }

    private void mostrarOpcionesImagen(View view) {
        PopupMenu popup = new PopupMenu(requireContext(), view);
        popup.getMenu().add("Cámara"); 
        popup.getMenu().add("Galería");
        popup.setOnMenuItemClickListener(item -> {
            if ("Cámara".equals(item.getTitle())) abrirCamara();
            else galleryLauncher.launch("image/*");
            return true;
        });
        popup.show();
    }

    private void mostrarMenuOpciones(View view, Ejercicio ejercicio) {
        Dialog dialog = new Dialog(requireContext());
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
        Dialog dialog = new Dialog(requireContext());
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
        Dialog dialog = new Dialog(requireContext());
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
        Dialog dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.pop_up_listado_generico);
        if (dialog.getWindow() != null) dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        TextView tvTitulo = dialog.findViewById(R.id.tvTituloPopUpGenerico);
        tvTitulo.setText("Seleccionar Sección");

        GridView gvPopup = dialog.findViewById(R.id.gvListadoGenerico);
        
        seccionRepository.obtenerTodasLasSecciones(secciones -> {
            List<Seccion> lista = new ArrayList<>();
            for (Seccion s : secciones) {
                if ((seccionActual == null || s.IdSeccion != seccionActual.IdSeccion) && s.TipoSeccion.equals(tipo)) {
                    lista.add(s);
                }
            }
            
            gvPopup.setAdapter(new BaseAdapter() {
                @Override public int getCount() { return lista.size(); }
                @Override public Object getItem(int i) { return i < lista.size() ? lista.get(i) : null; }
                @Override public long getItemId(int i) { return i; }
                @Override public View getView(int pos, View v, ViewGroup p) {
                    View row = v;
                    if (row == null) row = LayoutInflater.from(p.getContext()).inflate(R.layout.item_seccion_previa, p, false);
                    Seccion s = lista.get(pos);
                    ((TextView)row.findViewById(R.id.tv_nombre_seccion_previa)).setText(s.NombreSeccion);
                    
                    TextView tvRutina = row.findViewById(R.id.tv_nombre_rutina_previa);
                    if (tvRutina != null) {
                        if (s.nombreRutina != null) {
                            tvRutina.setText(String.format("Rutina: %s", s.nombreRutina));
                        } else {
                            tvRutina.setText("Sistema");
                        }
                    }

                    View container = row.findViewById(R.id.container_item_previa);
                    if (container != null) {
                        GradientDrawable shape = new GradientDrawable();
                        shape.setCornerRadius(15 * p.getContext().getResources().getDisplayMetrics().density);
                        shape.setStroke(4, Color.BLACK);
                        shape.setColor(Color.WHITE);
                        container.setBackground(shape);
                    }

                    row.setOnClickListener(view -> { dialog.dismiss(); mostrarPopUpEjerciciosDeSeccionSeleccionada(s); });
                    return row;
                }
            });
        });

        dialog.findViewById(R.id.btnCancelarGenerico).setOnClickListener(v -> { dialog.dismiss(); mostrarPopUpEleccionTipoEjercicio(); });
        dialog.show();
    }

    private void mostrarPopUpEjerciciosDeSeccionSeleccionada(Seccion seccionSel) {
        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.pop_up_listado_generico);
        if (dialog.getWindow() != null) dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        TextView tvTitulo = dialog.findViewById(R.id.tvTituloPopUpGenerico);
        tvTitulo.setText("Elegir Ejercicio");

        GridView gv = dialog.findViewById(R.id.gvListadoGenerico);
        ejercicioRepository.obtenerEjerciciosPorSeccion(seccionSel.IdSeccion, ejercicios -> {
            gv.setAdapter(new BaseAdapter() {
                @Override public int getCount() { return ejercicios.size(); }
                @Override public Object getItem(int i) { return ejercicios.get(i); }
                @Override public long getItemId(int i) { return i; }
                @Override public View getView(int pos, View v, ViewGroup p) {
                    View row = v;
                    if (row == null) row = LayoutInflater.from(p.getContext()).inflate(R.layout.item_ejercicio_previo, p, false);
                    Ejercicio e = ejercicios.get(pos);
                    ((TextView)row.findViewById(R.id.tv_nombre_ejercicio_previo)).setText(e.NombreEjercicio);
                    
                    TextView tvTipo = row.findViewById(R.id.tv_tipo_ejercicio_previo);
                    if (tvTipo != null) {
                        tvTipo.setText(e.TipoEjercicio != null ? e.TipoEjercicio : "Error");
                    }

                    row.setOnClickListener(view -> {
                        ejercicioRepository.insertarRelacionSeccionEjercicio(e.IdEjercicio, seccionActual.IdSeccion);
                        dialog.dismiss();
                        new Handler(Looper.getMainLooper()).postDelayed(EjerciciosFragment.this::cargarEjerciciosDesdeDB, 300);
                    });
                    return row;
                }
            });
        });
        dialog.findViewById(R.id.btnCancelarGenerico).setOnClickListener(v -> { dialog.dismiss(); mostrarPopUpSeccionesParaSeleccion(seccionSel.TipoSeccion); });
        dialog.show();
    }

    @Override public void onResume() { super.onResume(); configurarBotonFinalizar(); }
}

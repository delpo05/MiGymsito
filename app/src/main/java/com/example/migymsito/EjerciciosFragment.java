package com.example.migymsito;

import android.app.AlertDialog;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.migymsito.adapter.EjercicioAdapter;
import com.example.migymsito.data.Ejercicio;
import com.example.migymsito.data.Entrenamiento;
import com.example.migymsito.data.Seccion;
import com.example.migymsito.dataDataBase.AppDatabase;
import com.example.migymsito.dataRepository.EjercicioRepository;
import com.example.migymsito.dataRepository.EntrenamientoRepository;
import com.example.migymsito.dataRepository.SeccionRepository;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

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
    private RecyclerView rvEjercicios;
    private EjercicioRepository ejercicioRepository;
    private SeccionRepository seccionRepository;
    private EntrenamientoRepository entrenamientoRepository;
    private SharedViewModel sharedViewModel;
    private EjercicioAdapter adapter;
    private Button btnFinalizarEntrenamiento;
    private FloatingActionButton fabAdd;

    private Uri uriImagenSeleccionada;
    private ImageView ivPreviewImagen;
    private ActivityResultLauncher<String> galleryLauncher;
    private ActivityResultLauncher<Uri> cameraLauncher;
    private ActivityResultLauncher<String> requestCameraPermissionLauncher;
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
            
            sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
            sharedViewModel.getImportFinishedTrigger().observe(getViewLifecycleOwner(), finished -> {
                if (finished != null && finished) {
                    cargarEjerciciosDesdeDB();
                    sharedViewModel.resetImportFinishedTrigger();
                }
            });

            sharedViewModel.getUserLoadedTrigger().observe(getViewLifecycleOwner(), loaded -> {
                if (loaded != null && loaded) {
                    configurarBotonFinalizar();
                    cargarEjerciciosDesdeDB();
                    sharedViewModel.resetUserLoadedTrigger();
                }
            });
        }

        if (getArguments() != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                seccionActual = getArguments().getSerializable("seccion", Seccion.class);
            } else {
                seccionActual = (Seccion) getArguments().getSerializable("seccion");
            }
        }

        rvEjercicios = view.findViewById(R.id.rvGenerico);
        tvTituloGrid = view.findViewById(R.id.tvTituloGrid);
        btnFinalizarEntrenamiento = view.findViewById(R.id.btnFinalizarEntrenamiento);
        fabAdd = view.findViewById(R.id.fabAdd);

        if (rvEjercicios != null) {
            rvEjercicios.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        }

        if (fabAdd != null) {
            fabAdd.setOnClickListener(v -> mostrarPopUpAnadirEjercicio());
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

        requestCameraPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        abrirCamara();
                    } else {
                        Toast.makeText(requireContext(), "Permiso de cámara denegado", Toast.LENGTH_SHORT).show();
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
                btnFinalizarEntrenamiento.setText("FINALIZAR ENTRENAMIENTO");
                btnFinalizarEntrenamiento.setVisibility(View.VISIBLE);
                btnFinalizarEntrenamiento.setEnabled(true);
                
                btnFinalizarEntrenamiento.setOnClickListener(v -> {
                    new AlertDialog.Builder(requireContext(), R.style.CustomDialogTheme)
                            .setTitle("Finalizar Entrenamiento")
                            .setMessage("¿Estás seguro de que deseas finalizar esta sección?")
                            .setPositiveButton("Finalizar", (dialog, which) -> {
                                MediaPlayer mp = MediaPlayer.create(getContext(), R.raw.sonido3);
                                if (mp != null) {
                                    mp.start();
                                    mp.setOnCompletionListener(MediaPlayer::release);
                                }

                                btnFinalizarEntrenamiento.setEnabled(false);

                                entrenamientoRepository.finalizarOEliminarSiVacio(
                                        MainActivity.usuarioLogueado.IdUsuario, seccionActual.IdSeccion, finalizado -> {
                                            if (finalizado != null) {
                                                if (finalizado) {
                                                    Toast.makeText(getContext(), "¡Entrenamiento finalizado!", Toast.LENGTH_SHORT).show();
                                                    validarYRedirigir();
                                                } else {
                                                    Toast.makeText(getContext(), "Entrenamiento sin registros no guardado", Toast.LENGTH_SHORT).show();
                                                    if (isAdded()) {
                                                        Navigation.findNavController(requireView()).navigate(R.id.rutinasFragment, null);
                                                    }
                                                }
                                            } else {
                                                Toast.makeText(getContext(), "Error al finalizar", Toast.LENGTH_SHORT).show();
                                                btnFinalizarEntrenamiento.setEnabled(true);
                                            }
                                        });
                            })
                            .setNegativeButton("Cancelar", null)
                            .show();
                });
            } else {
                btnFinalizarEntrenamiento.setText("INICIAR SECCIÓN");
                btnFinalizarEntrenamiento.setVisibility(View.VISIBLE);
                btnFinalizarEntrenamiento.setEnabled(true);

                btnFinalizarEntrenamiento.setOnClickListener(v -> {
                    new AlertDialog.Builder(requireContext(), R.style.CustomDialogTheme)
                            .setTitle("Iniciar Sección")
                            .setMessage("¿Deseas comenzar con la sección \"" + seccionActual.NombreSeccion + "\"?")
                            .setPositiveButton("Comenzar", (dialog, which) -> {
                                entrenamientoRepository.iniciarNuevoEntrenamiento(MainActivity.usuarioLogueado.IdUsuario, seccionActual.IdSeccion, nuevoEnt -> {
                                    configurarBotonFinalizar();
                                });
                            })
                            .setNegativeButton("Cancelar", null)
                            .show();
                });
            }
        });
    }

    private void validarYRedirigir() {
        new Thread(() -> {
            if (MainActivity.usuarioLogueado == null) return;

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
            bundle.putInt("idEntA", entrenamientos.get(0).IdEntrenamiento);
            bundle.putInt("idEntB", entrenamientos.get(1).IdEntrenamiento);
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
        CheckBox cbPesoPorLado = dialog.findViewById(R.id.cbPesoPorLado);
        View llContenedorBarra = dialog.findViewById(R.id.llContenedorBarra);
        Spinner spTipoDeBarra = dialog.findViewById(R.id.spTipoDeBarra);
        EditText etPesoBarra = dialog.findViewById(R.id.etPesoBarra);
        ivPreviewImagen = dialog.findViewById(R.id.ivSeleccionarImagen);
        Button btnAceptar = dialog.findViewById(R.id.btnAceptarEjercicio);
        Button btnCancelar = dialog.findViewById(R.id.btnCancelarEjercicio);

        ArrayAdapter<CharSequence> barAdapter = ArrayAdapter.createFromResource(requireContext(),
                R.array.tipos_de_barra, R.layout.spinner_item_dark);
        barAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item_dark);
        spTipoDeBarra.setAdapter(barAdapter);

        cbPesoPorLado.setOnCheckedChangeListener((buttonView, isChecked) -> {
            llContenedorBarra.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        });

        spTipoDeBarra.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = parent.getItemAtPosition(position).toString();
                if (selection.contains("(") && selection.contains("kg)")) {
                    String weightStr = selection.substring(selection.lastIndexOf("(") + 1, selection.lastIndexOf(" kg)"));
                    try {
                        etPesoBarra.setText(weightStr);
                    } catch (Exception ignored) {}
                } else if (position == 0) { // Ninguna / None
                    etPesoBarra.setText("0");
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        uriImagenSeleccionada = null; 

        if (ejercicioExistente != null) {
            tvTitulo.setText("Editar ejercicio");
            etNombre.setText(ejercicioExistente.NombreEjercicio);
            cbPesoCorporal.setChecked(ejercicioExistente.PesoCorporalEjercicio != null && ejercicioExistente.PesoCorporalEjercicio);
            cbPesoPorLado.setChecked(ejercicioExistente.PesoPorLado != null && ejercicioExistente.PesoPorLado);
            llContenedorBarra.setVisibility(cbPesoPorLado.isChecked() ? View.VISIBLE : View.GONE);
            
            if (ejercicioExistente.TipoDeBarra != null) {
                for (int i = 0; i < spTipoDeBarra.getCount(); i++) {
                    if (spTipoDeBarra.getItemAtPosition(i).toString().equals(ejercicioExistente.TipoDeBarra)) {
                        spTipoDeBarra.setSelection(i);
                        break;
                    }
                }
            }
            if (ejercicioExistente.PesoBarra != null) {
                etPesoBarra.setText(String.valueOf(ejercicioExistente.PesoBarra));
            }

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

            String pesoBarraStr = etPesoBarra.getText().toString().trim();
            float pesoBarra = 0.0f;
            if (!pesoBarraStr.isEmpty()) {
                try {
                    pesoBarra = Float.parseFloat(pesoBarraStr);
                } catch (NumberFormatException ignored) {}
            }

            if (ejercicioExistente == null) {
                Ejercicio nuevo = new Ejercicio();
                nuevo.NombreEjercicio = nombre;
                nuevo.TipoEjercicio = "Personalizado";
                nuevo.CategoriaEjercicio = "FUERZA"; // Default for now
                nuevo.PesoCorporalEjercicio = cbPesoCorporal.isChecked();
                nuevo.PesoPorLado = cbPesoPorLado.isChecked();
                if (nuevo.PesoPorLado) {
                    nuevo.TipoDeBarra = spTipoDeBarra.getSelectedItem().toString();
                    nuevo.PesoBarra = pesoBarra;
                } else {
                    nuevo.TipoDeBarra = "Ninguna";
                    nuevo.PesoBarra = 0.0f;
                }
                if (uriImagenSeleccionada != null) nuevo.ImagenEjercicio = uriImagenSeleccionada.toString();

                ejercicioRepository.insertarEjercicioConSeccion(nuevo, seccionActual.IdSeccion, success -> {
                    dialog.dismiss();
                    new Handler(Looper.getMainLooper()).postDelayed(this::cargarEjerciciosDesdeDB, 300);
                });
            } else {
                ejercicioExistente.NombreEjercicio = nombre;
                ejercicioExistente.ImagenEjercicio = (uriImagenSeleccionada != null) ? uriImagenSeleccionada.toString() : ejercicioExistente.ImagenEjercicio;
                ejercicioExistente.TipoEjercicio = "Personalizado";
                ejercicioExistente.PesoCorporalEjercicio = cbPesoCorporal.isChecked();
                ejercicioExistente.PesoPorLado = cbPesoPorLado.isChecked();
                if (ejercicioExistente.PesoPorLado) {
                    ejercicioExistente.TipoDeBarra = spTipoDeBarra.getSelectedItem().toString();
                    ejercicioExistente.PesoBarra = pesoBarra;
                } else {
                    ejercicioExistente.TipoDeBarra = "Ninguna";
                    ejercicioExistente.PesoBarra = 0.0f;
                }

                ejercicioRepository.actualizarEjercicioIndependiente(ejercicioExistente, seccionActual.IdSeccion, success -> {
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
        adapter = new EjercicioAdapter(new ArrayList<>(), new EjercicioAdapter.OnEjercicioClickListener() {
            @Override public void onEjercicioClick(Ejercicio ej) {
                if (MainActivity.usuarioLogueado == null || seccionActual == null) return;
                
                Bundle bundle = new Bundle();
                bundle.putSerializable("ejercicio", ej);
                bundle.putSerializable("seccion", seccionActual);
                Navigation.findNavController(requireView()).navigate(R.id.cargarRegistroFragment, bundle);
            }
            @Override public void onOptionsClick(View v, Ejercicio ej) { mostrarMenuOpciones(v, ej); }
        });
        rvEjercicios.setAdapter(adapter);
        cargarEjerciciosDesdeDB();
    }

    private void cargarEjerciciosDesdeDB() {
        if (seccionActual != null && ejercicioRepository != null) {
            ejercicioRepository.obtenerEjerciciosPorSeccion(seccionActual.IdSeccion, ejercicios -> adapter.setEjercicios(ejercicios));
        }
    }

    private void abrirCamara() {
        if (androidx.core.content.ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.CAMERA) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            requestCameraPermissionLauncher.launch(android.Manifest.permission.CAMERA);
            return;
        }
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

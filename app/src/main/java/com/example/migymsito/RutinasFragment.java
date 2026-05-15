package com.example.migymsito;

import android.app.Dialog;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.migymsito.adapter.RutinasAdapter;
import com.example.migymsito.data.Ejercicio;
import com.example.migymsito.data.Rutina;
import com.example.migymsito.data.Seccion;
import com.example.migymsito.data.SeccionXejercicio;
import com.example.migymsito.dataRepository.RutinaRepository;
import com.example.migymsito.dataDataBase.AppDatabase;
import com.example.migymsito.dataRepository.UsuarioRepository;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RutinasFragment extends Fragment {

    private GridView gvRutinas;
    private RutinaRepository rutinaRepository;
    private UsuarioRepository usuarioRepository;
    private RutinasAdapter adapter;
    private Dialog progressDialog;
    private volatile boolean importacionCancelada = false;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    private final ActivityResultLauncher<String> filePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    leerArchivoImportacion(uri);
                }
            }
    );

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

        usuarioRepository = new UsuarioRepository(getActivity().getApplication());
        rutinaRepository = new RutinaRepository(getActivity().getApplication());

        if (getArguments() == null || !getArguments().getBoolean("cambiarRutina", false)) {
            int idRutinaGuardada = usuarioRepository.obtenerIdRutina();
            if (idRutinaGuardada != -1) {
                saltarASecciones(idRutinaGuardada);
            }
        }

        gvRutinas = view.findViewById(R.id.gvGenerico);

        if (gvRutinas != null) {
            gvRutinas.setNumColumns(1);
            float density = getResources().getDisplayMetrics().density;
            gvRutinas.setVerticalSpacing(0);
            gvRutinas.setPadding((int) (8 * density), 0, (int) (8 * density), (int) (8 * density));
        }
        
        View btnFinalizar = view.findViewById(R.id.btnFinalizarEntrenamiento);
        if (btnFinalizar != null) {
            btnFinalizar.setVisibility(View.GONE);
        }

        configurarGridView(view);
    }

    private void saltarASecciones(int idRutina) {
        executorService.execute(() -> {
            AppDatabase db = AppDatabase.getDatabase(getContext());
            Rutina rutina = db.rutinaDao().obtenerRutinaPorId(idRutina);
            if (rutina != null && isAdded()) {
                getActivity().runOnUiThread(() -> {
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("rutina", rutina);
                    Navigation.findNavController(requireView()).navigate(R.id.seccionesFragment, bundle);
                });
            }
        });
    }

    private void configurarGridView(View view) {
        TextView tituloGv = view.findViewById(R.id.tvTituloGrid);
        if (tituloGv != null) tituloGv.setText("Mis Rutinas");

        adapter = new RutinasAdapter(new ArrayList<>(), new RutinasAdapter.OnRutinaClickListener() {
            @Override
            public void onAddClick() {
                mostrarPopUpOpcionesCrear();
            }

            @Override
            public void onRutinaClick(Rutina rutina) {
                usuarioRepository.guardarIdRutina(rutina.IdRutina);
                Bundle bundle = new Bundle();
                bundle.putSerializable("rutina", rutina);
                Navigation.findNavController(requireView()).navigate(R.id.seccionesFragment, bundle);
            }

            @Override
            public void onOptionsClick(View view, Rutina rutina) {
                mostrarMenuOpciones(view, rutina);
            }
        });
        
        gvRutinas.setAdapter(adapter);
        cargarRutinasDesdeDB();
    }

    private void mostrarPopUpOpcionesCrear() {
        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.pop_up_dos_opciones);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        }

        TextView tvTitulo = dialog.findViewById(R.id.tvTituloPopUp);
        tvTitulo.setText("Nueva Rutina");

        TextView tvCrear = dialog.findViewById(R.id.tvTextoIzquierda);
        tvCrear.setText("Crear");
        ImageView ivCrear = dialog.findViewById(R.id.ivIconoIzquierda);
        ivCrear.setImageResource(R.drawable.ic_add);
        View btnCrear = dialog.findViewById(R.id.btnOpcionIzquierda);
        btnCrear.setOnClickListener(v -> {
            dialog.dismiss();
            mostrarPopUpRutina(null);
        });

        TextView tvImportar = dialog.findViewById(R.id.tvTextoDerecha);
        tvImportar.setText("Importar");
        ImageView ivImportar = dialog.findViewById(R.id.ivIconoDerecha);
        ivImportar.setImageResource(R.drawable.ic_import);
        View btnOpcionDerecha = dialog.findViewById(R.id.btnOpcionDerecha);
        btnOpcionDerecha.setOnClickListener(v -> {
            dialog.dismiss();
            filePickerLauncher.launch("application/json");
        });

        View btnCancelar = dialog.findViewById(R.id.btnCancelar);
        btnCancelar.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void mostrarMenuOpciones(View view, Rutina rutina) {
        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.pop_up_modificar_eliminar);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        }

        TextView tvNombre = dialog.findViewById(R.id.tvNombrePopUp);
        if (tvNombre != null) {
            tvNombre.setText(rutina.NombreRutina);
        }

        View btnEliminar = dialog.findViewById(R.id.btnEliminarPopUp);
        if (btnEliminar != null) {
            btnEliminar.setOnClickListener(v -> {
                if (usuarioRepository.obtenerIdRutina() == rutina.IdRutina) {
                    usuarioRepository.eliminarRutinaSeleccionada();
                }
                rutinaRepository.eliminarRutina(rutina);
                dialog.dismiss();
                new Handler(Looper.getMainLooper()).postDelayed(this::cargarRutinasDesdeDB, 200);
            });
        }

        View btnModificar = dialog.findViewById(R.id.btnModificarPopUp);
        if (btnModificar != null) {
            btnModificar.setOnClickListener(v -> {
                dialog.dismiss();
                mostrarPopUpRutina(rutina);
            });
        }

        View btnExportar = dialog.findViewById(R.id.btnExportarPopUp);
        if (btnExportar != null) {
            btnExportar.setVisibility(View.VISIBLE);
            btnExportar.setOnClickListener(v -> {
                exportarRutinaAArchivo(rutina);
                dialog.dismiss();
            });
        }

        View btnCancelar = dialog.findViewById(R.id.btnCancelarPopUp);
        if (btnCancelar != null) {
            btnCancelar.setOnClickListener(v -> dialog.dismiss());
        }

        dialog.show();
    }

    private void exportarRutinaAArchivo(Rutina rutina) {
        executorService.execute(() -> {
            AppDatabase db = AppDatabase.getDatabase(getContext());
            try {
                JSONObject jsonRutina = new JSONObject();
                jsonRutina.put("nombre", rutina.NombreRutina);

                JSONArray jsonSecciones = new JSONArray();
                List<Seccion> secciones = db.seccionDao().obtenerSeccionesPorRutina(rutina.IdRutina);
                
                for (Seccion seccion : secciones) {
                    JSONObject jsonSeccion = new JSONObject();
                    jsonSeccion.put("nombre", seccion.NombreSeccion);
                    jsonSeccion.put("tipo", seccion.TipoSeccion);

                    JSONArray jsonEjercicios = new JSONArray();
                    List<Ejercicio> ejercicios = db.ejercicioDao().obtenerEjerciciosPorSeccion(seccion.IdSeccion);
                    for (Ejercicio ejercicio : ejercicios) {
                        JSONObject jsonEjercicio = new JSONObject();
                        jsonEjercicio.put("nombre", ejercicio.NombreEjercicio);
                        jsonEjercicio.put("tipo", ejercicio.TipoEjercicio);
                        jsonEjercicio.put("pesoCorporal", ejercicio.PesoCorporalEjercicio);
                        
                        if (ejercicio.ImagenEjercicio != null && !ejercicio.ImagenEjercicio.isEmpty()) {
                            String base64 = uriToBase64(Uri.parse(ejercicio.ImagenEjercicio));
                            if (base64 != null) {
                                jsonEjercicio.put("imagenData", base64);
                            }
                        }
                        jsonEjercicios.put(jsonEjercicio);
                    }
                    jsonSeccion.put("ejercicios", jsonEjercicios);
                    jsonSecciones.put(jsonSeccion);
                }
                jsonRutina.put("secciones", jsonSecciones);

                String jsonString = jsonRutina.toString();
                
                File path = new File(requireContext().getCacheDir(), "rutinas");
                if (!path.exists()) {
                    boolean created = path.mkdirs();
                    if (!created) {
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Error al crear directorio", Toast.LENGTH_SHORT).show());
                        }
                        return;
                    }
                }
                
                String fileName = "Rutina_" + rutina.NombreRutina.replaceAll("[^a-zA-Z0-9]", "_") + ".json";
                File file = new File(path, fileName);
                
                try (FileOutputStream stream = new FileOutputStream(file)) {
                    stream.write(jsonString.getBytes());
                }

                Uri contentUri = FileProvider.getUriForFile(requireContext(), "com.example.migymsito.fileprovider", file);

                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        android.content.Intent shareIntent = new android.content.Intent(android.content.Intent.ACTION_SEND);
                        shareIntent.setType("application/json");
                        shareIntent.putExtra(android.content.Intent.EXTRA_STREAM, contentUri);
                        shareIntent.addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        startActivity(android.content.Intent.createChooser(shareIntent, "Compartir Rutina con..."));
                    });
                }

            } catch (Exception e) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Error al exportar", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    private String uriToBase64(Uri uri) {
        try (InputStream inputStream = getActivity().getContentResolver().openInputStream(uri);
             ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[1024];
            int len;
            while (inputStream != null && (len = inputStream.read(buffer)) != -1) {
                byteBuffer.write(buffer, 0, len);
            }
            return Base64.encodeToString(byteBuffer.toByteArray(), Base64.NO_WRAP);
        } catch (Exception e) {
            return null;
        }
    }

    private String guardarImagenDesdeBase64(String base64Str) {
        try {
            byte[] data = Base64.decode(base64Str, Base64.DEFAULT);
            File folder = new File(requireContext().getFilesDir(), "imagenes_ejercicios");
            if (!folder.exists()) {
                boolean created = folder.mkdirs();
                if (!created) return null;
            }
            
            File file = new File(folder, "img_" + System.currentTimeMillis() + "_" + (int)(Math.random() * 1000) + ".jpg");
            try (FileOutputStream fos = new FileOutputStream(file)) {
                fos.write(data);
            }
            
            return Uri.fromFile(file).toString();
        } catch (Exception e) {
            return null;
        }
    }

    private void cargarRutinasDesdeDB() {
        if (MainActivity.usuarioLogueado != null) {
            rutinaRepository.obtenerRutinasDeUsuario(MainActivity.usuarioLogueado.IdUsuario, rutinas -> adapter.setRutinas(rutinas));
        }
    }

    private void mostrarPopUpRutina(Rutina rutinaExistente) {
        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.secciones_rutinas_pop_up_add);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        }

        TextView tvTitulo = dialog.findViewById(R.id.tvTituloPopUp);
        EditText etNombre = dialog.findViewById(R.id.etNombreGenerico);
        Button btnCancelar = dialog.findViewById(R.id.btnCancelarGenerico);
        Button btnAceptar = dialog.findViewById(R.id.btnConfirmarGenerico);
        Button btnImportar = dialog.findViewById(R.id.btnImportarRutina);

        if (btnImportar != null) btnImportar.setVisibility(View.GONE);

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
                    if (MainActivity.usuarioLogueado != null) {
                        nueva.IdUsuarioRutina = MainActivity.usuarioLogueado.IdUsuario;
                        rutinaRepository.insertarRutina(nueva);
                    }
                } else {
                    rutinaExistente.NombreRutina = nombre;
                    rutinaRepository.actualizarRutina(rutinaExistente);
                }
                dialog.dismiss();
                new Handler(Looper.getMainLooper()).postDelayed(this::cargarRutinasDesdeDB, 300);
            }
        });
        dialog.show();
    }

    private void mostrarPopUpImportacion() {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                progressDialog = new Dialog(requireContext());
                progressDialog.setContentView(R.layout.pop_up_importacion);
                progressDialog.setCancelable(false);
                if (progressDialog.getWindow() != null) {
                    progressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                }

                Button btnCancelar = progressDialog.findViewById(R.id.btnCancelarImportacion);
                btnCancelar.setOnClickListener(v -> {
                    importacionCancelada = true;
                    progressDialog.dismiss();
                    Toast.makeText(getContext(), "Cancelando importación...", Toast.LENGTH_SHORT).show();
                });

                progressDialog.show();
            });
        }
    }

    private void ocultarPopUpImportacion() {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
            });
        }
    }

    private void leerArchivoImportacion(Uri uri) {
        importacionCancelada = false;
        mostrarPopUpImportacion();
        executorService.execute(() -> {
            try (InputStream inputStream = getActivity().getContentResolver().openInputStream(uri);
                 BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    if (importacionCancelada) {
                        ocultarPopUpImportacion();
                        return;
                    }
                    stringBuilder.append(line);
                }
                
                procesarJsonImportacion(stringBuilder.toString());

            } catch (Exception e) {
                ocultarPopUpImportacion();
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Error al leer el archivo", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    private void procesarJsonImportacion(String jsonString) {
        executorService.execute(() -> {
            try {
                JSONObject jsonRutina = new JSONObject(jsonString);
                String nombreRutina = jsonRutina.getString("nombre");

                AppDatabase db = AppDatabase.getDatabase(getContext());
                
                db.runInTransaction(() -> {
                    if (importacionCancelada) throw new RuntimeException("CANCEL");

                    Rutina nuevaRutina = new Rutina();
                    nuevaRutina.NombreRutina = nombreRutina + " (Importada)";
                    nuevaRutina.IdUsuarioRutina = MainActivity.usuarioLogueado.IdUsuario;
                    
                    long idRutina = db.rutinaDao().insertarRutina(nuevaRutina);
                    
                    try {
                        JSONArray jsonSecciones = jsonRutina.getJSONArray("secciones");
                        for (int i = 0; i < jsonSecciones.length(); i++) {
                            if (importacionCancelada) throw new RuntimeException("CANCEL");

                            JSONObject jsonSeccion = jsonSecciones.getJSONObject(i);
                            Seccion nuevaSeccion = new Seccion();
                            nuevaSeccion.NombreSeccion = jsonSeccion.getString("nombre");
                            nuevaSeccion.TipoSeccion = jsonSeccion.getString("tipo");
                            nuevaSeccion.IdRutinaSeccion = (int) idRutina;
                            
                            long idSeccion = db.seccionDao().insertarSeccion(nuevaSeccion);
                            
                            JSONArray jsonEjercicios = jsonSeccion.getJSONArray("ejercicios");
                            for (int j = 0; j < jsonEjercicios.length(); j++) {
                                if (importacionCancelada) throw new RuntimeException("CANCEL");

                                JSONObject jsonEjercicio = jsonEjercicios.getJSONObject(j);
                                String nombreEj = jsonEjercicio.getString("nombre");
                                
                                int idEjercicio;
                                Ejercicio ejExistente = buscarEjercicioPorNombre(db, nombreEj);
                                if (ejExistente != null) {
                                    idEjercicio = ejExistente.IdEjercicio;
                                    if (ejExistente.ImagenEjercicio == null || ejExistente.ImagenEjercicio.isEmpty()) {
                                        String base64Data = jsonEjercicio.optString("imagenData", "");
                                        if (!base64Data.isEmpty()) {
                                            ejExistente.ImagenEjercicio = guardarImagenDesdeBase64(base64Data);
                                            db.ejercicioDao().actualizarEjercicio(ejExistente);
                                        }
                                    }
                                } else {
                                    Ejercicio nuevoEj = new Ejercicio();
                                    nuevoEj.NombreEjercicio = nombreEj;
                                    nuevoEj.TipoEjercicio = jsonEjercicio.getString("tipo");
                                    nuevoEj.PesoCorporalEjercicio = jsonEjercicio.getBoolean("pesoCorporal");
                                    
                                    String base64Data = jsonEjercicio.optString("imagenData", "");
                                    if (!base64Data.isEmpty()) {
                                        nuevoEj.ImagenEjercicio = guardarImagenDesdeBase64(base64Data);
                                    } else {
                                        nuevoEj.ImagenEjercicio = jsonEjercicio.optString("imagen", null);
                                    }
                                    
                                    idEjercicio = (int) db.ejercicioDao().insertarEjercicio(nuevoEj);
                                }

                                SeccionXejercicio sxe = new SeccionXejercicio();
                                sxe.IdSeccion = (int) idSeccion;
                                sxe.IdEjercicio = idEjercicio;
                                db.seccionXejercicioDao().insert(sxe);
                            }
                        }
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                });

                ocultarPopUpImportacion();

                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "Rutina importada con éxito", Toast.LENGTH_SHORT).show();
                        cargarRutinasDesdeDB();
                    });
                }

            } catch (Exception e) {
                ocultarPopUpImportacion();
                if ("CANCEL".equals(e.getMessage())) {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            Toast.makeText(getContext(), "Importación cancelada", Toast.LENGTH_SHORT).show();
                            cargarRutinasDesdeDB();
                        });
                    }
                } else {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Error al importar: Formato inválido", Toast.LENGTH_SHORT).show());
                    }
                }
            }
        });
    }

    private Ejercicio buscarEjercicioPorNombre(AppDatabase db, String nombre) {
        List<Ejercicio> todos = db.ejercicioDao().obtenerTodosLosEjercicios();
        for (Ejercicio e : todos) {
            if (e.NombreEjercicio.equalsIgnoreCase(nombre)) return e;
        }
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }
}

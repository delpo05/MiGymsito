package com.example.migymsito;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.FileProvider;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.graphics.Insets;

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
import java.util.concurrent.Executors;

public class RutinasActivity extends HeaderActivity {

    private GridView gvRutinas;
    private RutinaRepository rutinaRepository;
    private UsuarioRepository usuarioRepository;
    private RutinasAdapter adapter;
    private Dialog progressDialog;
    private volatile boolean importacionCancelada = false;

    private final ActivityResultLauncher<String> filePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    leerArchivoImportacion(uri);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.secciones_rutinas_activity);

        usuarioRepository = new UsuarioRepository(getApplication());
        rutinaRepository = new RutinaRepository(getApplication());

        int idRutinaGuardada = usuarioRepository.obtenerIdRutina();
        if (idRutinaGuardada != -1 && getIntent().getBooleanExtra("cambiarRutina", false) == false) {
            saltarASecciones(idRutinaGuardada);
        }

        gvRutinas = findViewById(R.id.gvGenerico);

        if (gvRutinas != null) {
            gvRutinas.setNumColumns(1);
            float density = getResources().getDisplayMetrics().density;
            gvRutinas.setVerticalSpacing(0);
            gvRutinas.setPadding((int) (8 * density), 0, (int) (8 * density), (int) (8 * density));
        }
        
        View btnFinalizar = findViewById(R.id.btnFinalizarEntrenamiento);
        if (btnFinalizar != null) {
            btnFinalizar.setVisibility(View.GONE);
        }

        TextView tvUsername = findViewById(R.id.toolbar_username);

        if (tvUsername != null && usuarioLogueado != null) {
            tvUsername.setText(usuarioLogueado.NombreUsuario);
        } else if (tvUsername != null) {
            tvUsername.setText("Invitado");
        }

        configurarGridView();
        configurarWindowInsets(R.id.layout_contenedor_grid);
    }

    private void saltarASecciones(int idRutina) {
        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.getDatabase(getApplicationContext());
            Rutina rutina = db.rutinaDao().obtenerRutinaPorId(idRutina);
            if (rutina != null) {
                runOnUiThread(() -> {
                    Intent intent = new Intent(RutinasActivity.this, SeccionesActivity.class);
                    intent.putExtra("rutina", rutina);
                    startActivity(intent);
                    finish();
                });
            }
        });
    }

    private void configurarGridView() {
        TextView tituloGv = findViewById(R.id.tvTituloGrid);
        if (tituloGv != null) tituloGv.setText("Mis Rutinas");

        adapter = new RutinasAdapter(new ArrayList<>(), new RutinasAdapter.OnRutinaClickListener() {
            @Override
            public void onAddClick() {
                mostrarPopUpOpcionesCrear();
            }

            @Override
            public void onRutinaClick(Rutina rutina) {
                usuarioRepository.guardarIdRutina(rutina.IdRutina);
                Intent intent = new Intent(RutinasActivity.this, SeccionesActivity.class);
                intent.putExtra("rutina", rutina);
                startActivity(intent);
                finish();
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
        Dialog dialog = new Dialog(this);
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
        Dialog dialog = new Dialog(this);
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
                new Handler().postDelayed(this::cargarRutinasDesdeDB, 200);
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
        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.getDatabase(getApplicationContext());
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
                        
                        // Exportar imagen como Base64 si existe
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
                
                File path = new File(getCacheDir(), "rutinas");
                if (!path.exists()) path.mkdirs();
                
                String fileName = "Rutina_" + rutina.NombreRutina.replaceAll("[^a-zA-Z0-9]", "_") + ".json";
                File file = new File(path, fileName);
                
                FileOutputStream stream = new FileOutputStream(file);
                stream.write(jsonString.getBytes());
                stream.close();

                Uri contentUri = FileProvider.getUriForFile(this, "com.example.migymsito.fileprovider", file);

                runOnUiThread(() -> {
                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                    shareIntent.setType("application/json");
                    shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
                    shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    startActivity(Intent.createChooser(shareIntent, "Compartir Rutina con..."));
                });

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(this, "Error al exportar", Toast.LENGTH_SHORT).show());
            }
        });
    }

    private String uriToBase64(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len;
            while ((len = inputStream.read(buffer)) != -1) {
                byteBuffer.write(buffer, 0, len);
            }
            return Base64.encodeToString(byteBuffer.toByteArray(), Base64.NO_WRAP);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private String guardarImagenDesdeBase64(String base64Str) {
        try {
            byte[] data = Base64.decode(base64Str, Base64.DEFAULT);
            File folder = new File(getFilesDir(), "imagenes_ejercicios");
            if (!folder.exists()) folder.mkdirs();
            
            // Usamos un sufijo aleatorio para evitar colisiones si se procesan varias imágenes en el mismo milisegundo
            File file = new File(folder, "img_" + System.currentTimeMillis() + "_" + (int)(Math.random() * 1000) + ".jpg");
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(data);
            fos.close();
            
            return Uri.fromFile(file).toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void cargarRutinasDesdeDB() {
        if (usuarioLogueado != null) {
            rutinaRepository.obtenerRutinasDeUsuario(usuarioLogueado.IdUsuario, rutinas -> {
                adapter.setRutinas(rutinas);
            });
        }
    }

    private void mostrarPopUpRutina(Rutina rutinaExistente) {
        Dialog dialog = new Dialog(this);
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
                    com.example.migymsito.data.Rutina nueva = new com.example.migymsito.data.Rutina();
                    nueva.NombreRutina = nombre;
                    if (usuarioLogueado != null) {
                        nueva.IdUsuarioRutina = usuarioLogueado.IdUsuario;
                        rutinaRepository.insertarRutina(nueva);
                    }
                } else {
                    rutinaExistente.NombreRutina = nombre;
                    rutinaRepository.actualizarRutina(rutinaExistente);
                }
                dialog.dismiss();
                new Handler().postDelayed(this::cargarRutinasDesdeDB, 300);
            }
        });
        dialog.show();
    }

    @Override
    protected void onImportFinished() {
        cargarRutinasDesdeDB();
    }

    private void mostrarPopUpImportacion() {
        runOnUiThread(() -> {
            progressDialog = new Dialog(this);
            progressDialog.setContentView(R.layout.pop_up_importacion);
            progressDialog.setCancelable(false);
            if (progressDialog.getWindow() != null) {
                progressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            }

            Button btnCancelar = progressDialog.findViewById(R.id.btnCancelarImportacion);
            btnCancelar.setOnClickListener(v -> {
                importacionCancelada = true;
                progressDialog.dismiss();
                Toast.makeText(this, "Cancelando importación...", Toast.LENGTH_SHORT).show();
            });

            progressDialog.show();
        });
    }

    private void ocultarPopUpImportacion() {
        runOnUiThread(() -> {
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
        });
    }

    private void leerArchivoImportacion(Uri uri) {
        importacionCancelada = false;
        mostrarPopUpImportacion();
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                InputStream inputStream = getContentResolver().openInputStream(uri);
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    if (importacionCancelada) {
                        inputStream.close();
                        ocultarPopUpImportacion();
                        return;
                    }
                    stringBuilder.append(line);
                }
                inputStream.close();
                
                procesarJsonImportacion(stringBuilder.toString());

            } catch (Exception e) {
                e.printStackTrace();
                ocultarPopUpImportacion();
                runOnUiThread(() -> Toast.makeText(this, "Error al leer el archivo", Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void procesarJsonImportacion(String jsonString) {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                JSONObject jsonRutina = new JSONObject(jsonString);
                String nombreRutina = jsonRutina.getString("nombre");

                AppDatabase db = AppDatabase.getDatabase(getApplicationContext());
                
                db.runInTransaction(() -> {
                    if (importacionCancelada) throw new RuntimeException("CANCEL");

                    Rutina nuevaRutina = new Rutina();
                    nuevaRutina.NombreRutina = nombreRutina + " (Importada)";
                    nuevaRutina.IdUsuarioRutina = usuarioLogueado.IdUsuario;
                    
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
                                        String base64Data = jsonEjercicio.optString("imagenData", null);
                                        if (base64Data != null) {
                                            ejExistente.ImagenEjercicio = guardarImagenDesdeBase64(base64Data);
                                            db.ejercicioDao().actualizarEjercicio(ejExistente);
                                        }
                                    }
                                } else {
                                    Ejercicio nuevoEj = new Ejercicio();
                                    nuevoEj.NombreEjercicio = nombreEj;
                                    nuevoEj.TipoEjercicio = jsonEjercicio.getString("tipo");
                                    nuevoEj.PesoCorporalEjercicio = jsonEjercicio.getBoolean("pesoCorporal");
                                    
                                    String base64Data = jsonEjercicio.optString("imagenData", null);
                                    if (base64Data != null) {
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

                runOnUiThread(() -> {
                    Toast.makeText(this, "Rutina importada con éxito", Toast.LENGTH_SHORT).show();
                    cargarRutinasDesdeDB();
                });

            } catch (Exception e) {
                ocultarPopUpImportacion();
                if ("CANCEL".equals(e.getMessage())) {
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Importación cancelada", Toast.LENGTH_SHORT).show();
                        cargarRutinasDesdeDB();
                    });
                } else {
                    e.printStackTrace();
                    runOnUiThread(() -> Toast.makeText(this, "Error al importar: Formato inválido", Toast.LENGTH_SHORT).show());
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

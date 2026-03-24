package com.example.migymsito;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
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
    private RutinasAdapter adapter;

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

        gvRutinas = findViewById(R.id.gvGenerico);
        
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

    private void configurarGridView() {
        TextView tituloGv = findViewById(R.id.tvTituloGrid);
        if (tituloGv != null) tituloGv.setText("Mis Rutinas");

        rutinaRepository = new RutinaRepository(getApplication());
        
        adapter = new RutinasAdapter(new ArrayList<>(), new RutinasAdapter.OnRutinaClickListener() {
            @Override
            public void onAddClick() {
                mostrarPopUpOpcionesCrear();
            }

            @Override
            public void onRutinaClick(Rutina rutina) {
                Intent intent = new Intent(RutinasActivity.this, SeccionesActivity.class);
                intent.putExtra("rutina", rutina);
                startActivity(intent);
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

        // Configurar Opción Izquierda: Crear
        TextView tvCrear = dialog.findViewById(R.id.tvTextoIzquierda);
        tvCrear.setText("Crear");
        ImageView ivCrear = dialog.findViewById(R.id.ivIconoIzquierda);
        ivCrear.setImageResource(R.drawable.ic_add);
        View btnCrear = dialog.findViewById(R.id.btnOpcionIzquierda);
        btnCrear.setOnClickListener(v -> {
            dialog.dismiss();
            mostrarPopUpRutina(null);
        });

        // Configurar Opción Derecha: Importar
        TextView tvImportar = dialog.findViewById(R.id.tvTextoDerecha);
        tvImportar.setText("Importar");
        ImageView ivImportar = dialog.findViewById(R.id.ivIconoDerecha);
        ivImportar.setImageResource(R.drawable.ic_import);
        View btnImportar = dialog.findViewById(R.id.btnOpcionDerecha);
        btnImportar.setOnClickListener(v -> {
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
                        jsonEjercicio.put("imagen", ejercicio.ImagenEjercicio);
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

        // Ocultar el botón importar viejo ya que ahora está en el primer pop-up
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

    private void leerArchivoImportacion(Uri uri) {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                InputStream inputStream = getContentResolver().openInputStream(uri);
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line);
                }
                inputStream.close();
                
                procesarJsonImportacion(stringBuilder.toString());

            } catch (Exception e) {
                e.printStackTrace();
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
                    Rutina nuevaRutina = new Rutina();
                    nuevaRutina.NombreRutina = nombreRutina + " (Importada)";
                    nuevaRutina.IdUsuarioRutina = usuarioLogueado.IdUsuario;
                    
                    long idRutina = db.rutinaDao().insertarRutina(nuevaRutina);
                    
                    try {
                        JSONArray jsonSecciones = jsonRutina.getJSONArray("secciones");
                        for (int i = 0; i < jsonSecciones.length(); i++) {
                            JSONObject jsonSeccion = jsonSecciones.getJSONObject(i);
                            Seccion nuevaSeccion = new Seccion();
                            nuevaSeccion.NombreSeccion = jsonSeccion.getString("nombre");
                            nuevaSeccion.TipoSeccion = jsonSeccion.getString("tipo");
                            nuevaSeccion.IdRutinaSeccion = (int) idRutina;
                            
                            long idSeccion = db.seccionDao().insertarSeccion(nuevaSeccion);
                            
                            JSONArray jsonEjercicios = jsonSeccion.getJSONArray("ejercicios");
                            for (int j = 0; j < jsonEjercicios.length(); j++) {
                                JSONObject jsonEjercicio = jsonEjercicios.getJSONObject(j);
                                String nombreEj = jsonEjercicio.getString("nombre");
                                
                                int idEjercicio;
                                Ejercicio ejExistente = buscarEjercicioPorNombre(db, nombreEj);
                                if (ejExistente != null) {
                                    idEjercicio = ejExistente.IdEjercicio;
                                } else {
                                    Ejercicio nuevoEj = new Ejercicio();
                                    nuevoEj.NombreEjercicio = nombreEj;
                                    nuevoEj.TipoEjercicio = jsonEjercicio.getString("tipo");
                                    nuevoEj.PesoCorporalEjercicio = jsonEjercicio.getBoolean("pesoCorporal");
                                    nuevoEj.ImagenEjercicio = jsonEjercicio.optString("imagen", null);
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

                runOnUiThread(() -> {
                    Toast.makeText(this, "Rutina importada con éxito", Toast.LENGTH_SHORT).show();
                    cargarRutinasDesdeDB();
                });

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(this, "Error al importar: Formato inválido", Toast.LENGTH_SHORT).show());
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

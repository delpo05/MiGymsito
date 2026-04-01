package com.example.migymsito;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.LayoutRes;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.migymsito.data.Ejercicio;
import com.example.migymsito.data.Entrenamiento;
import com.example.migymsito.data.Registro;
import com.example.migymsito.data.Rutina;
import com.example.migymsito.data.Seccion;
import com.example.migymsito.data.SeccionXejercicio;
import com.example.migymsito.data.Usuario;
import com.example.migymsito.dataDataBase.AppDatabase;
import com.example.migymsito.dataRepository.UsuarioRepository;
import com.google.android.material.navigation.NavigationView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HeaderActivity extends AppCompatActivity {

    protected DrawerLayout drawerLayout;
    protected NavigationView navigationView;
    public static Usuario usuarioLogueado; 
    protected UsuarioRepository userRepo;

    private final ActivityResultLauncher<String> importLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    importarDatosDesdeCsv(uri);
                }
            }
    );

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userRepo = new UsuarioRepository(getApplication());

        // Recuperar usuario de la sesión si no está cargado
        int idSesion = userRepo.obtenerIdSesion();
        if (idSesion != -1 && usuarioLogueado == null) {
            userRepo.obtenerUsuarioPorId(idSesion, usuario -> {
                if (usuario != null) {
                    usuarioLogueado = usuario;
                    actualizarNombreHeader();
                    onUsuarioCargado();
                }
            });
        }

        // Manejo del botón atrás
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (drawerLayout != null && drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    setEnabled(false);
                    HeaderActivity.super.onBackPressed();
                }
            }
        });
    }

    protected void onUsuarioCargado() {}

    @Override
    protected void onResume() {
        super.onResume();
        actualizarNombreHeader();
    }

    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        super.setContentView(layoutResID);
        setupToolbar();
        setupNavigationDrawer();
        actualizarNombreHeader();
    }

    protected void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayShowTitleEnabled(false);
            }

            ImageButton menuButton = findViewById(R.id.toolbar_menu_button);
            if (menuButton != null) {
                menuButton.setOnClickListener(v -> {
                    if (drawerLayout != null) {
                        drawerLayout.openDrawer(GravityCompat.START);
                    }
                });
            }

            // --- REDIRECCIÓN A MI PERFIL AL TOCAR NOMBRE O ICONO ---
            TextView tvUsername = findViewById(R.id.toolbar_username);
            ImageView ivUserIcon = findViewById(R.id.toolbar_user_icon);

            View.OnClickListener perfilClickListener = v -> {
                Intent intent = new Intent(this, DatosPersonalesActivity.class);
                startActivity(intent);
            };

            if (tvUsername != null) tvUsername.setOnClickListener(perfilClickListener);
            if (ivUserIcon != null) ivUserIcon.setOnClickListener(perfilClickListener);
        }
    }

    protected void actualizarNombreHeader() {
        TextView tvUsername = findViewById(R.id.toolbar_username);
        if (tvUsername != null && usuarioLogueado != null) {
            tvUsername.setText(usuarioLogueado.NombreUsuario);
        }
    }

    protected void setupNavigationDrawer() {
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);

        if (navigationView != null) {
            navigationView.setNavigationItemSelectedListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.Home) {
                    // Acción de "Cambiar Rutina": Borrar preferencia y volver
                    userRepo.eliminarRutinaSeleccionada();
                    Intent intent = new Intent(this, RutinasActivity.class);
                    intent.putExtra("cambiarRutina", true);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                } else if (itemId == R.id.MiPerfil) {
                    Intent intent = new Intent(this, DatosPersonalesActivity.class);
                    startActivity(intent);
                } else if (itemId == R.id.MiProgreso) {
                    Intent intent = new Intent(this, EstadisticasActivity.class);
                    startActivity(intent);
                } else if (itemId == R.id.ComparativaRendimientos) {
                    Intent intent = new Intent(this, CompararEntrenamientosActivity.class);
                    startActivity(intent);
                } else if (itemId == R.id.Historial) {
                    Intent intent = new Intent(this, HistorialPesoActivity.class);
                    startActivity(intent);
                } else if (itemId == R.id.Exportar) {
                    mostrarPopUpExportar();
                } else if (itemId == R.id.Importar) {
                    importLauncher.launch("*/*");
                } else if (itemId == R.id.CerrarSesion) {
                    cerrarSesion();
                }

                if (drawerLayout != null) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                }
                return true;
            });
        }
    }

    private void mostrarPopUpExportar() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.pop_up_exportar_registros);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        }

        CheckBox cbRegistros = dialog.findViewById(R.id.cbConfirmarExportar);
        Button btnAceptar = dialog.findViewById(R.id.btnAceptarExportar);
        Button btnCancelar = dialog.findViewById(R.id.btnCancelarExportar);

        btnAceptar.setOnClickListener(v -> {
            boolean incluirRegistros = cbRegistros.isChecked();
            exportarDatosCsv(incluirRegistros);
            dialog.dismiss();
        });

        btnCancelar.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void exportarDatosCsv(boolean incluirRegistros) {
        if (usuarioLogueado == null) {
            Toast.makeText(this, "Inicia sesión para exportar", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            try {
                AppDatabase db = AppDatabase.getDatabase(getApplicationContext());
                StringBuilder csv = new StringBuilder();
                String sep = ";";

                // Encabezados
                if (incluirRegistros) {
                    csv.append("Rutina").append(sep).append("Seccion").append(sep).append("Ejercicio").append(sep)
                            .append("Tipo").append(sep).append("Fecha").append(sep).append("Peso").append(sep)
                            .append("Serie").append(sep).append("Reps").append(sep).append("Peso Corporal")
                            .append(sep).append("Peso del Usuario en ese momento\n");
                } else {
                    csv.append("Rutina").append(sep).append("Seccion").append(sep).append("Ejercicio").append(sep)
                            .append("Tipo").append(sep).append("Peso Corporal\n");
                }

                List<Rutina> rutinas = db.rutinaDao().obtenerRutinasPorUsuario(usuarioLogueado.IdUsuario);

                if (rutinas == null || rutinas.isEmpty()) {
                    runOnUiThread(() -> Toast.makeText(this, "No hay datos para exportar", Toast.LENGTH_SHORT).show());
                    return;
                }

                for (Rutina r : rutinas) {
                    List<Seccion> secciones = db.seccionDao().obtenerSeccionesPorRutina(r.IdRutina);
                    for (Seccion s : secciones) {
                        List<Ejercicio> ejercicios = db.ejercicioDao().obtenerEjerciciosPorSeccion(s.IdSeccion);
                        for (Ejercicio ej : ejercicios) {
                            String base = r.NombreRutina + sep + s.NombreSeccion + sep + ej.NombreEjercicio + sep + ej.TipoEjercicio;

                            if (incluirRegistros) {
                                List<Registro> registros = db.registroDao().obtenerHistorialPorEjercicioYUsuario(usuarioLogueado.IdUsuario, ej.IdEjercicio);
                                if (registros.isEmpty()) {
                                    csv.append(base).append(sep).append("Sin registros").append(sep).append("-").append(sep).append("-").append(sep).append("-\n");
                                } else {
                                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                                    for (Registro reg : registros) {
                                        csv.append(base).append(sep)
                                                .append(sdf.format(new Date(reg.FechaRegistro))).append(sep)
                                                .append(String.valueOf(reg.PesoRegistro).replace(".", ",")).append(sep)
                                                .append(reg.NumSeriesRegistro).append(sep)
                                                .append(reg.Repeticiones).append(sep)
                                                .append(ej.PesoCorporalEjercicio ? "Si" : "No").append(sep);

                                        if (ej.PesoCorporalEjercicio && reg.PesoCorporalMomento != null) {
                                            csv.append(String.valueOf(reg.PesoCorporalMomento).replace(".", ","));
                                        } else {
                                            csv.append("-");
                                        }
                                        csv.append("\n");
                                    }
                                }
                            } else {
                                csv.append(base).append(sep).append(ej.PesoCorporalEjercicio ? "Si" : "No").append("\n");
                            }
                        }
                    }
                }

                generarYCompartirArchivo(csv.toString(), incluirRegistros);

            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, "Error al exportar: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        }).start();
    }

    private void generarYCompartirArchivo(String contenido, boolean esCompleto) {
        try {
            File path = new File(getCacheDir(), "rutinas");
            if (!path.exists()) path.mkdirs();
            File file = new File(path, esCompleto ? "MiGymsito_Completo.csv" : "MiGymsito_Estructura.csv");

            FileOutputStream out = new FileOutputStream(file);
            out.write(contenido.getBytes());
            out.close();

            Uri uri = FileProvider.getUriForFile(this, "com.example.migymsito.fileprovider", file);

            runOnUiThread(() -> {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/csv");
                intent.putExtra(Intent.EXTRA_STREAM, uri);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivity(Intent.createChooser(intent, "Compartir Progreso"));
            });
        } catch (Exception e) {
            runOnUiThread(() -> Toast.makeText(this, "Error al crear archivo", Toast.LENGTH_SHORT).show());
        }
    }

    private void importarDatosDesdeCsv(Uri uri) {
        if (usuarioLogueado == null) return;

        new Thread(() -> {
            try {
                AppDatabase db = AppDatabase.getDatabase(getApplicationContext());
                InputStream is = getContentResolver().openInputStream(uri);
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));

                String linea = reader.readLine(); 
                String sep = ";";

                while ((linea = reader.readLine()) != null) {
                    String[] datos = linea.split(sep);
                    if (datos.length < 4) continue;

                    String nombreRutina = datos[0];
                    String nombreSeccion = datos[1];
                    String nombreEjercicio = datos[2];
                    String tipoEjercicio = datos[3];

                    Rutina rutina = buscarOInsertarRutina(db, nombreRutina);
                    Seccion seccion = buscarOInsertarSeccion(db, nombreSeccion, rutina.IdRutina);
                    Ejercicio ejercicio = buscarOInsertarEjercicio(db, nombreEjercicio, tipoEjercicio);
                    SeccionXejercicio sxe = buscarOInsertarRelacion(db, seccion.IdSeccion, ejercicio.IdEjercicio);

                    if (datos.length > 4 && !datos[4].equals("Sin registros") && !datos[4].equals("Si") && !datos[4].equals("No")) {
                        try {
                            String fechaStr = datos[4];
                            double peso = Double.parseDouble(datos[5].replace(",", "."));
                            int serie = Integer.parseInt(datos[6]);
                            int reps = Integer.parseInt(datos[7]);
                            Double pesoUsuario = null;
                            if (datos.length > 9 && !datos[9].equals("-")) {
                                pesoUsuario = Double.parseDouble(datos[9].replace(",", "."));
                            }

                            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                            long fechaLong = sdf.parse(fechaStr).getTime();

                            Entrenamiento ent = asegurarEntrenamiento(db, seccion.IdSeccion, fechaLong);

                            Registro reg = new Registro();
                            reg.IdEntrenamiento = ent.IdEntrenamiento;
                            reg.IdSeccionXejercicio = sxe.IdSeccionXejercicio;
                            reg.FechaRegistro = fechaLong;
                            reg.PesoRegistro = peso;
                            reg.NumSeriesRegistro = serie;
                            reg.Repeticiones = reps;
                            reg.PesoCorporalMomento = pesoUsuario;

                            db.registroDao().insertarRegistro(reg);

                        } catch (Exception e) {
                        }
                    }
                }
                reader.close();
                runOnUiThread(() -> {
                    Toast.makeText(this, "Importación completada", Toast.LENGTH_SHORT).show();
                    onImportFinished();
                });

            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, "Error al importar: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        }).start();
    }

    protected void onImportFinished() {}

    private Rutina buscarOInsertarRutina(AppDatabase db, String nombre) {
        List<Rutina> existentes = db.rutinaDao().obtenerRutinasPorUsuario(usuarioLogueado.IdUsuario);
        for (Rutina r : existentes) {
            if (r.NombreRutina.equalsIgnoreCase(nombre)) return r;
        }
        Rutina nueva = new Rutina();
        nueva.NombreRutina = nombre;
        nueva.IdUsuarioRutina = usuarioLogueado.IdUsuario;
        nueva.IdRutina = (int) db.rutinaDao().insertarRutina(nueva);
        return nueva;
    }

    private Seccion buscarOInsertarSeccion(AppDatabase db, String nombre, int idRutina) {
        List<Seccion> existentes = db.seccionDao().obtenerSeccionesPorRutina(idRutina);
        for (Seccion s : existentes) {
            if (s.NombreSeccion.equalsIgnoreCase(nombre)) return s;
        }
        Seccion nueva = new Seccion();
        nueva.NombreSeccion = nombre;
        nueva.IdRutinaSeccion = idRutina;
        nueva.TipoSeccion = "Personalizado";
        nueva.IdSeccion = (int) db.seccionDao().insertarSeccion(nueva);
        return nueva;
    }

    private Ejercicio buscarOInsertarEjercicio(AppDatabase db, String nombre, String tipo) {
        List<Ejercicio> existentes = db.ejercicioDao().obtenerTodosLosEjercicios();
        for (Ejercicio e : existentes) {
            if (e.NombreEjercicio.equalsIgnoreCase(nombre)) return e;
        }
        Ejercicio nuevo = new Ejercicio();
        nuevo.NombreEjercicio = nombre;
        nuevo.TipoEjercicio = tipo;
        nuevo.PesoCorporalEjercicio = false; 
        nuevo.IdEjercicio = (int) db.ejercicioDao().insertarEjercicio(nuevo);
        return nuevo;
    }

    private SeccionXejercicio buscarOInsertarRelacion(AppDatabase db, int idSeccion, int idEjercicio) {
        SeccionXejercicio sxe = db.seccionXejercicioDao().getRelacion(idSeccion, idEjercicio);
        if (sxe != null) return sxe;

        SeccionXejercicio nuevo = new SeccionXejercicio();
        nuevo.IdSeccion = idSeccion;
        nuevo.IdEjercicio = idEjercicio;
        nuevo.IdSeccionXejercicio = (int) db.seccionXejercicioDao().insert(nuevo);
        return nuevo;
    }

    private Entrenamiento asegurarEntrenamiento(AppDatabase db, int idSeccion, long fecha) {
        List<Entrenamiento> existentes = db.entrenamientoDao().getEntrenamientosByUsuario(usuarioLogueado.IdUsuario);
        for (Entrenamiento e : existentes) {
            if (e.IdSeccion == idSeccion && Math.abs(e.FechaInicio - fecha) < 3600000) { 
                return e;
            }
        }
        Entrenamiento nuevo = new Entrenamiento();
        nuevo.IdUsuario = usuarioLogueado.IdUsuario;
        nuevo.IdSeccion = idSeccion;
        nuevo.FechaInicio = fecha;
        nuevo.NumeroEntrenamiento = existentes.size() + 1;
        nuevo.IdEntrenamiento = (int) db.entrenamientoDao().insert(nuevo);
        return nuevo;
    }

    private void cerrarSesion() {
        usuarioLogueado = null;
        userRepo.eliminarSesion();

        Intent intent = new Intent(this, InicioSesionActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();

        Toast.makeText(this, "Sesión cerrada", Toast.LENGTH_SHORT).show();
    }
}

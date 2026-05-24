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
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.migymsito.data.Ejercicio;
import com.example.migymsito.data.Entrenamiento;
import com.example.migymsito.data.Historial;
import com.example.migymsito.data.Registro;
import com.example.migymsito.data.Rutina;
import com.example.migymsito.data.Seccion;
import com.example.migymsito.data.SeccionXejercicio;
import com.example.migymsito.data.Usuario;
import com.example.migymsito.dataDataBase.AppDatabase;
import com.example.migymsito.dataRepository.UsuarioRepository;
import com.example.migymsito.utils.LocaleHelper;
import com.example.migymsito.utils.NotificationHelper;
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

public class MainActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private NavController navController;
    private SharedViewModel sharedViewModel;
    public static Usuario usuarioLogueado;
    private UsuarioRepository userRepo;
    private Dialog progressDialog;
    private volatile boolean importacionCancelada = false;
    public static boolean isAppInForeground = false;

    private final ActivityResultLauncher<String> importLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            this::importarDatosDesdeCsv
    );

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        LocaleHelper.applyLocale(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedViewModel = new ViewModelProvider(this).get(SharedViewModel.class);
        userRepo = new UsuarioRepository(getApplication());

        NotificationHelper.createNotificationChannel(this);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        
        // El ID 'include_toolbar' es el ID de la etiqueta <include> que sobreescribe el ID root del layout incluido.
        Toolbar toolbar = findViewById(R.id.include_toolbar);

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        
        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();
            setupToolbar(toolbar);
            setupNavigationDrawer();
        }

        if (usuarioLogueado == null) {
            int idUsuario = userRepo.obtenerIdSesion();
            if (idUsuario != -1) {
                userRepo.obtenerUsuarioPorId(idUsuario, user -> {
                    if (user != null) {
                        usuarioLogueado = user;
                        actualizarNombreHeader();
                    }
                });
            }
        } else {
            actualizarNombreHeader();
        }

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (drawerLayout != null && drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else if (navController != null && !navController.popBackStack()) {
                    finish();
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        isAppInForeground = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        isAppInForeground = false;
    }

    private void setupToolbar(Toolbar toolbar) {
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayShowTitleEnabled(false);
            }

            ImageButton menuButton = toolbar.findViewById(R.id.toolbar_menu_button);
            if (menuButton != null) {
                menuButton.setOnClickListener(v -> {
                    if (drawerLayout != null) {
                        drawerLayout.openDrawer(GravityCompat.START);
                    }
                });
            }

            TextView tvUsername = toolbar.findViewById(R.id.toolbar_username);
            ImageView ivUserIcon = toolbar.findViewById(R.id.toolbar_user_icon);

            View.OnClickListener perfilClickListener = v -> {
                if (navController != null) navController.navigate(R.id.datosPersonalesFragment);
            };

            if (tvUsername != null) tvUsername.setOnClickListener(perfilClickListener);
            if (ivUserIcon != null) ivUserIcon.setOnClickListener(perfilClickListener);
        }
    }

    public void actualizarNombreHeader() {
        // Buscamos dentro de la actividad por si el include no está expuesto directamente
        TextView tvUsername = findViewById(R.id.toolbar_username);
        if (tvUsername != null && usuarioLogueado != null) {
            tvUsername.setText(usuarioLogueado.NombreUsuario);
        }
    }

    private void setupNavigationDrawer() {
        if (navigationView != null) {
            navigationView.setNavigationItemSelectedListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.Home) {
                    userRepo.eliminarRutinaSeleccionada();
                    Bundle bundle = new Bundle();
                    bundle.putBoolean("cambiarRutina", true);
                    navController.navigate(R.id.rutinasFragment, bundle);
                } else if (itemId == R.id.MiPerfil) {
                    navController.navigate(R.id.datosPersonalesFragment);
                } else if (itemId == R.id.MiProgreso) {
                    navController.navigate(R.id.estadisticasFragment);
                } else if (itemId == R.id.ComparativaRendimientos) {
                    navController.navigate(R.id.compararEntrenamientosFragment);
                } else if (itemId == R.id.Historial) {
                    navController.navigate(R.id.historialPesoFragment);
                } else if (itemId == R.id.Exportar) {
                    mostrarPopUpExportar();
                } else if (itemId == R.id.Importar) {
                    importLauncher.launch("text/*");
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

        CheckBox cbProgresoEjercicios = dialog.findViewById(R.id.cbConfirmarExportar);
        CheckBox cbHistorialPeso = dialog.findViewById(R.id.cbExportarHistorialPeso);
        Button btnAceptar = dialog.findViewById(R.id.btnAceptarExportar);
        Button btnCancelar = dialog.findViewById(R.id.btnCancelarExportar);

        btnAceptar.setOnClickListener(v -> {
            boolean incluirProgreso = cbProgresoEjercicios.isChecked();
            boolean incluirHistorial = cbHistorialPeso.isChecked();
            exportarDatosCsv(incluirProgreso, incluirHistorial);
            dialog.dismiss();
        });

        btnCancelar.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void exportarDatosCsv(boolean incluirProgreso, boolean incluirHistorial) {
        if (usuarioLogueado == null) {
            Toast.makeText(this, "Inicia sesión para exportar", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            try {
                AppDatabase db = AppDatabase.getDatabase(getApplicationContext());
                StringBuilder csv = new StringBuilder();
                String sep = ";";

                if (incluirProgreso) {
                    csv.append("Rutina").append(sep).append("Seccion").append(sep).append("Ejercicio").append(sep)
                            .append("Tipo").append(sep).append("Fecha").append(sep).append("Peso").append(sep)
                            .append("Serie").append(sep).append("Reps").append(sep).append("Peso Corporal")
                            .append(sep).append("Peso del Usuario en ese momento\n");
                } else {
                    csv.append("Rutina").append(sep).append("Seccion").append(sep).append("Ejercicio").append(sep)
                            .append("Tipo").append(sep).append("Peso Corporal\n");
                }

                List<Rutina> rutinas = db.rutinaDao().obtenerRutinasPorUsuario(usuarioLogueado.IdUsuario);

                for (Rutina r : rutinas) {
                    List<Seccion> secciones = db.seccionDao().obtenerSeccionesPorRutina(r.IdRutina);
                    for (Seccion s : secciones) {
                        List<Ejercicio> ejercicios = db.ejercicioDao().obtenerEjerciciosPorSeccion(s.IdSeccion);
                        for (Ejercicio ej : ejercicios) {
                            String base = r.NombreRutina + sep + s.NombreSeccion + sep + ej.NombreEjercicio + sep + ej.TipoEjercicio;

                            if (incluirProgreso) {
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

                if (incluirHistorial) {
                    List<Historial> historiales = db.historialDao().obtenerHistorialPorUsuario(usuarioLogueado.IdUsuario);
                    if (historiales != null && !historiales.isEmpty()) {
                        csv.append("\n--- HISTORIAL DE PESO ---\n");
                        csv.append("HISTORIAL_PESO;Fecha;Peso;Altura\n");
                        SimpleDateFormat sdfHist = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                        for (Historial h : historiales) {
                            csv.append("HISTORIAL_PESO").append(sep)
                                    .append(sdfHist.format(new Date(h.FechaHistorial))).append(sep)
                                    .append(String.valueOf(h.PesoHistorial).replace(".", ",")).append(sep)
                                    .append(String.valueOf(h.AlturaHistorial).replace(".", ",")).append("\n");
                        }
                    }
                }

                generarYCompartirArchivo(csv.toString(), incluirProgreso || incluirHistorial);

            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, "Error al exportar: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        }).start();
    }

    private void generarYCompartirArchivo(String contenido, boolean esCompleto) {
        try {
            File path = new File(getCacheDir(), "rutinas");
            if (!path.exists()) {
                boolean created = path.mkdirs();
                if (!created) return;
            }
            File file = new File(path, esCompleto ? "MiGymsito_Completo.csv" : "MiGymsito_Estructura.csv");

            try (FileOutputStream out = new FileOutputStream(file)) {
                out.write(contenido.getBytes());
            }

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

    private void importarDatosDesdeCsv(Uri uri) {
        if (uri == null || usuarioLogueado == null) return;

        try (InputStream isCheck = getContentResolver().openInputStream(uri);
             BufferedReader readerCheck = new BufferedReader(new InputStreamReader(isCheck))) {
            String primeraLinea = readerCheck.readLine();
            if (primeraLinea == null || !primeraLinea.contains("Rutina")) {
                Toast.makeText(this, "El archivo seleccionado no tiene un formato CSV válido de MiGymsito", Toast.LENGTH_LONG).show();
                return;
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error al leer el archivo seleccionado", Toast.LENGTH_SHORT).show();
            return;
        }

        importacionCancelada = false;
        mostrarPopUpImportacion();

        new Thread(() -> {
            try {
                AppDatabase db = AppDatabase.getDatabase(getApplicationContext());
                try (InputStream is = getContentResolver().openInputStream(uri);
                     BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {

                    db.runInTransaction(() -> {
                        try {
                            String linea = reader.readLine();
                            String sep = ";";

                            while ((linea = reader.readLine()) != null) {
                                if (importacionCancelada) throw new RuntimeException("CANCEL");

                                if (linea.isEmpty() || linea.startsWith("-")) continue;
                                String[] datos = linea.split(sep);
                                if (datos.length < 4) continue;

                                if (datos[0].equalsIgnoreCase("HISTORIAL_PESO")) {
                                    try {
                                        String fechaStr = datos[1];
                                        double peso = Double.parseDouble(datos[2].replace(",", "."));
                                        double altura = Double.parseDouble(datos[3].replace(",", "."));

                                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                                        Date parsedDate = sdf.parse(fechaStr);
                                        if (parsedDate != null) {
                                            long fechaLong = parsedDate.getTime();

                                            Historial h = new Historial();
                                            h.IdUsuarioHistorial = usuarioLogueado.IdUsuario;
                                            h.FechaHistorial = fechaLong;
                                            h.PesoHistorial = peso;
                                            h.AlturaHistorial = altura;

                                            db.historialDao().insertarHistorial(h);
                                        }
                                    } catch (Exception ignored) {}
                                    continue;
                                }

                                if (datos[0].equalsIgnoreCase("Rutina") || datos[0].equalsIgnoreCase("TIPO_REGISTRO")) continue;

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
                                        Date parsedDate = sdf.parse(fechaStr);
                                        if (parsedDate != null) {
                                            long fechaLong = parsedDate.getTime();

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
                                        }

                                    } catch (Exception ignored) {}
                                }
                            }
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    });
                }

                ocultarPopUpImportacion();
                runOnUiThread(() -> {
                    Toast.makeText(this, "Importación completada", Toast.LENGTH_SHORT).show();
                    sharedViewModel.notifyImportFinished();
                });

            } catch (Exception e) {
                ocultarPopUpImportacion();
                if (e.getCause() != null && "CANCEL".equals(e.getCause().getMessage()) || "CANCEL".equals(e.getMessage())) {
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Importación cancelada", Toast.LENGTH_SHORT).show();
                    });
                } else {
                    runOnUiThread(() -> Toast.makeText(this, "Error al importar: " + e.getMessage(), Toast.LENGTH_LONG).show());
                }
            }
        }).start();
    }

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
}

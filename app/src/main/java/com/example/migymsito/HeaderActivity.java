package com.example.migymsito;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.LayoutRes;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.migymsito.data.Ejercicio;
import com.example.migymsito.data.Registro;
import com.example.migymsito.data.Rutina;
import com.example.migymsito.data.Seccion;
import com.example.migymsito.data.Usuario;
import com.example.migymsito.dataDataBase.AppDatabase;
import com.example.migymsito.dataRepository.UsuarioRepository;
import com.google.android.material.navigation.NavigationView;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public abstract class HeaderActivity extends AppCompatActivity {

    protected DrawerLayout drawerLayout;
    protected NavigationView navigationView;

    // Variable estática para la sesión global
    public static Usuario usuarioLogueado;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1. Intentamos recuperar del Intent si viene (Login manual)
        if (getIntent() != null && getIntent().hasExtra("usuario")) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                usuarioLogueado = getIntent().getSerializableExtra("usuario", Usuario.class);
            } else {
                usuarioLogueado = (Usuario) getIntent().getSerializableExtra("usuario");
            }
        }

        // 2. Si no hay usuario en memoria (ej. se reinició la app), intentamos recuperar de SharedPreferences
        if (usuarioLogueado == null) {
            UsuarioRepository repo = new UsuarioRepository(getApplication());
            int idSesion = repo.obtenerIdSesion();
            if (idSesion != -1) {
                repo.obtenerUsuarioPorId(idSesion, usuario -> {
                    if (usuario != null) {
                        usuarioLogueado = usuario;
                        actualizarNombreHeader();
                    }
                });
            }
        }

        // Manejo moderno del botón atrás
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
                    if (!(this instanceof RutinasActivity)) {
                        Intent intent = new Intent(this, RutinasActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        startActivity(intent);
                    }
                } else if (itemId == R.id.MiPerfil) {
                    Intent intent = new Intent(this, DatosPersonalesActivity.class);
                    intent.putExtra("usuario", usuarioLogueado);
                    startActivity(intent);
                } else if (itemId == R.id.MiProgreso) {
                    Intent intent = new Intent(this, EstadisticasActivity.class);
                    intent.putExtra("usuario", usuarioLogueado);
                    startActivity(intent);
                } else if (itemId == R.id.Exportar) {
                    mostrarPopUpExportar();
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
                                if (registros == null || registros.isEmpty()) {
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

    private void cerrarSesion() {
        usuarioLogueado = null;
        UsuarioRepository repository = new UsuarioRepository(getApplication());
        repository.eliminarSesion();

        Intent intent = new Intent(this, InicioSesionActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();

        Toast.makeText(this, "Sesión cerrada", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed() {
        // Mantenemos onBackPressed por compatibilidad pero delegamos en el dispatcher si es necesario
        super.onBackPressed();
    }
}

package com.example.migymsito;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.migymsito.data.Usuario;
import com.example.migymsito.dataRepository.UsuarioRepository;
import com.example.migymsito.utils.BackupManager;
import com.example.migymsito.utils.LocaleHelper;
import com.example.migymsito.utils.NotificationHelper;
import com.google.android.material.navigation.NavigationView;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private NavController navController;
    private SharedViewModel sharedViewModel;
    private BackupManager backupManager;
    private AlertDialog loadingDialog;

    public static Usuario usuarioLogueado;
    private UsuarioRepository userRepo;
    public static boolean isAppInForeground = false;

    private final ActivityResultLauncher<String[]> filePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.OpenDocument(),
            uri -> {
                if (uri != null) {
                    importarCopia(uri);
                }
            }
    );

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        LocaleHelper.applyLocale(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedViewModel = new ViewModelProvider(this).get(SharedViewModel.class);
        userRepo = new UsuarioRepository(getApplication());
        backupManager = new BackupManager(this);

        NotificationHelper.createNotificationChannel(this);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        
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
            if (tvUsername != null && usuarioLogueado != null) {
                tvUsername.setText(usuarioLogueado.NombreUsuario);
            }
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
                } else if (itemId == R.id.MisRegistros) {
                    navController.navigate(R.id.misRegistrosFragment);
                } else if (itemId == R.id.CopiaSeguridad) {
                    mostrarPopUpCopiaSeguridad();
                }

                if (drawerLayout != null) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                }
                return true;
            });
        }
    }

    public void actualizarNombreHeader() {
        if (usuarioLogueado == null) return;

        // Update Toolbar Username
        TextView tvToolbarUsername = findViewById(R.id.toolbar_username);
        if (tvToolbarUsername != null) {
            tvToolbarUsername.setText(usuarioLogueado.NombreUsuario);
        }
    }

    public void mostrarPopUpCopiaSeguridad() {
        View view = getLayoutInflater().inflate(R.layout.pop_up_copia_seguridad, null);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(view)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        view.findViewById(R.id.btnExportarCopia).setOnClickListener(v -> {
            exportarCopia();
            dialog.dismiss();
        });

        view.findViewById(R.id.btnImportarCopia).setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Importar Copia")
                    .setMessage("Esto borrará todos tus datos actuales y los reemplazará con los de la copia. ¿Continuar?")
                    .setPositiveButton("Sí", (d, w) -> {
                        filePickerLauncher.launch(new String[]{"application/json"});
                        dialog.dismiss();
                    })
                    .setNegativeButton("No", null)
                    .show();
        });

        view.findViewById(R.id.btnCerrarCopia).setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    public void exportarCopia() {
        mostrarLoading("Generando copia de seguridad...");
        backupManager.exportFullBackup(new BackupManager.BackupCallback() {
            @Override
            public void onComplete(boolean success, Uri fileUri) {
                runOnUiThread(() -> {
                    ocultarLoading();
                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                    shareIntent.setType("application/json");
                    shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
                    shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    startActivity(Intent.createChooser(shareIntent, "Compartir Copia de Seguridad"));
                });
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> {
                    ocultarLoading();
                    Toast.makeText(MainActivity.this, "Error: " + message, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    public void lanzarSelectorDeArchivo() {
        filePickerLauncher.launch(new String[]{"application/json"});
    }

    public void importarCopia(Uri uri) {
        mostrarLoading("Importando datos...");
        backupManager.importFullBackup(uri, new BackupManager.ImportCallback() {
            @Override
            public void onComplete(boolean success) {
                runOnUiThread(() -> {
                    ocultarLoading();
                    Toast.makeText(MainActivity.this, "Importación exitosa. Reinicia la aplicación.", Toast.LENGTH_LONG).show();
                    // Reiniciar sesión del usuario actual
                    userRepo.obtenerPrimerUsuario(user -> {
                        usuarioLogueado = user;
                        actualizarNombreHeader();
                        navController.navigate(R.id.Home);
                    });
                });
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> {
                    ocultarLoading();
                    Toast.makeText(MainActivity.this, "Error al importar: " + message, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void mostrarLoading(String mensaje) {
        if (loadingDialog != null && loadingDialog.isShowing()) return;

        View view = getLayoutInflater().inflate(R.layout.pop_up_loading_backup, null);
        TextView tvMessage = view.findViewById(R.id.tvLoadingMessage);
        if (tvMessage != null) tvMessage.setText(mensaje);

        loadingDialog = new AlertDialog.Builder(this)
                .setView(view)
                .setCancelable(false)
                .create();

        if (loadingDialog.getWindow() != null) {
            loadingDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        // Mantener la pantalla encendida
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        
        loadingDialog.show();
    }

    private void ocultarLoading() {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
        // Permitir que la pantalla se apague normalmente
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }
}

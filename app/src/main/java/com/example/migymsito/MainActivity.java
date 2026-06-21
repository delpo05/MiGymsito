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
import com.example.migymsito.utils.LocaleHelper;
import com.example.migymsito.utils.NotificationHelper;
import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private NavController navController;
    private SharedViewModel sharedViewModel;

    public static Usuario usuarioLogueado;
    private UsuarioRepository userRepo;
    public static boolean isAppInForeground = false;

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
}

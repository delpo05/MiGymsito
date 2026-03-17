package com.example.migymsito;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.LayoutRes;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import com.example.migymsito.data.Usuario;
import com.google.android.material.navigation.NavigationView;

public abstract class HeaderActivity extends AppCompatActivity {

    protected DrawerLayout drawerLayout;
    protected NavigationView navigationView;
    
    // Variable estática para que todas las pantallas compartan la misma sesión
    public static Usuario usuarioLogueado; 

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Si el Intent trae un usuario (ej. desde el Login), actualizamos la sesión global
        if (getIntent() != null && getIntent().hasExtra("usuario")) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                usuarioLogueado = getIntent().getSerializableExtra("usuario", Usuario.class);
            } else {
                usuarioLogueado = (Usuario) getIntent().getSerializableExtra("usuario");
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Cada vez que volvemos a una pantalla, refrescamos el nombre del header
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
            tvUsername.setText(usuarioLogueado.nombreUsuario);
        }
    }

    protected void setupNavigationDrawer() {
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);

        if (navigationView != null) {
            navigationView.setNavigationItemSelectedListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.MiPerfil) {
                    Intent intent = new Intent(this, DatosPersonalesActivity.class);
                    // Ya no es estrictamente necesario pasar el extra, pero lo dejamos por compatibilidad
                    intent.putExtra("usuario", usuarioLogueado); 
                    startActivity(intent);
                } else if (itemId == R.id.Historial) {
                    Toast.makeText(this, "Historial", Toast.LENGTH_SHORT).show();
                } else if (itemId == R.id.MiProgreso) {
                    Toast.makeText(this, "Mi Progreso", Toast.LENGTH_SHORT).show();
                }
                
                if (drawerLayout != null) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                }
                return true;
            });
        }
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout != null && drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}

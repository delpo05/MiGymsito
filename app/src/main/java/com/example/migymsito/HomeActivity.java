package com.example.migymsito;

import android.os.Bundle;
import android.view.View;
import android.widget.GridView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.migymsito.adapter.RutinasAdapter;
import com.example.migymsito.data.Usuario;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private GridView gvRutinas;
    private Usuario usuarioActual;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.secciones_rutinas);

        // Recuperar el usuario si se pasó por el intent
        usuarioActual = (Usuario) getIntent().getSerializableExtra("usuario");

        gvRutinas = findViewById(R.id.gvRutinas);
        TextView tvUsername = findViewById(R.id.toolbar_username);

        if (tvUsername != null && usuarioActual != null) {
            tvUsername.setText(usuarioActual.nombreUsuario);
        } else if (tvUsername != null) {
            tvUsername.setText("Invitado");
        }

        configurarGridView();
        configurarWindowInsets(R.id.layout_secciones);
    }

    private void configurarGridView() {
        List<String> rutinasDummy = new ArrayList<>();
        // Aquí podrías cargar las rutinas reales desde la DB usando un ViewModel o Repository
        RutinasAdapter adapter = new RutinasAdapter(rutinasDummy);
        gvRutinas.setAdapter(adapter);
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

package com.example.migymsito;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.migymsito.data.Historial;
import com.example.migymsito.dataRepository.UsuarioRepository;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DatosPersonalesActivity extends HeaderActivity {

    private TextView tvHolaNombre;
    private EditText etNombre, etCorreo, etPassword, etFecha, etGenero, etAltura, etPeso;
    private UsuarioRepository usuarioRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.datos_personales);

        // Inicializar repositorio
        usuarioRepository = new UsuarioRepository(getApplication());

        // Vincular vistas (Respetando tus IDs del XML)
        vincularVistas();

        // Cargar datos si hay sesión activa
        if (usuarioLogueado != null) {
            cargarDatosUsuario();
        } else {
            Toast.makeText(this, "Sesión no iniciada", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void vincularVistas() {
        tvHolaNombre = findViewById(R.id.tvHolaNombre);
        etNombre = findViewById(R.id.etDatoNombre);
        etCorreo = findViewById(R.id.etDatoCorreo);
        etPassword = findViewById(R.id.etDatoPassword);
        etFecha = findViewById(R.id.etDatoFecha);
        etGenero = findViewById(R.id.etDatoGenero);
        etAltura = findViewById(R.id.etDatoAltura);
        etPeso = findViewById(R.id.etDatoPeso);
    }

    private void cargarDatosUsuario() {
        // 1. Datos básicos desde el objeto Usuario
        if (tvHolaNombre != null) tvHolaNombre.setText("Hola, " + usuarioLogueado.nombreUsuario + " !");
        if (etNombre != null) etNombre.setText(usuarioLogueado.nombreUsuario);
        if (etCorreo != null) etCorreo.setText(usuarioLogueado.correoElectronicoUsuario);
        if (etPassword != null) etPassword.setText(usuarioLogueado.contraseniaUsuario);
        if (etGenero != null) etGenero.setText(usuarioLogueado.generoUsuario);

        // 2. Formatear la fecha de nacimiento (Timestamp -> Texto dd/MM/yyyy)
        if (usuarioLogueado.fechaNacimiento != null && etFecha != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            etFecha.setText(sdf.format(new Date(usuarioLogueado.fechaNacimiento)));
        }

        // 3. Consultar el último historial físico (Peso y Altura)
        usuarioRepository.obtenerUltimoHistorial(usuarioLogueado.id, result -> {
            if (result != null) {
                if (etAltura != null) etAltura.setText(String.valueOf(result.AlturaHistorial));
                if (etPeso != null) etPeso.setText(String.valueOf(result.PesoHistorial));
            }
        });
    }
}
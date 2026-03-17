package com.example.migymsito;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.example.migymsito.data.Historial;
import com.example.migymsito.data.Usuario;
import com.example.migymsito.dataRepository.UsuarioRepository;

import java.text.ParseException;
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

        usuarioRepository = new UsuarioRepository(getApplication());
        vincularVistas();

        if (usuarioLogueado != null) {
            cargarDatosUsuario();
        } else {
            Toast.makeText(this, "Sesión no iniciada", Toast.LENGTH_SHORT).show();
            finish();
        }

        View btnGuardar = findViewById(R.id.btnGuardarDatos);
        if (btnGuardar != null) {
            btnGuardar.setOnClickListener(this::EventoBotonActualizar);
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
        if (tvHolaNombre != null) tvHolaNombre.setText("Hola, " + usuarioLogueado.nombreUsuario + " !");
        if (etNombre != null) etNombre.setText(usuarioLogueado.nombreUsuario);
        if (etCorreo != null) etCorreo.setText(usuarioLogueado.correoElectronicoUsuario);
        if (etPassword != null) etPassword.setText(usuarioLogueado.contraseniaUsuario);
        if (etGenero != null) etGenero.setText(usuarioLogueado.generoUsuario);

        if (usuarioLogueado.fechaNacimiento != null && etFecha != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            etFecha.setText(sdf.format(new Date(usuarioLogueado.fechaNacimiento)));
        }

        usuarioRepository.obtenerUltimoHistorial(usuarioLogueado.id, result -> {
            if (result != null) {
                if (etAltura != null) etAltura.setText(String.valueOf(result.AlturaHistorial));
                if (etPeso != null) etPeso.setText(String.valueOf(result.PesoHistorial));
            }
        });
    }

    public void EventoBotonActualizar(View view) {
        if (usuarioLogueado == null) {
            Toast.makeText(this, "Error: Sesión no válida", Toast.LENGTH_SHORT).show();
            return;
        }

        String nuevoNombre = etNombre.getText().toString().trim();
        String nuevoCorreo = etCorreo.getText().toString().trim();
        String nuevaPass = etPassword.getText().toString().trim();
        String nuevoGenero = etGenero.getText().toString().trim();
        String fechaStr = etFecha.getText().toString().trim();
        String pesoStr = etPeso.getText().toString().trim();
        String alturaStr = etAltura.getText().toString().trim();

        if (nuevoNombre.isEmpty() || nuevoCorreo.isEmpty() || nuevaPass.isEmpty()) {
            Toast.makeText(this, "Nombre, Correo y Contraseña obligatorios", Toast.LENGTH_SHORT).show();
            return;
        }

        // Actualizamos los datos del objeto local
        usuarioLogueado.nombreUsuario = nuevoNombre;
        usuarioLogueado.correoElectronicoUsuario = nuevoCorreo;
        usuarioLogueado.contraseniaUsuario = nuevaPass;
        usuarioLogueado.generoUsuario = nuevoGenero;

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Date date = sdf.parse(fechaStr);
            if (date != null) {
                usuarioLogueado.fechaNacimiento = date.getTime();
            }
        } catch (ParseException e) {
            Log.e("DatosPersonales", "Error parseando fecha: " + fechaStr);
        }

        Historial nuevoHistorial = null;
        try {
            if (!pesoStr.isEmpty() && !alturaStr.isEmpty()) {
                nuevoHistorial = new Historial();
                nuevoHistorial.IdUsuario = usuarioLogueado.id;
                nuevoHistorial.PesoHistorial = Double.parseDouble(pesoStr);
                nuevoHistorial.AlturaHistorial = Double.parseDouble(alturaStr);
                nuevoHistorial.FechaHistorial = System.currentTimeMillis();
            }
        } catch (NumberFormatException e) {
            Log.e("DatosPersonales", "Error en formato de peso/altura");
        }

        // LLAMADA AL REPOSITORIO
        usuarioRepository.actualizarPerfilUsuario(usuarioLogueado, nuevoHistorial, (success, errorMessage) -> {
            if (success) {
                Toast.makeText(this, "Datos actualizados correctamente", Toast.LENGTH_SHORT).show();
                
                // REFRESCAR EL HEADER Y EL SALUDO
                actualizarNombreHeader(); 
                if (tvHolaNombre != null) {
                    tvHolaNombre.setText("Hola, " + usuarioLogueado.nombreUsuario + " !");
                }
            } else {
                new AlertDialog.Builder(this)
                        .setTitle("Error de Base de Datos")
                        .setMessage(errorMessage)
                        .setPositiveButton("Cerrar", null)
                        .show();
            }
        });
    }
}
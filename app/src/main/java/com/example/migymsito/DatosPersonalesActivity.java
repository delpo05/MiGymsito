package com.example.migymsito;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.example.migymsito.data.Historial;
import com.example.migymsito.data.Usuario;
import com.example.migymsito.dataRepository.UsuarioRepository;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DatosPersonalesActivity extends HeaderActivity {

    private TextView tvHolaNombre;
    private EditText etNombre, etCorreo, etPassword, etFecha, etAltura, etPeso;
    private AutoCompleteTextView etGenero;
    private UsuarioRepository usuarioRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.datos_personales);

        usuarioRepository = new UsuarioRepository(getApplication());
        vincularVistas();
        configurarDropdownGenero();

        // ARREGLO: Configurar el DatePicker con múltiples triggers para asegurar que abra
        if (etFecha != null) {
            // Se abre al hacer click
            etFecha.setOnClickListener(v -> mostrarDatePicker());
            
            // Se abre si por alguna razón recibe el foco (aunque esté en focusable false)
            etFecha.setOnFocusChangeListener((v, hasFocus) -> {
                if (hasFocus) {
                    mostrarDatePicker();
                }
            });
        }

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

    private void configurarDropdownGenero() {
        String[] generos = {"Masculino", "Femenino", "Otro"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, generos);
        if (etGenero != null) {
            etGenero.setAdapter(adapter);
        }
    }

    private void mostrarDatePicker() {
        final Calendar c = Calendar.getInstance();
        
        String fechaActual = etFecha.getText().toString();
        if (!fechaActual.isEmpty()) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                Date date = sdf.parse(fechaActual);
                if (date != null) c.setTime(date);
            } catch (ParseException e) {
                Log.e("DatosPersonales", "Error parseando fecha");
            }
        }

        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year1, monthOfYear, dayOfMonth) -> {
                    String fechaSeleccionada = String.format(Locale.getDefault(), "%02d/%02d/%d", dayOfMonth, (monthOfYear + 1), year1);
                    etFecha.setText(fechaSeleccionada);
                }, year, month, day);

        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    private void cargarDatosUsuario() {
        if (tvHolaNombre != null) tvHolaNombre.setText("Hola, " + usuarioLogueado.nombreUsuario + " !");
        if (etNombre != null) etNombre.setText(usuarioLogueado.nombreUsuario);
        if (etCorreo != null) etCorreo.setText(usuarioLogueado.correoElectronicoUsuario);
        if (etPassword != null) etPassword.setText(usuarioLogueado.contraseniaUsuario);
        
        if (etGenero != null) {
            etGenero.setText(usuarioLogueado.generoUsuario, false);
        }

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
        if (usuarioLogueado == null) return;

        String nuevoNombre = etNombre.getText().toString().trim();
        String nuevoCorreo = etCorreo.getText().toString().trim();
        String nuevaPass = etPassword.getText().toString().trim();
        String nuevoGenero = etGenero.getText().toString().trim();
        String fechaStr = etFecha.getText().toString().trim();
        String pesoStr = etPeso.getText().toString().trim();
        String alturaStr = etAltura.getText().toString().trim();

        if (nuevoNombre.isEmpty() || nuevoCorreo.isEmpty() || nuevaPass.isEmpty()) {
            Toast.makeText(this, "Campos obligatorios incompletos", Toast.LENGTH_SHORT).show();
            return;
        }

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
            Log.e("DatosPersonales", "Error parseando fecha");
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
            Log.e("DatosPersonales", "Error formato peso/altura");
        }

        usuarioRepository.actualizarPerfilUsuario(usuarioLogueado, nuevoHistorial, (success, errorMessage) -> {
            if (success) {
                Toast.makeText(this, "Datos actualizados", Toast.LENGTH_SHORT).show();
                actualizarNombreHeader();
                if (tvHolaNombre != null) tvHolaNombre.setText("Hola, " + usuarioLogueado.nombreUsuario + " !");
            } else {
                new AlertDialog.Builder(this)
                        .setTitle("Error")
                        .setMessage(errorMessage)
                        .setPositiveButton("Cerrar", null)
                        .show();
            }
        });
    }
}
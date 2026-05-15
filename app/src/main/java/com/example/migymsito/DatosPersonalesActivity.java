package com.example.migymsito;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
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

        if (etFecha != null) {
            etFecha.setOnClickListener(v -> mostrarDatePicker());
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
                    etFecha.setError(null); // Limpiar error al seleccionar fecha
                }, year, month, day);

        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    private void cargarDatosUsuario() {
        if (tvHolaNombre != null) tvHolaNombre.setText("Hola, " + usuarioLogueado.NombreUsuario + " !");
        if (etNombre != null) etNombre.setText(usuarioLogueado.NombreUsuario);
        if (etCorreo != null) etCorreo.setText(usuarioLogueado.CorreoElectronicoUsuario);
        if (etPassword != null) etPassword.setText(usuarioLogueado.ContraseniaUsuario);
        
        if (etGenero != null) {
            etGenero.setText(usuarioLogueado.GeneroUsuario, false);
        }

        if (usuarioLogueado.FechaNacimientoUsuario != null && etFecha != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            etFecha.setText(sdf.format(new Date(usuarioLogueado.FechaNacimientoUsuario)));
        }

        usuarioRepository.obtenerUltimoHistorial(usuarioLogueado.IdUsuario, result -> {
            if (result != null) {
                if (etAltura != null) etAltura.setText(String.valueOf(result.AlturaHistorial));
                if (etPeso != null) etPeso.setText(String.valueOf(result.PesoHistorial));
            }
        });
    }

    public void EventoBotonActualizar(View view) {
        if (usuarioLogueado == null) return;

        boolean isValid = true;

        String nuevoNombre = etNombre.getText().toString().trim();
        String nuevoCorreo = etCorreo.getText().toString().trim();
        String nuevaPass = etPassword.getText().toString().trim();
        String nuevoGenero = etGenero.getText().toString().trim();
        String fechaStr = etFecha.getText().toString().trim();
        String pesoStr = etPeso.getText().toString().trim();
        String alturaStr = etAltura.getText().toString().trim();

        // Limpiar errores previos
        etNombre.setError(null);
        etCorreo.setError(null);
        etPassword.setError(null);
        etFecha.setError(null);
        etGenero.setError(null);
        etPeso.setError(null);
        etAltura.setError(null);

        // Validación Nombre
        if (nuevoNombre.isEmpty()) {
            etNombre.setError("El nombre es obligatorio");
            isValid = false;
        }

        // Validación Correo
        if (nuevoCorreo.isEmpty()) {
            etCorreo.setError("El correo es obligatorio");
            isValid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(nuevoCorreo).matches()) {
            etCorreo.setError("Correo inválido");
            isValid = false;
        }

        // Validación Contraseña
        if (nuevaPass.isEmpty()) {
            etPassword.setError("La contraseña es obligatoria");
            isValid = false;
        } else if (nuevaPass.length() < 6) {
            etPassword.setError("Mínimo 6 caracteres");
            isValid = false;
        }

        // Validación Fecha
        if (fechaStr.isEmpty()) {
            etFecha.setError("La fecha es obligatoria");
            isValid = false;
        }

        // Validación Género
        if (nuevoGenero.isEmpty()) {
            etGenero.setError("El género es obligatorio");
            isValid = false;
        }

        // Validación Peso
        double peso = 0;
        if (pesoStr.isEmpty()) {
            etPeso.setError("El peso es obligatorio");
            isValid = false;
        } else {
            try {
                peso = Double.parseDouble(pesoStr);
                if (peso <= 0) {
                    etPeso.setError("Debe ser mayor a 0");
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                etPeso.setError("Formato inválido");
                isValid = false;
            }
        }

        // Validación Altura
        double altura = 0;
        if (alturaStr.isEmpty()) {
            etAltura.setError("La altura es obligatoria");
            isValid = false;
        } else {
            try {
                altura = Double.parseDouble(alturaStr);
                if (altura <= 0) {
                    etAltura.setError("Debe ser mayor a 0");
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                etAltura.setError("Formato inválido");
                isValid = false;
            }
        }

        if (!isValid) {
            Toast.makeText(this, "Por favor, corrige los errores señalados", Toast.LENGTH_SHORT).show();
            return;
        }

        usuarioLogueado.NombreUsuario = nuevoNombre;
        usuarioLogueado.CorreoElectronicoUsuario = nuevoCorreo;
        usuarioLogueado.ContraseniaUsuario = nuevaPass;
        usuarioLogueado.GeneroUsuario = nuevoGenero;

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Date date = sdf.parse(fechaStr);
            if (date != null) {
                usuarioLogueado.FechaNacimientoUsuario = date.getTime();
            }
        } catch (ParseException e) {
            Log.e("DatosPersonales", "Error parseando fecha");
            etFecha.setError("Formato de fecha inválido");
            return;
        }

        Historial nuevoHistorial = new Historial();
        nuevoHistorial.IdUsuarioHistorial = usuarioLogueado.IdUsuario;
        nuevoHistorial.PesoHistorial = peso;
        nuevoHistorial.AlturaHistorial = altura;
        nuevoHistorial.FechaHistorial = System.currentTimeMillis();

        usuarioRepository.actualizarPerfilUsuario(usuarioLogueado, nuevoHistorial, (success, errorMessage) -> {
            if (success) {
                Toast.makeText(this, "¡Datos actualizados!", Toast.LENGTH_SHORT).show();
                actualizarNombreHeader();
                if (tvHolaNombre != null) tvHolaNombre.setText("Hola, " + usuarioLogueado.NombreUsuario + " !");
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

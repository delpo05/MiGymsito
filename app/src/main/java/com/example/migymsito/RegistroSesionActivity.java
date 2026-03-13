package com.example.migymsito;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.migymsito.data.Historial;
import com.example.migymsito.data.Usuario;
import com.example.migymsito.dataRepository.UsuarioRepository;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class RegistroSesionActivity extends AppCompatActivity {

    private EditText etRegNombre, etRegCorreo, etRegFechaNac, etRegPeso, etRegAltura, etRegContrasenia;
    private AutoCompleteTextView etRegGenero;
    private UsuarioRepository usuarioRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.registro_sesion_activity);

        usuarioRepository = new UsuarioRepository(getApplication());

        etRegNombre = findViewById(R.id.etRegNombre);
        etRegCorreo = findViewById(R.id.etRegCorreo);
        etRegContrasenia = findViewById(R.id.etRegContrasenia);
        etRegFechaNac = findViewById(R.id.etRegFechaNac);
        etRegPeso = findViewById(R.id.etRegPeso);
        etRegAltura = findViewById(R.id.etRegAltura);
        etRegGenero = findViewById(R.id.etRegGenero);

        String[] opcionesGenero = {"Masculino", "Femenino", "Otro"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, opcionesGenero);
        etRegGenero.setAdapter(adapter);

        etRegFechaNac.setOnClickListener(v -> mostrarDatePicker());

        configurarWindowInsets(R.id.registro);
    }

    private void mostrarDatePicker() {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year1, monthOfYear, dayOfMonth) -> {
                    String fechaSeleccionada = String.format(Locale.getDefault(), "%02d/%02d/%d", dayOfMonth, (monthOfYear + 1), year1);
                    etRegFechaNac.setText(fechaSeleccionada);
                }, year, month, day);

        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        datePickerDialog.show();
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

    public void EventoBotonRegistrar(View view) {
        if (!validacionesRegistrarUsuario()) {
            return;
        }

        String correo = etRegCorreo.getText().toString().trim();

        usuarioRepository.validarCorreoExistente(correo, usuarioExistente -> {
            if (usuarioExistente != null) {
                etRegCorreo.setError("Este correo ya está registrado");
                Toast.makeText(RegistroSesionActivity.this, "El correo ya existe", Toast.LENGTH_SHORT).show();
            } else {
                registrarNuevoUsuario();
            }
        });
    }

    private void registrarNuevoUsuario() {
        Usuario nuevoUsuario = new Usuario();
        nuevoUsuario.NombreUsuario = etRegNombre.getText().toString().trim();
        nuevoUsuario.CorreoElectronicoUsuario = etRegCorreo.getText().toString().trim();
        nuevoUsuario.ContraseniaUsuario = etRegContrasenia.getText().toString().trim();
        nuevoUsuario.GeneroUsuario = etRegGenero.getText().toString();

        String fechaString = etRegFechaNac.getText().toString();
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Date date = sdf.parse(fechaString);
            if (date != null) {
                nuevoUsuario.FechaNacimientoUsuario = date.getTime();
            }
        } catch (ParseException e) {
            nuevoUsuario.FechaNacimientoUsuario = 0L;
        }

        Historial nuevoHistorial = new Historial();
        nuevoHistorial.PesoHistorial = Double.valueOf(etRegPeso.getText().toString());
        nuevoHistorial.AlturaHistorial = Double.valueOf(etRegAltura.getText().toString());
        nuevoHistorial.FechaHistorial = System.currentTimeMillis();

        usuarioRepository.registrarUsuarioConHistorial(nuevoUsuario, nuevoHistorial, exito -> {
            if (exito) {
                Toast.makeText(RegistroSesionActivity.this, "Registro exitoso", Toast.LENGTH_SHORT).show();
                finish(); // Volver al login
            } else {
                Toast.makeText(RegistroSesionActivity.this, "Error al registrar usuario e historial", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void EventoBotonVolver(View view) {
        finish();
    }

    private boolean validacionesRegistrarUsuario() {
        boolean estado = true;

        if (etRegNombre.getText().toString().trim().isEmpty()) {
            etRegNombre.setError("Campo requerido");
            estado = false;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(etRegCorreo.getText().toString().trim()).matches()) {
            etRegCorreo.setError("Correo inválido");
            estado = false;
        }

        if (etRegContrasenia.getText().toString().trim().isEmpty()) {
            etRegContrasenia.setError("Campo requerido");
            estado = false;
        } else if (etRegContrasenia.getText().toString().length() < 6) {
            etRegContrasenia.setError("Mínimo 6 caracteres");
            estado = false;
        }

        if (etRegFechaNac.getText().toString().isEmpty()) {
            etRegFechaNac.setError("Campo requerido");
            estado = false;
        }

        if (etRegPeso.getText().toString().trim().isEmpty()) {
            etRegPeso.setError("Campo requerido");
            estado = false;
        }

        if (etRegAltura.getText().toString().trim().isEmpty()) {
            etRegAltura.setError("Campo requerido");
            estado = false;
        }

        if (etRegGenero.getText().toString().trim().isEmpty()) {
            etRegGenero.setError("Campo requerido");
            estado = false;
        }

        return estado;
    }
}

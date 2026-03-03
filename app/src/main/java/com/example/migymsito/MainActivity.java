package com.example.migymsito;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.migymsito.data.Usuario;
import com.example.migymsito.dataRepository.UsuarioRepository;


import java.util.Calendar;

public class MainActivity extends AppCompatActivity implements UsuarioRepository.RepositoryCallback<Usuario> {


    private EditText etUsuario, etPassword; // Login
    private EditText etRegNombre, etRegCorreo, etRegFechaNac, etRegPeso, etRegAltura, etRegContrasenia;
    private AutoCompleteTextView etRegGenero; // Registro
    private UsuarioRepository usuarioRepository;
    private Usuario usuario;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        mostrarLogin();
    }

    private void mostrarLogin() {
        setContentView(R.layout.activity_main);

        etUsuario = findViewById(R.id.etUsuario);
        etPassword = findViewById(R.id.etPassword);
        usuarioRepository = new UsuarioRepository(getApplication());
        configurarWindowInsets(R.id.main);
    }

    private void mostrarRegistro() {
        setContentView(R.layout.registro_sesion);
        etRegNombre = findViewById(R.id.etRegNombre);
        etRegCorreo = findViewById(R.id.etRegCorreo);
        etRegContrasenia = findViewById(R.id.etRegContrasenia);
        etRegFechaNac = findViewById(R.id.etRegFechaNac);
        etRegPeso = findViewById(R.id.etRegPeso);
        etRegAltura = findViewById(R.id.etRegAltura);
        etRegGenero = findViewById(R.id.etRegGenero);

        // Configuración del desplegable de Género
        String[] opcionesGenero = {"Masculino", "Femenino", "Otro"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, opcionesGenero);
        etRegGenero.setAdapter(adapter);

        // Configuración del Calendario para Fecha de Nacimiento
        etRegFechaNac.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mostrarDatePicker();
            }
        });

        configurarWindowInsets(R.id.registro);
    }

    private void mostrarDatePicker() {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year1, monthOfYear, dayOfMonth) -> {
                    // Formateamos la fecha para que siempre tenga 2 dígitos en día y mes
                    String fechaSeleccionada = String.format("%02d/%02d/%d", dayOfMonth, (monthOfYear + 1), year1);
                    etRegFechaNac.setText(fechaSeleccionada);
                }, year, month, day);

        // Establece la fecha máxima como "hoy" para que no puedan elegir el futuro
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

    public void EventoBotonContinuar(View view) {
        String usuario = etUsuario.getText().toString();
        String password = etPassword.getText().toString();

        if (usuario.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Por favor complete todos los campos", Toast.LENGTH_SHORT).show();
        } else {
            usuarioRepository.validarLogin(usuario, password, this);
        }
    }

    public void EventoRegistrarse(View view) {
        mostrarRegistro();
    }

    public void EventoBotonRegistrar(View view) {
        validacionesRegistrarUsuario(view);
        usuario = new Usuario();
        usuario.nombreUsuario = etRegNombre.getText().toString();
        usuario.correoElectronicoUsuario = etRegCorreo.getText().toString();
        usuario.contraseniaUsuario = etRegContrasenia.getText().toString();
        usuario.generoUsuario = etRegGenero.getText().toString();
        // --- Conversion de string a long en fecha nacimiento ---
        String fechaString = etRegFechaNac.getText().toString(); // "dd/MM/yyyy"
        try {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault());
            java.util.Date date = sdf.parse(fechaString);
            if (date != null) {
                usuario.fechaNacimiento = date.getTime(); // .getTime() devuelve el LONG que necesitas
            }
        } catch (java.text.ParseException e) {
            usuario.fechaNacimiento = 0L; // Valor por defecto si hay error
        }
    }

    public void validacionesRegistrarUsuario(View view) {
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

        if (estado) {
            Toast.makeText(this, "Registro exitoso", Toast.LENGTH_SHORT).show();
            mostrarLogin();
        }
    }

    @Override
    public void onResult(Usuario result) {
        if (result != null) {
            Toast.makeText(this, "Bienvenido " + result.nombreUsuario, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Usuario o contraseña incorrectos", Toast.LENGTH_SHORT).show();
        }
    }
}
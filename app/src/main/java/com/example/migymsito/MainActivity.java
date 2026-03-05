package com.example.migymsito;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.migymsito.data.Historial;
import com.example.migymsito.data.Usuario;
import com.example.migymsito.dataRepository.UsuarioRepository;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity implements UsuarioRepository.RepositoryCallback<Usuario> {

    private EditText etUsuario, etPassword; // Login
    private EditText etRegNombre, etRegCorreo, etRegFechaNac, etRegPeso, etRegAltura, etRegContrasenia;
    private AutoCompleteTextView etRegGenero; // Registro
    private UsuarioRepository usuarioRepository;

    // VARIABLE PARA GUARDAR LA SESIÓN DEL USUARIO
    private Usuario usuarioLogeado;

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

        String[] opcionesGenero = {"Masculino", "Femenino", "Otro"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, opcionesGenero);
        etRegGenero.setAdapter(adapter);

        etRegFechaNac.setOnClickListener(v -> mostrarDatePicker());
        configurarWindowInsets(R.id.registro);
    }

    private void mostrarSecciones() {
        setContentView(R.layout.secciones_rutinas);
        // Si no querés agregar el ID al XML todavía, comentamos la siguiente línea:
        // configurarWindowInsets(R.id.secciones_rutinas_root);

        // ACTUALIZAMOS EL NOMBRE DE USUARIO EN EL HEADER
        TextView tvUsername = findViewById(R.id.toolbar_username);
        if (tvUsername != null && usuarioLogeado != null) {
            tvUsername.setText(usuarioLogeado.nombreUsuario);
        }
    }



    private void mostrarDatePicker() {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year1, monthOfYear, dayOfMonth) -> {
                    String fechaSeleccionada = String.format("%02d/%02d/%d", dayOfMonth, (monthOfYear + 1), year1);
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

    // Funcion para volver al login desde el registro.
    public void EventoBotonVolver(View view) {
        mostrarLogin();
    }

    public void EventoBotonRegistrar(View view) {

        // --- Validamos que los campos en el formulario esten cargados correctamente.

        if (!validacionesRegistrarUsuario()) {
            return;
        }

        // --- Cargamos la variable de correo con el correo ingresado para utilizarla para validar.

        String correo = etRegCorreo.getText().toString().trim();

        // --- Validamos si el correo ya existe en la BD (UsuarioExistente es el resultado de la consulta)

        usuarioRepository.validarCorreoExistente(correo, usuarioExistente -> {
            if (usuarioExistente != null) {
                // El correo ya está registrado porque encontro coincidencia en la BD
                etRegCorreo.setError("Este correo ya está registrado");
                Toast.makeText(MainActivity.this, "El correo ya existe", Toast.LENGTH_SHORT).show();
            } else {
                // El correo no existe, procedemos a registrar
                registrarNuevoUsuario();
            }
        });
    }

    private void registrarNuevoUsuario() {

        // --- Creamos un objeto usuario y lo cargamos con los datos ingresados en el formulario.

        Usuario nuevoUsuario = new Usuario();
        nuevoUsuario.nombreUsuario = etRegNombre.getText().toString().trim();
        nuevoUsuario.correoElectronicoUsuario = etRegCorreo.getText().toString().trim();
        nuevoUsuario.contraseniaUsuario = etRegContrasenia.getText().toString().trim();
        nuevoUsuario.generoUsuario = etRegGenero.getText().toString();

        // --- Seteamos el input recibido como string y lo seteamos en la fecha de nacimiento en milisegundos (LONG).

        String fechaString = etRegFechaNac.getText().toString();
        try {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault());
            java.util.Date date = sdf.parse(fechaString);
            if (date != null) {
                nuevoUsuario.fechaNacimiento = date.getTime();
            }
        } catch (java.text.ParseException e) {
            nuevoUsuario.fechaNacimiento = 0L;
        }

        // --- Creamos el historial inicial
        Historial nuevoHistorial = new Historial();
        nuevoHistorial.PesoHistorial = Double.valueOf(etRegPeso.getText().toString());
        nuevoHistorial.AlturaHistorial = Double.valueOf(etRegAltura.getText().toString());
        nuevoHistorial.FechaHistorial = System.currentTimeMillis(); // Aquí registramos la fecha actual

        // --- Registramos ambos (Usuario e Historial) en una sola operación del repositorio
        usuarioRepository.registrarUsuarioConHistorial(nuevoUsuario, nuevoHistorial, exito -> {
            if (exito) {
                Toast.makeText(MainActivity.this, "Registro exitoso", Toast.LENGTH_SHORT).show();
                mostrarLogin();
            } else {
                Toast.makeText(MainActivity.this, "Error al registrar usuario e historial", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public boolean validacionesRegistrarUsuario() {
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

    @Override
    public void onResult(Usuario result) {
        if (result != null) {
            this.usuarioLogeado = result;
            Toast.makeText(this, "Bienvenido " + result.nombreUsuario, Toast.LENGTH_SHORT).show();
            mostrarSecciones();
        } else {
            Toast.makeText(this, "Usuario o contraseña incorrectos", Toast.LENGTH_SHORT).show();
        }
    }
}
package com.example.migymsito;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.migymsito.adapter.RegistroAdapter;
import com.example.migymsito.data.Ejercicio;
import com.example.migymsito.data.Historial;
import com.example.migymsito.data.Registro;
import com.example.migymsito.data.Rutina;
import com.example.migymsito.data.Seccion;
import com.example.migymsito.data.Usuario;
import com.example.migymsito.dataRepository.EjercicioRepository;
import com.example.migymsito.dataRepository.RegistroRepository;
import com.example.migymsito.dataRepository.RutinaRepository;
import com.example.migymsito.dataRepository.SeccionRepository;
import com.example.migymsito.dataRepository.UsuarioRepository;

import java.util.ArrayList;
import java.util.List;

public class CargarRegistroActivity extends HeaderActivity {

    private TextView tvNombreEjercicio, tvSerieValue;
    private EditText etRepeticiones, etPeso;
    private ImageButton btnRepUp, btnRepDown, btnPesoUp, btnPesoDown, btnEliminarUltimo;
    private Button btnCargar;
    private RecyclerView rvHistorial;
    private RegistroAdapter adapter;
    private List<Registro> listaHistorial = new ArrayList<>();

    private RegistroRepository registroRepository;
    private UsuarioRepository usuarioRepository;
    private RutinaRepository rutinaRepository;
    private SeccionRepository seccionRepository;
    private EjercicioRepository ejercicioRepository;

    //Esto se setea para tener registros creados de prueba
    private int serieActual = 1;
    private int idEjercicio = 1;
    private int idUsuario = 1;
    private String nombreEjercicio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cargar_registro_activity);

        registroRepository = new RegistroRepository(getApplication());
        usuarioRepository = new UsuarioRepository(getApplication());
        rutinaRepository = new RutinaRepository(getApplication());
        seccionRepository = new SeccionRepository(getApplication());
        ejercicioRepository = new EjercicioRepository(getApplication());

        initViews();        // Inserta los objetos con los recibidos por los inputs de la vista
        setupListeners();   // Configura los clics de los botones. (Aumentar / Descotar / Cargar registro)
        setupRecyclerView(); // Prepara el grid view con el registro Adapter para mostrar los datos

        //Verifica si existen datos de prueba en la DB. Esto es para setear
        verificarOPrepararEntorno();
    }

    private void initViews() {
        tvNombreEjercicio = findViewById(R.id.tvNombreEjercicio);
        tvSerieValue = findViewById(R.id.tvSerieValue);
        etRepeticiones = findViewById(R.id.etRepeticiones);
        etPeso = findViewById(R.id.etPeso);
        btnRepUp = findViewById(R.id.btnRepUp);
        btnRepDown = findViewById(R.id.btnRepDown);
        btnPesoUp = findViewById(R.id.btnPesoUp);
        btnPesoDown = findViewById(R.id.btnPesoDown);
        btnCargar = findViewById(R.id.btnCargar);
        btnEliminarUltimo = findViewById(R.id.btnEliminarUltimo);
        rvHistorial = findViewById(R.id.rvHistorial);

        tvNombreEjercicio.setText(nombreEjercicio);
        btnCargar.setEnabled(false); // Bloqueado hasta que idUsuario e idEjercicio sean válidos
    }

    private void setupListeners() {
        btnRepUp.setOnClickListener(v -> modificarValor(etRepeticiones, 1));
        btnRepDown.setOnClickListener(v -> modificarValor(etRepeticiones, -1));
        btnPesoUp.setOnClickListener(v -> modificarValor(etPeso, 1));
        btnPesoDown.setOnClickListener(v ->  modificarValor(etPeso, -1));
        btnCargar.setOnClickListener(v -> guardarRegistro());
        btnEliminarUltimo.setOnClickListener(v -> eliminarUltimoRegistro());
    }

    private void setupRecyclerView() {
        adapter = new RegistroAdapter(listaHistorial);
        rvHistorial.setLayoutManager(new LinearLayoutManager(this));
        rvHistorial.setAdapter(adapter);
    }

    //Logica de cambio de valor el peso y repeticiones
    private void modificarValor(EditText et, double delta) {
        try {
            double val = Double.parseDouble(et.getText().toString());
            val += delta;
            if (val < 0) val = 0;
            if (et.getId() == R.id.etRepeticiones) {
                et.setText(String.valueOf((int) val));
            } else {
                et.setText(String.valueOf(val));
            }
        } catch (Exception e) { et.setText("0"); }
    }

    private void cargarHistorial() {
        // Obtiene el historial del ejercicio
        registroRepository.obtenerHistorialPorEjercicio(idUsuario, idEjercicio, registros -> {
            listaHistorial.clear();
            listaHistorial.addAll(registros); // Llena la lista con registros si es que existen
            adapter.notifyDataSetChanged();   // Refresca la grilla.

            // Logica de incremento de serie:
            actualizarSerieActual(registros);
            
            btnCargar.setEnabled(true); // Ya es seguro guardar.
        });
    }

    private void actualizarSerieActual(List<Registro> registros) {
        if (!registros.isEmpty()) {
            int maxSerie = 0;
            for (Registro r : registros) {
                // Busca el número de serie más alto guardado.
                if (r.NumSeriesRegistro > maxSerie) maxSerie = r.NumSeriesRegistro;
            }
            serieActual = maxSerie + 1; // La siguiente serie será el máximo + 1.
        } else {
            serieActual = 1; // Si no hay registros, empieza en 1.
        }
        tvSerieValue.setText(String.valueOf(serieActual)); // Actualiza el número en pantalla.
    }

    private void guardarRegistro() {

        // verifica que los datos no esten vacios o sean 0
        String repStr = etRepeticiones.getText().toString();
        String pesoStr = etPeso.getText().toString();

        if (repStr.isEmpty() || pesoStr.isEmpty() || repStr.equals("0")) {
            Toast.makeText(this, "Introduce datos válidos", Toast.LENGTH_SHORT).show();
            return;
        }

        Registro nuevo = new Registro();
        nuevo.IdUsuarioRegistro = idUsuario;
        nuevo.IdEjercicioRegistro = idEjercicio;
        nuevo.NumSeriesRegistro = serieActual;
        nuevo.RepeticionesRegistro = Integer.parseInt(repStr);
        nuevo.PesoRegistro = Double.parseDouble(pesoStr);
        nuevo.FechaRegistro = System.currentTimeMillis();

        registroRepository.insertarRegistro(nuevo);

        // Añade el registro al grid view y lo mueve a la parte superior.
        listaHistorial.add(0, nuevo);
        adapter.notifyItemInserted(0);
        rvHistorial.scrollToPosition(0);

        //aumenta el numero de la seria
        serieActual++;
        tvSerieValue.setText(String.valueOf(serieActual));
        Toast.makeText(this, "Serie " + (serieActual-1) + " guardada", Toast.LENGTH_SHORT).show();
    }

    private void eliminarUltimoRegistro() {
        if (listaHistorial.isEmpty()) {
            Toast.makeText(this, "No hay registros para eliminar", Toast.LENGTH_SHORT).show();
            return;
        }

        // El último agregado es el primero de la lista (índice 0) debido al ORDER BY DESC y listaHistorial.add(0, nuevo)
        Registro ultimo = listaHistorial.get(0);
        
        registroRepository.eliminarRegistro(ultimo);
        listaHistorial.remove(0);
        adapter.notifyItemRemoved(0);

        // Recalcular la serie actual después de eliminar
        actualizarSerieActual(listaHistorial);

        Toast.makeText(this, "Último registro eliminado", Toast.LENGTH_SHORT).show();
    }

    /**
     Esta funcion verifica y crea datos de prueba para probrar esta vista.
     Mas adelante se debe reemplazar la logica de esta vista para que reciba por sesion los datos necesarios
     */
    private void verificarOPrepararEntorno() {
        usuarioRepository.validarCorreoExistente("prueba@gym.com", u -> {
            if (u == null) {
                Usuario newU = new Usuario();
                newU.nombreUsuario = "Admin Test";
                newU.correoElectronicoUsuario = "prueba@gym.com";
                newU.contraseniaUsuario = "1234";
                newU.generoUsuario = "M";
                newU.fechaNacimiento = System.currentTimeMillis();
                usuarioRepository.registrarUsuarioConHistorial(newU, new Historial(), success -> {
                    // Re-verificamos después de un breve delay para que Room procese
                    new Handler(Looper.getMainLooper()).postDelayed(this::verificarOPrepararEntorno, 300);
                });
            } else {
                idUsuario = u.id;
                rutinaRepository.obtenerRutinasDeUsuario(idUsuario, rutinas -> {
                    if (rutinas.isEmpty()) {
                        Rutina r = new Rutina();
                        r.IdUsuarioRutina = idUsuario;
                        r.NombreRutina = "Rutina de Prueba";
                        r.ColorRutina = "#FF5722";
                        rutinaRepository.insertarRutina(r);
                        new Handler(Looper.getMainLooper()).postDelayed(this::verificarOPrepararEntorno, 300);
                    } else {
                        int idRutina = rutinas.get(0).idRutina;
                        seccionRepository.obtenerSeccionesDeRutina(idRutina, secciones -> {
                            if (secciones.isEmpty()) {
                                Seccion s = new Seccion();
                                s.IdRutinaSeccion = idRutina;
                                s.NombreSeccion = "Pecho";
                                s.ColorSeccion = "#4CAF50";
                                seccionRepository.insertarSeccion(s);
                                new Handler(Looper.getMainLooper()).postDelayed(this::verificarOPrepararEntorno, 300);
                            } else {
                                int idSeccion = secciones.get(0).idSeccion;
                                ejercicioRepository.obtenerEjerciciosPorSeccion(idSeccion, ejercicios -> {
                                    if (ejercicios.isEmpty()) {
                                        Ejercicio e = new Ejercicio();
                                        e.idSeccionEjercicio = idSeccion;
                                        e.NombreEjercicio = "Press de Banca";
                                        e.EsCalistenico = false;
                                        ejercicioRepository.insertarEjercicio(e);
                                        new Handler(Looper.getMainLooper()).postDelayed(this::verificarOPrepararEntorno, 300);
                                    } else {
                                        Ejercicio e = ejercicios.get(0);
                                        idEjercicio = e.idEjercicio;
                                        nombreEjercicio = e.NombreEjercicio;
                                        tvNombreEjercicio.setText(nombreEjercicio);
                                        // Todo listo, cargamos el historial real
                                        cargarHistorial();
                                    }
                                });
                            }
                        });
                    }
                });
            }
        });
    }
}
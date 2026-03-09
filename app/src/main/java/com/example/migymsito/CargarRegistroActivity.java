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

        initViews();
        setupListeners();
        setupRecyclerView();

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
        btnCargar.setEnabled(false);
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
        registroRepository.obtenerHistorialPorEjercicio(idUsuario, idEjercicio, registros -> {
            listaHistorial.clear();
            listaHistorial.addAll(registros);
            adapter.notifyDataSetChanged();
            actualizarSerieActual(registros);
            btnCargar.setEnabled(true);
        });
    }

    private void actualizarSerieActual(List<Registro> registros) {
        if (!registros.isEmpty()) {
            int maxSerie = 0;
            for (Registro r : registros) {
                if (r.NumSeriesRegistro > maxSerie) maxSerie = r.NumSeriesRegistro;
            }
            serieActual = maxSerie + 1;
        } else {
            serieActual = 1;
        }
        tvSerieValue.setText(String.valueOf(serieActual));
    }

    private void guardarRegistro() {
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

        listaHistorial.add(0, nuevo);
        adapter.notifyItemInserted(0);
        rvHistorial.scrollToPosition(0);

        serieActual++;
        tvSerieValue.setText(String.valueOf(serieActual));
        Toast.makeText(this, "Serie " + (serieActual-1) + " guardada", Toast.LENGTH_SHORT).show();
    }

    private void eliminarUltimoRegistro() {
        if (listaHistorial.isEmpty()) {
            Toast.makeText(this, "No hay registros para eliminar", Toast.LENGTH_SHORT).show();
            return;
        }

        Registro ultimo = listaHistorial.get(0);
        registroRepository.eliminarRegistro(ultimo);
        listaHistorial.remove(0);
        adapter.notifyItemRemoved(0);
        actualizarSerieActual(listaHistorial);
        Toast.makeText(this, "Último registro eliminado", Toast.LENGTH_SHORT).show();
    }

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
                    new Handler(Looper.getMainLooper()).postDelayed(this::verificarOPrepararEntorno, 300);
                });
            } else {
                idUsuario = u.id;
                rutinaRepository.obtenerRutinasDeUsuario(idUsuario, rutinas -> {
                    if (rutinas.isEmpty()) {
                        Rutina r = new Rutina();
                        r.IdUsuarioRutina = idUsuario;
                        r.NombreRutina = "Rutina de Prueba";
                        rutinaRepository.insertarRutina(r);
                        new Handler(Looper.getMainLooper()).postDelayed(this::verificarOPrepararEntorno, 300);
                    } else {
                        int idRutina = rutinas.get(0).IdRutina;
                        seccionRepository.obtenerSeccionesDeRutina(idRutina, secciones -> {
                            if (secciones.isEmpty()) {
                                Seccion s = new Seccion();
                                s.IdRutinaSeccion = idRutina;
                                s.NombreSeccion = "Pecho";
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
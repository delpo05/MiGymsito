package com.example.migymsito;

import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.migymsito.adapter.RegistroAdapter;
import com.example.migymsito.data.Ejercicio;
import com.example.migymsito.data.Registro;
import com.example.migymsito.data.Seccion;
import com.example.migymsito.data.Usuario;
import com.example.migymsito.dataRepository.RegistroRepository;

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

    private int serieActual = 1;
    private int idEjercicio;
    private int idUsuario;
    private int idSeccion;
    private String nombreEjercicio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cargar_registro_activity);

        // Recuperar datos del Intent de forma segura
        Usuario usuario;
        Ejercicio ejercicio;
        Seccion seccion;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            usuario = getIntent().getSerializableExtra("usuario", Usuario.class);
            ejercicio = getIntent().getSerializableExtra("ejercicio", Ejercicio.class);
            seccion = getIntent().getSerializableExtra("seccion", Seccion.class);
        } else {
            usuario = (Usuario) getIntent().getSerializableExtra("usuario");
            ejercicio = (Ejercicio) getIntent().getSerializableExtra("ejercicio");
            seccion = (Seccion) getIntent().getSerializableExtra("seccion");
        }

        if (usuario != null && ejercicio != null) {
            idUsuario = usuario.IdUsuario;
            idEjercicio = ejercicio.IdEjercicio;
            nombreEjercicio = ejercicio.NombreEjercicio;
        }
        
        if (seccion != null) {
            idSeccion = seccion.IdSeccion;
        }

        registroRepository = new RegistroRepository(getApplication());

        initViews();
        setupListeners();
        setupRecyclerView();

        if (idUsuario != 0 && idEjercicio != 0) {
            cargarHistorial();
        }
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
            String text = et.getText().toString();
            double val = text.isEmpty() ? 0 : Double.parseDouble(text);
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

        double peso = Double.parseDouble(pesoStr);
        int reps = Integer.parseInt(repStr);

        btnCargar.setEnabled(false);
        registroRepository.guardarRegistroCompleto(idUsuario, idSeccion, idEjercicio, peso, serieActual, reps, nuevo -> {
            if (nuevo != null) {
                listaHistorial.add(0, nuevo);
                adapter.notifyItemInserted(0);
                rvHistorial.scrollToPosition(0);

                serieActual++;
                tvSerieValue.setText(String.valueOf(serieActual));
                Toast.makeText(CargarRegistroActivity.this, "Serie guardada", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(CargarRegistroActivity.this, "Error al guardar serie", Toast.LENGTH_SHORT).show();
            }
            btnCargar.setEnabled(true);
        });
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
}

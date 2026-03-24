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

    private TextView tvNombreEjercicio, tvSerieValue, tvPesoLabel, tvColumnaPeso;
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
    private boolean esPesoCorporal = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cargar_registro_activity);

        if (usuarioLogueado != null) {
            idUsuario = usuarioLogueado.IdUsuario;
        }

        Ejercicio ejercicio;
        Seccion seccion;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ejercicio = getIntent().getSerializableExtra("ejercicio", Ejercicio.class);
            seccion = getIntent().getSerializableExtra("seccion", Seccion.class);
        } else {
            ejercicio = (Ejercicio) getIntent().getSerializableExtra("ejercicio");
            seccion = (Seccion) getIntent().getSerializableExtra("seccion");
        }

        if (ejercicio != null) {
            idEjercicio = ejercicio.IdEjercicio;
            nombreEjercicio = ejercicio.NombreEjercicio;
            esPesoCorporal = (ejercicio.PesoCorporalEjercicio != null && ejercicio.PesoCorporalEjercicio);
        }
        
        if (seccion != null) {
            idSeccion = seccion.IdSeccion;
        }

        if (idUsuario == 0) {
            Toast.makeText(this, "Error: Sesión de usuario no encontrada", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        registroRepository = new RegistroRepository(getApplication());

        initViews();
        setupListeners();
        setupRecyclerView();

        if (idEjercicio != 0) {
            cargarSeriesDelDia();
        }
    }

    private void initViews() {
        tvNombreEjercicio = findViewById(R.id.tvNombreEjercicio);
        tvSerieValue = findViewById(R.id.tvSerieValue);
        tvPesoLabel = findViewById(R.id.tvPesoLabel);
        tvColumnaPeso = findViewById(R.id.tvColumnaPeso);
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

        if (esPesoCorporal) {
            tvPesoLabel.setText("Lastre (kg)");
            tvColumnaPeso.setText("Lastre");
        }
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
        adapter = new RegistroAdapter(listaHistorial, esPesoCorporal);
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

    private void cargarSeriesDelDia() {
        // Usamos el nuevo método que filtra solo por el entrenamiento activo
        registroRepository.obtenerRegistrosEntrenamientoActivo(idUsuario, idSeccion, idEjercicio, registros -> {
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

        if (repStr.isEmpty() || repStr.equals("0")) {
            Toast.makeText(this, "Introduce repeticiones válidas", Toast.LENGTH_SHORT).show();
            return;
        }

        if (pesoStr.isEmpty()) {
            pesoStr = "0";
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
                
                // Limpiar campos para la siguiente serie
                etRepeticiones.setText("");
                if (!esPesoCorporal) etPeso.setText("");
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

package com.example.migymsito;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;

import com.example.migymsito.data.Ejercicio;
import com.example.migymsito.data.ProgresoData;
import com.example.migymsito.data.Usuario;
import com.example.migymsito.dataDao.EjercicioDao;
import com.example.migymsito.dataDao.ProgresoDao;
import com.example.migymsito.dataDataBase.AppDatabase;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MiProgresoActivity extends HeaderActivity {

    private AutoCompleteTextView autoCompleteEjercicio;
    private LineChart lineChart;
    private TextView tvMaxPeso, tvTotalReps;
    private AppDatabase db;
    private int idUsuarioActual = 1; 
    private List<Ejercicio> listaEjercicios;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mi_progreso);

        Usuario usuario = (Usuario) getIntent().getSerializableExtra("usuario");
        if (usuario != null) {
            idUsuarioActual = usuario.IdUsuario;
            TextView tvUsername = findViewById(R.id.toolbar_username);
            if (tvUsername != null) tvUsername.setText(usuario.NombreUsuario);
        }

        db = AppDatabase.getInstance(this);
        initViews();
        cargarEjercicios();
    }

    private void initViews() {
        autoCompleteEjercicio = findViewById(R.id.autoCompleteEjercicio);
        lineChart = findViewById(R.id.lineChart);
        tvMaxPeso = findViewById(R.id.tvMaxPeso);
        tvTotalReps = findViewById(R.id.tvTotalReps);

        setupChart();

        autoCompleteEjercicio.setOnItemClickListener((parent, view, position, id) -> {
            String nombreSeleccionado = (String) parent.getItemAtPosition(position);
            for (Ejercicio e : listaEjercicios) {
                if (e.NombreEjercicio.equals(nombreSeleccionado)) {
                    cargarProgresoEjercicio(e.IdEjercicio);
                    break;
                }
            }
        });
    }

    private void setupChart() {
        lineChart.getDescription().setEnabled(false);
        lineChart.setTouchEnabled(true);
        lineChart.setDragEnabled(true);
        lineChart.setScaleEnabled(true);
        lineChart.setPinchZoom(true);
        lineChart.setDrawGridBackground(false);
        lineChart.getLegend().setTextColor(Color.WHITE);
        lineChart.setNoDataText("Selecciona un ejercicio para ver tu progreso");
        lineChart.setNoDataTextColor(Color.WHITE);

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(Color.WHITE);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(86400000f); 
        xAxis.setLabelRotationAngle(-45);
        xAxis.setValueFormatter(new ValueFormatter() {
            private final SimpleDateFormat mFormat = new SimpleDateFormat("dd/MM", Locale.getDefault());
            @Override
            public String getFormattedValue(float value) {
                return mFormat.format(new Date((long) value));
            }
        });

        lineChart.getAxisLeft().setTextColor(Color.WHITE);
        lineChart.getAxisLeft().setGridColor(Color.GRAY);
        lineChart.getAxisRight().setEnabled(false);
    }

    private void cargarEjercicios() {
        executorService.execute(() -> {
            EjercicioDao ejercicioDao = db.ejercicioDao();
            listaEjercicios = ejercicioDao.obtenerTodosLosEjercicios(); 
            
            runOnUiThread(() -> {
                if (listaEjercicios != null) {
                    List<String> nombres = new ArrayList<>();
                    for (Ejercicio e : listaEjercicios) nombres.add(e.NombreEjercicio);
                    
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                            android.R.layout.simple_dropdown_item_1line, nombres);
                    autoCompleteEjercicio.setAdapter(adapter);
                }
            });
        });
    }

    private void cargarProgresoEjercicio(int idEjercicio) {
        executorService.execute(() -> {
            ProgresoDao progresoDao = db.progresoDao();
            List<ProgresoData> datosPeso = progresoDao.obtenerProgresoPesoMaximo(idUsuarioActual, idEjercicio);
            Integer totalReps = progresoDao.obtenerTotalRepeticiones(idUsuarioActual, idEjercicio);

            runOnUiThread(() -> {
                if (datosPeso != null && !datosPeso.isEmpty()) {
                    actualizarGrafico(datosPeso);
                    double max = 0;
                    for (ProgresoData d : datosPeso) if (d.valor > max) max = d.valor;
                    tvMaxPeso.setText(String.format(Locale.getDefault(), "%.1f kg", max));
                } else {
                    lineChart.clear();
                    tvMaxPeso.setText("0 kg");
                }
                
                tvTotalReps.setText(String.valueOf(totalReps != null ? totalReps : 0));
            });
        });
    }

    private void actualizarGrafico(List<ProgresoData> datos) {
        List<Entry> entries = new ArrayList<>();
        for (ProgresoData d : datos) {
            entries.add(new Entry((float) d.fecha, (float) d.valor));
        }

        LineDataSet dataSet = new LineDataSet(entries, "Peso Máximo (kg)");
        dataSet.setColor(Color.parseColor("#BB86FC"));
        dataSet.setCircleColor(Color.WHITE);
        dataSet.setLineWidth(2.5f);
        dataSet.setCircleRadius(5f); 
        dataSet.setDrawCircleHole(true);
        dataSet.setCircleHoleColor(Color.parseColor("#1E1E1E"));
        dataSet.setCircleHoleRadius(2.5f);
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueTextSize(10f);
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(Color.parseColor("#BB86FC"));
        dataSet.setFillAlpha(40);
        
        // Si hay solo un punto, no usamos BEZIER para que se vea el círculo
        if (entries.size() > 1) {
            dataSet.setMode(LineDataSet.Mode.HORIZONTAL_BEZIER);
        } else {
            dataSet.setMode(LineDataSet.Mode.LINEAR);
        }

        LineData lineData = new LineData(dataSet);
        lineChart.setData(lineData);

        // Ajuste crucial: Si hay un solo punto, expandimos el eje X para que sea visible
        if (entries.size() == 1) {
            float dayMs = 86400000f;
            lineChart.getXAxis().setAxisMinimum(entries.get(0).getX() - dayMs);
            lineChart.getXAxis().setAxisMaximum(entries.get(0).getX() + dayMs);
        } else {
            lineChart.getXAxis().resetAxisMinimum();
            lineChart.getXAxis().resetAxisMaximum();
        }

        lineChart.animateX(800);
        lineChart.invalidate();
    }
}

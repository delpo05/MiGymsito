package com.example.migymsito;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import com.example.migymsito.data.Ejercicio;
import com.example.migymsito.data.Registro;
import com.example.migymsito.data.Seccion;
import com.example.migymsito.dataRepository.EjercicioRepository;
import com.example.migymsito.dataRepository.RegistroRepository;
import com.example.migymsito.dataRepository.SeccionRepository;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class EstadisticasActivity extends HeaderActivity {

    private AutoCompleteTextView autoCompleteSecciones, autoCompleteEjercicios, autoCompleteConsulta;
    private MaterialButton btnConsultarProgreso;
    private BarChart barChart;
    
    private SeccionRepository seccionRepository;
    private EjercicioRepository ejerciciosRepository;
    private RegistroRepository registroRepository;
    
    private List<Seccion> listaSecciones = new ArrayList<>();
    private List<Ejercicio> listaEjerciciosActuales = new ArrayList<>();
    private Ejercicio ejercicioSeleccionado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.estadisticas_activity);

        // Inicializar vistas
        autoCompleteSecciones = findViewById(R.id.autoCompleteSecciones);
        autoCompleteEjercicios = findViewById(R.id.autoCompleteEjercicios);
        autoCompleteConsulta = findViewById(R.id.autoCompleteConsulta);
        btnConsultarProgreso = findViewById(R.id.btnConsultarProgreso);
        barChart = findViewById(R.id.barChart);

        seccionRepository = new SeccionRepository(getApplication());
        ejerciciosRepository = new EjercicioRepository(getApplication());
        registroRepository = new RegistroRepository(getApplication());

        cargarSeccionesDelUsuario();
        configurarDropdownConsulta();
        configurarGrafico();

        btnConsultarProgreso.setOnClickListener(v -> consultarProgreso());
    }

    private void configurarGrafico() {
        if (barChart == null) return;
        
        barChart.getDescription().setEnabled(false);
        barChart.setDrawGridBackground(false);
        barChart.getLegend().setTextColor(Color.WHITE);
        
        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(Color.WHITE);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);

        barChart.getAxisLeft().setTextColor(Color.WHITE);
        barChart.getAxisRight().setEnabled(false);
        barChart.setNoDataText("Selecciona opciones y consulta para ver datos");
        barChart.setNoDataTextColor(Color.GRAY);
    }

    private void cargarSeccionesDelUsuario() {
        if (usuarioLogueado != null) {
            seccionRepository.obtenerSeccionesPorUsuario(usuarioLogueado.IdUsuario, secciones -> {
                this.listaSecciones = secciones;
                List<String> nombresSecciones = new ArrayList<>();
                for (Seccion s : secciones) {
                    nombresSecciones.add(s.NombreSeccion);
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.dropdown_item, nombresSecciones);
                autoCompleteSecciones.setAdapter(adapter);

                autoCompleteSecciones.setOnItemClickListener((parent, view, position, id) -> {
                    Seccion seleccionada = listaSecciones.get(position);
                    autoCompleteEjercicios.setText(""); 
                    ejercicioSeleccionado = null;
                    cargarEjerciciosDeSeccion(seleccionada.IdSeccion);
                });
            });
        }
    }

    private void cargarEjerciciosDeSeccion(int idSeccion) {
        ejerciciosRepository.obtenerEjerciciosPorSeccion(idSeccion, ejercicios -> {
            this.listaEjerciciosActuales = ejercicios;
            List<String> nombres = new ArrayList<>();
            for (Ejercicio e : ejercicios) {
                nombres.add(e.NombreEjercicio);
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.dropdown_item, nombres);
            autoCompleteEjercicios.setAdapter(adapter);

            autoCompleteEjercicios.setOnItemClickListener((parent, view, position, id) -> {
                ejercicioSeleccionado = listaEjerciciosActuales.get(position);
            });
        });
    }

    private void configurarDropdownConsulta() {
        String[] opcionesConsulta = {"Progreso de Cargas", "Volumen de Entrenamiento", "Frecuencia"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.dropdown_item, opcionesConsulta);
        autoCompleteConsulta.setAdapter(adapter);
    }

    private void consultarProgreso() {
        String consulta = autoCompleteConsulta.getText().toString();

        if (ejercicioSeleccionado == null) {
            Toast.makeText(this, "Por favor selecciona un ejercicio", Toast.LENGTH_SHORT).show();
            return;
        }

        if (consulta.equals("Progreso de Cargas")) {
            registroRepository.obtenerProgresoCargas(ejercicioSeleccionado.IdEjercicio, registros -> {
                if (registros == null || registros.isEmpty()) {
                    Toast.makeText(this, "No hay datos para este ejercicio", Toast.LENGTH_SHORT).show();
                    barChart.clear();
                    return;
                }
                mostrarGraficoBarras(registros);
            });
        } else {
            Toast.makeText(this, "Consulta no implementada aún", Toast.LENGTH_SHORT).show();
        }
    }

    private void mostrarGraficoBarras(List<Registro> registros) {
        List<BarEntry> entries = new ArrayList<>();
        final List<String> fechas = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM", Locale.getDefault());

        for (int i = 0; i < registros.size(); i++) {
            Registro r = registros.get(i);
            entries.add(new BarEntry(i, r.PesoRegistro.floatValue()));
            fechas.add(sdf.format(new Date(r.FechaRegistro)));
        }

        BarDataSet dataSet = new BarDataSet(entries, "Peso Máximo (kg)");
        dataSet.setColor(Color.parseColor("#BB86FC"));
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueTextSize(10f);

        BarData barData = new BarData(dataSet);
        barChart.setData(barData);

        // Formatear el eje X para mostrar fechas
        barChart.getXAxis().setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int index = (int) value;
                if (index >= 0 && index < fechas.size()) {
                    return fechas.get(index);
                }
                return "";
            }
        });

        barChart.animateY(1000);
        barChart.invalidate();
    }
}

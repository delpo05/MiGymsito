package com.example.migymsito;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import com.example.migymsito.data.Ejercicio;
import com.example.migymsito.data.Registro;
import com.example.migymsito.data.Rutina;
import com.example.migymsito.data.Seccion;
import com.example.migymsito.dataRepository.EjercicioRepository;
import com.example.migymsito.dataRepository.RegistroRepository;
import com.example.migymsito.dataRepository.RutinaRepository;
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
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class EstadisticasActivity extends HeaderActivity {

    private AutoCompleteTextView autoCompleteRutinas, autoCompleteSecciones, autoCompleteEjercicios, autoCompleteConsulta;
    private MaterialButton btnConsultarProgreso;
    private BarChart barChart;
    
    private RutinaRepository rutinaRepository;
    private SeccionRepository seccionRepository;
    private EjercicioRepository ejerciciosRepository;
    private RegistroRepository registroRepository;
    
    private List<Rutina> listaRutinas = new ArrayList<>();
    private List<Seccion> listaSecciones = new ArrayList<>();
    private List<Ejercicio> listaEjerciciosActuales = new ArrayList<>();
    private Ejercicio ejercicioSeleccionado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.estadisticas_activity);

        // Inicializar vistas
        autoCompleteRutinas = findViewById(R.id.autoCompleteRutinas);
        autoCompleteSecciones = findViewById(R.id.autoCompleteSecciones);
        autoCompleteEjercicios = findViewById(R.id.autoCompleteEjercicios);
        autoCompleteConsulta = findViewById(R.id.autoCompleteConsulta);
        btnConsultarProgreso = findViewById(R.id.btnConsultarProgreso);
        barChart = findViewById(R.id.barChart);

        rutinaRepository = new RutinaRepository(getApplication());
        seccionRepository = new SeccionRepository(getApplication());
        ejerciciosRepository = new EjercicioRepository(getApplication());
        registroRepository = new RegistroRepository(getApplication());

        cargarRutinasDelUsuario();
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

    private void cargarRutinasDelUsuario() {
        if (usuarioLogueado != null) {
            rutinaRepository.obtenerRutinasDeUsuario(usuarioLogueado.IdUsuario, rutinas -> {
                this.listaRutinas = rutinas;
                List<String> nombresRutinas = new ArrayList<>();
                for (Rutina r : rutinas) {
                    nombresRutinas.add(r.NombreRutina);
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.dropdown_item, nombresRutinas);
                autoCompleteRutinas.setAdapter(adapter);

                autoCompleteRutinas.setOnItemClickListener((parent, view, position, id) -> {
                    autoCompleteSecciones.setText("");
                    autoCompleteEjercicios.setText("");
                    ejercicioSeleccionado = null;
                    
                    // Al elegir una rutina, cargamos sus secciones
                    Rutina seleccionada = listaRutinas.get(position);
                    cargarSeccionesDeRutina(seleccionada.IdRutina);
                });
            });
        }
    }

    private void cargarSeccionesDeRutina(int idRutina) {
        seccionRepository.obtenerSeccionesDeRutina(idRutina, this::actualizarDropdownSecciones);
    }

    private void actualizarDropdownSecciones(List<Seccion> secciones) {
        this.listaSecciones = secciones;
        List<String> nombresSecciones = new ArrayList<>();
        nombresSecciones.add("Todas las secciones"); 
        
        for (Seccion s : secciones) {
            nombresSecciones.add(s.NombreSeccion);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.dropdown_item, nombresSecciones);
        autoCompleteSecciones.setAdapter(adapter);

        autoCompleteSecciones.setOnItemClickListener((parent, view, position, id) -> {
            autoCompleteEjercicios.setText(""); 
            ejercicioSeleccionado = null;
            
            if (position == 0) {
                // Sigue funcionando globalmente: trae todos los ejercicios con registros
                cargarEjerciciosEnUso();
            } else {
                Seccion seleccionada = listaSecciones.get(position - 1);
                cargarEjerciciosDeSeccion(seleccionada.IdSeccion);
            }
        });
    }

    private void cargarEjerciciosDeSeccion(int idSeccion) {
        ejerciciosRepository.obtenerEjerciciosPorSeccion(idSeccion, ejercicios -> {
            actualizarDropdownEjercicios(ejercicios);
        });
    }

    private void cargarEjerciciosEnUso() {
        if (usuarioLogueado != null) {
            ejerciciosRepository.obtenerEjerciciosEnUso(usuarioLogueado.IdUsuario, ejercicios -> {
                actualizarDropdownEjercicios(ejercicios);
            });
        }
    }

    private void actualizarDropdownEjercicios(List<Ejercicio> ejercicios) {
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
    }

    private void configurarDropdownConsulta() {
        String[] opcionesConsulta = {"Peso Máximo", "Volumen de Entrenamiento", "Frecuencia"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.dropdown_item, opcionesConsulta);
        autoCompleteConsulta.setAdapter(adapter);
    }

    private void consultarProgreso() {
        String consulta = autoCompleteConsulta.getText().toString();

        if (ejercicioSeleccionado == null) {
            Toast.makeText(this, "Por favor selecciona un ejercicio", Toast.LENGTH_SHORT).show();
            return;
        }

        if (consulta.equals("Peso Máximo")) {
            registroRepository.obtenerProgresoCargas(ejercicioSeleccionado.IdEjercicio, registros -> {
                if (registros == null || registros.isEmpty()) {
                    Toast.makeText(this, "No hay datos para este ejercicio", Toast.LENGTH_SHORT).show();
                    barChart.clear();
                    return;
                }
                mostrarGraficoBarras(registros, "Peso Máximo (kg)", false);
            });
        } else if (consulta.equals("Volumen de Entrenamiento")) {
            registroRepository.obtenerVolumenEntrenamiento(ejercicioSeleccionado.IdEjercicio, registros -> {
                if (registros == null || registros.isEmpty()) {
                    Toast.makeText(this, "No hay datos para este ejercicio", Toast.LENGTH_SHORT).show();
                    barChart.clear();
                    return;
                }
                mostrarGraficoBarras(registros, "Volumen Total (kg)", true);
            });
        } else {
            Toast.makeText(this, "Consulta no implementada aún", Toast.LENGTH_SHORT).show();
        }
    }

    private void mostrarGraficoBarras(List<Registro> registros, String etiqueta, boolean calcularVolumen) {
        List<BarEntry> entries = new ArrayList<>();
        final List<String> fechas = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM", Locale.getDefault());

        // Usamos un Map para consolidar los datos por fecha
        Map<String, Float> datosConsolidados = new LinkedHashMap<>();

        for (Registro r : registros) {
            String fechaStr = sdf.format(new Date(r.FechaRegistro));
            float valorRegistro;
            
            if (calcularVolumen) {
                // VOLUMEN = Peso * Repeticiones
                valorRegistro = r.PesoRegistro.floatValue() * r.Repeticiones;
            } else {
                // CARGA MÁXIMA
                valorRegistro = r.PesoRegistro.floatValue();
            }

            if (datosConsolidados.containsKey(fechaStr)) {
                if (calcularVolumen) {
                    datosConsolidados.put(fechaStr, datosConsolidados.get(fechaStr) + valorRegistro);
                } else {
                    datosConsolidados.put(fechaStr, Math.max(datosConsolidados.get(fechaStr), valorRegistro));
                }
            } else {
                datosConsolidados.put(fechaStr, valorRegistro);
            }
        }

        // Convertir el Map a las entradas del gráfico
        int i = 0;
        for (Map.Entry<String, Float> entry : datosConsolidados.entrySet()) {
            entries.add(new BarEntry(i++, entry.getValue()));
            fechas.add(entry.getKey());
        }

        BarDataSet dataSet = new BarDataSet(entries, etiqueta);
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

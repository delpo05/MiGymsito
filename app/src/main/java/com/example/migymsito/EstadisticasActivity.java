package com.example.migymsito;

import android.app.DatePickerDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
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
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

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
    private TextView tvFormulaEstadistica;
    private BarChart barChart;

    // Se utilizan para filtrar los registros obtenidos de la base de datos antes de graficarlos.
    // etFechaDesde: Campo para seleccionar la fecha de inicio del rango.
    // etFechaHasta: Campo para seleccionar la fecha de fin del rango.
    private TextInputEditText etFechaDesde, etFechaHasta;
    private Calendar calendarDesde = Calendar.getInstance();
    private Calendar calendarHasta = Calendar.getInstance();
    private SimpleDateFormat dateFormatoVisual = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    // --- NUEVO BOTÓN PARA LIMPIAR FILTROS ---
    private MaterialButton btnLimpiarFiltros;

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
        tvFormulaEstadistica = findViewById(R.id.tvFormulaEstadistica);
        barChart = findViewById(R.id.barChart);

        // Inicialización de los nuevos campos de fecha
        etFechaDesde = findViewById(R.id.etFechaDesde);
        etFechaHasta = findViewById(R.id.etFechaHasta);
        
        // Inicialización del botón de limpiar
        btnLimpiarFiltros = findViewById(R.id.btnLimpiarFiltros);

        rutinaRepository = new RutinaRepository(getApplication());
        seccionRepository = new SeccionRepository(getApplication());
        ejerciciosRepository = new EjercicioRepository(getApplication());
        registroRepository = new RegistroRepository(getApplication());

        cargarRutinasDelUsuario();
        configurarDropdownConsulta();
        configurarGrafico();
        configurarFiltrosFecha(); // Configura el comportamiento de los DatePickers

        btnConsultarProgreso.setOnClickListener(v -> consultarProgreso());

        // Configuración del click para limpiar filtros
        btnLimpiarFiltros.setOnClickListener(v -> limpiarFiltrosYCampos());
    }

    // --- LÓGICA PARA LIMPIAR FILTROS ---
    // Este método restablece los calendarios y limpia el texto de los campos de fecha.
    // Permite que el usuario vuelva a consultar toda la información sin restricciones.
    private void limpiarFiltrosYCampos() {
        etFechaDesde.setText("");
        etFechaHasta.setText("");
        calendarDesde = Calendar.getInstance();
        calendarHasta = Calendar.getInstance();
        Toast.makeText(this, "Filtros de fecha eliminados", Toast.LENGTH_SHORT).show();
    }

    // Este método asigna los Listeners a los campos de fecha para abrir un diálogo de selección (DatePicker).
    // Cuando el usuario elige una fecha, se actualiza el texto del campo y se ajusta el calendario correspondiente.
    private void configurarFiltrosFecha() {
        etFechaDesde.setOnClickListener(v -> mostrarDatePicker(calendarDesde, etFechaDesde));
        etFechaHasta.setOnClickListener(v -> mostrarDatePicker(calendarHasta, etFechaHasta));
    }

    private void mostrarDatePicker(Calendar calendar, TextInputEditText editText) {
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            // Si es fecha "Desde", ponemos al inicio del día (00:00:00)
            // Si es fecha "Hasta", ponemos al final del día (23:59:59) para incluir registros de ese día
            if (editText.getId() == R.id.etFechaDesde) {
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
            } else {
                calendar.set(Calendar.HOUR_OF_DAY, 23);
                calendar.set(Calendar.MINUTE, 59);
                calendar.set(Calendar.SECOND, 59);
            }
            
            editText.setText(dateFormatoVisual.format(calendar.getTime()));
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        
        datePickerDialog.show();
    }

    private void configurarGrafico() {
        if (barChart == null) return;

        barChart.getDescription().setEnabled(false);
        barChart.setDrawGridBackground(false);
        
        // --- CONFIGURAR LEYENDA (Mover arriba para evitar colisiones) ---
        Legend legend = barChart.getLegend();
        legend.setTextColor(Color.WHITE);
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setDrawInside(false);

        // --- HABILITAR ZOOM Y DESPLAZAMIENTO ---
        barChart.setTouchEnabled(true);
        barChart.setDragEnabled(true);
        barChart.setScaleEnabled(true);
        barChart.setPinchZoom(true);
        barChart.setDoubleTapToZoomEnabled(true);

        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(Color.WHITE);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        // Rotar etiquetas para que no se pisen si hay muchas
        xAxis.setLabelRotationAngle(-45f);

        barChart.getAxisLeft().setTextColor(Color.WHITE);
        barChart.getAxisRight().setEnabled(false);
        barChart.setNoDataText("Selecciona opciones y consulta para ver datos");
        barChart.setNoDataTextColor(Color.GRAY);
        
        // --- MARGEN INFERIOR (Aumentado para evitar colisiones con fechas rotadas) ---
        barChart.setExtraBottomOffset(40f);
    }

    private void cargarRutinasDelUsuario() {
        if (usuarioLogueado != null) {
            rutinaRepository.obtenerRutinasDeUsuario(usuarioLogueado.IdUsuario, rutinas -> {
                this.listaRutinas = rutinas;
                List<String> nombresRutinas = new ArrayList<>();
                nombresRutinas.add("Todas las rutinas");
                for (Rutina r : rutinas) {
                    nombresRutinas.add(r.NombreRutina);
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.dropdown_item, nombresRutinas);
                autoCompleteRutinas.setAdapter(adapter);

                autoCompleteRutinas.setOnItemClickListener((parent, view, position, id) -> {
                    autoCompleteSecciones.setText("");
                    autoCompleteEjercicios.setText("");
                    ejercicioSeleccionado = null;

                    // Todas las rutinas
                    if (position == 0) {
                        cargarTodasLasSeccionesDelUsuario();
                    } else {
                        // Rutina específica (position - 1 porque agregamos "Todas")
                        Rutina seleccionada = listaRutinas.get(position - 1);
                        cargarSeccionesDeRutina(seleccionada.IdRutina);
                    }
                });
            });
        }
    }

    private void cargarTodasLasSeccionesDelUsuario() {
        if (usuarioLogueado != null) {
            seccionRepository.obtenerSeccionesPorUsuario(usuarioLogueado.IdUsuario, this::actualizarDropdownSecciones);
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
                // Todas las secciones de la rutina elegida (o de todas las rutinas)
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
        String[] opcionesConsulta = {"Peso Máximo", "Volumen de Entrenamiento"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.dropdown_item, opcionesConsulta);
        autoCompleteConsulta.setAdapter(adapter);
    }

    private void consultarProgreso() {
        String consulta = autoCompleteConsulta.getText().toString();

        if (ejercicioSeleccionado == null) {
            Toast.makeText(this, "Por favor selecciona un ejercicio", Toast.LENGTH_SHORT).show();
            return;
        }

        // --- VALIDACIÓN DE RANGO DE FECHAS ---
        // Si uno de los campos está lleno, el otro también debe estarlo.
        // Si ambos están vacíos, se traen todos los registros.
        String desdeStr = etFechaDesde.getText().toString();
        String hastaStr = etFechaHasta.getText().toString();
        
        boolean tieneDesde = !desdeStr.isEmpty();
        boolean tieneHasta = !hastaStr.isEmpty();
        
        if (tieneDesde != tieneHasta) {
            Toast.makeText(this, "Debes completar ambos campos de fecha o ninguno", Toast.LENGTH_SHORT).show();
            return;
        }

        // VALIDACIÓN: La fecha hasta no puede ser menor a la fecha desde
        if (tieneDesde && tieneHasta) {
            if (calendarHasta.before(calendarDesde)) {
                Toast.makeText(this, "La fecha 'Hasta' no puede ser anterior a 'Desde'", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        if (consulta.equals("Peso Máximo")) {
            registroRepository.obtenerProgresoCargas(ejercicioSeleccionado.IdEjercicio, registros -> {
                if (registros == null || registros.isEmpty()) {
                    Toast.makeText(this, "No hay datos para este ejercicio", Toast.LENGTH_SHORT).show();
                    barChart.clear();
                    tvFormulaEstadistica.setVisibility(View.GONE);
                    return;
                }
                // Aplicamos el filtro de fecha manualmente sobre la lista obtenida
                List<Registro> registrosFiltrados = filtrarPorFecha(registros, tieneDesde, calendarDesde, calendarHasta);
                
                if (registrosFiltrados.isEmpty()) {
                    Toast.makeText(this, "No hay datos en el rango seleccionado", Toast.LENGTH_SHORT).show();
                    barChart.clear();
                    tvFormulaEstadistica.setVisibility(View.GONE);
                    return;
                }

                tvFormulaEstadistica.setText("Peso Máximo registrado por día");
                tvFormulaEstadistica.setVisibility(View.VISIBLE);
                mostrarGraficoBarras(registrosFiltrados, "Peso Máximo (kg)", false);
            });
        } else if (consulta.equals("Volumen de Entrenamiento")) {
            registroRepository.obtenerVolumenEntrenamiento(ejercicioSeleccionado.IdEjercicio, registros -> {
                if (registros == null || registros.isEmpty()) {
                    Toast.makeText(this, "No hay datos para este ejercicio", Toast.LENGTH_SHORT).show();
                    barChart.clear();
                    tvFormulaEstadistica.setVisibility(View.GONE);
                    return;
                }
                // Aplicamos el filtro de fecha manualmente sobre la lista obtenida
                List<Registro> registrosFiltrados = filtrarPorFecha(registros, tieneDesde, calendarDesde, calendarHasta);

                if (registrosFiltrados.isEmpty()) {
                    Toast.makeText(this, "No hay datos en el rango seleccionado", Toast.LENGTH_SHORT).show();
                    barChart.clear();
                    tvFormulaEstadistica.setVisibility(View.GONE);
                    return;
                }

                tvFormulaEstadistica.setText("Fórmula:  (Peso × Repeticiones × Series)");
                tvFormulaEstadistica.setVisibility(View.VISIBLE);
                mostrarGraficoBarras(registrosFiltrados, "Volumen Total (kg)", true);
            });
        } else {
            tvFormulaEstadistica.setVisibility(View.GONE);
            Toast.makeText(this, "Consulta no implementada aún", Toast.LENGTH_SHORT).show();
        }
    }

    // --- LÓGICA DE FILTRADO POR FECHA ---
    // Este método recorre la lista de registros y devuelve solo aquellos que se encuentren dentro del rango temporal seleccionado por el usuario.
    private List<Registro> filtrarPorFecha(List<Registro> original, boolean aplicarFiltro, Calendar desde, Calendar hasta) {
        if (!aplicarFiltro) return original;
        
        List<Registro> filtrados = new ArrayList<>();
        long inicio = desde.getTimeInMillis();
        long fin = hasta.getTimeInMillis();
        
        for (Registro r : original) {
            if (r.FechaRegistro >= inicio && r.FechaRegistro <= fin) {
                filtrados.add(r);
            }
        }
        return filtrados;
    }

    private void mostrarGraficoBarras(List<Registro> registros, String etiqueta, boolean calcularVolumen) {
        List<BarEntry> entries = new ArrayList<>();
        final List<String> fechas = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM", Locale.getDefault());

        Map<String, Float> datosConsolidados = new LinkedHashMap<>();

        for (Registro r : registros) {
            String fechaStr = sdf.format(new Date(r.FechaRegistro));
            float valorRegistro;

            if (calcularVolumen) {
                valorRegistro = r.PesoRegistro.floatValue() * r.Repeticiones;
            } else {
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

        int i = 0;
        for (Map.Entry<String, Float> entry : datosConsolidados.entrySet()) {
            entries.add(new BarEntry(i++, entry.getValue()));
            fechas.add(entry.getKey());
        }

        BarDataSet dataSet = new BarDataSet(entries, etiqueta);
        dataSet.setColor(Color.WHITE);
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueTextSize(10f);

        BarData barData = new BarData(dataSet);
        barChart.setData(barData);

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

        // --- LIMITAR BARRAS VISIBLES ---
        // Si hay muchos registros, mostramos solo los últimos 7 para que no se pisen
        if (fechas.size() > 7) {
            barChart.setVisibleXRangeMaximum(7);
            // Hacer scroll hasta el final para ver los datos más recientes
            barChart.moveViewToX(fechas.size() - 7);
        } else {
            barChart.setVisibleXRangeMaximum(fechas.size());
        }

        barChart.invalidate();
    }

    @Override
    protected void onImportFinished() {
        cargarRutinasDelUsuario();
    }
}

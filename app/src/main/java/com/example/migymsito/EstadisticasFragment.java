package com.example.migymsito;

import android.app.DatePickerDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

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
import java.util.Objects;

public class EstadisticasFragment extends Fragment {

    private AutoCompleteTextView autoCompleteRutinas, autoCompleteSecciones, autoCompleteEjercicios, autoCompleteConsulta;
    private TextView tvFormulaEstadistica;
    private BarChart barChart;

    private TextInputEditText etFechaDesde, etFechaHasta;
    private Calendar calendarDesde = Calendar.getInstance();
    private Calendar calendarHasta = Calendar.getInstance();
    private final SimpleDateFormat dateFormatoVisual = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    private RutinaRepository rutinaRepository;
    private SeccionRepository seccionRepository;
    private EjercicioRepository ejerciciosRepository;
    private RegistroRepository registroRepository;

    private List<Rutina> listaRutinas = new ArrayList<>();
    private List<Seccion> listaSecciones = new ArrayList<>();
    private List<Ejercicio> listaEjerciciosActuales = new ArrayList<>();
    private Ejercicio ejercicioSeleccionado;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.estadisticas_activity, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        if (getActivity() != null) {
            View toolbarInclude = getActivity().findViewById(R.id.include_toolbar);
            if (toolbarInclude != null) toolbarInclude.setVisibility(View.VISIBLE);
            
            rutinaRepository = new RutinaRepository(getActivity().getApplication());
            seccionRepository = new SeccionRepository(getActivity().getApplication());
            ejerciciosRepository = new EjercicioRepository(getActivity().getApplication());
            registroRepository = new RegistroRepository(getActivity().getApplication());
        }

        autoCompleteRutinas = view.findViewById(R.id.autoCompleteRutinas);
        autoCompleteSecciones = view.findViewById(R.id.autoCompleteSecciones);
        autoCompleteEjercicios = view.findViewById(R.id.autoCompleteEjercicios);
        autoCompleteConsulta = view.findViewById(R.id.autoCompleteConsulta);
        MaterialButton btnConsultarProgreso = view.findViewById(R.id.btnConsultarProgreso);
        tvFormulaEstadistica = view.findViewById(R.id.tvFormulaEstadistica);
        barChart = view.findViewById(R.id.barChart);

        etFechaDesde = view.findViewById(R.id.etFechaDesde);
        etFechaHasta = view.findViewById(R.id.etFechaHasta);
        
        MaterialButton btnLimpiarFiltros = view.findViewById(R.id.btnLimpiarFiltros);

        cargarRutinasDelUsuario();
        configurarDropdownConsulta();
        configurarGrafico();
        configurarFiltrosFecha(); 

        btnConsultarProgreso.setOnClickListener(v -> consultarProgreso());
        btnLimpiarFiltros.setOnClickListener(v -> limpiarFiltrosYCampos());
    }

    private void limpiarFiltrosYCampos() {
        if (etFechaDesde != null) etFechaDesde.setText("");
        if (etFechaHasta != null) etFechaHasta.setText("");
        calendarDesde = Calendar.getInstance();
        calendarHasta = Calendar.getInstance();
        Toast.makeText(getContext(), "Filtros de fecha eliminados", Toast.LENGTH_SHORT).show();
    }

    private void configurarFiltrosFecha() {
        if (etFechaDesde != null) etFechaDesde.setOnClickListener(v -> mostrarDatePicker(calendarDesde, etFechaDesde));
        if (etFechaHasta != null) etFechaHasta.setOnClickListener(v -> mostrarDatePicker(calendarHasta, etFechaHasta));
    }

    private void mostrarDatePicker(Calendar calendar, TextInputEditText editText) {
        DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(), (view, year, month, dayOfMonth) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

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
        
        Legend legend = barChart.getLegend();
        legend.setTextColor(Color.WHITE);
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setDrawInside(false);

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
        xAxis.setLabelRotationAngle(-45f);

        barChart.getAxisLeft().setTextColor(Color.WHITE);
        barChart.getAxisRight().setEnabled(false);
        barChart.setNoDataText("Selecciona opciones y consulta para ver datos");
        barChart.setNoDataTextColor(Color.GRAY);
        
        barChart.setExtraBottomOffset(40f);
    }

    private void cargarRutinasDelUsuario() {
        if (MainActivity.usuarioLogueado != null && rutinaRepository != null) {
            rutinaRepository.obtenerRutinasDeUsuario(MainActivity.usuarioLogueado.IdUsuario, rutinas -> {
                this.listaRutinas = rutinas;
                List<String> nombresRutinas = new ArrayList<>();
                nombresRutinas.add("Todas las rutinas");
                for (Rutina r : rutinas) {
                    nombresRutinas.add(r.NombreRutina);
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), R.layout.dropdown_item, nombresRutinas);
                autoCompleteRutinas.setAdapter(adapter);

                autoCompleteRutinas.setOnItemClickListener((parent, view, position, id) -> {
                    autoCompleteSecciones.setText("");
                    autoCompleteEjercicios.setText("");
                    ejercicioSeleccionado = null;

                    if (position == 0) {
                        cargarTodasLasSeccionesDelUsuario();
                    } else {
                        Rutina seleccionada = listaRutinas.get(position - 1);
                        cargarSeccionesDeRutina(seleccionada.IdRutina);
                    }
                });
            });
        }
    }

    private void cargarTodasLasSeccionesDelUsuario() {
        if (MainActivity.usuarioLogueado != null && seccionRepository != null) {
            seccionRepository.obtenerSeccionesPorUsuario(MainActivity.usuarioLogueado.IdUsuario, this::actualizarDropdownSecciones);
        }
    }

    private void cargarSeccionesDeRutina(int idRutina) {
        if (seccionRepository != null) {
            seccionRepository.obtenerSeccionesDeRutina(idRutina, this::actualizarDropdownSecciones);
        }
    }

    private void actualizarDropdownSecciones(List<Seccion> secciones) {
        this.listaSecciones = secciones;
        List<String> nombresSecciones = new ArrayList<>();
        nombresSecciones.add("Todas las secciones");

        for (Seccion s : secciones) {
            nombresSecciones.add(s.NombreSeccion);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), R.layout.dropdown_item, nombresSecciones);
        autoCompleteSecciones.setAdapter(adapter);

        autoCompleteSecciones.setOnItemClickListener((parent, view, position, id) -> {
            autoCompleteEjercicios.setText("");
            ejercicioSeleccionado = null;

            if (position == 0) {
                cargarEjerciciosEnUso();
            } else {
                Seccion seleccionada = listaSecciones.get(position - 1);
                cargarEjerciciosDeSeccion(seleccionada.IdSeccion);
            }
        });
    }

    private void cargarEjerciciosDeSeccion(int idSeccion) {
        if (ejerciciosRepository != null) {
            ejerciciosRepository.obtenerEjerciciosPorSeccion(idSeccion, this::actualizarDropdownEjercicios);
        }
    }

    private void cargarEjerciciosEnUso() {
        if (MainActivity.usuarioLogueado != null && ejerciciosRepository != null) {
            ejerciciosRepository.obtenerEjerciciosEnUso(MainActivity.usuarioLogueado.IdUsuario, this::actualizarDropdownEjercicios);
        }
    }

    private void actualizarDropdownEjercicios(List<Ejercicio> ejercicios) {
        this.listaEjerciciosActuales = ejercicios;
        List<String> nombres = new ArrayList<>();
        for (Ejercicio e : ejercicios) {
            nombres.add(e.NombreEjercicio);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), R.layout.dropdown_item, nombres);
        autoCompleteEjercicios.setAdapter(adapter);

        autoCompleteEjercicios.setOnItemClickListener((parent, view, position, id) -> {
            ejercicioSeleccionado = listaEjerciciosActuales.get(position);
        });
    }

    private void configurarDropdownConsulta() {
        String[] opcionesConsulta = {"Peso Máximo", "Volumen de Entrenamiento"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), R.layout.dropdown_item, opcionesConsulta);
        autoCompleteConsulta.setAdapter(adapter);
    }

    private void consultarProgreso() {
        String consulta = autoCompleteConsulta.getText().toString();

        if (ejercicioSeleccionado == null) {
            Toast.makeText(getContext(), "Por favor selecciona un ejercicio", Toast.LENGTH_SHORT).show();
            return;
        }

        String desdeStr = etFechaDesde != null ? Objects.requireNonNull(etFechaDesde.getText()).toString() : "";
        String hastaStr = etFechaHasta != null ? Objects.requireNonNull(etFechaHasta.getText()).toString() : "";
        
        boolean tieneDesde = !desdeStr.isEmpty();
        boolean tieneHasta = !hastaStr.isEmpty();
        
        if (tieneDesde != tieneHasta) {
            Toast.makeText(getContext(), "Debes completar ambos campos de fecha o ninguno", Toast.LENGTH_SHORT).show();
            return;
        }

        if (tieneDesde) {
            if (calendarHasta.before(calendarDesde)) {
                Toast.makeText(getContext(), "La fecha 'Hasta' no puede ser anterior a 'Desde'", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        if (consulta.equals("Peso Máximo")) {
            registroRepository.obtenerProgresoCargas(ejercicioSeleccionado.IdEjercicio, registros -> {
                if (registros == null || registros.isEmpty()) {
                    Toast.makeText(getContext(), "No hay datos para este ejercicio", Toast.LENGTH_SHORT).show();
                    barChart.clear();
                    tvFormulaEstadistica.setVisibility(View.GONE);
                    return;
                }
                List<Registro> registrosFiltrados = filtrarPorFecha(registros, tieneDesde, calendarDesde, calendarHasta);
                
                if (registrosFiltrados.isEmpty()) {
                    Toast.makeText(getContext(), "No hay datos en el rango seleccionado", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(getContext(), "No hay datos para este ejercicio", Toast.LENGTH_SHORT).show();
                    barChart.clear();
                    tvFormulaEstadistica.setVisibility(View.GONE);
                    return;
                }
                List<Registro> registrosFiltrados = filtrarPorFecha(registros, tieneDesde, calendarDesde, calendarHasta);

                if (registrosFiltrados.isEmpty()) {
                    Toast.makeText(getContext(), "No hay datos en el rango seleccionado", Toast.LENGTH_SHORT).show();
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
            Toast.makeText(getContext(), "Consulta no implementada aún", Toast.LENGTH_SHORT).show();
        }
    }

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
                    Float current = datosConsolidados.get(fechaStr);
                    datosConsolidados.put(fechaStr, (current != null ? current : 0f) + valorRegistro);
                } else {
                    Float current = datosConsolidados.get(fechaStr);
                    datosConsolidados.put(fechaStr, Math.max(current != null ? current : 0f, valorRegistro));
                }
            } else {
                datosConsolidados.put(fechaStr, valorRegistro);
            }
        }

        int index = 0;
        for (Map.Entry<String, Float> entry : datosConsolidados.entrySet()) {
            entries.add(new BarEntry(index++, entry.getValue()));
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
                int i = (int) value;
                if (i >= 0 && i < fechas.size()) {
                    return fechas.get(i);
                }
                return "";
            }
        });

        barChart.animateY(1000);

        if (fechas.size() > 7) {
            barChart.setVisibleXRangeMaximum(7);
            barChart.moveViewToX(fechas.size() - 7);
        } else {
            barChart.setVisibleXRangeMaximum(fechas.size());
        }

        barChart.invalidate();
    }
}

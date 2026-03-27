package com.example.migymsito;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.migymsito.adapter.HistorialPesoAdapter;
import com.example.migymsito.data.Historial;
import com.example.migymsito.dataRepository.HistorialRepository;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HistorialPesoActivity extends HeaderActivity {

    private RecyclerView rvHistorialPeso;
    private LineChart chartPeso;
    private HistorialRepository historialRepository;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.historial_peso_activity);

        rvHistorialPeso = findViewById(R.id.rvHistorialPeso);
        chartPeso = findViewById(R.id.chartPeso);
        historialRepository = new HistorialRepository(getApplication());

        rvHistorialPeso.setLayoutManager(new LinearLayoutManager(this));

        cargarDatos();
    }

    private void cargarDatos() {
        if (usuarioLogueado == null) {
            return;
        }

        historialRepository.obtenerHistorialPorUsuario(usuarioLogueado.IdUsuario, lista -> {
            if (lista != null && !lista.isEmpty()) {
                configurarTabla(lista);
                configurarGrafico(lista);
            } else {
                configurarTabla(new ArrayList<>());
                configurarGrafico(new ArrayList<>());
                Toast.makeText(this, "No hay historial de peso registrado", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void actualizarNombreHeader() {
        super.actualizarNombreHeader();
        if (usuarioLogueado != null && (rvHistorialPeso != null && rvHistorialPeso.getAdapter() == null)) {
            cargarDatos();
        }
    }

    private void configurarTabla(List<Historial> lista) {
        HistorialPesoAdapter adapter = new HistorialPesoAdapter(lista, this::mostrarPopUpConfirmacion);
        rvHistorialPeso.setAdapter(adapter);
    }

    private void mostrarPopUpConfirmacion(Historial historial) {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.pop_up_confirmacion_eliminar);
        
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        Button btnSi = dialog.findViewById(R.id.btnConfirmarSi);
        Button btnNo = dialog.findViewById(R.id.btnConfirmarNo);

        btnSi.setOnClickListener(v -> {
            historialRepository.eliminarHistorial(historial);
            Toast.makeText(this, "Registro eliminado", Toast.LENGTH_SHORT).show();
            cargarDatos();
            dialog.dismiss();
        });

        btnNo.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void configurarGrafico(List<Historial> lista) {
        if (lista.isEmpty()) {
            chartPeso.clear();
            return;
        }

        List<Entry> entries = new ArrayList<>();
        List<Historial> listaAsc = new ArrayList<>(lista);
        Collections.reverse(listaAsc);

        for (Historial h : listaAsc) {
            entries.add(new Entry(h.FechaHistorial.floatValue(), h.PesoHistorial.floatValue()));
        }

        LineDataSet dataSet = new LineDataSet(entries, "Evolución de Peso (kg)");
        int colorBlanco = getColor(R.color.blanco);
        
        dataSet.setColor(colorBlanco);
        dataSet.setCircleColor(colorBlanco);
        dataSet.setLineWidth(2f);
        dataSet.setCircleRadius(4f);
        dataSet.setDrawCircleHole(true);
        dataSet.setValueTextSize(10f);
        dataSet.setValueTextColor(colorBlanco);
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(colorBlanco);
        dataSet.setFillAlpha(50);

        LineData lineData = new LineData(dataSet);
        chartPeso.setData(lineData);

        XAxis xAxis = chartPeso.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(colorBlanco);
        xAxis.setValueFormatter(new ValueFormatter() {
            private final SimpleDateFormat mFormat = new SimpleDateFormat("dd/MM", Locale.getDefault());
            @Override
            public String getFormattedValue(float value) {
                return mFormat.format(new Date((long) value));
            }
        });
        xAxis.setLabelRotationAngle(-45);
        xAxis.setGranularity(1f);

        YAxis leftAxis = chartPeso.getAxisLeft();
        leftAxis.setTextColor(colorBlanco);
        chartPeso.getAxisRight().setEnabled(false);

        chartPeso.getLegend().setTextColor(colorBlanco);
        chartPeso.getDescription().setEnabled(false);
        chartPeso.animateX(1000);
        chartPeso.invalidate();
    }
}

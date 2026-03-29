package com.example.migymsito;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.migymsito.adapter.RegistroAdapter;
import com.example.migymsito.data.Ejercicio;
import com.example.migymsito.data.Registro;
import com.example.migymsito.data.Seccion;
import com.example.migymsito.dataRepository.RegistroRepository;
import com.example.migymsito.dataRepository.UsuarioRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CargarRegistroActivity extends HeaderActivity {

    private TextView tvNombreEjercicio, tvSerieValue, tvPesoLabel, tvColumnaPeso;
    private NumberPicker npRepeticiones, npPesoEntero, npPesoDecimal;
    private ImageButton btnEliminarUltimo;
    private Button btnCargar;
    private RecyclerView rvHistorial;
    private RegistroAdapter adapter;
    private List<Registro> listaHistorial = new ArrayList<>();

    private RegistroRepository registroRepository;
    private UsuarioRepository usuarioRepository;

    private int serieActual = 1;
    private int idEjercicio;
    private int idUsuario;
    private int idSeccion;
    private String nombreEjercicio;
    private boolean esPesoCorporal = false;

    // --- Variables del Temporizador ---
    private long startTimeInMillis = 60000; 
    private TextView tvTimerValue;
    private ImageButton btnStartTimer, btnResetTimer;
    private CountDownTimer countDownTimer;
    private boolean timerRunning;
    private long timeLeftInMillis = startTimeInMillis;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cargar_registro_activity);

        usuarioRepository = new UsuarioRepository(getApplication());
        registroRepository = new RegistroRepository(getApplication());

        if (usuarioLogueado != null) {
            idUsuario = usuarioLogueado.IdUsuario;
            continuarCarga();
        } else {
            idUsuario = usuarioRepository.obtenerIdSesion();
            if (idUsuario != -1) {
                usuarioRepository.obtenerUsuarioPorId(idUsuario, usuario -> {
                    if (usuario != null) {
                        usuarioLogueado = usuario;
                        continuarCarga();
                    } else {
                        Toast.makeText(this, "Error: Sesión de usuario no encontrada", Toast.LENGTH_LONG).show();
                        finish();
                    }
                });
            } else {
                Toast.makeText(this, "Error: No hay una sesión activa", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    private void continuarCarga() {
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

        initViews();
        setupPickers();
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
        
        npRepeticiones = findViewById(R.id.npRepeticiones);
        npPesoEntero = findViewById(R.id.npPesoEntero);
        npPesoDecimal = findViewById(R.id.npPesoDecimal);
        
        btnCargar = findViewById(R.id.btnCargar);
        btnEliminarUltimo = findViewById(R.id.btnEliminarUltimo);
        rvHistorial = findViewById(R.id.rvHistorial);

        tvTimerValue = findViewById(R.id.tvTimerValue);
        btnStartTimer = findViewById(R.id.btnStartTimer);
        btnResetTimer = findViewById(R.id.btnResetTimer);

        tvNombreEjercicio.setText(nombreEjercicio);

        if (esPesoCorporal) {
            tvPesoLabel.setText("Lastre (kg)");
            tvColumnaPeso.setText("Lastre");
        }
    }

    private void setupPickers() {
        npRepeticiones.setDescendantFocusability(NumberPicker.FOCUS_AFTER_DESCENDANTS);
        npPesoEntero.setDescendantFocusability(NumberPicker.FOCUS_AFTER_DESCENDANTS);
        npPesoDecimal.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);

        npRepeticiones.setMinValue(0);
        npRepeticiones.setMaxValue(100);
        npRepeticiones.setValue(10);
        npRepeticiones.setWrapSelectorWheel(true);

        npPesoEntero.setMinValue(0);
        npPesoEntero.setMaxValue(500);
        npPesoEntero.setValue(0);
        npPesoEntero.setWrapSelectorWheel(true);

        String[] valoresDecimales = {"00", "25", "50", "75"};
        npPesoDecimal.setMinValue(0);
        npPesoDecimal.setMaxValue(valoresDecimales.length - 1);
        npPesoDecimal.setDisplayedValues(valoresDecimales);
        npPesoDecimal.setWrapSelectorWheel(true);
    }

    private void setupListeners() {
        btnCargar.setOnClickListener(v -> guardarRegistro());
        btnEliminarUltimo.setOnClickListener(v -> eliminarUltimoRegistro());

        btnStartTimer.setOnClickListener(v -> {
            if (timerRunning) {
                pauseTimer();
            } else {
                startTimer();
            }
        });

        btnResetTimer.setOnClickListener(v -> resetTimer());
        tvTimerValue.setOnClickListener(v -> mostrarDialogoAjustarTiempo());
    }

    private void mostrarDialogoAjustarTiempo() {
        if (timerRunning) {
            pauseTimer();
        }

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_ajustar_tiempo, null);
        NumberPicker npMinutos = dialogView.findViewById(R.id.npMinutos);
        NumberPicker npSegundos = dialogView.findViewById(R.id.npSegundos);

        npMinutos.setMinValue(0);
        npMinutos.setMaxValue(10);
        npMinutos.setValue((int) (startTimeInMillis / 1000) / 60);

        npSegundos.setMinValue(0);
        npSegundos.setMaxValue(59);
        npSegundos.setValue((int) (startTimeInMillis / 1000) % 60);

        new AlertDialog.Builder(this)
                .setTitle("Tiempo de descanso")
                .setView(dialogView)
                .setPositiveButton("Aceptar", (dialog, which) -> {
                    int min = npMinutos.getValue();
                    int seg = npSegundos.getValue();
                    startTimeInMillis = (min * 60 + seg) * 1000L;
                    resetTimer();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void startTimer() {
        if (timeLeftInMillis <= 0) {
            resetTimer();
        }

        countDownTimer = new CountDownTimer(timeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftInMillis = millisUntilFinished;
                updateCountDownText();
            }

            @Override
            public void onFinish() {
                timerRunning = false;
                btnStartTimer.setImageResource(android.R.drawable.ic_media_play);
                timeLeftInMillis = startTimeInMillis;
                updateCountDownText();
                vibrarAlFinalizar();
                Toast.makeText(CargarRegistroActivity.this, "¡Descanso terminado!", Toast.LENGTH_SHORT).show();
            }
        }.start();

        timerRunning = true;
        btnStartTimer.setImageResource(android.R.drawable.ic_media_pause);
    }

    private void pauseTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        timerRunning = false;
        btnStartTimer.setImageResource(android.R.drawable.ic_media_play);
    }

    private void resetTimer() {
        pauseTimer();
        timeLeftInMillis = startTimeInMillis;
        updateCountDownText();
    }

    private void updateCountDownText() {
        int minutes = (int) (timeLeftInMillis / 1000) / 60;
        int seconds = (int) (timeLeftInMillis / 1000) % 60;
        String timeLeftFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
        tvTimerValue.setText(timeLeftFormatted);
    }

    private void vibrarAlFinalizar() {
        try {
            Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            if (vibrator != null && vibrator.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
                } else {
                    vibrator.vibrate(500);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupRecyclerView() {
        adapter = new RegistroAdapter(listaHistorial, esPesoCorporal);
        rvHistorial.setLayoutManager(new LinearLayoutManager(this));
        rvHistorial.setAdapter(adapter);
    }

    private void cargarSeriesDelDia() {
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
            Registro ultimoRegistro = registros.get(0);
            for (Registro r : registros) {
                if (r.NumSeriesRegistro > maxSerie) maxSerie = r.NumSeriesRegistro;
            }
            serieActual = maxSerie + 1;
            npRepeticiones.setValue(ultimoRegistro.Repeticiones);
            double peso = ultimoRegistro.PesoRegistro;
            int entero = (int) peso;
            double decimal = peso - entero;
            npPesoEntero.setValue(entero);
            if (decimal < 0.125) npPesoDecimal.setValue(0);
            else if (decimal < 0.375) npPesoDecimal.setValue(1);
            else if (decimal < 0.625) npPesoDecimal.setValue(2);
            else npPesoDecimal.setValue(3);
        } else {
            serieActual = 1;
        }
        tvSerieValue.setText(String.valueOf(serieActual));
    }

    private void guardarRegistro() {
        npRepeticiones.clearFocus();
        npPesoEntero.clearFocus();
        int reps = npRepeticiones.getValue();
        int entero = npPesoEntero.getValue();
        String[] valoresDecimales = npPesoDecimal.getDisplayedValues();
        double decimal = Double.parseDouble("0." + valoresDecimales[npPesoDecimal.getValue()]);
        double peso = entero + decimal;
        if (reps <= 0) {
            Toast.makeText(this, "Introduce repeticiones válidas", Toast.LENGTH_SHORT).show();
            return;
        }
        btnCargar.setEnabled(false);
        registroRepository.guardarRegistroCompleto(idUsuario, idSeccion, idEjercicio, peso, serieActual, reps, null, nuevo -> {
            if (nuevo != null) {
                listaHistorial.add(0, nuevo);
                adapter.notifyItemInserted(0);
                rvHistorial.scrollToPosition(0);
                serieActual++;
                tvSerieValue.setText(String.valueOf(serieActual));
                Toast.makeText(CargarRegistroActivity.this, "Serie guardada", Toast.LENGTH_SHORT).show();
                resetTimer();
                startTimer();
            } else {
                Toast.makeText(CargarRegistroActivity.this, "Error al guardar serie", Toast.LENGTH_SHORT).show();
            }
            btnCargar.setEnabled(true);
        });
    }

    private void eliminarUltimoRegistro() {
        if (listaHistorial.isEmpty()) return;
        Registro ultimo = listaHistorial.get(0);
        registroRepository.eliminarRegistro(ultimo);
        listaHistorial.remove(0);
        adapter.notifyItemRemoved(0);
        actualizarSerieActual(listaHistorial);
    }
}

package com.example.migymsito;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.migymsito.adapter.CardioHistorialAdapter;
import com.example.migymsito.data.Ejercicio;
import com.example.migymsito.data.RegistroCardio;
import com.example.migymsito.data.Seccion;
import com.example.migymsito.dataRepository.RegistroCardioRepository;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CargarRegistroCardioFragment extends Fragment {

    private TextView tvNombreEjercicio, tvTimerValue;
    private View cvCardioTimer;
    private ImageButton btnStartPause, btnReset, btnEdit, btnEliminarUltimo;
    private Button btnRegistrar;
    private RecyclerView rvHistorial;
    private CardioHistorialAdapter adapter;
    private final List<RegistroCardio> listaHistorial = new ArrayList<>();
    private TabLayout tlTimerMode;

    private Ejercicio ejercicio;
    private Seccion seccion;
    private int idUsuario;

    private RegistroCardioRepository repository;

    // Timer state
    private boolean isRunning = false;
    private long startTime = 0L;
    private long timeInMilliseconds = 0L;
    private long timeSwapBuff = 0L;
    private long updatedTime = 0L;
    private final Handler timerHandler = new Handler(Looper.getMainLooper());
    
    // CountDown specific
    private CountDownTimer countDownTimer;
    private long timeLeftInMillis = 0L;
    private long initialTimeLimit = 0L;

    private int currentMode = 0; // 0: Cronómetro Libre (Up), 1: Timer Definido (Down)

    // Inputs (Dynamic)
    private TextInputLayout tilDistancia, tilCalorias, tilCadencia, tilRitmo, tilInclinacion, tilResistencia, tilRPE, tilNotas;
    private TextInputEditText etDistancia, etCalorias, etCadencia, etRitmo, etInclinacion, etResistencia, etRPE, etNotas;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_cargar_registro_cardio, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        repository = new RegistroCardioRepository(requireActivity().getApplication());

        if (getArguments() != null) {
            ejercicio = (Ejercicio) getArguments().getSerializable("ejercicio");
            seccion = (Seccion) getArguments().getSerializable("seccion");
        }

        if (MainActivity.usuarioLogueado != null) {
            idUsuario = MainActivity.usuarioLogueado.IdUsuario;
        }

        initViews(view);
        setupListeners();
        setupRecyclerView();
        configurarInputsDinamicos();
        cargarHistorial();
    }

    private void initViews(View view) {
        tvNombreEjercicio = view.findViewById(R.id.tvNombreEjercicioCardio);
        tvTimerValue = view.findViewById(R.id.tvCardioTimerValue);
        cvCardioTimer = view.findViewById(R.id.cvCardioTimer);
        btnStartPause = view.findViewById(R.id.btnStartPauseCardioTimer);
        btnReset = view.findViewById(R.id.btnResetCardioTimer);
        btnEdit = view.findViewById(R.id.btnEditCardioTimer);
        btnRegistrar = view.findViewById(R.id.btnRegistrarCardio);
        btnEliminarUltimo = view.findViewById(R.id.btnEliminarUltimoCardio);
        rvHistorial = view.findViewById(R.id.rvCardioHistorial);
        tlTimerMode = view.findViewById(R.id.tlTimerMode);

        tilDistancia = view.findViewById(R.id.tilCardioDistancia);
        tilCalorias = view.findViewById(R.id.tilCardioCalorias);
        tilCadencia = view.findViewById(R.id.tilCardioCadencia);
        tilRitmo = view.findViewById(R.id.tilCardioRitmo);
        tilInclinacion = view.findViewById(R.id.tilCardioInclinacion);
        tilResistencia = view.findViewById(R.id.tilCardioResistencia);
        tilRPE = view.findViewById(R.id.tilCardioRPE);
        tilNotas = view.findViewById(R.id.tilCardioNotas);

        etDistancia = view.findViewById(R.id.etCardioDistancia);
        etCalorias = view.findViewById(R.id.etCardioCalorias);
        etCadencia = view.findViewById(R.id.etCardioCadencia);
        etRitmo = view.findViewById(R.id.etCardioRitmo);
        etInclinacion = view.findViewById(R.id.etCardioInclinacion);
        etResistencia = view.findViewById(R.id.etCardioResistencia);
        etRPE = view.findViewById(R.id.etCardioRPE);
        etNotas = view.findViewById(R.id.etCardioNotas);

        if (ejercicio != null) {
            tvNombreEjercicio.setText(ejercicio.NombreEjercicio);
        }

        // Timer por defecto
        if (tlTimerMode != null) {
            TabLayout.Tab tab = tlTimerMode.getTabAt(0);
            if (tab != null) {
                tab.select();
            }
        }
    }

    private void setupListeners() {
        tlTimerMode.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                resetTimer();
                currentMode = tab.getPosition();
                actualizarUIModo();
                if (currentMode == 1) {
                    mostrarDialogoAjustarTimer();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                if (tab.getPosition() == 1) {
                    mostrarDialogoAjustarTimer();
                }
            }
        });

        btnStartPause.setOnClickListener(v -> {
            if (isRunning) {
                pauseTimer();
            } else {
                startTimer();
            }
        });

        btnReset.setOnClickListener(v -> resetTimer());
        btnEdit.setOnClickListener(v -> mostrarDialogoAjustarTimer());
        tvTimerValue.setOnClickListener(v -> mostrarDialogoAjustarTimer());
        if (cvCardioTimer != null) {
            cvCardioTimer.setOnClickListener(v -> mostrarDialogoAjustarTimer());
        }

        btnRegistrar.setOnClickListener(v -> guardarRegistro());
        btnEliminarUltimo.setOnClickListener(v -> eliminarUltimoRegistro());
    }

    private void actualizarUIModo() {
        if (currentMode == 1) { // Timer
            if (initialTimeLimit == 0) {
                tvTimerValue.setText("00:00:00");
            } else {
                actualizarTextoTimer(initialTimeLimit);
            }
        } else { // Cronómetro Libre
            actualizarTextoTimer(updatedTime);
        }
    }

    private void setupRecyclerView() {
        adapter = new CardioHistorialAdapter(listaHistorial);
        rvHistorial.setLayoutManager(new LinearLayoutManager(getContext()));
        rvHistorial.setAdapter(adapter);
    }

    private void configurarInputsDinamicos() {
        if (ejercicio == null) return;
        tilDistancia.setVisibility(ejercicio.registraDistancia ? View.VISIBLE : View.GONE);
        tilCalorias.setVisibility(ejercicio.registraCalorias ? View.VISIBLE : View.GONE);
        tilCadencia.setVisibility(ejercicio.registraCadencia ? View.VISIBLE : View.GONE);
        tilRitmo.setVisibility(ejercicio.registraRitmo ? View.VISIBLE : View.GONE);
        tilInclinacion.setVisibility(ejercicio.registraInclinacion ? View.VISIBLE : View.GONE);
        tilResistencia.setVisibility(ejercicio.registraResistencia ? View.VISIBLE : View.GONE);
    }

    private void cargarHistorial() {
        if (ejercicio != null && seccion != null) {
            repository.obtenerHistorial(seccion.IdSeccion, ejercicio.IdEjercicio, registros -> {
                listaHistorial.clear();
                listaHistorial.addAll(registros);
                adapter.notifyDataSetChanged();
            });
        }
    }

    private void startTimer() {
        if (currentMode == 0) { // Cronómetro Libre (Up)
            isRunning = true;
            startTime = SystemClock.uptimeMillis();
            timerHandler.postDelayed(updateTimerThread, 0);
        } else { // Timer Definido (Down)
            if (timeLeftInMillis <= 0) {
                if (initialTimeLimit > 0) {
                    timeLeftInMillis = initialTimeLimit;
                } else {
                    mostrarDialogoAjustarTimer();
                    return;
                }
            }
            isRunning = true;
            countDownTimer = new CountDownTimer(timeLeftInMillis, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    timeLeftInMillis = millisUntilFinished;
                    updatedTime = initialTimeLimit - timeLeftInMillis;
                    actualizarTextoTimer(timeLeftInMillis);
                }

                @Override
                public void onFinish() {
                    finalizarTimer();
                }
            }.start();
            scheduleAlarm(timeLeftInMillis);
        }
        btnStartPause.setImageResource(android.R.drawable.ic_media_pause);
    }

    private void pauseTimer() {
        isRunning = false;
        if (currentMode == 0) {
            timeSwapBuff += timeInMilliseconds;
            timerHandler.removeCallbacks(updateTimerThread);
        } else {
            if (countDownTimer != null) {
                countDownTimer.cancel();
            }
            cancelAlarm();
        }
        btnStartPause.setImageResource(android.R.drawable.ic_media_play);
    }

    private void resetTimer() {
        pauseTimer();
        timeSwapBuff = 0L;
        timeInMilliseconds = 0L;
        updatedTime = 0L;
        if (currentMode == 1) {
            timeLeftInMillis = initialTimeLimit;
        } else {
            timeLeftInMillis = 0L;
        }
        actualizarUIModo();
    }

    private void finalizarTimer() {
        isRunning = false;
        updatedTime = initialTimeLimit;
        actualizarTextoTimer(0);
        btnStartPause.setImageResource(android.R.drawable.ic_media_play);
        
        vibrarAlFinalizar();
        sonarAlerta();
        if (isAdded()) Toast.makeText(getContext(), "¡Tiempo de cardio finalizado!", Toast.LENGTH_SHORT).show();
    }

    private void sonarAlerta() {
        try {
            MediaPlayer mp = MediaPlayer.create(getContext(), R.raw.sonido1);
            if (mp != null) {
                mp.start();
                mp.setOnCompletionListener(MediaPlayer::release);
            }
        } catch (Exception ignored) {}
    }

    private void vibrarAlFinalizar() {
        try {
            Vibrator v = (Vibrator) requireContext().getSystemService(Context.VIBRATOR_SERVICE);
            if (v != null && v.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    v.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
                } else {
                    v.vibrate(500);
                }
            }
        } catch (Exception ignored) {}
    }

    private void scheduleAlarm(long durationMs) {
        AlarmManager am = (AlarmManager) requireContext().getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(requireContext(), TimerReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(requireContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        long triggerTime = System.currentTimeMillis() + durationMs;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !am.canScheduleExactAlarms()) {
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pi);
        } else {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pi);
        }
    }

    private void cancelAlarm() {
        AlarmManager am = (AlarmManager) requireContext().getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(requireContext(), TimerReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(requireContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        am.cancel(pi);
    }

    private void mostrarDialogoAjustarTimer() {
        if (isRunning) {
            pauseTimer();
        }

        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_ajustar_tiempo, null);
        NumberPicker npMinutos = dialogView.findViewById(R.id.npMinutos);
        NumberPicker npSegundos = dialogView.findViewById(R.id.npSegundos);
        Button btnCancelar = dialogView.findViewById(R.id.btnCancelarDialog);
        Button btnAceptar = dialogView.findViewById(R.id.btnAceptarDialog);
        TextView tvTitulo = dialogView.findViewById(R.id.tvTituloDialog);

        if (tvTitulo != null) {
            tvTitulo.setText(currentMode == 0 ? "Ajustar Tiempo Transcurrido" : "Configurar Tiempo Cardio");
        }

        npMinutos.setMinValue(0);
        npMinutos.setMaxValue(120);
        npSegundos.setMinValue(0);
        npSegundos.setMaxValue(59);

        long displayTime = (currentMode == 1 && initialTimeLimit > 0) ? initialTimeLimit : updatedTime;
        npMinutos.setValue((int) (displayTime / 1000) / 60);
        npSegundos.setValue((int) (displayTime / 1000) % 60);

        AlertDialog dialog = new AlertDialog.Builder(requireContext(), R.style.CustomDialogTheme)
                .setView(dialogView)
                .create();

        if (btnCancelar != null) {
            btnCancelar.setOnClickListener(v -> dialog.dismiss());
        }

        if (btnAceptar != null) {
            btnAceptar.setOnClickListener(v -> {
                long totalMillis = (npMinutos.getValue() * 60L + npSegundos.getValue()) * 1000L;
                if (currentMode == 0) {
                    updatedTime = totalMillis;
                    timeSwapBuff = totalMillis;
                    timeInMilliseconds = 0;
                    startTime = SystemClock.uptimeMillis();
                } else {
                    initialTimeLimit = totalMillis;
                    timeLeftInMillis = totalMillis;
                    updatedTime = 0;
                }
                actualizarTextoTimer(totalMillis);
                dialog.dismiss();
            });
        }

        dialog.show();
    }

    private void actualizarTextoTimer(long timeMs) {
        int secs = (int) (timeMs / 1000);
        int mins = secs / 60;
        int hrs = mins / 60;
        secs = secs % 60;
        mins = mins % 60;
        tvTimerValue.setText(String.format(Locale.getDefault(), "%02d:%02d:%02d", hrs, mins, secs));
    }

    private final Runnable updateTimerThread = new Runnable() {
        public void run() {
            timeInMilliseconds = SystemClock.uptimeMillis() - startTime;
            updatedTime = timeSwapBuff + timeInMilliseconds;
            actualizarTextoTimer(updatedTime);
            timerHandler.postDelayed(this, 1000);
        }
    };

    private void guardarRegistro() {
        long finalTimeSeconds = updatedTime / 1000;
        
        if (finalTimeSeconds == 0 && etDistancia.getText().toString().isEmpty()) {
            Toast.makeText(getContext(), "Ingresa al menos el tiempo o la distancia", Toast.LENGTH_SHORT).show();
            return;
        }

        RegistroCardio reg = new RegistroCardio();
        reg.DuracionSegundos = finalTimeSeconds;
        reg.FechaRegistro = System.currentTimeMillis();

        try {
            String dist = etDistancia.getText().toString();
            if (!dist.isEmpty()) reg.Distancia = Double.parseDouble(dist);
            String cal = etCalorias.getText().toString();
            if (!cal.isEmpty()) reg.CaloriasQuemadas = Integer.parseInt(cal);
            String cad = etCadencia.getText().toString();
            if (!cad.isEmpty()) reg.CadenciaPromedio = Integer.parseInt(cad);
            String rit = etRitmo.getText().toString();
            if (!rit.isEmpty()) reg.RitmoCardiacoPromedio = Integer.parseInt(rit);
            String inc = etInclinacion.getText().toString();
            if (!inc.isEmpty()) reg.Inclinacion = Double.parseDouble(inc);
            String res = etResistencia.getText().toString();
            if (!res.isEmpty()) reg.NivelResistencia = Integer.parseInt(res);
            String rpe = etRPE.getText().toString();
            if (!rpe.isEmpty()) reg.EsfuerzoPercibido = Integer.parseInt(rpe);
            reg.Notas = etNotas.getText().toString();
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Error en el formato", Toast.LENGTH_SHORT).show();
            return;
        }

        repository.guardarRegistro(idUsuario, seccion.IdSeccion, ejercicio.IdEjercicio, reg, nuevo -> {
            if (nuevo != null) {
                Toast.makeText(getContext(), "Registro guardado", Toast.LENGTH_SHORT).show();
                resetTimer();
                limpiarCampos();
                cargarHistorial();
            }
        });
    }

    private void eliminarUltimoRegistro() {
        if (listaHistorial.isEmpty()) return;
        new AlertDialog.Builder(requireContext(), R.style.CustomDialogTheme)
                .setTitle("Eliminar Registro")
                .setMessage("¿Deseas eliminar el último registro de cardio?")
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    repository.eliminarUltimo(listaHistorial.get(0));
                    cargarHistorial();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void limpiarCampos() {
        etDistancia.setText(""); etCalorias.setText(""); etCadencia.setText("");
        etRitmo.setText(""); etInclinacion.setText(""); etResistencia.setText("");
        etRPE.setText(""); etNotas.setText("");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        timerHandler.removeCallbacks(updateTimerThread);
        if (countDownTimer != null) countDownTimer.cancel();
    }
}

package com.example.migymsito;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.migymsito.adapter.HistorialEjercicioAdapter;
import com.example.migymsito.adapter.RegistroAdapter;
import com.example.migymsito.data.Ejercicio;
import com.example.migymsito.data.Entrenamiento;
import com.example.migymsito.data.Historial;
import com.example.migymsito.data.Registro;
import com.example.migymsito.data.Seccion;
import com.example.migymsito.dataRepository.RegistroRepository;
import com.example.migymsito.dataDataBase.AppDatabase;
import com.example.migymsito.dataRepository.UsuarioRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CargarRegistroFragment extends Fragment {

    private TextView tvNombreEjercicio, tvSerieValue, tvPesoLabel, tvColumnaPeso;
    private NumberPicker npRepeticiones, npPesoEntero, npPesoDecimal;
    private ImageButton btnEliminarUltimo;
    private Button btnCargar, btnVerHistorialPrevio;
    private RecyclerView rvHistorial;
    private RegistroAdapter adapter;
    private final List<Registro> listaHistorial = new ArrayList<>();

    private RegistroRepository registroRepository;
    private SharedViewModel sharedViewModel;
    private UsuarioRepository usuarioRepository;

    private int serieActual = 1;
    private int idEjercicio;
    private int idUsuario;
    private int idSeccion;
    private String nombreEjercicio;
    private boolean esPesoCorporal = false;
    private boolean esPesoPorLado = false;

    private long startTimeInMillis = 60000; 
    private TextView tvTimerValue;
    private ImageButton btnStartTimer, btnResetTimer;
    private ImageView ivEditTimer;
    private CountDownTimer countDownTimer;
    private boolean timerRunning;
    private long timeLeftInMillis = startTimeInMillis;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    private static final String PREFS_TIMER = "TimerPrefs";
    private static final String KEY_END_TIME = "endTime";
    private static final String KEY_TIMER_RUNNING = "timerRunning";
    private static final String KEY_START_TIME = "startTime";

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    Toast.makeText(getContext(), "Permiso de notificaciones concedido", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "El permiso de notificaciones es necesario para avisarte cuando termine el descanso", Toast.LENGTH_LONG).show();
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.cargar_registro_activity, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        if (getActivity() != null) {
            View toolbarInclude = getActivity().findViewById(R.id.include_toolbar);
            if (toolbarInclude != null) toolbarInclude.setVisibility(View.VISIBLE);
            
            usuarioRepository = new UsuarioRepository(getActivity().getApplication());
            registroRepository = new RegistroRepository(getActivity().getApplication());

            sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
            sharedViewModel.getImportFinishedTrigger().observe(getViewLifecycleOwner(), finished -> {
                if (finished != null && finished) {
                    cargarSeriesDelDia();
                    sharedViewModel.resetImportFinishedTrigger();
                }
            });
        }

        if (MainActivity.usuarioLogueado != null) {
            idUsuario = MainActivity.usuarioLogueado.IdUsuario;
            continuarCarga(view);
        } else if (usuarioRepository != null) {
            idUsuario = usuarioRepository.obtenerIdSesion();
            if (idUsuario != -1) {
                usuarioRepository.obtenerUsuarioPorId(idUsuario, usuario -> {
                    if (usuario != null) {
                        MainActivity.usuarioLogueado = usuario;
                        continuarCarga(view);
                    } else {
                        Toast.makeText(getContext(), "Error: Sesión de usuario no encontrada", Toast.LENGTH_LONG).show();
                    }
                });
            } else {
                Toast.makeText(getContext(), "Error: No hay una sesión activa", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void continuarCarga(View view) {
        if (getArguments() != null) {
            Ejercicio ejercicio;
            Seccion seccion;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ejercicio = getArguments().getSerializable("ejercicio", Ejercicio.class);
                seccion = getArguments().getSerializable("seccion", Seccion.class);
            } else {
                ejercicio = (Ejercicio) getArguments().getSerializable("ejercicio");
                seccion = (Seccion) getArguments().getSerializable("seccion");
            }
            
            if (ejercicio != null) {
                idEjercicio = ejercicio.IdEjercicio;
                nombreEjercicio = ejercicio.NombreEjercicio;
                esPesoCorporal = (ejercicio.PesoCorporalEjercicio != null && ejercicio.PesoCorporalEjercicio);
                esPesoPorLado = (ejercicio.PesoPorLado != null && ejercicio.PesoPorLado);
            }
            
            if (seccion != null) {
                idSeccion = seccion.IdSeccion;
            }
        }

        initViews(view);
        setupPickers();
        setupListeners();
        setupRecyclerView();

        if (idEjercicio != 0) {
            cargarSeriesDelDia();
        }
    }

    private void initViews(View view) {
        tvNombreEjercicio = view.findViewById(R.id.tvNombreEjercicio);
        tvSerieValue = view.findViewById(R.id.tvSerieValue);
        tvPesoLabel = view.findViewById(R.id.tvPesoLabel);
        tvColumnaPeso = view.findViewById(R.id.tvColumnaPeso);
        
        npRepeticiones = view.findViewById(R.id.npRepeticiones);
        npPesoEntero = view.findViewById(R.id.npPesoEntero);
        npPesoDecimal = view.findViewById(R.id.npPesoDecimal);
        
        btnCargar = view.findViewById(R.id.btnCargar);
        btnEliminarUltimo = view.findViewById(R.id.btnEliminarUltimo);
        btnVerHistorialPrevio = view.findViewById(R.id.btnVerHistorialPrevio);
        rvHistorial = view.findViewById(R.id.rvHistorial);

        tvTimerValue = view.findViewById(R.id.tvTimerValue);
        btnStartTimer = view.findViewById(R.id.btnStartTimer);
        btnResetTimer = view.findViewById(R.id.btnResetTimer);
        ivEditTimer = view.findViewById(R.id.ivEditTimer);

        tvNombreEjercicio.setText(nombreEjercicio);

        if (esPesoCorporal) {
            tvPesoLabel.setText("Lastre (kg)");
            tvColumnaPeso.setText("Lastre");
        } else if (esPesoPorLado) {
            tvPesoLabel.setText("Peso x lado (kg)");
            tvColumnaPeso.setText("Peso (x2)");
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
        btnVerHistorialPrevio.setOnClickListener(v -> mostrarHistorialCompleto());

        btnStartTimer.setOnClickListener(v -> {
            if (timerRunning) {
                pauseTimer();
            } else {
                startTimer();
            }
        });

        btnResetTimer.setOnClickListener(v -> resetTimer());
        tvTimerValue.setOnClickListener(v -> mostrarDialogoAjustarTiempo());
        if (ivEditTimer != null) {
            ivEditTimer.setOnClickListener(v -> mostrarDialogoAjustarTiempo());
        }
    }

    private void mostrarDialogoAjustarTiempo() {
        if (timerRunning) {
            pauseTimer();
        }

        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_ajustar_tiempo, null);
        NumberPicker npMinutos = dialogView.findViewById(R.id.npMinutos);
        NumberPicker npSegundos = dialogView.findViewById(R.id.npSegundos);
        Button btnCancelar = dialogView.findViewById(R.id.btnCancelarDialog);
        Button btnAceptar = dialogView.findViewById(R.id.btnAceptarDialog);

        npMinutos.setMinValue(0);
        npMinutos.setMaxValue(10);
        npMinutos.setValue((int) (startTimeInMillis / 1000) / 60);

        npSegundos.setMinValue(0);
        npSegundos.setMaxValue(59);
        npSegundos.setValue((int) (startTimeInMillis / 1000) % 60);

        AlertDialog dialog = new AlertDialog.Builder(requireContext(), R.style.CustomDialogTheme)
                .setView(dialogView)
                .create();

        btnCancelar.setOnClickListener(v -> dialog.dismiss());

        btnAceptar.setOnClickListener(v -> {
            int min = npMinutos.getValue();
            int seg = npSegundos.getValue();
            startTimeInMillis = (min * 60L + seg) * 1000L;
            resetTimer();
            dialog.dismiss();
        });

        dialog.show();
    }

    @Override
    public void onStart() {
        super.onStart();
        checkPermissions();
        restoreTimerState();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }

    private void checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (requireContext().checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }

    private void restoreTimerState() {
        SharedPreferences prefs = requireContext().getSharedPreferences(PREFS_TIMER, Context.MODE_PRIVATE);
        startTimeInMillis = prefs.getLong(KEY_START_TIME, 60000);
        long endTime = prefs.getLong(KEY_END_TIME, 0);
        timerRunning = prefs.getBoolean(KEY_TIMER_RUNNING, false);

        if (timerRunning) {
            timeLeftInMillis = endTime - System.currentTimeMillis();
            if (timeLeftInMillis < 0) {
                timeLeftInMillis = startTimeInMillis;
                timerRunning = false;
                updateCountDownText();
                updateTimerUI();
                clearTimerState();
            } else {
                startTimer();
            }
        } else {
            timeLeftInMillis = startTimeInMillis;
            updateCountDownText();
        }
    }

    private void saveTimerState() {
        SharedPreferences prefs = requireContext().getSharedPreferences(PREFS_TIMER, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong(KEY_START_TIME, startTimeInMillis);
        editor.putLong(KEY_END_TIME, System.currentTimeMillis() + timeLeftInMillis);
        editor.putBoolean(KEY_TIMER_RUNNING, timerRunning);
        editor.apply();
    }

    private void clearTimerState() {
        SharedPreferences prefs = requireContext().getSharedPreferences(PREFS_TIMER, Context.MODE_PRIVATE);
        prefs.edit().remove(KEY_END_TIME).putBoolean(KEY_TIMER_RUNNING, false).apply();
    }

    private void updateTimerUI() {
        if (timerRunning) {
            btnStartTimer.setImageResource(android.R.drawable.ic_media_pause);
        } else {
            btnStartTimer.setImageResource(android.R.drawable.ic_media_play);
        }
    }

    private void scheduleAlarm() {
        AlarmManager alarmManager = (AlarmManager) requireContext().getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(requireContext(), TimerReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(requireContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        long triggerTime = System.currentTimeMillis() + timeLeftInMillis;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
            } else {
                alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
        }
    }

    private void cancelAlarm() {
        AlarmManager alarmManager = (AlarmManager) requireContext().getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(requireContext(), TimerReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(requireContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        alarmManager.cancel(pendingIntent);
    }

    private void startTimer() {
        if (timeLeftInMillis <= 0) {
            resetTimer();
        }

        if (countDownTimer != null) {
            countDownTimer.cancel();
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
                timeLeftInMillis = startTimeInMillis;
                updateCountDownText();
                updateTimerUI();
                clearTimerState();
                vibrarAlFinalizar();
                if (isAdded()) {
                    Toast.makeText(getContext(), "¡Descanso terminado!", Toast.LENGTH_SHORT).show();
                }
            }
        }.start();

        timerRunning = true;
        updateTimerUI();
        saveTimerState();
        scheduleAlarm();
    }

    private void pauseTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        timerRunning = false;
        updateTimerUI();
        saveTimerState();
        cancelAlarm();
    }

    private void resetTimer() {
        pauseTimer();
        timeLeftInMillis = startTimeInMillis;
        updateCountDownText();
        clearTimerState();
    }

    private void updateCountDownText() {
        int minutes = (int) (timeLeftInMillis / 1000) / 60;
        int seconds = (int) (timeLeftInMillis / 1000) % 60;
        String timeLeftFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
        tvTimerValue.setText(timeLeftFormatted);
    }

    private void vibrarAlFinalizar() {
        try {
            if (getActivity() != null) {
                Vibrator vibrator = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
                if (vibrator != null && vibrator.hasVibrator()) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
                    } else {
                        vibrator.vibrate(500);
                    }
                }
            }
        } catch (Exception ignored) {}
    }

    private void setupRecyclerView() {
        adapter = new RegistroAdapter(listaHistorial, esPesoCorporal);
        adapter.setOnRegistroEditListener((registro, position) -> mostrarDialogoEditarRegistro(registro, position));
        rvHistorial.setLayoutManager(new LinearLayoutManager(getContext()));
        rvHistorial.setAdapter(adapter);
    }

    private void mostrarDialogoEditarRegistro(Registro registro, int position) {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_editar_registro, null);
        NumberPicker npReps = dialogView.findViewById(R.id.npRepeticionesEdit);
        NumberPicker npEntero = dialogView.findViewById(R.id.npPesoEnteroEdit);
        NumberPicker npDecimal = dialogView.findViewById(R.id.npPesoDecimalEdit);
        Button btnCancelar = dialogView.findViewById(R.id.btnCancelarEdit);
        Button btnAceptar = dialogView.findViewById(R.id.btnAceptarEdit);

        // Setup Pickers
        npReps.setMinValue(1);
        npReps.setMaxValue(100);
        npReps.setValue(registro.Repeticiones);

        npEntero.setMinValue(0);
        npEntero.setMaxValue(500);
        int entero = (int) Math.floor(registro.PesoRegistro);
        npEntero.setValue(entero);

        String[] valoresDecimales = {"00", "25", "50", "75"};
        npDecimal.setMinValue(0);
        npDecimal.setMaxValue(valoresDecimales.length - 1);
        npDecimal.setDisplayedValues(valoresDecimales);
        
        double decimal = registro.PesoRegistro - entero;
        if (decimal < 0.125) npDecimal.setValue(0);
        else if (decimal < 0.375) npDecimal.setValue(1);
        else if (decimal < 0.625) npDecimal.setValue(2);
        else npDecimal.setValue(3);

        AlertDialog dialog = new AlertDialog.Builder(requireContext(), R.style.CustomDialogTheme)
                .setView(dialogView)
                .create();

        btnCancelar.setOnClickListener(v -> dialog.dismiss());

        btnAceptar.setOnClickListener(v -> {
            int newReps = npReps.getValue();
            int newEntero = npEntero.getValue();
            double newDecimal = Double.parseDouble("0." + valoresDecimales[npDecimal.getValue()]);
            double newPeso = newEntero + newDecimal;

            registro.Repeticiones = newReps;
            registro.PesoRegistro = newPeso;

            registroRepository.actualizarRegistro(registro);
            adapter.notifyItemChanged(position);
            
            // Si editamos el último registro cargado (el primero en la lista visual),
            // actualizamos los pickers de la pantalla principal para que la siguiente serie
            // use los nuevos valores como base.
            if (position == 0) {
                actualizarPickersConRegistro(registro);
            }

            dialog.dismiss();
            Toast.makeText(getContext(), "Serie actualizada", Toast.LENGTH_SHORT).show();
        });

        dialog.show();
    }

    private void actualizarPickersConRegistro(Registro registro) {
        npRepeticiones.setValue(registro.Repeticiones);
        double peso = registro.PesoRegistro;
        int entero = (int) peso;
        double decimal = peso - entero;
        npPesoEntero.setValue(entero);
        if (decimal < 0.125) npPesoDecimal.setValue(0);
        else if (decimal < 0.375) npPesoDecimal.setValue(1);
        else if (decimal < 0.625) npPesoDecimal.setValue(2);
        else npPesoDecimal.setValue(3);
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
            actualizarPickersConRegistro(ultimoRegistro);
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
        double pesoFinal = entero + decimal;

        if (esPesoPorLado) {
            pesoFinal *= 2;
        }

        if (reps <= 0) {
            Toast.makeText(getContext(), "Introduce repeticiones válidas", Toast.LENGTH_SHORT).show();
            return;
        }
        btnCargar.setEnabled(false);

        double finalPesoFinal = pesoFinal;
        executorService.execute(() -> {
            AppDatabase db = AppDatabase.getDatabase(getContext());
            Historial ultimoPeso = db.historialDao().obtenerUltimoHistorial(idUsuario);
            Double pesoCorporal = (ultimoPeso != null) ? ultimoPeso.PesoHistorial : null;

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    registroRepository.guardarRegistroCompleto(idUsuario, idSeccion, idEjercicio, finalPesoFinal, serieActual, reps, pesoCorporal, nuevo -> {
                        if (nuevo != null) {
                            listaHistorial.add(0, nuevo);
                            adapter.notifyItemInserted(0);
                            rvHistorial.scrollToPosition(0);
                            serieActual++;
                            tvSerieValue.setText(String.valueOf(serieActual));
                            Toast.makeText(getContext(), "Serie guardada", Toast.LENGTH_SHORT).show();
                            resetTimer();
                            startTimer();
                        } else {
                            Toast.makeText(getContext(), "Error al guardar serie", Toast.LENGTH_SHORT).show();
                        }
                        btnCargar.setEnabled(true);
                    });
                });
            }
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

    private void mostrarHistorialCompleto() {
        executorService.execute(() -> {
            AppDatabase db = AppDatabase.getDatabase(getContext());
            Entrenamiento activo = db.entrenamientoDao().getEntrenamientoActivoPorSeccion(idUsuario, idSeccion);
            int idEntActual = (activo != null) ? activo.IdEntrenamiento : Integer.MAX_VALUE;

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    registroRepository.obtenerRegistrosUltimoEntrenamientoPrevio(idUsuario, idEjercicio, idEntActual, registros -> {
                        if (registros == null || registros.isEmpty()) {
                            Toast.makeText(getContext(), "No hay registros previos para este ejercicio", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_historial_ejercicio, null);
                        RecyclerView rvHistorialDialog = dialogView.findViewById(R.id.rvHistorialEjercicio);
                        Button btnCerrar = dialogView.findViewById(R.id.btnCerrarHistorial);

                        HistorialEjercicioAdapter dialogAdapter = new HistorialEjercicioAdapter(registros, esPesoCorporal);
                        rvHistorialDialog.setLayoutManager(new LinearLayoutManager(getContext()));
                        rvHistorialDialog.setAdapter(dialogAdapter);

                        AlertDialog dialog = new AlertDialog.Builder(requireContext(), R.style.CustomDialogTheme)
                                .setView(dialogView)
                                .create();

                        btnCerrar.setOnClickListener(v -> dialog.dismiss());
                        dialog.show();

                        if (dialog.getWindow() != null) {
                            int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.95);
                            dialog.getWindow().setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);
                        }
                    });
                });
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }
}

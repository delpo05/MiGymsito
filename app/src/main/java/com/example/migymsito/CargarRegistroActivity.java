package com.example.migymsito;

import android.app.Dialog;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

    // Cronómetro
    private TextView tvCronometro;
    private ImageButton btnEditarCronometro;
    private CountDownTimer countDownTimer;
    private int tiempoDescansoSegundos = 120; // Default 2 min
    private boolean timerCorriendo = false;

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
            tiempoDescansoSegundos = ejercicio.TiempoDeDescanso;
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

        tvCronometro = findViewById(R.id.tvCronometro);
        btnEditarCronometro = findViewById(R.id.btnEditarCronometro);

        tvNombreEjercicio.setText(nombreEjercicio);
        actualizarTextoCronometro(tiempoDescansoSegundos);

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
        btnEditarCronometro.setOnClickListener(v -> mostrarPopUpEditarTiempo());
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

                // Iniciar cronómetro de descanso
                iniciarCronometro();

            } else {
                Toast.makeText(CargarRegistroActivity.this, "Error al guardar serie", Toast.LENGTH_SHORT).show();
            }
            btnCargar.setEnabled(true);
        });
    }

    private void iniciarCronometro() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        countDownTimer = new CountDownTimer(tiempoDescansoSegundos * 1000L, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timerCorriendo = true;
                actualizarTextoCronometro((int) (millisUntilFinished / 1000));
            }

            @Override
            public void onFinish() {
                timerCorriendo = false;
                actualizarTextoCronometro(tiempoDescansoSegundos);
                alertarFinDescanso();
            }
        }.start();
    }

    private void actualizarTextoCronometro(int segundosTotales) {
        int minutes = segundosTotales / 60;
        int seconds = segundosTotales % 60;
        tvCronometro.setText(String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds));
    }

    private void alertarFinDescanso() {
        Toast.makeText(this, "¡Tiempo de descanso finalizado!", Toast.LENGTH_LONG).show();
        MediaPlayer mp = MediaPlayer.create(this, R.raw.sonido3);
        if (mp != null) {
            mp.start();
            mp.setOnCompletionListener(MediaPlayer::release);
        }
    }

    private void mostrarPopUpEditarTiempo() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.pop_up_aniadir_ej_personalizado); // Reusamos el layout o parte de él
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        }

        // Configurar UI del popup para solo editar tiempo
        TextView tvTitulo = dialog.findViewById(R.id.tvTituloPopUpEjercicio);
        tvTitulo.setText("Editar tiempo de descanso");
        
        // Ocultar lo que no necesitamos
        dialog.findViewById(R.id.ivSeleccionarImagen).setVisibility(View.GONE);
        // Cast a View para poder llamar a setVisibility
        View layoutNombre = (View) dialog.findViewById(R.id.etNombreEjercicio).getParent();
        if (layoutNombre != null) layoutNombre.setVisibility(View.GONE);
        
        dialog.findViewById(R.id.cbPesoCorporal).setVisibility(View.GONE);

        EditText etMinutos = dialog.findViewById(R.id.etMinutosDescanso);
        EditText etSegundos = dialog.findViewById(R.id.etSegundosDescanso);
        
        etMinutos.setText(String.format(Locale.getDefault(), "%02d", tiempoDescansoSegundos / 60));
        etSegundos.setText(String.format(Locale.getDefault(), "%02d", tiempoDescansoSegundos % 60));

        Button btnAceptar = dialog.findViewById(R.id.btnAceptarEjercicio);
        Button btnCancelar = dialog.findViewById(R.id.btnCancelarEjercicio);

        btnAceptar.setOnClickListener(v -> {
            int mins = 0, segs = 0;
            try {
                String m = etMinutos.getText().toString();
                String s = etSegundos.getText().toString();
                mins = m.isEmpty() ? 0 : Integer.parseInt(m);
                segs = s.isEmpty() ? 0 : Integer.parseInt(s);
            } catch (NumberFormatException e) {}
            
            tiempoDescansoSegundos = (mins * 60) + segs;
            if (!timerCorriendo) {
                actualizarTextoCronometro(tiempoDescansoSegundos);
            }
            dialog.dismiss();
        });

        btnCancelar.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}

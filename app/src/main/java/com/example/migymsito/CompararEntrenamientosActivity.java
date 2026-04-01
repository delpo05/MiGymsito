package com.example.migymsito;

import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.migymsito.data.Ejercicio;
import com.example.migymsito.data.Entrenamiento;
import com.example.migymsito.data.Registro;
import com.example.migymsito.data.Rutina;
import com.example.migymsito.data.Seccion;
import com.example.migymsito.data.SeccionXejercicio;
import com.example.migymsito.dataDataBase.AppDatabase;
import com.example.migymsito.dataRepository.RutinaRepository;
import com.example.migymsito.dataRepository.SeccionRepository;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CompararEntrenamientosActivity extends HeaderActivity implements ComparacionAdapter.OnItemClickListener {

    private AutoCompleteTextView actvRutina, actvSeccion;
    private TextInputEditText tietEntrenamientoA, tietEntrenamientoB;
    private MaterialButton btnComparar;
    private LinearLayout llTablaComparacion;
    private RecyclerView rvComparacion;
    private TextView tvHeaderVolA, tvHeaderVolB;

    private RutinaRepository rutinaRepository;
    private SeccionRepository seccionRepository;

    private List<Rutina> listaRutinas = new ArrayList<>();
    private List<Seccion> listaSecciones = new ArrayList<>();
    private List<Entrenamiento> entrenamientosFinalizados = new ArrayList<>();
    
    private Seccion seccionSeleccionada;
    private Entrenamiento entrenamientoA, entrenamientoB;
    private int numEntA, numEntB;

    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.comparar_entrenamientos_activity);

        actvRutina = findViewById(R.id.actv_rutina);
        actvSeccion = findViewById(R.id.actv_seccion);
        tietEntrenamientoA = findViewById(R.id.tiet_entrenamiento_a);
        tietEntrenamientoB = findViewById(R.id.tiet_entrenamiento_b);
        btnComparar = findViewById(R.id.btn_comparar);
        llTablaComparacion = findViewById(R.id.ll_tabla_comparacion);
        rvComparacion = findViewById(R.id.rv_comparacion);
        tvHeaderVolA = findViewById(R.id.tv_header_vol_ent_a);
        tvHeaderVolB = findViewById(R.id.tv_header_vol_ent_b);

        rutinaRepository = new RutinaRepository(getApplication());
        seccionRepository = new SeccionRepository(getApplication());

        rvComparacion.setLayoutManager(new LinearLayoutManager(this));

        cargarRutinas();
        configurarSelectoresEntrenamiento();

        btnComparar.setOnClickListener(v -> realizarComparacion());

        // Manejar el intent si venimos de finalizar un entrenamiento
        manejarIntentDirecto();
    }

    private void manejarIntentDirecto() {
        if (getIntent() != null && getIntent().hasExtra("idSeccion")) {
            int idSeccion = getIntent().getIntExtra("idSeccion", -1);
            int idEntB_intent = getIntent().getIntExtra("idEntB", -1);
            int idEntA_intent = getIntent().getIntExtra("idEntA", -1);

            new Thread(() -> {
                AppDatabase db = AppDatabase.getDatabase(this);
                // Buscar la sección
                List<Seccion> todas = db.seccionDao().obtenerSeccionesPorUsuario(usuarioLogueado.IdUsuario);
                for (Seccion s : todas) {
                    if (s.IdSeccion == idSeccion) {
                        seccionSeleccionada = s;
                        break;
                    }
                }

                if (seccionSeleccionada != null) {
                    entrenamientosFinalizados = db.entrenamientoDao().getEntrenamientosFinalizadosPorSeccion(usuarioLogueado.IdUsuario, idSeccion);

                    if (idEntB_intent != -1) {
                        for (int i = 0; i < entrenamientosFinalizados.size(); i++) {
                            if (entrenamientosFinalizados.get(i).IdEntrenamiento == idEntB_intent) {
                                entrenamientoB = entrenamientosFinalizados.get(i);
                                numEntB = i + 1;
                                break;
                            }
                        }
                    }
                    if (idEntA_intent != -1) {
                        for (int i = 0; i < entrenamientosFinalizados.size(); i++) {
                            if (entrenamientosFinalizados.get(i).IdEntrenamiento == idEntA_intent) {
                                entrenamientoA = entrenamientosFinalizados.get(i);
                                numEntA = i + 1;
                                break;
                            }
                        }
                    }

                    runOnUiThread(() -> {
                        actvSeccion.setText(seccionSeleccionada.NombreSeccion);
                        if (entrenamientoA != null) {
                            tietEntrenamientoA.setText("Entrenamiento #" + numEntA + " (" + dateFormat.format(new Date(entrenamientoA.FechaFin)) + ")");
                        }
                        if (entrenamientoB != null) {
                            tietEntrenamientoB.setText("Entrenamiento #" + numEntB + " (" + dateFormat.format(new Date(entrenamientoB.FechaFin)) + ")");
                        }

                        if (entrenamientoA != null && entrenamientoB != null) {
                            realizarComparacion();
                        }
                    });
                }
            }).start();
        }
    }

    private void cargarRutinas() {
        if (usuarioLogueado != null) {
            rutinaRepository.obtenerRutinasDeUsuario(usuarioLogueado.IdUsuario, rutinas -> {
                this.listaRutinas = new ArrayList<>(rutinas);
                List<String> nombres = new ArrayList<>();
                nombres.add("Todas");

                for (Rutina r : rutinas) nombres.add(r.NombreRutina);

                actvRutina.setAdapter(new ArrayAdapter<>(this, R.layout.dropdown_item, nombres));

                actvRutina.setOnItemClickListener((parent, view, position, id) -> {
                    limpiarSeleccionSeccion();
                    if (position == 0) cargarTodasLasSecciones();
                    else cargarSeccionesDeRutina(listaRutinas.get(position - 1).IdRutina);
                });
            });
        }
    }

    private void limpiarSeleccionSeccion() {
        actvSeccion.setText("");
        seccionSeleccionada = null;
        entrenamientoA = null;
        entrenamientoB = null;
        tietEntrenamientoA.setText("");
        tietEntrenamientoB.setText("");
        entrenamientosFinalizados.clear();
        llTablaComparacion.setVisibility(View.GONE);
    }

    private void cargarTodasLasSecciones() {
        if (usuarioLogueado != null) {
            seccionRepository.obtenerSeccionesPorUsuario(usuarioLogueado.IdUsuario, secciones -> {
                this.listaSecciones = secciones;
                List<String> nombres = new ArrayList<>();
                for (Seccion s : secciones) nombres.add(s.NombreSeccion);
                actvSeccion.setAdapter(new ArrayAdapter<>(this, R.layout.dropdown_item, nombres));
                actvSeccion.setOnItemClickListener((p, v, pos, id) -> onSeccionSeleccionada(listaSecciones.get(pos)));
            });
        }
    }

    private void cargarSeccionesDeRutina(int idRutina) {
        seccionRepository.obtenerSeccionesDeRutina(idRutina, secciones -> {
            this.listaSecciones = secciones;
            List<String> nombres = new ArrayList<>();
            for (Seccion s : secciones) nombres.add(s.NombreSeccion);
            actvSeccion.setAdapter(new ArrayAdapter<>(this, R.layout.dropdown_item, nombres));
            actvSeccion.setOnItemClickListener((p, v, pos, id) -> onSeccionSeleccionada(listaSecciones.get(pos)));
        });
    }

    private void onSeccionSeleccionada(Seccion seccion) {
        seccionSeleccionada = seccion;
        entrenamientoA = null;
        entrenamientoB = null;
        tietEntrenamientoA.setText("");
        tietEntrenamientoB.setText("");
        llTablaComparacion.setVisibility(View.GONE);

        new Thread(() -> {
            AppDatabase db = AppDatabase.getDatabase(this);
            entrenamientosFinalizados = db.entrenamientoDao().getEntrenamientosFinalizadosPorSeccion(usuarioLogueado.IdUsuario, seccion.IdSeccion);
        }).start();
    }

    private void configurarSelectoresEntrenamiento() {
        tietEntrenamientoA.setOnClickListener(v -> mostrarDialogoSeleccionEntrenamiento(true));
        tietEntrenamientoB.setOnClickListener(v -> mostrarDialogoSeleccionEntrenamiento(false));
    }

    private void mostrarDialogoSeleccionEntrenamiento(boolean esA) {
        if (seccionSeleccionada == null) {
            Toast.makeText(this, "Primero selecciona una sección", Toast.LENGTH_SHORT).show();
            return;
        }

        if (entrenamientosFinalizados.isEmpty()) {
            Toast.makeText(this, "No hay entrenamientos finalizados para esta sección", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] opciones = new String[entrenamientosFinalizados.size()];
        for (int i = 0; i < entrenamientosFinalizados.size(); i++) {
            Entrenamiento e = entrenamientosFinalizados.get(i);
            String fechaStr = dateFormat.format(new Date(e.FechaFin));
            opciones[i] = "Entrenamiento #" + (i + 1) + " (" + fechaStr + ")";
        }

        new AlertDialog.Builder(this)
                .setTitle("Seleccionar Entrenamiento (" + (esA ? "A" : "B") + ")")
                .setItems(opciones, (dialog, which) -> {
                    Entrenamiento seleccionado = entrenamientosFinalizados.get(which);
                    String textoSeleccionado = opciones[which];
                    if (esA) {
                        entrenamientoA = seleccionado;
                        numEntA = which + 1;
                        tietEntrenamientoA.setText(textoSeleccionado);
                    } else {
                        entrenamientoB = seleccionado;
                        numEntB = which + 1;
                        tietEntrenamientoB.setText(textoSeleccionado);
                    }
                })
                .show();
    }

    private void realizarComparacion() {
        if (entrenamientoA == null || entrenamientoB == null) {
            Toast.makeText(this, "Selecciona ambos entrenamientos para comparar", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            AppDatabase db = AppDatabase.getDatabase(this);

            Entrenamiento eBase, eTarget;
            int nBase, nTarget;

            if (entrenamientoA.FechaFin < entrenamientoB.FechaFin) {
                eBase = entrenamientoA; nBase = numEntA;
                eTarget = entrenamientoB; nTarget = numEntB;
            } else {
                eBase = entrenamientoB; nBase = numEntB;
                eTarget = entrenamientoA; nTarget = numEntA;
            }

            final Entrenamiento eA_final = eBase;
            final int nA_final = nBase;
            final Entrenamiento eB_final = eTarget;
            final int nB_final = nTarget;

            Map<Integer, Double> volumenesBase = obtenerVolumenesDeEntrenamiento(db, eBase.IdEntrenamiento);
            Map<Integer, Double> volumenesTarget = obtenerVolumenesDeEntrenamiento(db, eTarget.IdEntrenamiento);

            List<ComparacionAdapter.ComparacionFila> filas = new ArrayList<>();
            List<SeccionXejercicio> sxeList = db.seccionXejercicioDao().getEjerciciosBySeccion(seccionSeleccionada.IdSeccion);

            for (SeccionXejercicio sxe : sxeList) {
                Ejercicio ej = db.ejercicioDao().obtenerEjercicioPorId(sxe.IdEjercicio);
                double vBase = volumenesBase.getOrDefault(sxe.IdSeccionXejercicio, 0.0);
                double vTarget = volumenesTarget.getOrDefault(sxe.IdSeccionXejercicio, 0.0);

                if (vBase > 0 || vTarget > 0) {
                    filas.add(new ComparacionAdapter.ComparacionFila(ej.NombreEjercicio, sxe.IdSeccionXejercicio, vTarget, vBase));
                }
            }

            runOnUiThread(() -> {
                tvHeaderVolA.setText("Ent. #" + nA_final);
                tvHeaderVolB.setText("Ent. #" + nB_final);
                llTablaComparacion.setVisibility(View.VISIBLE);
                rvComparacion.setAdapter(new ComparacionAdapter(filas, item -> mostrarPopUpDetalle(item, eA_final, nA_final, eB_final, nB_final)));
            });
        }).start();
    }

    private Map<Integer, Double> obtenerVolumenesDeEntrenamiento(AppDatabase db, int idEntrenamiento) {
        List<Registro> registros = db.registroDao().obtenerRegistrosPorEntrenamiento(idEntrenamiento);
        Map<Integer, Double> volumenes = new HashMap<>();
        for (Registro r : registros) {
            double vol = r.PesoRegistro * r.Repeticiones;
            volumenes.put(r.IdSeccionXejercicio, volumenes.getOrDefault(r.IdSeccionXejercicio, 0.0) + vol);
        }
        return volumenes;
    }

    @Override
    public void onItemClick(ComparacionAdapter.ComparacionFila item) {
    }

    private void mostrarPopUpDetalle(ComparacionAdapter.ComparacionFila item, Entrenamiento entA, int nA, Entrenamiento entB, int nB) {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.pop_up_detalle_ejercicio_comparacion);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        TextView tvTitulo = dialog.findViewById(R.id.tv_titulo_detalle);
        TextView tvLabelA = dialog.findViewById(R.id.tv_label_ant);
        TextView tvLabelB = dialog.findViewById(R.id.tv_label_post);
        LinearLayout llFilas = dialog.findViewById(R.id.ll_filas_detalle);
        Button btnCerrar = dialog.findViewById(R.id.btn_cerrar_detalle);

        tvTitulo.setText("Detalle: " + item.getNombreEjercicio());
        tvLabelA.setText("Ent. #" + nA);
        tvLabelB.setText("Ent. #" + nB);

        new Thread(() -> {
            AppDatabase db = AppDatabase.getDatabase(this);
            List<Registro> registrosA = db.registroDao().obtenerRegistrosPorEntrenamiento(entA.IdEntrenamiento);
            List<Registro> registrosB = db.registroDao().obtenerRegistrosPorEntrenamiento(entB.IdEntrenamiento);

            List<Registro> filtradosA = new ArrayList<>();
            for (Registro r : registrosA) if (r.IdSeccionXejercicio == item.getIdSeccionXejercicio()) filtradosA.add(r);

            List<Registro> filtradosB = new ArrayList<>();
            for (Registro r : registrosB) if (r.IdSeccionXejercicio == item.getIdSeccionXejercicio()) filtradosB.add(r);

            int maxSets = Math.max(filtradosA.size(), filtradosB.size());

            runOnUiThread(() -> {
                for (int i = 0; i < maxSets; i++) {
                    View filaView = LayoutInflater.from(this).inflate(R.layout.item_comparacion_detalle_fila, llFilas, false);
                    TextView tvSet = filaView.findViewById(R.id.tv_detalle_set);
                    TextView tvA = filaView.findViewById(R.id.tv_detalle_ant);
                    TextView tvB = filaView.findViewById(R.id.tv_detalle_post);
                    TextView tvDif = filaView.findViewById(R.id.tv_detalle_dif);

                    tvSet.setText(String.valueOf(i + 1));

                    Registro rA = i < filtradosA.size() ? filtradosA.get(i) : null;
                    Registro rB = i < filtradosB.size() ? filtradosB.get(i) : null;

                    String sA = rA != null ? String.format(Locale.getDefault(), "%.1fk x %d", rA.PesoRegistro, rA.Repeticiones) : "-";
                    String sB = rB != null ? String.format(Locale.getDefault(), "%.1fk x %d", rB.PesoRegistro, rB.Repeticiones) : "-";

                    tvA.setText(sA);
                    tvB.setText(sB);

                    if (rA != null && rB != null) {
                        double difPeso = rB.PesoRegistro - rA.PesoRegistro;
                        int difReps = rB.Repeticiones - rA.Repeticiones;
                        String d = String.format(Locale.getDefault(), "%s%.1fk\n%s%d reps", 
                                difPeso >= 0 ? "+" : "", difPeso,
                                difReps >= 0 ? "+" : "", difReps);
                        tvDif.setText(d);
                        if (difPeso > 0 || difReps > 0) tvDif.setTextColor(Color.parseColor("#4CAF50"));
                        else if (difPeso < 0 || difReps < 0) tvDif.setTextColor(Color.parseColor("#F44336"));
                    } else {
                        tvDif.setText("-");
                    }

                    llFilas.addView(filaView);
                }
            });
        }).start();

        btnCerrar.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }
}

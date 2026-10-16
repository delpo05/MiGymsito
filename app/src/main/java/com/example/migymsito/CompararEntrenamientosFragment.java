package com.example.migymsito;

import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.migymsito.data.Ejercicio;
import com.example.migymsito.data.Entrenamiento;
import com.example.migymsito.data.Registro;
import com.example.migymsito.data.RegistroCardio;
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

public class CompararEntrenamientosFragment extends Fragment implements ComparacionAdapter.OnItemClickListener {

    private AutoCompleteTextView actvRutina, actvSeccion;
    private TextInputEditText tietEntrenamientoA, tietEntrenamientoB;
    private LinearLayout llTablaComparacion;
    private RecyclerView rvComparacion;
    private TextView tvHeaderVolA, tvHeaderVolB;
    private SharedViewModel sharedViewModel;

    private RutinaRepository rutinaRepository;
    private SeccionRepository seccionRepository;

    private List<Rutina> listaRutinas = new ArrayList<>();
    private List<Seccion> listaSecciones = new ArrayList<>();
    private List<Entrenamiento> entrenamientosFinalizados = new ArrayList<>();
    
    private Seccion seccionSeleccionada;
    private Entrenamiento entrenamientoA, entrenamientoB;
    private int numEntA, numEntB;

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.comparar_entrenamientos_activity, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        if (getActivity() != null) {
            View toolbarInclude = getActivity().findViewById(R.id.include_toolbar);
            if (toolbarInclude != null) toolbarInclude.setVisibility(View.VISIBLE);
            
            rutinaRepository = new RutinaRepository(getActivity().getApplication());
            seccionRepository = new SeccionRepository(getActivity().getApplication());

            sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
            sharedViewModel.getImportFinishedTrigger().observe(getViewLifecycleOwner(), finished -> {
                if (finished != null && finished) {
                    cargarRutinas();
                    sharedViewModel.resetImportFinishedTrigger();
                }
            });

            sharedViewModel.getUserLoadedTrigger().observe(getViewLifecycleOwner(), loaded -> {
                if (loaded != null && loaded) {
                    if (seccionSeleccionada == null) {
                        manejarArgumentsDirecto();
                    }
                    cargarRutinas();
                    sharedViewModel.resetUserLoadedTrigger();
                }
            });
        }

        actvRutina = view.findViewById(R.id.actv_rutina);
        actvSeccion = view.findViewById(R.id.actv_seccion);
        tietEntrenamientoA = view.findViewById(R.id.tiet_entrenamiento_a);
        tietEntrenamientoB = view.findViewById(R.id.tiet_entrenamiento_b);
        MaterialButton btnComparar = view.findViewById(R.id.btn_comparar);
        llTablaComparacion = view.findViewById(R.id.ll_tabla_comparacion);
        rvComparacion = view.findViewById(R.id.rv_comparacion);
        tvHeaderVolA = view.findViewById(R.id.tv_header_vol_ent_a);
        tvHeaderVolB = view.findViewById(R.id.tv_header_vol_ent_b);

        rvComparacion.setLayoutManager(new LinearLayoutManager(getContext()));

        cargarRutinas();
        configurarSelectoresEntrenamiento();

        btnComparar.setOnClickListener(v -> realizarComparacion());

        manejarArgumentsDirecto();
    }

    private void manejarArgumentsDirecto() {
        if (getArguments() != null && getArguments().containsKey("idSeccion")) {
            int idSeccion = getArguments().getInt("idSeccion", -1);
            int idEntB_intent = getArguments().getInt("idEntB", -1);
            int idEntA_intent = getArguments().getInt("idEntA", -1);

            new Thread(() -> {
                AppDatabase db = AppDatabase.getDatabase(getContext());
                int userId = -1;
                if (MainActivity.usuarioLogueado != null) {
                    userId = MainActivity.usuarioLogueado.IdUsuario;
                } else if (getActivity() != null) {
                    com.example.migymsito.dataRepository.UsuarioRepository tempRepo = new com.example.migymsito.dataRepository.UsuarioRepository(getActivity().getApplication());
                    userId = tempRepo.obtenerIdSesion();
                }

                if (userId == -1) return;

                List<Seccion> todas = db.seccionDao().obtenerSeccionesPorUsuario(userId);
                for (Seccion s : todas) {
                    if (s.IdSeccion == idSeccion) {
                        seccionSeleccionada = s;
                        break;
                    }
                }

                if (seccionSeleccionada != null) {
                    entrenamientosFinalizados = db.entrenamientoDao().getEntrenamientosFinalizadosPorSeccion(userId, idSeccion);

                    if (idEntB_intent != -1) {
                        for (int i = 0; i < entrenamientosFinalizados.size(); i++) {
                            if (entrenamientosFinalizados.get(i).IdEntrenamiento == idEntB_intent) {
                                entrenamientoB = entrenamientosFinalizados.get(i);
                                numEntB = entrenamientoB.NumeroEntrenamiento;
                                break;
                            }
                        }
                    }
                    if (idEntA_intent != -1) {
                        for (int i = 0; i < entrenamientosFinalizados.size(); i++) {
                            if (entrenamientosFinalizados.get(i).IdEntrenamiento == idEntA_intent) {
                                entrenamientoA = entrenamientosFinalizados.get(i);
                                numEntA = entrenamientoA.NumeroEntrenamiento;
                                break;
                            }
                        }
                    }

                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            actvSeccion.setText(seccionSeleccionada.NombreSeccion);
                            if (entrenamientoA != null) {
                                String textA = String.format("Entrenamiento #%d (%s)", numEntA, dateFormat.format(new Date(entrenamientoA.FechaFin)));
                                tietEntrenamientoA.setText(textA);
                            }
                            if (entrenamientoB != null) {
                                String textB = String.format("Entrenamiento #%d (%s)", numEntB, dateFormat.format(new Date(entrenamientoB.FechaFin)));
                                tietEntrenamientoB.setText(textB);
                            }

                            if (entrenamientoA != null && entrenamientoB != null) {
                                realizarComparacion();
                            }
                        });
                    }
                }
            }).start();
        }
    }

    private void cargarRutinas() {
        if (MainActivity.usuarioLogueado != null && rutinaRepository != null) {
            rutinaRepository.obtenerRutinasDeUsuario(MainActivity.usuarioLogueado.IdUsuario, rutinas -> {
                this.listaRutinas = new ArrayList<>(rutinas);
                List<String> nombres = new ArrayList<>();
                nombres.add("Todas");

                for (Rutina r : rutinas) nombres.add(r.NombreRutina);

                actvRutina.setAdapter(new ArrayAdapter<>(requireContext(), R.layout.dropdown_item, nombres));

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
        if (MainActivity.usuarioLogueado != null && seccionRepository != null) {
            seccionRepository.obtenerSeccionesPorUsuario(MainActivity.usuarioLogueado.IdUsuario, secciones -> {
                this.listaSecciones = secciones;
                List<String> nombres = new ArrayList<>();
                for (Seccion s : secciones) nombres.add(s.NombreSeccion);
                actvSeccion.setAdapter(new ArrayAdapter<>(requireContext(), R.layout.dropdown_item, nombres));
                actvSeccion.setOnItemClickListener((p, v, pos, id) -> onSeccionSeleccionada(listaSecciones.get(pos)));
            });
        }
    }

    private void cargarSeccionesDeRutina(int idRutina) {
        if (seccionRepository != null) {
            seccionRepository.obtenerSeccionesDeRutina(idRutina, secciones -> {
                this.listaSecciones = secciones;
                List<String> nombres = new ArrayList<>();
                for (Seccion s : secciones) nombres.add(s.NombreSeccion);
                actvSeccion.setAdapter(new ArrayAdapter<>(requireContext(), R.layout.dropdown_item, nombres));
                actvSeccion.setOnItemClickListener((p, v, pos, id) -> onSeccionSeleccionada(listaSecciones.get(pos)));
            });
        }
    }

    private void onSeccionSeleccionada(Seccion seccion) {
        seccionSeleccionada = seccion;
        entrenamientoA = null;
        entrenamientoB = null;
        tietEntrenamientoA.setText("");
        tietEntrenamientoB.setText("");
        llTablaComparacion.setVisibility(View.GONE);

        new Thread(() -> {
            AppDatabase db = AppDatabase.getDatabase(getContext());
            entrenamientosFinalizados = db.entrenamientoDao().getEntrenamientosFinalizadosPorSeccion(MainActivity.usuarioLogueado.IdUsuario, seccion.IdSeccion);
        }).start();
    }

    private void configurarSelectoresEntrenamiento() {
        tietEntrenamientoA.setOnClickListener(v -> mostrarDialogoSeleccionEntrenamiento(true));
        tietEntrenamientoB.setOnClickListener(v -> mostrarDialogoSeleccionEntrenamiento(false));
    }

    private void mostrarDialogoSeleccionEntrenamiento(boolean esA) {
        if (seccionSeleccionada == null) {
            Toast.makeText(getContext(), "Primero selecciona una sección", Toast.LENGTH_SHORT).show();
            return;
        }

        if (entrenamientosFinalizados.isEmpty()) {
            Toast.makeText(getContext(), "No hay entrenamientos finalizados para esta sección", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] opciones = new String[entrenamientosFinalizados.size()];
        for (int i = 0; i < entrenamientosFinalizados.size(); i++) {
            Entrenamiento e = entrenamientosFinalizados.get(i);
            String fechaStr = dateFormat.format(new Date(e.FechaFin));
            opciones[i] = String.format("Entrenamiento #%d (%s)", e.NumeroEntrenamiento, fechaStr);
        }

        new AlertDialog.Builder(requireContext())
                .setTitle(String.format("Seleccionar Entrenamiento (%s)", (esA ? "A" : "B")))
                .setItems(opciones, (dialog, which) -> {
                    Entrenamiento seleccionado = entrenamientosFinalizados.get(which);
                    String textoSeleccionado = opciones[which];
                    if (esA) {
                        entrenamientoA = seleccionado;
                        numEntA = seleccionado.NumeroEntrenamiento;
                        tietEntrenamientoA.setText(textoSeleccionado);
                    } else {
                        entrenamientoB = seleccionado;
                        numEntB = seleccionado.NumeroEntrenamiento;
                        tietEntrenamientoB.setText(textoSeleccionado);
                    }
                })
                .show();
    }

    private void realizarComparacion() {
        if (entrenamientoA == null || entrenamientoB == null) {
            Toast.makeText(getContext(), "Selecciona ambos entrenamientos para comparar", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            AppDatabase db = AppDatabase.getDatabase(getContext());

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
                Double volBase = volumenesBase.get(sxe.IdSeccionXejercicio);
                double vBase = volBase != null ? volBase : 0.0;
                Double volTarget = volumenesTarget.get(sxe.IdSeccionXejercicio);
                double vTarget = volTarget != null ? volTarget : 0.0;

                if (vBase > 0 || vTarget > 0) {
                    filas.add(new ComparacionAdapter.ComparacionFila(ej.NombreEjercicio, sxe.IdSeccionXejercicio, vTarget, vBase));
                }
            }

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    if (tvHeaderVolA != null) tvHeaderVolA.setText(String.format("Ent. #%d", nA_final));
                    if (tvHeaderVolB != null) tvHeaderVolB.setText(String.format("Ent. #%d", nB_final));
                    llTablaComparacion.setVisibility(View.VISIBLE);
                    rvComparacion.setAdapter(new ComparacionAdapter(filas, item -> mostrarPopUpDetalle(item, eA_final, nA_final, eB_final, nB_final)));
                });
            }
        }).start();
    }

    private Map<Integer, Double> obtenerVolumenesDeEntrenamiento(AppDatabase db, int idEntrenamiento) {
        Map<Integer, Double> volumenes = new HashMap<>();

        List<Registro> registros = db.registroDao().obtenerRegistrosPorEntrenamiento(idEntrenamiento);
        for (Registro r : registros) {
            double vol = r.PesoRegistro * r.Repeticiones;
            Double current = volumenes.get(r.IdSeccionXejercicio);
            volumenes.put(r.IdSeccionXejercicio, (current != null ? current : 0.0) + vol);
        }

        List<RegistroCardio> registrosCardio = db.registroCardioDao().obtenerPorEntrenamiento(idEntrenamiento);
        for (RegistroCardio rc : registrosCardio) {
            double vol = 0.0;
            if (rc.Distancia != null && rc.Distancia > 0) {
                vol = rc.Distancia;
            } else if (rc.DuracionSegundos > 0) {
                vol = rc.DuracionSegundos / 60.0;
            } else if (rc.CaloriasQuemadas != null && rc.CaloriasQuemadas > 0) {
                vol = rc.CaloriasQuemadas;
            }
            Double current = volumenes.get(rc.IdSeccionXejercicio);
            volumenes.put(rc.IdSeccionXejercicio, (current != null ? current : 0.0) + vol);
        }

        return volumenes;
    }

    @Override
    public void onItemClick(ComparacionAdapter.ComparacionFila item) {
    }

    private void mostrarPopUpDetalle(ComparacionAdapter.ComparacionFila item, Entrenamiento entA, int nA, Entrenamiento entB, int nB) {
        Dialog dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.pop_up_detalle_ejercicio_comparacion);
        
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
            lp.copyFrom(dialog.getWindow().getAttributes());
            lp.width = (int) (getResources().getDisplayMetrics().widthPixels * 0.90);
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
            dialog.getWindow().setAttributes(lp);
        }

        TextView tvTitulo = dialog.findViewById(R.id.tv_titulo_detalle);
        TextView tvLabelA = dialog.findViewById(R.id.tv_label_ant);
        TextView tvLabelB = dialog.findViewById(R.id.tv_label_post);
        LinearLayout llFilas = dialog.findViewById(R.id.ll_filas_detalle);
        Button btnCerrar = dialog.findViewById(R.id.btn_cerrar_detalle);

        tvTitulo.setText(item.getNombreEjercicio());
        if (tvLabelA != null) tvLabelA.setText(String.format("Ent. #%d", nA));
        if (tvLabelB != null) tvLabelB.setText(String.format("Ent. #%d", nB));

        new Thread(() -> {
            AppDatabase db = AppDatabase.getDatabase(getContext());
            SeccionXejercicio sxe = db.seccionXejercicioDao().getRelacionById(item.getIdSeccionXejercicio());
            Ejercicio ej = null;
            if (sxe != null) {
                ej = db.ejercicioDao().obtenerEjercicioPorId(sxe.IdEjercicio);
            }

            boolean esCardio = ej != null && "CARDIO".equalsIgnoreCase(ej.CategoriaEjercicio);

            if (esCardio) {
                List<RegistroCardio> listA = db.registroCardioDao().obtenerPorEntrenamiento(entA.IdEntrenamiento);
                List<RegistroCardio> listB = db.registroCardioDao().obtenerPorEntrenamiento(entB.IdEntrenamiento);

                RegistroCardio rcA = null;
                for (RegistroCardio rc : listA) {
                    if (rc.IdSeccionXejercicio == item.getIdSeccionXejercicio()) {
                        rcA = rc;
                        break;
                    }
                }
                RegistroCardio rcB = null;
                for (RegistroCardio rc : listB) {
                    if (rc.IdSeccionXejercicio == item.getIdSeccionXejercicio()) {
                        rcB = rc;
                        break;
                    }
                }

                final RegistroCardio finalRcA = rcA;
                final RegistroCardio finalRcB = rcB;

                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        agregarFilaCardio(llFilas, "Tiempo",
                                finalRcA != null ? formatDuration(finalRcA.DuracionSegundos) : "-",
                                finalRcB != null ? formatDuration(finalRcB.DuracionSegundos) : "-",
                                calcularDifTiempo(finalRcA != null ? finalRcA.DuracionSegundos : null, finalRcB != null ? finalRcB.DuracionSegundos : null));

                        if ((finalRcA != null && finalRcA.Distancia != null) || (finalRcB != null && finalRcB.Distancia != null)) {
                            Double dA = finalRcA != null ? finalRcA.Distancia : null;
                            Double dB = finalRcB != null ? finalRcB.Distancia : null;
                            String uA = finalRcA != null && finalRcA.UnidadDistancia != null ? finalRcA.UnidadDistancia : "km";
                            agregarFilaCardio(llFilas, "Dist.",
                                    dA != null ? String.format(Locale.getDefault(), "%.2f %s", dA, uA) : "-",
                                    dB != null ? String.format(Locale.getDefault(), "%.2f %s", dB, uA) : "-",
                                    calcularDifDouble(dA, dB, uA));
                        }

                        if ((finalRcA != null && finalRcA.CaloriasQuemadas != null) || (finalRcB != null && finalRcB.CaloriasQuemadas != null)) {
                            Integer cA = finalRcA != null ? finalRcA.CaloriasQuemadas : null;
                            Integer cB = finalRcB != null ? finalRcB.CaloriasQuemadas : null;
                            agregarFilaCardio(llFilas, "Cal.",
                                    cA != null ? cA + " kcal" : "-",
                                    cB != null ? cB + " kcal" : "-",
                                    calcularDifInteger(cA, cB, "kcal"));
                        }

                        if ((finalRcA != null && finalRcA.VelocidadPromedio != null) || (finalRcB != null && finalRcB.VelocidadPromedio != null)) {
                            Double vA = finalRcA != null ? finalRcA.VelocidadPromedio : null;
                            Double vB = finalRcB != null ? finalRcB.VelocidadPromedio : null;
                            agregarFilaCardio(llFilas, "Vel.",
                                    vA != null ? String.format(Locale.getDefault(), "%.1f km/h", vA) : "-",
                                    vB != null ? String.format(Locale.getDefault(), "%.1f km/h", vB) : "-",
                                    calcularDifDouble(vA, vB, "km/h"));
                        }

                        if ((finalRcA != null && finalRcA.RitmoCardiacoPromedio != null) || (finalRcB != null && finalRcB.RitmoCardiacoPromedio != null)) {
                            Integer rA = finalRcA != null ? finalRcA.RitmoCardiacoPromedio : null;
                            Integer rB = finalRcB != null ? finalRcB.RitmoCardiacoPromedio : null;
                            agregarFilaCardio(llFilas, "Pulso",
                                    rA != null ? rA + " bpm" : "-",
                                    rB != null ? rB + " bpm" : "-",
                                    calcularDifInteger(rA, rB, "bpm"));
                        }
                    });
                }
            } else {
                List<Registro> registrosA = db.registroDao().obtenerRegistrosPorEntrenamiento(entA.IdEntrenamiento);
                List<Registro> registrosB = db.registroDao().obtenerRegistrosPorEntrenamiento(entB.IdEntrenamiento);

                List<Registro> filtradosA = new ArrayList<>();
                for (Registro r : registrosA) if (r.IdSeccionXejercicio == item.getIdSeccionXejercicio()) filtradosA.add(r);

                List<Registro> filtradosB = new ArrayList<>();
                for (Registro r : registrosB) if (r.IdSeccionXejercicio == item.getIdSeccionXejercicio()) filtradosB.add(r);

                int maxSets = Math.max(filtradosA.size(), filtradosB.size());

                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        for (int index = 0; index < maxSets; index++) {
                            View filaView = LayoutInflater.from(requireContext()).inflate(R.layout.item_comparacion_detalle_fila, llFilas, false);
                            TextView tvSet = filaView.findViewById(R.id.tv_detalle_set);
                            TextView tvA = filaView.findViewById(R.id.tv_detalle_ant);
                            TextView tvB = filaView.findViewById(R.id.tv_detalle_post);
                            TextView tvDif = filaView.findViewById(R.id.tv_detalle_dif);

                            tvSet.setText(String.valueOf(index + 1));

                            Registro rA = index < filtradosA.size() ? filtradosA.get(index) : null;
                            Registro rB = index < filtradosB.size() ? filtradosB.get(index) : null;

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
                }
            }
        }).start();

        btnCerrar.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void agregarFilaCardio(LinearLayout parent, String labelSet, String textA, String textB, String difText) {
        View filaView = LayoutInflater.from(requireContext()).inflate(R.layout.item_comparacion_detalle_fila, parent, false);
        TextView tvSet = filaView.findViewById(R.id.tv_detalle_set);
        TextView tvA = filaView.findViewById(R.id.tv_detalle_ant);
        TextView tvB = filaView.findViewById(R.id.tv_detalle_post);
        TextView tvDif = filaView.findViewById(R.id.tv_detalle_dif);

        tvSet.setText(labelSet);
        tvA.setText(textA);
        tvB.setText(textB);
        tvDif.setText(difText);

        if (difText.startsWith("+")) {
            tvDif.setTextColor(Color.parseColor("#4CAF50"));
        } else if (difText.startsWith("-")) {
            tvDif.setTextColor(Color.parseColor("#F44336"));
        }

        parent.addView(filaView);
    }

    private String calcularDifTiempo(Long sA, Long sB) {
        if (sA == null || sB == null) return "-";
        long dif = sB - sA;
        String sign = dif >= 0 ? "+" : "-";
        return sign + formatDuration(Math.abs(dif));
    }

    private String calcularDifDouble(Double valA, Double valB, String unidad) {
        if (valA == null || valB == null) return "-";
        double dif = valB - valA;
        String sign = dif >= 0 ? "+" : "";
        return String.format(Locale.getDefault(), "%s%.2f %s", sign, dif, unidad);
    }

    private String calcularDifInteger(Integer valA, Integer valB, String unidad) {
        if (valA == null || valB == null) return "-";
        int dif = valB - valA;
        String sign = dif >= 0 ? "+" : "";
        return String.format(Locale.getDefault(), "%s%d %s", sign, dif, unidad);
    }

    private String formatDuration(long totalSeconds) {
        long hrs = totalSeconds / 3600;
        long mins = (totalSeconds % 3600) / 60;
        long secs = totalSeconds % 60;
        if (hrs > 0) {
            return String.format(Locale.getDefault(), "%02d:%02d:%02d", hrs, mins, secs);
        } else {
            return String.format(Locale.getDefault(), "%02d:%02d", mins, secs);
        }
    }
}

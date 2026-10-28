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
        if (getArguments() != null && (getArguments().containsKey("idEntA") || getArguments().containsKey("idSeccion"))) {
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

                // Al ser redireccionado tras finalizar entrenamiento, comparamos por ejercicio sin filtrar por sección
                seccionSeleccionada = null;

                entrenamientosFinalizados = db.entrenamientoDao().getEntrenamientosFinalizadosPorUsuario(userId);

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
                        actvSeccion.setText("");
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
            if (MainActivity.usuarioLogueado != null) {
                entrenamientosFinalizados = db.entrenamientoDao().getEntrenamientosFinalizadosPorUsuario(MainActivity.usuarioLogueado.IdUsuario);
            }
        }).start();
    }

    private void configurarSelectoresEntrenamiento() {
        tietEntrenamientoA.setOnClickListener(v -> mostrarDialogoSeleccionEntrenamiento(true));
        tietEntrenamientoB.setOnClickListener(v -> mostrarDialogoSeleccionEntrenamiento(false));
    }

    private void mostrarDialogoSeleccionEntrenamiento(boolean esA) {
        new Thread(() -> {
            AppDatabase db = AppDatabase.getDatabase(getContext());
            if (MainActivity.usuarioLogueado == null) return;
            entrenamientosFinalizados = db.entrenamientoDao().getEntrenamientosFinalizadosPorUsuario(MainActivity.usuarioLogueado.IdUsuario);

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    if (entrenamientosFinalizados.isEmpty()) {
                        Toast.makeText(getContext(), "No hay entrenamientos finalizados", Toast.LENGTH_SHORT).show();
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
                });
            }
        }).start();
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

            final int nA_final = nBase;
            final int nB_final = nTarget;

            int userId = MainActivity.usuarioLogueado != null ? MainActivity.usuarioLogueado.IdUsuario : -1;

            Map<Integer, Double> volumenesTarget = obtenerVolumenesPorEjercicio(db, eTarget.IdEntrenamiento);
            Map<Integer, Double> volumenesBaseMap = obtenerVolumenesPorEjercicio(db, eBase.IdEntrenamiento);

            java.util.Set<Integer> allEjercicioIds = new java.util.HashSet<>();
            allEjercicioIds.addAll(volumenesTarget.keySet());
            allEjercicioIds.addAll(volumenesBaseMap.keySet());

            List<ComparacionAdapter.ComparacionFila> filas = new ArrayList<>();

            for (Integer idEjercicio : allEjercicioIds) {
                if (seccionSeleccionada != null) {
                    List<SeccionXejercicio> relaciones = db.seccionXejercicioDao().getEjerciciosBySeccion(seccionSeleccionada.IdSeccion);
                    boolean perteneceASeccion = false;
                    for (SeccionXejercicio sxe : relaciones) {
                        if (sxe.IdEjercicio == idEjercicio) {
                            perteneceASeccion = true;
                            break;
                        }
                    }
                    if (!perteneceASeccion) continue;
                }

                Ejercicio ej = db.ejercicioDao().obtenerEjercicioPorId(idEjercicio);
                if (ej == null) continue;

                Double volTarget = volumenesTarget.get(idEjercicio);
                double vTarget = volTarget != null ? volTarget : 0.0;

                // Solo comparar ejercicios en los que se hicieron registros en el entrenamiento actual/objetivo
                if (vTarget <= 0) continue;

                Double volBase = volumenesBaseMap.get(idEjercicio);
                double vBase = volBase != null ? volBase : 0.0;
                int idEntBaseForExercise = eBase.IdEntrenamiento;

                if (vBase == 0.0 && userId != -1) {
                    Entrenamiento entPrevioEj = obtenerUltimoEntrenamientoPrevioConEjercicio(db, userId, idEjercicio, eTarget.IdEntrenamiento);
                    if (entPrevioEj != null) {
                        Map<Integer, Double> volPrevioMap = obtenerVolumenesPorEjercicio(db, entPrevioEj.IdEntrenamiento);
                        Double vPrev = volPrevioMap.get(idEjercicio);
                        if (vPrev != null && vPrev > 0) {
                            vBase = vPrev;
                            idEntBaseForExercise = entPrevioEj.IdEntrenamiento;
                        }
                    }
                }

                filas.add(new ComparacionAdapter.ComparacionFila(ej.NombreEjercicio, idEjercicio, idEntBaseForExercise, vTarget, vBase));
            }

            final int finalTargetId = eTarget.IdEntrenamiento;
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    if (tvHeaderVolA != null) tvHeaderVolA.setText(String.format("Ent. #%d", nA_final));
                    if (tvHeaderVolB != null) tvHeaderVolB.setText(String.format("Ent. #%d", nB_final));
                    llTablaComparacion.setVisibility(View.VISIBLE);
                    rvComparacion.setAdapter(new ComparacionAdapter(filas, item -> mostrarPopUpDetalle(item, finalTargetId, nB_final)));
                });
            }
        }).start();
    }

    private Entrenamiento obtenerUltimoEntrenamientoPrevioConEjercicio(AppDatabase db, int idUsuario, int idEjercicio, int idEntrenamientoActual) {
        List<Registro> registrosPrevios = db.registroDao().obtenerRegistrosUltimoEntrenamientoPrevio(idUsuario, idEjercicio, idEntrenamientoActual);
        if (registrosPrevios != null && !registrosPrevios.isEmpty()) {
            return db.entrenamientoDao().getEntrenamientoById(registrosPrevios.get(0).IdEntrenamiento);
        }
        
        List<RegistroCardio> cardioPrevio = db.registroCardioDao().obtenerPorEjercicioYUsuario(idUsuario, idEjercicio);
        for (RegistroCardio rc : cardioPrevio) {
            if (rc.IdEntrenamiento < idEntrenamientoActual) {
                Entrenamiento ent = db.entrenamientoDao().getEntrenamientoById(rc.IdEntrenamiento);
                if (ent != null && ent.FechaFin != null) {
                    return ent;
                }
            }
        }
        return null;
    }

    private Map<Integer, Double> obtenerVolumenesPorEjercicio(AppDatabase db, int idEntrenamiento) {
        Map<Integer, Double> volumenes = new HashMap<>();

        List<Registro> registros = db.registroDao().obtenerRegistrosPorEntrenamiento(idEntrenamiento);
        for (Registro r : registros) {
            SeccionXejercicio sxe = db.seccionXejercicioDao().getRelacionById(r.IdSeccionXejercicio);
            if (sxe != null) {
                double vol = r.PesoRegistro * r.Repeticiones;
                Double current = volumenes.get(sxe.IdEjercicio);
                volumenes.put(sxe.IdEjercicio, (current != null ? current : 0.0) + vol);
            }
        }

        List<RegistroCardio> registrosCardio = db.registroCardioDao().obtenerPorEntrenamiento(idEntrenamiento);
        for (RegistroCardio rc : registrosCardio) {
            SeccionXejercicio sxe = db.seccionXejercicioDao().getRelacionById(rc.IdSeccionXejercicio);
            if (sxe != null) {
                double vol = 0.0;
                if (rc.Distancia != null && rc.Distancia > 0) {
                    vol = rc.Distancia;
                } else if (rc.DuracionSegundos > 0) {
                    vol = rc.DuracionSegundos / 60.0;
                } else if (rc.CaloriasQuemadas != null && rc.CaloriasQuemadas > 0) {
                    vol = rc.CaloriasQuemadas;
                }
                Double current = volumenes.get(sxe.IdEjercicio);
                volumenes.put(sxe.IdEjercicio, (current != null ? current : 0.0) + vol);
            }
        }

        return volumenes;
    }

    @Override
    public void onItemClick(ComparacionAdapter.ComparacionFila item) {
    }

    private void mostrarPopUpDetalle(ComparacionAdapter.ComparacionFila item, int idEntrenamientoTarget, int nTarget) {
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

        new Thread(() -> {
            AppDatabase db = AppDatabase.getDatabase(getContext());
            Ejercicio ej = db.ejercicioDao().obtenerEjercicioPorId(item.getIdEjercicio());
            int idEntBase = item.getIdEntrenamientoBase();
            Entrenamiento entBase = db.entrenamientoDao().getEntrenamientoById(idEntBase);

            int nBase = (entBase != null) ? entBase.NumeroEntrenamiento : -1;

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    if (tvLabelA != null) tvLabelA.setText(nBase != -1 ? String.format("Ent. #%d", nBase) : "-");
                    if (tvLabelB != null) tvLabelB.setText(String.format("Ent. #%d", nTarget));
                });
            }

            boolean esCardio = ej != null && "CARDIO".equalsIgnoreCase(ej.CategoriaEjercicio);

            if (esCardio) {
                List<RegistroCardio> listTarget = db.registroCardioDao().obtenerPorEntrenamientoYEjercicio(idEntrenamientoTarget, item.getIdEjercicio());
                List<RegistroCardio> listBase = entBase != null ? db.registroCardioDao().obtenerPorEntrenamientoYEjercicio(entBase.IdEntrenamiento, item.getIdEjercicio()) : new ArrayList<>();

                RegistroCardio rcBase = listBase.isEmpty() ? null : listBase.get(0);
                RegistroCardio rcTarget = listTarget.isEmpty() ? null : listTarget.get(0);

                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        agregarFilaCardio(llFilas, "Tiempo",
                                rcBase != null ? formatDuration(rcBase.DuracionSegundos) : "-",
                                rcTarget != null ? formatDuration(rcTarget.DuracionSegundos) : "-",
                                calcularDifTiempo(rcBase != null ? rcBase.DuracionSegundos : null, rcTarget != null ? rcTarget.DuracionSegundos : null));

                        if ((rcBase != null && rcBase.Distancia != null) || (rcTarget != null && rcTarget.Distancia != null)) {
                            Double dA = rcBase != null ? rcBase.Distancia : null;
                            Double dB = rcTarget != null ? rcTarget.Distancia : null;
                            String uA = rcTarget != null && rcTarget.UnidadDistancia != null ? rcTarget.UnidadDistancia : (rcBase != null && rcBase.UnidadDistancia != null ? rcBase.UnidadDistancia : "km");
                            agregarFilaCardio(llFilas, "Dist.",
                                    dA != null ? String.format(Locale.getDefault(), "%.2f %s", dA, uA) : "-",
                                    dB != null ? String.format(Locale.getDefault(), "%.2f %s", dB, uA) : "-",
                                    calcularDifDouble(dA, dB, uA));
                        }

                        if ((rcBase != null && rcBase.CaloriasQuemadas != null) || (rcTarget != null && rcTarget.CaloriasQuemadas != null)) {
                            Integer cA = rcBase != null ? rcBase.CaloriasQuemadas : null;
                            Integer cB = rcTarget != null ? rcTarget.CaloriasQuemadas : null;
                            agregarFilaCardio(llFilas, "Cal.",
                                    cA != null ? cA + " kcal" : "-",
                                    cB != null ? cB + " kcal" : "-",
                                    calcularDifInteger(cA, cB, "kcal"));
                        }

                        if ((rcBase != null && rcBase.VelocidadPromedio != null) || (rcTarget != null && rcTarget.VelocidadPromedio != null)) {
                            Double vA = rcBase != null ? rcBase.VelocidadPromedio : null;
                            Double vB = rcTarget != null ? rcTarget.VelocidadPromedio : null;
                            agregarFilaCardio(llFilas, "Vel.",
                                    vA != null ? String.format(Locale.getDefault(), "%.1f km/h", vA) : "-",
                                    vB != null ? String.format(Locale.getDefault(), "%.1f km/h", vB) : "-",
                                    calcularDifDouble(vA, vB, "km/h"));
                        }

                        if ((rcBase != null && rcBase.RitmoCardiacoPromedio != null) || (rcTarget != null && rcTarget.RitmoCardiacoPromedio != null)) {
                            Integer rA = rcBase != null ? rcBase.RitmoCardiacoPromedio : null;
                            Integer rB = rcTarget != null ? rcTarget.RitmoCardiacoPromedio : null;
                            agregarFilaCardio(llFilas, "Pulso",
                                    rA != null ? rA + " bpm" : "-",
                                    rB != null ? rB + " bpm" : "-",
                                    calcularDifInteger(rA, rB, "bpm"));
                        }
                    });
                }
            } else {
                List<Registro> filtradosTarget = db.registroDao().obtenerRegistrosPorEntrenamientoYEjercicio(idEntrenamientoTarget, item.getIdEjercicio());
                List<Registro> filtradosBase = entBase != null ? db.registroDao().obtenerRegistrosPorEntrenamientoYEjercicio(entBase.IdEntrenamiento, item.getIdEjercicio()) : new ArrayList<>();

                int maxSets = Math.max(filtradosBase.size(), filtradosTarget.size());

                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        for (int index = 0; index < maxSets; index++) {
                            View filaView = LayoutInflater.from(requireContext()).inflate(R.layout.item_comparacion_detalle_fila, llFilas, false);
                            TextView tvSet = filaView.findViewById(R.id.tv_detalle_set);
                            TextView tvA = filaView.findViewById(R.id.tv_detalle_ant);
                            TextView tvB = filaView.findViewById(R.id.tv_detalle_post);
                            TextView tvDif = filaView.findViewById(R.id.tv_detalle_dif);

                            tvSet.setText(String.valueOf(index + 1));

                            Registro rA = index < filtradosBase.size() ? filtradosBase.get(index) : null;
                            Registro rB = index < filtradosTarget.size() ? filtradosTarget.get(index) : null;

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

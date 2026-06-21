package com.example.migymsito;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.migymsito.data.Ejercicio;
import com.example.migymsito.data.RegistroDetallado;
import com.example.migymsito.data.Rutina;
import com.example.migymsito.data.Seccion;
import com.example.migymsito.dataRepository.EjercicioRepository;
import com.example.migymsito.dataRepository.RegistroRepository;
import com.example.migymsito.dataRepository.RutinaRepository;
import com.example.migymsito.dataRepository.SeccionRepository;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MisRegistrosFragment extends Fragment {

    private AutoCompleteTextView autoCompleteRutinas, autoCompleteSecciones, autoCompleteEjercicios;
    private RecyclerView rvResultados;
    private RegistroDetalladoAdapter adapter;

    private RutinaRepository rutinaRepository;
    private SeccionRepository seccionRepository;
    private EjercicioRepository ejerciciosRepository;
    private RegistroRepository registroRepository;

    private List<Rutina> listaRutinas = new ArrayList<>();
    private List<Seccion> listaSecciones = new ArrayList<>();
    private List<Ejercicio> listaEjerciciosActuales = new ArrayList<>();

    private int idRutinaSeleccionada = -1;
    private int idSeccionSeleccionada = -1;
    private int idEjercicioSeleccionado = -1;

    private List<RegistroDetallado> registrosActuales = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_mis_registros, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        autoCompleteRutinas = view.findViewById(R.id.autoCompleteRutinas);
        autoCompleteSecciones = view.findViewById(R.id.autoCompleteSecciones);
        autoCompleteEjercicios = view.findViewById(R.id.autoCompleteEjercicios);
        rvResultados = view.findViewById(R.id.rvResultados);

        rvResultados.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new RegistroDetalladoAdapter(new ArrayList<>());
        rvResultados.setAdapter(adapter);

        rutinaRepository = new RutinaRepository(requireActivity().getApplication());
        seccionRepository = new SeccionRepository(requireActivity().getApplication());
        ejerciciosRepository = new EjercicioRepository(requireActivity().getApplication());
        registroRepository = new RegistroRepository(requireActivity().getApplication());

        cargarRutinasDelUsuario();
        configurarListeners();

        view.findViewById(R.id.btnBuscar).setOnClickListener(v -> buscarRegistros());
        view.findViewById(R.id.btnExportarCSV).setOnClickListener(v -> exportarCSV());
    }

    private void configurarListeners() {
        autoCompleteRutinas.setOnItemClickListener((parent, view, position, id) -> {
            autoCompleteSecciones.setText("");
            autoCompleteEjercicios.setText("");
            idSeccionSeleccionada = -1;
            idEjercicioSeleccionado = -1;

            if (position == 0) {
                idRutinaSeleccionada = -1;
                cargarTodasLasSeccionesDelUsuario();
            } else {
                Rutina seleccionada = listaRutinas.get(position - 1);
                idRutinaSeleccionada = seleccionada.IdRutina;
                cargarSeccionesDeRutina(idRutinaSeleccionada);
            }
        });

        autoCompleteSecciones.setOnItemClickListener((parent, view, position, id) -> {
            autoCompleteEjercicios.setText("");
            idEjercicioSeleccionado = -1;

            if (position == 0) {
                idSeccionSeleccionada = -1;
                cargarEjerciciosEnUso();
            } else {
                Seccion seleccionada = listaSecciones.get(position - 1);
                idSeccionSeleccionada = seleccionada.IdSeccion;
                cargarEjerciciosDeSeccion(idSeccionSeleccionada);
            }
        });

        autoCompleteEjercicios.setOnItemClickListener((parent, view, position, id) -> {
            if (position == 0) {
                idEjercicioSeleccionado = -1;
            } else {
                Ejercicio seleccionado = listaEjerciciosActuales.get(position - 1);
                idEjercicioSeleccionado = seleccionado.IdEjercicio;
            }
        });
    }

    private void cargarRutinasDelUsuario() {
        if (MainActivity.usuarioLogueado != null) {
            rutinaRepository.obtenerRutinasDeUsuario(MainActivity.usuarioLogueado.IdUsuario, rutinas -> {
                this.listaRutinas = rutinas;
                List<String> nombresRutinas = new ArrayList<>();
                nombresRutinas.add("Todas las rutinas");
                for (Rutina r : rutinas) {
                    nombresRutinas.add(r.NombreRutina);
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), R.layout.dropdown_item, nombresRutinas);
                autoCompleteRutinas.setAdapter(adapter);
            });
        }
    }

    private void cargarTodasLasSeccionesDelUsuario() {
        if (MainActivity.usuarioLogueado != null) {
            seccionRepository.obtenerSeccionesPorUsuario(MainActivity.usuarioLogueado.IdUsuario, this::actualizarDropdownSecciones);
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
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), R.layout.dropdown_item, nombresSecciones);
        autoCompleteSecciones.setAdapter(adapter);
    }

    private void cargarEjerciciosDeSeccion(int idSeccion) {
        ejerciciosRepository.obtenerEjerciciosPorSeccion(idSeccion, this::actualizarDropdownEjercicios);
    }

    private void cargarEjerciciosEnUso() {
        if (MainActivity.usuarioLogueado != null) {
            ejerciciosRepository.obtenerEjerciciosEnUso(MainActivity.usuarioLogueado.IdUsuario, this::actualizarDropdownEjercicios);
        }
    }

    private void actualizarDropdownEjercicios(List<Ejercicio> ejercicios) {
        this.listaEjerciciosActuales = ejercicios;
        List<String> nombres = new ArrayList<>();
        nombres.add("Todos los ejercicios");
        for (Ejercicio e : ejercicios) {
            nombres.add(e.NombreEjercicio);
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), R.layout.dropdown_item, nombres);
        autoCompleteEjercicios.setAdapter(adapter);
    }

    private void buscarRegistros() {
        if (MainActivity.usuarioLogueado == null) return;

        registroRepository.buscarRegistrosDetallados(MainActivity.usuarioLogueado.IdUsuario, 
                idRutinaSeleccionada, idSeccionSeleccionada, idEjercicioSeleccionado, 
                registros -> {
                    this.registrosActuales = registros;
                    adapter.setRegistros(registros);
                    if (registros.isEmpty()) {
                        Toast.makeText(getContext(), "No se encontraron registros", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void exportarCSV() {
        if (registrosActuales.isEmpty()) {
            Toast.makeText(getContext(), "Primero realiza una búsqueda con resultados", Toast.LENGTH_SHORT).show();
            return;
        }

        StringBuilder csv = new StringBuilder();
        String sep = ";";
        csv.append("Rutina").append(sep)
           .append("Seccion").append(sep)
           .append("Ejercicio").append(sep)
           .append("Numero de Serie").append(sep)
           .append("Cantidad de repeticiones").append(sep)
           .append("Peso").append(sep)
           .append("Es Peso Corporal").append(sep)
           .append("Barra").append(sep)
           .append("Peso Barra").append(sep)
           .append("Hora y Fecha\n");

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

        for (RegistroDetallado reg : registrosActuales) {
            csv.append(reg.nombreRutina).append(sep)
               .append(reg.nombreSeccion).append(sep)
               .append(reg.nombreEjercicio).append(sep)
               .append(reg.numSerie).append(sep)
               .append(reg.repeticiones).append(sep)
               .append(String.valueOf(reg.peso).replace(".", ",")).append(sep)
               .append(reg.esPesoCorporal ? "Si" : "No").append(sep)
               .append(reg.tipoBarra != null ? reg.tipoBarra : "-").append(sep)
               .append(reg.pesoBarra != null ? String.valueOf(reg.pesoBarra).replace(".", ",") : "-").append(sep)
               .append(sdf.format(new Date(reg.fecha)))
               .append("\n");
        }

        generarYCompartirArchivo(csv.toString());
    }

    private void generarYCompartirArchivo(String contenido) {
        try {
            File path = new File(requireContext().getCacheDir(), "registros");
            if (!path.exists()) path.mkdirs();
            File file = new File(path, "mis_registros.csv");
            FileOutputStream writer = new FileOutputStream(file);
            writer.write(contenido.getBytes(StandardCharsets.UTF_8));
            writer.close();

            Uri contentUri = FileProvider.getUriForFile(requireContext(), "com.example.migymsito.fileprovider", file);
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/csv");
            shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(shareIntent, "Compartir registros"));

        } catch (IOException e) {
            Toast.makeText(getContext(), "Error al exportar: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}

package com.example.migymsito;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import com.example.migymsito.data.Seccion;
import com.example.migymsito.dataRepository.EjercicioRepository;
import com.example.migymsito.dataRepository.SeccionRepository;

import java.util.ArrayList;
import java.util.List;

public class EstadisticasActivity extends HeaderActivity {

    private AutoCompleteTextView autoCompleteSecciones, autoCompleteEjercicios, autoCompleteConsulta;
    private SeccionRepository seccionRepository;
    private EjercicioRepository ejerciciosRepository;
    private List<Seccion> listaSecciones = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.estadisticas_activity);

        // Inicializar vistas
        autoCompleteSecciones = findViewById(R.id.autoCompleteSecciones);
        autoCompleteEjercicios = findViewById(R.id.autoCompleteEjercicios);
        autoCompleteConsulta = findViewById(R.id.autoCompleteConsulta);

        seccionRepository = new SeccionRepository(getApplication());
        ejerciciosRepository = new EjercicioRepository(getApplication());

        cargarSeccionesDelUsuario();
        configurarDropdownConsulta();
    }

    private void cargarSeccionesDelUsuario() {
        if (usuarioLogueado != null) {
            seccionRepository.obtenerSeccionesPorUsuario(usuarioLogueado.IdUsuario, secciones -> {
                this.listaSecciones = secciones;
                
                List<String> nombresSecciones = new ArrayList<>();
                for (Seccion s : secciones) {
                    nombresSecciones.add(s.NombreSeccion);
                }

                // Usamos el layout personalizado dropdown_item
                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                        this,
                        R.layout.dropdown_item,
                        nombresSecciones
                );
                autoCompleteSecciones.setAdapter(adapter);

                autoCompleteSecciones.setOnItemClickListener((parent, view, position, id) -> {
                    Seccion seleccionada = listaSecciones.get(position);
                    autoCompleteEjercicios.setText(""); 
                    cargarEjerciciosDeSeccion(seleccionada.IdSeccion);
                });
            });
        } else {
            Toast.makeText(this, "Error: Usuario no identificado", Toast.LENGTH_SHORT).show();
        }
    }

    public void cargarEjerciciosDeSeccion(int idSeccion) {
        ejerciciosRepository.obtenerNombresEjerciciosPorSeccion(idSeccion, nombresEjercicios -> {
            // Usamos el layout personalizado dropdown_item
            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                    this,
                    R.layout.dropdown_item,
                    nombresEjercicios
            );
            autoCompleteEjercicios.setAdapter(adapter);
            
            if (nombresEjercicios.isEmpty()) {
                Toast.makeText(this, "No hay ejercicios en esta sección", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void configurarDropdownConsulta() {
        String[] opcionesConsulta = {"Progreso de Cargas", "Volumen de Entrenamiento", "Frecuencia"};
        // Usamos el layout personalizado dropdown_item
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                R.layout.dropdown_item,
                opcionesConsulta
        );
        autoCompleteConsulta.setAdapter(adapter);
    }
}
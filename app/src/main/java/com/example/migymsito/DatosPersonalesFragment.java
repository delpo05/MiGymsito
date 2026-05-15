package com.example.migymsito;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.migymsito.data.Historial;
import com.example.migymsito.dataRepository.UsuarioRepository;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DatosPersonalesFragment extends Fragment {

    private TextView tvHolaNombre;
    private EditText etNombre, etCorreo, etFecha, etAltura, etPeso;
    private AutoCompleteTextView etGenero;
    private UsuarioRepository usuarioRepository;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.datos_personales, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        if (getActivity() != null) {
            View toolbarInclude = getActivity().findViewById(R.id.include_toolbar);
            if (toolbarInclude != null) toolbarInclude.setVisibility(View.VISIBLE);
            
            usuarioRepository = new UsuarioRepository(getActivity().getApplication());
        }

        vincularVistas(view);
        configurarDropdownGenero();

        if (etFecha != null) {
            etFecha.setOnClickListener(v -> mostrarDatePicker());
            etFecha.setOnFocusChangeListener((v, hasFocus) -> {
                if (hasFocus) {
                    mostrarDatePicker();
                }
            });
        }

        if (MainActivity.usuarioLogueado != null) {
            cargarDatosUsuario();
        } else {
            Toast.makeText(getContext(), "Sesión no iniciada", Toast.LENGTH_SHORT).show();
        }

        View btnGuardar = view.findViewById(R.id.btnGuardarDatos);
        if (btnGuardar != null) {
            btnGuardar.setOnClickListener(this::EventoBotonActualizar);
        }
    }

    private void vincularVistas(View view) {
        tvHolaNombre = view.findViewById(R.id.tvHolaNombre);
        etNombre = view.findViewById(R.id.etDatoNombre);
        etCorreo = view.findViewById(R.id.etDatoCorreo);
        etFecha = view.findViewById(R.id.etDatoFecha);
        etGenero = view.findViewById(R.id.etDatoGenero);
        etAltura = view.findViewById(R.id.etDatoAltura);
        etPeso = view.findViewById(R.id.etDatoPeso);
    }

    private void configurarDropdownGenero() {
        String[] generos = {"Masculino", "Femenino", "Otro"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, generos);
        if (etGenero != null) {
            etGenero.setAdapter(adapter);
        }
    }

    private void mostrarDatePicker() {
        final Calendar c = Calendar.getInstance();
        
        String fechaActual = etFecha.getText().toString();
        if (!fechaActual.isEmpty()) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                Date date = sdf.parse(fechaActual);
                if (date != null) c.setTime(date);
            } catch (ParseException e) {
                Log.e("DatosPersonales", "Error parseando fecha");
            }
        }

        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(),
                (view, year1, monthOfYear, dayOfMonth) -> {
                    String fechaSeleccionada = String.format(Locale.getDefault(), "%02d/%02d/%d", dayOfMonth, (monthOfYear + 1), year1);
                    etFecha.setText(fechaSeleccionada);
                    etFecha.setError(null); 
                }, year, month, day);

        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    private void cargarDatosUsuario() {
        if (tvHolaNombre != null) tvHolaNombre.setText(String.format("Hola, %s !", MainActivity.usuarioLogueado.NombreUsuario));
        if (etNombre != null) etNombre.setText(MainActivity.usuarioLogueado.NombreUsuario);
        if (etCorreo != null) etCorreo.setText(MainActivity.usuarioLogueado.CorreoElectronicoUsuario);
        
        if (etGenero != null) {
            etGenero.setText(MainActivity.usuarioLogueado.GeneroUsuario, false);
        }

        if (MainActivity.usuarioLogueado.FechaNacimientoUsuario != null && etFecha != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            etFecha.setText(sdf.format(new Date(MainActivity.usuarioLogueado.FechaNacimientoUsuario)));
        }

        usuarioRepository.obtenerUltimoHistorial(MainActivity.usuarioLogueado.IdUsuario, result -> {
            if (result != null) {
                if (etAltura != null) etAltura.setText(String.valueOf(result.AlturaHistorial));
                if (etPeso != null) etPeso.setText(String.valueOf(result.PesoHistorial));
            }
        });
    }

    public void EventoBotonActualizar(View view) {
        if (MainActivity.usuarioLogueado == null) return;

        boolean isFormValid = true;

        String nuevoNombre = etNombre.getText().toString().trim();
        String nuevoCorreo = etCorreo.getText().toString().trim();
        String nuevoGenero = etGenero.getText().toString().trim();
        String fechaStr = etFecha.getText().toString().trim();
        String pesoStr = etPeso.getText().toString().trim();
        String alturaStr = etAltura.getText().toString().trim();

        etNombre.setError(null);
        etCorreo.setError(null);
        etFecha.setError(null);
        etGenero.setError(null);
        etPeso.setError(null);
        etAltura.setError(null);

        if (nuevoNombre.isEmpty()) {
            etNombre.setError("El nombre es obligatorio");
            isFormValid = false;
        }

        if (nuevoCorreo.isEmpty()) {
            etCorreo.setError("El correo es obligatorio");
            isFormValid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(nuevoCorreo).matches()) {
            etCorreo.setError("Correo inválido");
            isFormValid = false;
        }

        if (fechaStr.isEmpty()) {
            etFecha.setError("La fecha es obligatoria");
            isFormValid = false;
        }

        if (nuevoGenero.isEmpty()) {
            etGenero.setError("El género es obligatorio");
            isFormValid = false;
        }

        double peso = 0;
        if (pesoStr.isEmpty()) {
            etPeso.setError("El peso es obligatorio");
            isFormValid = false;
        } else {
            try {
                peso = Double.parseDouble(pesoStr);
                if (peso <= 0) {
                    etPeso.setError("Debe ser mayor a 0");
                    isFormValid = false;
                }
            } catch (NumberFormatException e) {
                etPeso.setError("Formato inválido");
                isFormValid = false;
            }
        }

        double altura = 0;
        if (alturaStr.isEmpty()) {
            etAltura.setError("La altura es obligatoria");
            isFormValid = false;
        } else {
            try {
                altura = Double.parseDouble(alturaStr);
                if (altura <= 0) {
                    etAltura.setError("Debe ser mayor a 0");
                    isFormValid = false;
                }
            } catch (NumberFormatException e) {
                etAltura.setError("Formato inválido");
                isFormValid = false;
            }
        }

        if (!isFormValid) {
            Toast.makeText(getContext(), "Por favor, corrige los errores señalados", Toast.LENGTH_SHORT).show();
            return;
        }

        MainActivity.usuarioLogueado.NombreUsuario = nuevoNombre;
        MainActivity.usuarioLogueado.CorreoElectronicoUsuario = nuevoCorreo;
        MainActivity.usuarioLogueado.GeneroUsuario = nuevoGenero;

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Date date = sdf.parse(fechaStr);
            if (date != null) {
                MainActivity.usuarioLogueado.FechaNacimientoUsuario = date.getTime();
            }
        } catch (ParseException e) {
            Log.e("DatosPersonales", "Error parseando fecha");
            etFecha.setError("Formato de fecha inválido");
            return;
        }

        Historial nuevoHistorial = new Historial();
        nuevoHistorial.IdUsuarioHistorial = MainActivity.usuarioLogueado.IdUsuario;
        nuevoHistorial.PesoHistorial = peso;
        nuevoHistorial.AlturaHistorial = altura;
        nuevoHistorial.FechaHistorial = System.currentTimeMillis();

        usuarioRepository.actualizarPerfilUsuario(MainActivity.usuarioLogueado, nuevoHistorial, (success, errorMessage) -> {
            if (success) {
                Toast.makeText(getContext(), "¡Datos actualizados!", Toast.LENGTH_SHORT).show();
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).actualizarNombreHeader();
                }
                if (tvHolaNombre != null) tvHolaNombre.setText(String.format("Hola, %s !", MainActivity.usuarioLogueado.NombreUsuario));
            } else {
                new AlertDialog.Builder(requireContext())
                        .setTitle("Error")
                        .setMessage(errorMessage)
                        .setPositiveButton("Cerrar", null)
                        .show();
            }
        });
    }
}

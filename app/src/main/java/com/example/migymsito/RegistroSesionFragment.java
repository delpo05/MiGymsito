package com.example.migymsito;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.migymsito.data.Historial;
import com.example.migymsito.data.Usuario;
import com.example.migymsito.dataRepository.UsuarioRepository;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class RegistroSesionFragment extends Fragment {

    private EditText etRegNombre, etRegCorreo, etRegFechaNac, etRegPeso, etRegAltura;
    private AutoCompleteTextView etRegGenero;
    private UsuarioRepository usuarioRepository;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.registro_sesion_activity, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        View toolbar = getActivity().findViewById(R.id.include_toolbar);
        if (toolbar != null) toolbar.setVisibility(View.GONE);

        usuarioRepository = new UsuarioRepository(getActivity().getApplication());

        etRegNombre = view.findViewById(R.id.etRegNombre);
        etRegCorreo = view.findViewById(R.id.etRegCorreo);
        etRegFechaNac = view.findViewById(R.id.etRegFechaNac);
        etRegPeso = view.findViewById(R.id.etRegPeso);
        etRegAltura = view.findViewById(R.id.etRegAltura);
        etRegGenero = view.findViewById(R.id.etRegGenero);

        String[] opcionesGenero = {"Masculino", "Femenino", "Otro"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), R.layout.dropdown_item, opcionesGenero);
        etRegGenero.setAdapter(adapter);

        etRegFechaNac.setOnClickListener(v -> mostrarDatePicker());

        view.findViewById(R.id.btnRegistrar).setOnClickListener(this::EventoBotonRegistrar);
    }

    private void mostrarDatePicker() {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(),
                (view, year1, monthOfYear, dayOfMonth) -> {
                    String fechaSeleccionada = String.format(Locale.getDefault(), "%02d/%02d/%d", dayOfMonth, (monthOfYear + 1), year1);
                    etRegFechaNac.setText(fechaSeleccionada);
                }, year, month, day);

        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    public void EventoBotonRegistrar(View view) {
        if (!validacionesRegistrarUsuario()) {
            return;
        }

        String correo = etRegCorreo.getText().toString().trim();

        usuarioRepository.validarCorreoExistente(correo, usuarioExistente -> {
            if (usuarioExistente != null) {
                etRegCorreo.setError("Este correo ya está registrado");
                Toast.makeText(getContext(), "El correo ya existe", Toast.LENGTH_SHORT).show();
            } else {
                registrarNuevoUsuario();
            }
        });
    }

    private void registrarNuevoUsuario() {
        Usuario nuevoUsuario = new Usuario();
        nuevoUsuario.NombreUsuario = etRegNombre.getText().toString().trim();
        nuevoUsuario.CorreoElectronicoUsuario = etRegCorreo.getText().toString().trim();
        nuevoUsuario.GeneroUsuario = etRegGenero.getText().toString();

        String fechaString = etRegFechaNac.getText().toString();
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Date date = sdf.parse(fechaString);
            if (date != null) {
                nuevoUsuario.FechaNacimientoUsuario = date.getTime();
            }
        } catch (ParseException e) {
            nuevoUsuario.FechaNacimientoUsuario = 0L;
        }

        Historial nuevoHistorial = new Historial();
        nuevoHistorial.PesoHistorial = Double.valueOf(etRegPeso.getText().toString());
        nuevoHistorial.AlturaHistorial = Double.valueOf(etRegAltura.getText().toString());
        nuevoHistorial.FechaHistorial = System.currentTimeMillis();

        usuarioRepository.registrarUsuarioConHistorial(nuevoUsuario, nuevoHistorial, idGenerado -> {
            if (idGenerado != -1) {
                Toast.makeText(getContext(), "Registro exitoso.", Toast.LENGTH_LONG).show();
                
                usuarioRepository.guardarIdSesion(idGenerado);
                MainActivity.usuarioLogueado = nuevoUsuario;
                nuevoUsuario.IdUsuario = idGenerado;
                ((MainActivity)requireActivity()).actualizarNombreHeader();

                Navigation.findNavController(requireView()).navigate(R.id.rutinasFragment);
            } else {
                Toast.makeText(getContext(), "Error al registrar usuario e historial", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean validacionesRegistrarUsuario() {
        boolean estado = true;

        if (etRegNombre.getText().toString().trim().isEmpty()) {
            etRegNombre.setError("Campo requerido");
            estado = false;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(etRegCorreo.getText().toString().trim()).matches()) {
            etRegCorreo.setError("Correo inválido");
            estado = false;
        }

        if (etRegFechaNac.getText().toString().isEmpty()) {
            etRegFechaNac.setError("Campo requerido");
            estado = false;
        }

        if (etRegPeso.getText().toString().trim().isEmpty()) {
            etRegPeso.setError("Campo requerido");
            estado = false;
        }

        if (etRegAltura.getText().toString().trim().isEmpty()) {
            etRegAltura.setError("Campo requerido");
            estado = false;
        }

        if (etRegGenero.getText().toString().trim().isEmpty()) {
            etRegGenero.setError("Campo requerido");
            estado = false;
        }

        return estado;
    }
}

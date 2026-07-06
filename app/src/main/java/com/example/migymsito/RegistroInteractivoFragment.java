package com.example.migymsito;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.migymsito.data.Historial;
import com.example.migymsito.data.Usuario;
import com.example.migymsito.dataRepository.UsuarioRepository;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class RegistroInteractivoFragment extends Fragment {

    private ViewPager2 viewPager;
    private ProgressBar progressBar;
    private MaterialButton btnBack, btnNext;
    private RegistrationViewModel viewModel;
    private UsuarioRepository usuarioRepository;

    private static final int TOTAL_STEPS = 6;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.registro_interactivo, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        if (getActivity() != null) {
            View toolbar = getActivity().findViewById(R.id.include_toolbar);
            if (toolbar != null) toolbar.setVisibility(View.GONE);
            usuarioRepository = new UsuarioRepository(getActivity().getApplication());
        }

        viewModel = new ViewModelProvider(this).get(RegistrationViewModel.class);

        progressBar = view.findViewById(R.id.progressBar);
        viewPager = view.findViewById(R.id.viewPagerRegistro);
        btnBack = view.findViewById(R.id.btnBack);
        btnNext = view.findViewById(R.id.btnNext);

        progressBar.setMax(TOTAL_STEPS);
        progressBar.setProgress(1);

        RegistrationAdapter adapter = new RegistrationAdapter();
        viewPager.setAdapter(adapter);
        viewPager.setUserInputEnabled(false);

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                progressBar.setProgress(position + 1);
                btnBack.setVisibility(position == 0 ? View.INVISIBLE : View.VISIBLE);
                
                if (position == TOTAL_STEPS - 1) {
                    btnNext.setText(R.string.finalizar);
                } else {
                    btnNext.setText(R.string.siguiente);
                }
            }
        });

        btnBack.setOnClickListener(v -> {
            if (viewPager.getCurrentItem() > 0) {
                viewPager.setCurrentItem(viewPager.getCurrentItem() - 1);
            }
        });

        btnNext.setOnClickListener(v -> {
            int currentStep = viewPager.getCurrentItem();
            if (validarPaso(currentStep)) {
                if (currentStep < TOTAL_STEPS - 1) {
                    viewPager.setCurrentItem(currentStep + 1);
                } else {
                    registrarUsuario();
                }
            }
        });
    }

    private boolean validarPaso(int step) {
        String value;
        switch (step) {
            case 0: // Nombre
                value = viewModel.nombre.getValue();
                if (value == null || value.trim().isEmpty()) {
                    mostrarError(R.string.error_nombre);
                    return false;
                }
                return true;
            case 1: // Correo
                value = viewModel.correo.getValue();
                if (value == null || !Patterns.EMAIL_ADDRESS.matcher(value.trim()).matches()) {
                    mostrarError(R.string.error_correo);
                    return false;
                }
                return true;
            case 2: // Fecha
                value = viewModel.fechaStr.getValue();
                if (value == null || value.isEmpty()) {
                    mostrarError(R.string.error_fecha);
                    return false;
                }
                return true;
            case 3: // Género
                value = viewModel.genero.getValue();
                if (value == null || value.isEmpty()) {
                    mostrarError(R.string.error_genero);
                    return false;
                }
                return true;
            case 4: // Peso
                value = viewModel.pesoStr.getValue();
                try {
                    if (value == null || value.isEmpty()) throw new Exception();
                    double p = Double.parseDouble(value);
                    if (p <= 0) throw new Exception();
                    viewModel.peso.setValue(p);
                } catch (Exception e) {
                    mostrarError(R.string.error_peso);
                    return false;
                }
                return true;
            case 5: // Altura
                value = viewModel.alturaStr.getValue();
                try {
                    if (value == null || value.isEmpty()) throw new Exception();
                    double a = Double.parseDouble(value);
                    if (a <= 0) throw new Exception();
                    viewModel.altura.setValue(a);
                } catch (Exception e) {
                    mostrarError(R.string.error_altura);
                    return false;
                }
                return true;
        }
        return false;
    }

    private void mostrarError(int resId) {
        if (getContext() != null) {
            Toast.makeText(getContext(), resId, Toast.LENGTH_SHORT).show();
        }
    }

    private void registrarUsuario() {
        Usuario nuevoUsuario = new Usuario();
        nuevoUsuario.NombreUsuario = viewModel.nombre.getValue() != null ? viewModel.nombre.getValue().trim() : "";
        nuevoUsuario.CorreoElectronicoUsuario = viewModel.correo.getValue() != null ? viewModel.correo.getValue().trim() : "";
        nuevoUsuario.GeneroUsuario = viewModel.genero.getValue() != null ? viewModel.genero.getValue() : "";

        String fecha = viewModel.fechaStr.getValue();
        if (fecha != null && !fecha.isEmpty()) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                Date date = sdf.parse(fecha);
                if (date != null) {
                    nuevoUsuario.FechaNacimientoUsuario = date.getTime();
                }
            } catch (ParseException e) {
                nuevoUsuario.FechaNacimientoUsuario = 0L;
            }
        } else {
            nuevoUsuario.FechaNacimientoUsuario = 0L;
        }

        Historial nuevoHistorial = new Historial();
        nuevoHistorial.PesoHistorial = viewModel.peso.getValue() != null ? viewModel.peso.getValue() : 0.0;
        nuevoHistorial.AlturaHistorial = viewModel.altura.getValue() != null ? viewModel.altura.getValue() : 0.0;
        nuevoHistorial.FechaHistorial = System.currentTimeMillis();

        if (usuarioRepository != null && getContext() != null) {
            usuarioRepository.validarCorreoExistente(nuevoUsuario.CorreoElectronicoUsuario, usuarioExistente -> {
                if (usuarioExistente != null) {
                    Toast.makeText(getContext(), R.string.error_correo_existe, Toast.LENGTH_SHORT).show();
                    viewPager.setCurrentItem(1);
                } else {
                    usuarioRepository.registrarUsuarioConHistorial(nuevoUsuario, nuevoHistorial, idGenerado -> {
                        if (idGenerado != -1) {
                            Toast.makeText(getContext(), R.string.registro_exitoso, Toast.LENGTH_LONG).show();
                            usuarioRepository.guardarIdSesion(idGenerado);
                            MainActivity.usuarioLogueado = nuevoUsuario;
                            nuevoUsuario.IdUsuario = idGenerado;
                            if (getActivity() instanceof MainActivity) {
                                ((MainActivity) getActivity()).actualizarNombreHeader();
                            }
                            Navigation.findNavController(requireView()).navigate(R.id.rutinasFragment);
                        } else {
                            Toast.makeText(getContext(), R.string.error_registro, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
        }
    }

    private class RegistrationAdapter extends RecyclerView.Adapter<RegistrationAdapter.ViewHolder> {

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.slide_registro_step, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.bind(position);
        }

        @Override
        public int getItemCount() {
            return TOTAL_STEPS;
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvQuestion;
            FrameLayout containerInput;

            ViewHolder(View itemView) {
                super(itemView);
                tvQuestion = itemView.findViewById(R.id.tvQuestion);
                containerInput = itemView.findViewById(R.id.containerInput);
            }

            void bind(int position) {
                containerInput.removeAllViews();
                LayoutInflater inflater = LayoutInflater.from(itemView.getContext());
                
                switch (position) {
                    case 0:
                        tvQuestion.setText(R.string.pregunta_nombre);
                        setupEditText(inflater, R.string.hint_nombre, InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS, viewModel.nombre);
                        break;
                    case 1:
                        tvQuestion.setText(R.string.pregunta_correo);
                        setupEditText(inflater, R.string.hint_correo, InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS, viewModel.correo);
                        break;
                    case 2:
                        tvQuestion.setText(R.string.pregunta_fecha);
                        setupDatePicker(inflater);
                        break;
                    case 3:
                        tvQuestion.setText(R.string.pregunta_genero);
                        setupGenderSpinner(inflater);
                        break;
                    case 4:
                        tvQuestion.setText(R.string.pregunta_peso);
                        setupEditText(inflater, R.string.hint_peso, InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL, viewModel.pesoStr);
                        break;
                    case 5:
                        tvQuestion.setText(R.string.pregunta_altura);
                        setupEditText(inflater, R.string.hint_altura, InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL, viewModel.alturaStr);
                        break;
                }
            }

            private void setupEditText(LayoutInflater inflater, int hintRes, int inputType, androidx.lifecycle.MutableLiveData<String> liveData) {
                View layout = inflater.inflate(R.layout.item_edit_text_registration, containerInput, false);
                TextInputLayout textInputLayout = layout.findViewById(R.id.textInputLayout);
                TextInputEditText editText = layout.findViewById(R.id.editText);
                
                textInputLayout.setHint(itemView.getContext().getString(hintRes));
                editText.setInputType(inputType);
                editText.setText(liveData.getValue());
                
                editText.addTextChangedListener(new TextWatcher() {
                    @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                    @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
                    @Override public void afterTextChanged(Editable s) {
                        liveData.setValue(s.toString());
                    }
                });
                
                containerInput.addView(layout);
            }

            private void setupDatePicker(LayoutInflater inflater) {
                View layout = inflater.inflate(R.layout.item_edit_text_registration, containerInput, false);
                TextInputLayout textInputLayout = layout.findViewById(R.id.textInputLayout);
                TextInputEditText editText = layout.findViewById(R.id.editText);
                
                textInputLayout.setHint(itemView.getContext().getString(R.string.hint_fecha));
                editText.setFocusable(false);
                editText.setClickable(true);
                editText.setText(viewModel.fechaStr.getValue());

                editText.setOnClickListener(v -> {
                    final Calendar c = Calendar.getInstance();
                    DatePickerDialog datePickerDialog = new DatePickerDialog(itemView.getContext(),
                            (view, year, month, dayOfMonth) -> {
                                String fecha = String.format(Locale.getDefault(), "%02d/%02d/%d", dayOfMonth, (month + 1), year);
                                viewModel.fechaStr.setValue(fecha);
                                editText.setText(fecha);
                            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
                    datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
                    datePickerDialog.show();
                });

                containerInput.addView(layout);
            }

            private void setupGenderSpinner(LayoutInflater inflater) {
                View layout = inflater.inflate(R.layout.item_spinner_registration, containerInput, false);
                AutoCompleteTextView spinner = layout.findViewById(R.id.spinner);
                
                String[] opciones = {"Masculino", "Femenino", "Otro"};
                ArrayAdapter<String> adapter = new ArrayAdapter<>(itemView.getContext(), R.layout.dropdown_item, opciones);
                spinner.setAdapter(adapter);
                spinner.setText(viewModel.genero.getValue(), false);

                spinner.setOnItemClickListener((parent, view, position, id) -> viewModel.genero.setValue(opciones[position]));

                containerInput.addView(layout);
            }
        }
    }
}

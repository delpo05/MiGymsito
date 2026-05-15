package com.example.migymsito;

import static android.content.Context.MODE_PRIVATE;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.migymsito.data.Rutina;
import com.example.migymsito.dataDataBase.AppDatabase;
import com.example.migymsito.dataRepository.UsuarioRepository;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class InicioSesionFragment extends Fragment {

    private UsuarioRepository usuarioRepository;
    private static final String CONFIG_PREFS = "AppConfig";
    private static final String KEY_LANGUAGE = "selected_language";
    private static final String KEY_TUTORIAL_DONE = "tutorial_done";
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.inicio_sesion_activity, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        if (getActivity() != null) {
            View toolbar = getActivity().findViewById(R.id.include_toolbar);
            if (toolbar != null) toolbar.setVisibility(View.GONE);
            
            usuarioRepository = new UsuarioRepository(getActivity().getApplication());
        }

        new Handler(Looper.getMainLooper()).postDelayed(this::verificarEstadoSesion, 1500);
    }

    private void verificarEstadoSesion() {
        if (!isAdded() || getActivity() == null) return;
        
        SharedPreferences prefs = getActivity().getSharedPreferences(CONFIG_PREFS, MODE_PRIVATE);
        String lang = prefs.getString(KEY_LANGUAGE, null);
        boolean tutorialDone = prefs.getBoolean(KEY_TUTORIAL_DONE, false);

        if (lang == null) {
            irASeleccionIdioma();
            return;
        }

        if (!tutorialDone) {
            irATutorial();
            return;
        }

        int idSesion = usuarioRepository.obtenerIdSesion();
        
        if (idSesion != -1) {
            continuarComoUsuarioLogueado(idSesion);
        } else {
            usuarioRepository.obtenerPrimerUsuario(usuario -> {
                if (usuario != null) {
                    usuarioRepository.guardarIdSesion(usuario.IdUsuario);
                    continuarComoUsuarioLogueado(usuario.IdUsuario);
                } else {
                    irARegistro();
                }
            });
        }
    }

    private void irASeleccionIdioma() {
        if (isAdded()) {
            Navigation.findNavController(requireView()).navigate(R.id.languageSelectionFragment);
        }
    }

    private void irATutorial() {
        if (isAdded()) {
            Navigation.findNavController(requireView()).navigate(R.id.tutorialFragment);
        }
    }

    private void continuarComoUsuarioLogueado(int idUsuario) {
        int idRutina = usuarioRepository.obtenerIdRutina();
        
        usuarioRepository.obtenerUsuarioPorId(idUsuario, usuario -> {
            if (usuario != null) {
                MainActivity.usuarioLogueado = usuario;
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).actualizarNombreHeader();
                }
                
                if (idRutina != -1) {
                    saltarDirectoASecciones(idRutina);
                } else {
                    irARutinas();
                }
            } else {
                usuarioRepository.eliminarSesion();
                irARegistro();
            }
        });
    }

    private void saltarDirectoASecciones(int idRutina) {
        executorService.execute(() -> {
            AppDatabase db = AppDatabase.getDatabase(getContext());
            Rutina rutina = db.rutinaDao().obtenerRutinaPorId(idRutina);
            if (getActivity() != null && isAdded()) {
                getActivity().runOnUiThread(() -> {
                    if (rutina != null) {
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("rutina", rutina);
                        Navigation.findNavController(requireView()).navigate(R.id.seccionesFragment, bundle);
                    } else {
                        usuarioRepository.eliminarRutinaSeleccionada();
                        irARutinas();
                    }
                });
            }
        });
    }

    private void irARutinas() {
        if (isAdded()) {
            Navigation.findNavController(requireView()).navigate(R.id.rutinasFragment);
        }
    }

    private void irARegistro() {
        if (isAdded()) {
            Navigation.findNavController(requireView()).navigate(R.id.registroSesionFragment);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }
}

package com.example.migymsito;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.migymsito.adapter.SeccionesAdapter;
import com.example.migymsito.data.Rutina;
import com.example.migymsito.data.Seccion;
import com.example.migymsito.dataRepository.SeccionRepository;

import java.util.ArrayList;
import java.util.List;

public class SeccionesFragment extends Fragment {

    private GridView gvSecciones;
    private Rutina rutinaActual;
    private SeccionRepository seccionRepository;
    private SharedViewModel sharedViewModel;
    private SeccionesAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.secciones_rutinas_activity, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        if (getActivity() != null) {
            View toolbarInclude = getActivity().findViewById(R.id.include_toolbar);
            if (toolbarInclude != null) toolbarInclude.setVisibility(View.VISIBLE);
            
            sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
            sharedViewModel.getImportFinishedTrigger().observe(getViewLifecycleOwner(), finished -> {
                if (finished != null && finished) {
                    cargarSeccionesDesdeDB();
                    sharedViewModel.resetImportFinishedTrigger();
                }
            });
        }

        if (getArguments() != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                rutinaActual = getArguments().getSerializable("rutina", Rutina.class);
            } else {
                rutinaActual = (Rutina) getArguments().getSerializable("rutina");
            }
        }

        gvSecciones = view.findViewById(R.id.gvGenerico);

        View btnFinalizar = view.findViewById(R.id.btnFinalizarEntrenamiento);
        if (btnFinalizar != null) {
            btnFinalizar.setVisibility(View.GONE);
        }

        configurarGridView(view);
    }

    private void configurarGridView(View view) {
        TextView tituloGv = view.findViewById(R.id.tvTituloGrid);
        if (tituloGv != null) {
            if (rutinaActual != null) {
                tituloGv.setText(String.format("Secciones de %s", rutinaActual.NombreRutina));
            } else {
                tituloGv.setText("Mis Secciones");
            }
        }
        
        if (getActivity() != null) {
            seccionRepository = new SeccionRepository(getActivity().getApplication());
        }
        
        adapter = new SeccionesAdapter(new ArrayList<>(), new SeccionesAdapter.OnSeccionClickListener() {
            @Override
            public void onAddClick() {
                mostrarPopUpAnadirSeccion();
            }

            @Override
            public void onSeccionClick(Seccion seccion) {
                Bundle bundle = new Bundle();
                bundle.putSerializable("seccion", seccion);
                Navigation.findNavController(requireView()).navigate(R.id.ejerciciosFragment, bundle);
            }

            @Override
            public void onOptionsClick(View view, Seccion seccion) {
                mostrarMenuOpciones(view, seccion);
            }
        });
        
        gvSecciones.setAdapter(adapter);
        cargarSeccionesDesdeDB();
    }

    private void mostrarMenuOpciones(View view, Seccion seccion) {
        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.pop_up_modificar_eliminar);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        }

        TextView tvNombre = dialog.findViewById(R.id.tvNombrePopUp);
        if (tvNombre != null) {
            tvNombre.setText(seccion.NombreSeccion);
        }

        View btnEliminar = dialog.findViewById(R.id.btnEliminarPopUp);
        if (btnEliminar != null) {
            btnEliminar.setOnClickListener(v -> {
                seccionRepository.eliminarSeccion(seccion);
                dialog.dismiss();
                new Handler(Looper.getMainLooper()).postDelayed(this::cargarSeccionesDesdeDB, 200);
            });
        }

        View btnModificar = dialog.findViewById(R.id.btnModificarPopUp);
        if (btnModificar != null) {
            btnModificar.setOnClickListener(v -> {
                dialog.dismiss();
                mostrarPopUpCrearSeccion(seccion, false);
            });
        }

        View btnCancelar = dialog.findViewById(R.id.btnCancelarPopUp);
        if (btnCancelar != null) {
            btnCancelar.setOnClickListener(v -> dialog.dismiss());
        }

        dialog.show();
    }

    private void cargarSeccionesDesdeDB() {
        if (rutinaActual != null && seccionRepository != null) {
            seccionRepository.obtenerSeccionesDeRutina(rutinaActual.IdRutina, secciones -> adapter.setSecciones(secciones));
        }
    }

    private void mostrarPopUpAnadirSeccion() {
        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.pop_up_dos_opciones);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        }

        TextView tvTitulo = dialog.findViewById(R.id.tvTituloPopUp);
        TextView tvOpcionIzq = dialog.findViewById(R.id.tvTextoIzquierda);
        TextView tvOpcionDer = dialog.findViewById(R.id.tvTextoDerecha);

        tvTitulo.setText("Añadir Sección");
        if (tvOpcionIzq != null) tvOpcionIzq.setText("Sección\nPrevia");
        if (tvOpcionDer != null) tvOpcionDer.setText("Nueva\nSección");

        dialog.findViewById(R.id.btnCancelar).setOnClickListener(v -> dialog.dismiss());
        
        dialog.findViewById(R.id.btnOpcionDerecha).setOnClickListener(v -> {
            dialog.dismiss();
            mostrarPopUpCrearSeccion(null, false);
        });

        dialog.findViewById(R.id.btnOpcionIzquierda).setOnClickListener(v -> {
            dialog.dismiss();
            mostrarPopUpSeccionesPrevias();
        });

        dialog.show();
    }

    private void mostrarPopUpSeccionesPrevias() {
        Dialog dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.pop_up_listado_generico);
        
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        }

        TextView tvTitulo = dialog.findViewById(R.id.tvTituloPopUpGenerico);
        tvTitulo.setText("Elegir sección previa");

        GridView gvPopup = dialog.findViewById(R.id.gvListadoGenerico);
        Button btnCancelar = dialog.findViewById(R.id.btnCancelarGenerico);

        btnCancelar.setOnClickListener(v -> {
            dialog.dismiss();
            mostrarPopUpAnadirSeccion();
        });

        seccionRepository.obtenerTodasLasSecciones(secciones -> {
             List<Seccion> filtradas = new ArrayList<>();
             for (Seccion s : secciones) {
                 if ("Personalizado".equals(s.TipoSeccion)) filtradas.add(s);
             }

             gvPopup.setAdapter(new BaseAdapter() {
                 @Override public int getCount() { return filtradas.size(); }
                 @Override public Object getItem(int i) { return filtradas.get(i); }
                 @Override public long getItemId(int i) { return i; }
                 @Override public View getView(int position, View convertView, ViewGroup parent) {
                     View row = convertView;
                     if (row == null) {
                         row = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_seccion_previa, parent, false);
                     }
                     Seccion s = filtradas.get(position);
                     TextView tvNombre = row.findViewById(R.id.tv_nombre_seccion_previa);
                     TextView tvRutina = row.findViewById(R.id.tv_nombre_rutina_previa);
                     View container = row.findViewById(R.id.container_item_previa);
                     
                     tvNombre.setText(s.NombreSeccion);
                     tvRutina.setText(s.nombreRutina != null ? "Rutina: " + s.nombreRutina : "Sistema");
                     
                     GradientDrawable shape = new GradientDrawable();
                     shape.setCornerRadius(15 * parent.getContext().getResources().getDisplayMetrics().density);
                     shape.setStroke(4, Color.BLACK);
                     shape.setColor(Color.WHITE);
                     container.setBackground(shape);
                     
                     row.setOnClickListener(v -> {
                         dialog.dismiss();
                         mostrarPopUpCrearSeccion(s, true);
                     });
                     return row;
                 }
             });
        });

        dialog.show();
    }

    private void mostrarPopUpCrearSeccion(Seccion seccionBase, boolean esClonacion) {
        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.secciones_rutinas_pop_up_add);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        }

        TextView tvTitulo = dialog.findViewById(R.id.tvTituloPopUp);
        EditText etNombre = dialog.findViewById(R.id.etNombreGenerico);
        Button btnCancelar = dialog.findViewById(R.id.btnCancelarGenerico);
        Button btnAceptar = dialog.findViewById(R.id.btnConfirmarGenerico);

        if (seccionBase == null) {
            tvTitulo.setText("Crear Sección");
            btnAceptar.setText("Crear");
        } else if (esClonacion) {
            tvTitulo.setText("Clonar Sección");
            etNombre.setText(seccionBase.NombreSeccion);
            btnAceptar.setText("Clonar");
        } else {
            tvTitulo.setText("Editar Sección");
            etNombre.setText(seccionBase.NombreSeccion);
            btnAceptar.setText("Guardar");
        }

        btnCancelar.setOnClickListener(v -> {
            dialog.dismiss();
            if (esClonacion) {
                mostrarPopUpSeccionesPrevias();
            } else if (seccionBase == null) {
                mostrarPopUpAnadirSeccion();
            }
        });

        btnAceptar.setOnClickListener(v -> {
            String nombre = etNombre.getText().toString().trim();
            if (!nombre.isEmpty()) {
                if (seccionBase == null) {
                    Seccion nueva = new Seccion();
                    nueva.NombreSeccion = nombre;
                    nueva.IdRutinaSeccion = rutinaActual.IdRutina;
                    seccionRepository.insertarSeccion(nueva);
                    dialog.dismiss();
                    new Handler(Looper.getMainLooper()).postDelayed(this::cargarSeccionesDesdeDB, 300);
                } else if (esClonacion) {
                    seccionRepository.clonarSeccionConNombre(seccionBase, rutinaActual.IdRutina, nombre, result -> cargarSeccionesDesdeDB());
                    dialog.dismiss();
                } else {
                    seccionBase.NombreSeccion = nombre;
                    seccionRepository.actualizarSeccion(seccionBase);
                    dialog.dismiss();
                    new Handler(Looper.getMainLooper()).postDelayed(this::cargarSeccionesDesdeDB, 300);
                }
            }
        });
        dialog.show();
    }
}

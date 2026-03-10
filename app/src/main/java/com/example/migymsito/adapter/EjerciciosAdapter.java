package com.example.migymsito.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.example.migymsito.R;
import com.example.migymsito.data.Ejercicio;

import java.util.List;

public class EjerciciosAdapter extends BaseAdapter {

    private List<Ejercicio> ejercicios;
    private OnEjercicioClickListener listener;

    public interface OnEjercicioClickListener {
        void onAddClick();
        void onEjercicioClick(Ejercicio ejercicio);
        void onOptionsClick(View view, Ejercicio ejercicio);
    }

    public EjerciciosAdapter(List<Ejercicio> ejercicios, OnEjercicioClickListener listener) {
        this.ejercicios = ejercicios;
        this.listener = listener;
    }

    public void setEjercicios(List<Ejercicio> ejercicios) {
        this.ejercicios = ejercicios;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return ejercicios != null ? ejercicios.size() + 1 : 1;
    }

    @Override
    public Object getItem(int position) {
        if (ejercicios != null && position < ejercicios.size()) {
            return ejercicios.get(position);
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.boton_agregar_gv, parent, false);
        }

        Button btnAdd = convertView.findViewById(R.id.btn_item_add);
        TextView txtNombre = convertView.findViewById(R.id.tv_nombre_item);
        TextView tvOpciones = convertView.findViewById(R.id.tv_opciones);

        if (ejercicios == null || position == ejercicios.size()) {
            btnAdd.setVisibility(View.VISIBLE);
            txtNombre.setVisibility(View.GONE);
            tvOpciones.setVisibility(View.GONE);

            convertView.setOnClickListener(v -> {
                if (listener != null) listener.onAddClick();
            });
        } else {
            Ejercicio ejercicio = ejercicios.get(position);
            btnAdd.setVisibility(View.GONE);
            txtNombre.setVisibility(View.VISIBLE);
            tvOpciones.setVisibility(View.VISIBLE);

            txtNombre.setText(ejercicio.NombreEjercicio);

            tvOpciones.setOnClickListener(v -> {
                if (listener != null) listener.onOptionsClick(v, ejercicio);
            });

            convertView.setOnClickListener(v -> {
                if (listener != null) listener.onEjercicioClick(ejercicio);
            });
        }

        return convertView;
    }
}
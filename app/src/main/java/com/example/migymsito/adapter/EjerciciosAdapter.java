package com.example.migymsito.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.migymsito.R;
import com.example.migymsito.data.Ejercicio;

import java.util.List;

public class EjerciciosAdapter extends BaseAdapter {

    private List<Ejercicio> ejercicios;
    private OnEjercicioClickListener listener;

    public interface OnEjercicioClickListener {
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
        return ejercicios != null ? ejercicios.size() : 0;
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
        if (convertView == null || convertView.findViewById(R.id.ivEjercicio) == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ejercicio, parent, false);
        }

        Ejercicio ejercicio = ejercicios.get(position);
        TextView txtNombre = convertView.findViewById(R.id.tvNombreEjercicio);
        ImageView ivImagen = convertView.findViewById(R.id.ivEjercicio);
        TextView tvOpciones = convertView.findViewById(R.id.tvOpcionesEjercicio);

        if (txtNombre != null) txtNombre.setText(ejercicio.NombreEjercicio);
        
        if (tvOpciones != null) {
            tvOpciones.setOnClickListener(v -> {
                if (listener != null) listener.onOptionsClick(v, ejercicio);
            });
        }

        convertView.setOnClickListener(v -> {
            if (listener != null) listener.onEjercicioClick(ejercicio);
        });

        return convertView;
    }
}
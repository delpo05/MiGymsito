package com.example.migymsito.adapter;

import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.migymsito.R;
import com.example.migymsito.data.Ejercicio;

import java.util.List;

public class EjerciciosAdapter extends BaseAdapter {

    private List<Ejercicio> ejercicios;
    private OnEjercicioClickListener listener;
    private int lastPosition = -1;

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
        this.lastPosition = -1;
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
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ejercicio, parent, false);
        }

        ImageView btnAdd = convertView.findViewById(R.id.btn_item_add);
        TextView txtNombre = convertView.findViewById(R.id.tv_nombre_item);
        TextView tvOpciones = convertView.findViewById(R.id.tv_opciones);
        ImageView ivImagen = convertView.findViewById(R.id.iv_item_imagen);
        View overlay = convertView.findViewById(R.id.view_overlay);
        View layoutPlaceholder = convertView.findViewById(R.id.layout_placeholder);

        if (ejercicios == null || position == ejercicios.size()) {
            btnAdd.setVisibility(View.VISIBLE);
            txtNombre.setVisibility(View.GONE);
            tvOpciones.setVisibility(View.GONE);
            ivImagen.setVisibility(View.GONE);
            if (overlay != null) overlay.setVisibility(View.GONE);
            if (layoutPlaceholder != null) layoutPlaceholder.setVisibility(View.VISIBLE);
            
            convertView.setOnClickListener(v -> {
                if (listener != null) listener.onAddClick();
            });
        } else {
            Ejercicio ejercicio = ejercicios.get(position);
            btnAdd.setVisibility(View.GONE);
            txtNombre.setVisibility(View.VISIBLE);
            tvOpciones.setVisibility(View.VISIBLE);
            txtNombre.setText(ejercicio.NombreEjercicio);

            if (ejercicio.ImagenEjercicio != null && !ejercicio.ImagenEjercicio.isEmpty()) {
                ivImagen.setVisibility(View.VISIBLE);
                if (layoutPlaceholder != null) layoutPlaceholder.setVisibility(View.GONE);
                if (overlay != null) overlay.setVisibility(View.VISIBLE);

                Glide.with(convertView.getContext())
                        .load(Uri.parse(ejercicio.ImagenEjercicio))
                        .placeholder(R.drawable.cargar_imagen_default)
                        .error(R.drawable.cargar_imagen_default)
                        .centerCrop()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(ivImagen);
            } else {
                ivImagen.setVisibility(View.GONE);
                if (layoutPlaceholder != null) layoutPlaceholder.setVisibility(View.VISIBLE);
                if (overlay != null) overlay.setVisibility(View.GONE);
                Glide.with(convertView.getContext()).clear(ivImagen);
            }

            tvOpciones.setOnClickListener(v -> {
                if (listener != null) listener.onOptionsClick(v, ejercicio);
            });

            convertView.setOnClickListener(v -> {
                if (listener != null) listener.onEjercicioClick(ejercicio);
            });
        }

        // Animación de entrada escalonada
        if (position > lastPosition) {
            android.view.animation.Animation animation = android.view.animation.AnimationUtils.loadAnimation(parent.getContext(), R.anim.item_entrance);
            animation.setStartOffset(position * 50L);
            convertView.startAnimation(animation);
            lastPosition = position;
        }

        return convertView;
    }
}

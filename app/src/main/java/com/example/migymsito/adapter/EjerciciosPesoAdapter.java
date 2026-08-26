package com.example.migymsito.adapter;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.migymsito.R;
import com.example.migymsito.data.EjercicioPeso;

import java.util.List;

public class EjerciciosPesoAdapter extends RecyclerView.Adapter<EjerciciosPesoAdapter.EjercicioViewHolder> {

    private List<EjercicioPeso> ejercicios;
    private final OnEjercicioClickListener listener;
    private int lastPosition = -1;

    public interface OnEjercicioClickListener {
        void onEjercicioClick(EjercicioPeso ejercicio);
        void onOptionsClick(View view, EjercicioPeso ejercicio);
    }

    public EjerciciosPesoAdapter(List<EjercicioPeso> ejercicios, OnEjercicioClickListener listener) {
        this.ejercicios = ejercicios;
        this.listener = listener;
    }

    public void setEjercicios(List<EjercicioPeso> ejercicios) {
        this.ejercicios = ejercicios;
        this.lastPosition = -1;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public EjercicioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ejercicio, parent, false);
        return new EjercicioViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EjercicioViewHolder holder, int position) {
        EjercicioPeso ejercicio = ejercicios.get(position);
        holder.txtNombre.setText(ejercicio.NombreEjercicio);

        if (ejercicio.ImagenEjercicio != null && !ejercicio.ImagenEjercicio.isEmpty()) {
            holder.ivImagen.setVisibility(View.VISIBLE);
            if (holder.layoutPlaceholder != null) holder.layoutPlaceholder.setVisibility(View.GONE);
            if (holder.overlay != null) holder.overlay.setVisibility(View.VISIBLE);

            Glide.with(holder.itemView.getContext())
                    .load(Uri.parse(ejercicio.ImagenEjercicio))
                    .placeholder(R.drawable.cargar_imagen_default)
                    .error(R.drawable.cargar_imagen_default)
                    .centerCrop()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(holder.ivImagen);
        } else {
            holder.ivImagen.setVisibility(View.GONE);
            if (holder.layoutPlaceholder != null) holder.layoutPlaceholder.setVisibility(View.VISIBLE);
            if (holder.overlay != null) holder.overlay.setVisibility(View.GONE);
            Glide.with(holder.itemView.getContext()).clear(holder.ivImagen);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onEjercicioClick(ejercicio);
        });

        holder.tvOpciones.setOnClickListener(v -> {
            if (listener != null) listener.onOptionsClick(v, ejercicio);
        });

        setAnimation(holder.itemView, position);
    }

    @Override
    public int getItemCount() {
        return ejercicios != null ? ejercicios.size() : 0;
    }

    private void setAnimation(View viewToAnimate, int position) {
        if (position > lastPosition) {
            Animation animation = AnimationUtils.loadAnimation(viewToAnimate.getContext(), R.anim.item_entrance);
            animation.setStartOffset(position * 50L);
            viewToAnimate.startAnimation(animation);
            lastPosition = position;
        }
    }

    public static class EjercicioViewHolder extends RecyclerView.ViewHolder {
        TextView txtNombre;
        TextView tvOpciones;
        ImageView ivImagen;
        View overlay;
        View layoutPlaceholder;

        public EjercicioViewHolder(@NonNull View itemView) {
            super(itemView);
            txtNombre = itemView.findViewById(R.id.tv_nombre_item);
            tvOpciones = itemView.findViewById(R.id.tv_opciones);
            ivImagen = itemView.findViewById(R.id.iv_item_imagen);
            overlay = itemView.findViewById(R.id.view_overlay);
            layoutPlaceholder = itemView.findViewById(R.id.layout_placeholder);
        }
    }
}

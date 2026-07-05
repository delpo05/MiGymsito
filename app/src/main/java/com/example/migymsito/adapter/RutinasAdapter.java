package com.example.migymsito.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.migymsito.R;
import com.example.migymsito.data.Rutina;

import java.util.List;

public class RutinasAdapter extends RecyclerView.Adapter<RutinasAdapter.RutinaViewHolder> {

    private List<Rutina> rutinas;
    private final OnRutinaClickListener listener;
    private int lastPosition = -1;

    public interface OnRutinaClickListener {
        void onRutinaClick(Rutina rutina);
        void onOptionsClick(View view, Rutina rutina);
    }

    public RutinasAdapter(List<Rutina> rutinas, OnRutinaClickListener listener) {
        this.rutinas = rutinas;
        this.listener = listener;
    }

    public void setRutinas(List<Rutina> rutinas) {
        this.rutinas = rutinas;
        this.lastPosition = -1;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RutinaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_rutina, parent, false);
        return new RutinaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RutinaViewHolder holder, int position) {
        Rutina rutina = rutinas.get(position);
        holder.txtNombre.setText(rutina.NombreRutina);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onRutinaClick(rutina);
        });

        holder.tvOpciones.setOnClickListener(v -> {
            if (listener != null) listener.onOptionsClick(v, rutina);
        });

        setAnimation(holder.itemView, position);
    }

    @Override
    public int getItemCount() {
        return rutinas != null ? rutinas.size() : 0;
    }

    private void setAnimation(View viewToAnimate, int position) {
        if (position > lastPosition) {
            Animation animation = AnimationUtils.loadAnimation(viewToAnimate.getContext(), R.anim.item_entrance);
            animation.setStartOffset(position * 50L);
            viewToAnimate.startAnimation(animation);
            lastPosition = position;
        }
    }

    public static class RutinaViewHolder extends RecyclerView.ViewHolder {
        TextView txtNombre;
        TextView tvOpciones;

        public RutinaViewHolder(@NonNull View itemView) {
            super(itemView);
            txtNombre = itemView.findViewById(R.id.tv_nombre_item);
            tvOpciones = itemView.findViewById(R.id.tv_opciones);
        }
    }
}

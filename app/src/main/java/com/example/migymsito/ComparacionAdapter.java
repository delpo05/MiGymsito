package com.example.migymsito;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Locale;

public class ComparacionAdapter extends RecyclerView.Adapter<ComparacionAdapter.ViewHolder> {

    private final List<ComparacionFila> items;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(ComparacionFila item);
    }

    public ComparacionAdapter(List<ComparacionFila> items, OnItemClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comparacion_entrenamiento, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ComparacionFila item = items.get(position);
        holder.tvNombre.setText(item.getNombreEjercicio());
        
        // Volumen A (Antiguo) - Mostrado como Puntos
        holder.tvVolumenA.setText(String.format(Locale.getDefault(), "%.0f pts", item.getVolumenA()));
        
        // Volumen B (Reciente) - Mostrado como Puntos
        holder.tvVolumenB.setText(String.format(Locale.getDefault(), "%.0f pts", item.getVolumenB()));
        
        double dif = item.getDiferencia();
        double porc = item.getPorcentajeDif();
        
        String evolucion = String.format(Locale.getDefault(), "%s%.0f (%s%.1f%%)", 
                                        dif >= 0 ? "+" : "", dif, dif >= 0 ? "+" : "", porc);
        holder.tvEvolucionTexto.setText(evolucion);

        if (dif > 0) {
            holder.ivIndicador.setImageResource(R.drawable.ic_arrow_up);
            holder.ivIndicador.setColorFilter(ContextCompat.getColor(holder.itemView.getContext(), android.R.color.holo_green_light));
            holder.tvEvolucionTexto.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), android.R.color.holo_green_light));
        } else if (dif < 0) {
            holder.ivIndicador.setImageResource(R.drawable.ic_arrow_down);
            holder.ivIndicador.setColorFilter(ContextCompat.getColor(holder.itemView.getContext(), android.R.color.holo_red_light));
            holder.tvEvolucionTexto.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), android.R.color.holo_red_light));
        } else {
            holder.ivIndicador.setImageDrawable(null);
            holder.tvEvolucionTexto.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.blanco));
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombre, tvVolumenA, tvVolumenB, tvEvolucionTexto;
        ImageView ivIndicador;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tv_nombre_ejercicio);
            tvVolumenA = itemView.findViewById(R.id.tv_volumen_actual);
            tvVolumenB = itemView.findViewById(R.id.tv_volumen_anterior);
            tvEvolucionTexto = itemView.findViewById(R.id.tv_evolucion_texto);
            ivIndicador = itemView.findViewById(R.id.iv_indicador);
        }
    }

    public static class ComparacionFila {
        private String nombreEjercicio;
        private int idSeccionXejercicio;
        private double volumenB; // Reciente
        private double volumenA; // Antiguo

        public ComparacionFila(String nombreEjercicio, int idSeccionXejercicio, double volumenB, double volumenA) {
            this.nombreEjercicio = nombreEjercicio;
            this.idSeccionXejercicio = idSeccionXejercicio;
            this.volumenB = volumenB;
            this.volumenA = volumenA;
        }

        public String getNombreEjercicio() { return nombreEjercicio; }
        public int getIdSeccionXejercicio() { return idSeccionXejercicio; }
        public double getVolumenB() { return volumenB; }
        public double getVolumenA() { return volumenA; }
        public double getDiferencia() { return volumenB - volumenA; }
        public double getPorcentajeDif() {
            if (volumenA == 0) return volumenB > 0 ? 100 : 0;
            return ((volumenB - volumenA) / volumenA) * 100;
        }
    }
}

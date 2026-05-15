package com.example.migymsito.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.migymsito.R;
import com.example.migymsito.data.Historial;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HistorialPesoAdapter extends RecyclerView.Adapter<HistorialPesoAdapter.ViewHolder> {

    private final List<Historial> listaHistorial;
    private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    private final OnItemDeleteListener deleteListener;

    public interface OnItemDeleteListener {
        void onDelete(Historial historial);
    }

    public HistorialPesoAdapter(List<Historial> listaHistorial, OnItemDeleteListener deleteListener) {
        this.listaHistorial = listaHistorial;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_historial_peso, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Historial actual = listaHistorial.get(position);
        
        holder.tvPesoActual.setText(String.format(Locale.getDefault(), "%.1f", actual.PesoHistorial));
        holder.tvFecha.setText(sdf.format(new Date(actual.FechaHistorial)));

        if (position + 1 < listaHistorial.size()) {
            Historial pasado = listaHistorial.get(position + 1);
            double diferencia = actual.PesoHistorial - pasado.PesoHistorial;
            
            holder.tvPesoPasado.setText(String.format(Locale.getDefault(), "%.1f", pasado.PesoHistorial));
            
            String diffStr = (diferencia >= 0 ? "+" : "") + String.format(Locale.getDefault(), "%.1f", diferencia);
            holder.tvDiferencia.setText(diffStr);
        } else {
            holder.tvPesoPasado.setText("-");
            holder.tvDiferencia.setText("-");
        }

        // Color de diferencia siempre blanco como pidió el usuario
        holder.tvDiferencia.setTextColor(holder.itemView.getContext().getColor(R.color.blanco));

        holder.btnEliminar.setOnClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onDelete(actual);
            }
        });
    }

    @Override
    public int getItemCount() {
        return listaHistorial.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvPesoActual, tvPesoPasado, tvDiferencia, tvFecha;
        ImageButton btnEliminar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPesoActual = itemView.findViewById(R.id.tvPesoActual);
            tvPesoPasado = itemView.findViewById(R.id.tvPesoPasado);
            tvDiferencia = itemView.findViewById(R.id.tvDiferencia);
            tvFecha = itemView.findViewById(R.id.tvFecha);
            btnEliminar = itemView.findViewById(R.id.btnEliminarHistorial);
        }
    }
}

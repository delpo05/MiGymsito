package com.example.migymsito.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.migymsito.R;
import com.example.migymsito.data.RegistroCardio;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CardioHistorialAdapter extends RecyclerView.Adapter<CardioHistorialAdapter.ViewHolder> {

    private List<RegistroCardio> registros;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    public CardioHistorialAdapter(List<RegistroCardio> registros) {
        this.registros = registros;
    }

    public void setRegistros(List<RegistroCardio> registros) {
        this.registros = registros;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_historial_cardio, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RegistroCardio reg = registros.get(position);
        
        holder.tvFecha.setText(dateFormat.format(new Date(reg.FechaRegistro)));
        holder.tvDuracion.setText(formatDuration(reg.DuracionSegundos));
        
        StringBuilder resumen = new StringBuilder();
        if (reg.Distancia != null) resumen.append(reg.Distancia).append(" km");
        if (reg.RitmoCardiacoPromedio != null) {
            if (resumen.length() > 0) resumen.append(" • ");
            resumen.append(reg.RitmoCardiacoPromedio).append(" bpm");
        }
        if (reg.CaloriasQuemadas != null) {
            if (resumen.length() > 0) resumen.append(" • ");
            resumen.append(reg.CaloriasQuemadas).append(" kcal");
        }
        
        holder.tvResumen.setText(resumen.toString());
        
        if (reg.Notas != null && !reg.Notas.isEmpty()) {
            holder.tvNotas.setText(reg.Notas);
            holder.tvNotas.setVisibility(View.VISIBLE);
        } else {
            holder.tvNotas.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return registros != null ? registros.size() : 0;
    }

    private String formatDuration(long totalSeconds) {
        long hrs = totalSeconds / 3600;
        long mins = (totalSeconds % 3600) / 60;
        long secs = totalSeconds % 60;
        if (hrs > 0) {
            return String.format(Locale.getDefault(), "%02d:%02d:%02d", hrs, mins, secs);
        } else {
            return String.format(Locale.getDefault(), "%02d:%02d", mins, secs);
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvFecha, tvDuracion, tvResumen, tvNotas;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFecha = itemView.findViewById(R.id.tvHistorialCardioFecha);
            tvDuracion = itemView.findViewById(R.id.tvHistorialCardioDuracion);
            tvResumen = itemView.findViewById(R.id.tvHistorialCardioResumen);
            tvNotas = itemView.findViewById(R.id.tvHistorialCardioNotas);
        }
    }
}

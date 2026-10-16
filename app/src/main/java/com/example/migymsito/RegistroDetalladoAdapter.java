package com.example.migymsito;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.migymsito.data.RegistroDetallado;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RegistroDetalladoAdapter extends RecyclerView.Adapter<RegistroDetalladoAdapter.ViewHolder> {

    private List<RegistroDetallado> registros;
    private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

    public RegistroDetalladoAdapter(List<RegistroDetallado> registros) {
        this.registros = registros;
    }

    public void setRegistros(List<RegistroDetallado> registros) {
        this.registros = registros;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_registro_detallado, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RegistroDetallado reg = registros.get(position);
        holder.tvFecha.setText(sdf.format(new Date(reg.fecha)));
        holder.tvNombres.setText(String.format("%s > %s > %s", reg.nombreRutina, reg.nombreSeccion, reg.nombreEjercicio));

        if ("CARDIO".equalsIgnoreCase(reg.categoriaEjercicio)) {
            long duracion = reg.duracionSegundos != null ? reg.duracionSegundos : 0;
            holder.tvSerieReps.setText(String.format("Duración: %s", formatDuration(duracion)));

            StringBuilder resumen = new StringBuilder();
            if (reg.distancia != null && reg.distancia > 0) {
                resumen.append(String.format(Locale.getDefault(), "%.2f %s", reg.distancia, reg.unidadDistancia != null ? reg.unidadDistancia : "km"));
            }
            if (reg.caloriasQuemadas != null && reg.caloriasQuemadas > 0) {
                if (resumen.length() > 0) resumen.append(" • ");
                resumen.append(reg.caloriasQuemadas).append(" kcal");
            }
            if (reg.ritmoCardiacoPromedio != null && reg.ritmoCardiacoPromedio > 0) {
                if (resumen.length() > 0) resumen.append(" • ");
                resumen.append(reg.ritmoCardiacoPromedio).append(" bpm");
            }
            holder.tvPeso.setText(resumen.length() > 0 ? resumen.toString() : "Cardio");

            StringBuilder extras = new StringBuilder();
            if (reg.velocidadPromedio != null && reg.velocidadPromedio > 0) {
                extras.append(String.format(Locale.getDefault(), "Vel. Prom: %.1f km/h", reg.velocidadPromedio));
            }
            if (reg.inclinacion != null && reg.inclinacion > 0) {
                if (extras.length() > 0) extras.append(" | ");
                extras.append(String.format(Locale.getDefault(), "Inclinación: %.1f%%", reg.inclinacion));
            }
            if (reg.nivelResistencia != null && reg.nivelResistencia > 0) {
                if (extras.length() > 0) extras.append(" | ");
                extras.append(String.format(Locale.getDefault(), "Resistencia: %d", reg.nivelResistencia));
            }
            if (reg.notas != null && !reg.notas.trim().isEmpty()) {
                if (extras.length() > 0) extras.append("\n");
                extras.append("Notas: ").append(reg.notas);
            }

            if (extras.length() > 0) {
                holder.tvBarra.setVisibility(View.VISIBLE);
                holder.tvBarra.setText(extras.toString());
            } else {
                holder.tvBarra.setVisibility(View.GONE);
            }
        } else {
            holder.tvSerieReps.setText(String.format(Locale.getDefault(), "Serie: %d | Reps: %d", reg.numSerie, reg.repeticiones));

            String pesoStr = reg.esPesoCorporal ? "Peso Corp." : String.format(Locale.getDefault(), "%.1f %s", reg.peso, (reg.unidadPeso != null ? reg.unidadPeso : "kg"));
            holder.tvPeso.setText(String.format("Peso: %s", pesoStr));

            if (reg.tipoBarra != null && !reg.tipoBarra.isEmpty() && reg.pesoBarra != null) {
                holder.tvBarra.setVisibility(View.VISIBLE);
                holder.tvBarra.setText(String.format(Locale.getDefault(), "Barra: %s (%.1f kg)", reg.tipoBarra, reg.pesoBarra));
            } else {
                holder.tvBarra.setVisibility(View.GONE);
            }
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
        TextView tvFecha, tvNombres, tvSerieReps, tvPeso, tvBarra;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFecha = itemView.findViewById(R.id.tvFecha);
            tvNombres = itemView.findViewById(R.id.tvNombres);
            tvSerieReps = itemView.findViewById(R.id.tvSerieReps);
            tvPeso = itemView.findViewById(R.id.tvPeso);
            tvBarra = itemView.findViewById(R.id.tvBarra);
        }
    }
}

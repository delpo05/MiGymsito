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
        holder.tvSerieReps.setText(String.format("Serie: %d | Reps: %d", reg.numSerie, reg.repeticiones));
        
        String pesoStr = reg.esPesoCorporal ? "Peso Corp." : String.format("%.1f %s", reg.peso, (reg.unidadPeso != null ? reg.unidadPeso : "kg"));
        holder.tvPeso.setText(String.format("Peso: %s", pesoStr));

        if (reg.tipoBarra != null && !reg.tipoBarra.isEmpty() && reg.pesoBarra != null) {
            holder.tvBarra.setVisibility(View.VISIBLE);
            holder.tvBarra.setText(String.format("Barra: %s (%.1f kg)", reg.tipoBarra, reg.pesoBarra));
        } else {
            holder.tvBarra.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return registros != null ? registros.size() : 0;
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

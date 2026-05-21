package com.example.migymsito.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.migymsito.R;
import com.example.migymsito.data.Registro;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HistorialEjercicioAdapter extends RecyclerView.Adapter<HistorialEjercicioAdapter.HistorialViewHolder> {

    private final List<Registro> listaRegistros;
    private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy HH:mm", Locale.getDefault());
    private final boolean esPesoCorporal;

    public HistorialEjercicioAdapter(List<Registro> listaRegistros, boolean esPesoCorporal) {
        this.listaRegistros = listaRegistros;
        this.esPesoCorporal = esPesoCorporal;
    }

    @NonNull
    @Override
    public HistorialViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_historial_dialogo, parent, false);
        return new HistorialViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistorialViewHolder holder, int position) {
        Registro registro = listaRegistros.get(position);
        
        holder.tvFecha.setText(sdf.format(new Date(registro.FechaRegistro)));
        holder.tvSerie.setText(String.valueOf(registro.NumSeriesRegistro));
        holder.tvRepeticiones.setText(String.valueOf(registro.Repeticiones));
        
        if (esPesoCorporal) {
            if (registro.PesoRegistro == 0) {
                holder.tvPeso.setText("PC");
            } else {
                holder.tvPeso.setText("+" + registro.PesoRegistro);
            }
        } else {
            holder.tvPeso.setText(registro.PesoRegistro + " kg");
        }
    }

    @Override
    public int getItemCount() {
        return listaRegistros.size();
    }

    static class HistorialViewHolder extends RecyclerView.ViewHolder {
        TextView tvFecha, tvSerie, tvRepeticiones, tvPeso;

        public HistorialViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFecha = itemView.findViewById(R.id.tvFechaHistorial);
            tvSerie = itemView.findViewById(R.id.tvSerieHistorial);
            tvRepeticiones = itemView.findViewById(R.id.tvRepeticionesHistorial);
            tvPeso = itemView.findViewById(R.id.tvPesoHistorial);
        }
    }
}

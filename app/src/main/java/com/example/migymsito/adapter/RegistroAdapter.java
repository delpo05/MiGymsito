package com.example.migymsito.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.migymsito.R;
import com.example.migymsito.data.Registro;

import java.util.List;

public class RegistroAdapter extends RecyclerView.Adapter<RegistroAdapter.RegistroViewHolder> {

    private List<Registro> listaRegistros;

    public RegistroAdapter(List<Registro> listaRegistros) {
        this.listaRegistros = listaRegistros;
    }

    @NonNull
    @Override
    public RegistroViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_historial_cargar_registro, parent, false);
        return new RegistroViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RegistroViewHolder holder, int position) {
        Registro registro = listaRegistros.get(position);
        holder.tvSerie.setText(String.valueOf(registro.NumSeriesRegistro));
        holder.tvRepeticiones.setText(String.valueOf(registro.RepeticionesRegistro));
        holder.tvPeso.setText(registro.PesoRegistro + " kg");
    }

    @Override
    public int getItemCount() {
        return listaRegistros.size();
    }

    public void setRegistros(List<Registro> registros) {
        this.listaRegistros = registros;
        notifyDataSetChanged();
    }

    static class RegistroViewHolder extends RecyclerView.ViewHolder {
        TextView tvSerie, tvRepeticiones, tvPeso;

        public RegistroViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSerie = itemView.findViewById(R.id.tvSerieItem);
            tvRepeticiones = itemView.findViewById(R.id.tvRepeticionesItem);
            tvPeso = itemView.findViewById(R.id.tvPesoItem);
        }
    }
}

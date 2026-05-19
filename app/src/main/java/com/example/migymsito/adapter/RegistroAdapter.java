package com.example.migymsito.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.migymsito.R;
import com.example.migymsito.data.Registro;

import java.util.List;

public class RegistroAdapter extends RecyclerView.Adapter<RegistroAdapter.RegistroViewHolder> {

    private List<Registro> listaRegistros;
    private boolean esPesoCorporal;
    private OnRegistroEditListener editListener;

    public interface OnRegistroEditListener {
        void onEditClick(Registro registro, int position);
    }

    public RegistroAdapter(List<Registro> listaRegistros, boolean esPesoCorporal) {
        this.listaRegistros = listaRegistros;
        this.esPesoCorporal = esPesoCorporal;
    }

    public void setOnRegistroEditListener(OnRegistroEditListener listener) {
        this.editListener = listener;
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
        holder.tvRepeticiones.setText(String.valueOf(registro.Repeticiones));
        
        if (esPesoCorporal) {
            if (registro.PesoRegistro == 0) {
                holder.tvPeso.setText("Peso Corp.");
            } else {
                holder.tvPeso.setText("PC + " + registro.PesoRegistro + " kg");
            }
        } else {
            holder.tvPeso.setText(registro.PesoRegistro + " kg");
        }

        holder.btnEdit.setOnClickListener(v -> {
            if (editListener != null) {
                editListener.onEditClick(registro, holder.getAdapterPosition());
            }
        });
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
        ImageButton btnEdit;

        public RegistroViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSerie = itemView.findViewById(R.id.tvSerieItem);
            tvRepeticiones = itemView.findViewById(R.id.tvRepeticionesItem);
            tvPeso = itemView.findViewById(R.id.tvPesoItem);
            btnEdit = itemView.findViewById(R.id.btnEditItem);
        }
    }
}

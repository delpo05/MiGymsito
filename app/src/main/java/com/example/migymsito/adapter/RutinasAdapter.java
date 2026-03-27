package com.example.migymsito.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.migymsito.R;
import com.example.migymsito.data.Rutina;

import java.util.List;

public class RutinasAdapter extends BaseAdapter {

    private List<Rutina> rutinas;
    private OnRutinaClickListener listener;

    public interface OnRutinaClickListener {
        void onAddClick();
        void onRutinaClick(Rutina rutina);
        void onOptionsClick(View view, Rutina rutina);
    }

    public RutinasAdapter(List<Rutina> rutinas, OnRutinaClickListener listener) {
        this.rutinas = rutinas;
        this.listener = listener;
    }

    public void setRutinas(List<Rutina> rutinas) {
        this.rutinas = rutinas;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return rutinas != null ? rutinas.size() + 1 : 1;
    }

    @Override
    public Object getItem(int position) {
        if (rutinas != null && position < rutinas.size()) {
            return rutinas.get(position);
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_gv, parent, false);
        }

        View container = convertView.findViewById(R.id.container_item);
        ImageView btnAdd = convertView.findViewById(R.id.btn_item_add);
        TextView txtNombre = convertView.findViewById(R.id.tv_nombre_item);
        TextView tvOpciones = convertView.findViewById(R.id.tv_opciones);
        View ivImagen = convertView.findViewById(R.id.iv_item_imagen);

        if (ivImagen != null) ivImagen.setVisibility(View.GONE);

        // --- RESETEAR DISEÑO PARA RUTINAS (Centrado perfecto) ---
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) txtNombre.getLayoutParams();
        params.removeRule(RelativeLayout.ALIGN_PARENT_TOP);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        params.setMargins(0, 0, 0, 0); // Quitar márgenes de Ejercicios
        txtNombre.setLayoutParams(params);
        txtNombre.setTextSize(16f);
        txtNombre.setShadowLayer(0, 0, 0, 0);

        if (container != null) {
            container.setBackgroundResource(R.drawable.card_border_white);
        }

        if (rutinas == null || position == rutinas.size()) {
            btnAdd.setVisibility(View.VISIBLE);
            txtNombre.setVisibility(View.GONE);
            tvOpciones.setVisibility(View.GONE);
            convertView.setOnClickListener(v -> { if (listener != null) listener.onAddClick(); });
        } else {
            Rutina rutina = rutinas.get(position);
            btnAdd.setVisibility(View.GONE);
            txtNombre.setVisibility(View.VISIBLE);
            tvOpciones.setVisibility(View.VISIBLE);
            txtNombre.setText(rutina.NombreRutina);

            tvOpciones.setOnClickListener(v -> { if (listener != null) listener.onOptionsClick(v, rutina); });
            convertView.setOnClickListener(v -> { if (listener != null) listener.onRutinaClick(rutina); });
        }

        return convertView;
    }
}

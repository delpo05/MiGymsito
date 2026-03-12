package com.example.migymsito.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.migymsito.R;
import com.example.migymsito.data.Seccion;

import java.util.List;

public class SeccionesAdapter extends BaseAdapter {

    private List<Seccion> secciones;
    private OnSeccionClickListener listener;

    public interface OnSeccionClickListener {
        void onAddClick();
        void onSeccionClick(Seccion seccion);
        void onOptionsClick(View view, Seccion seccion);
    }

    public SeccionesAdapter(List<Seccion> secciones, OnSeccionClickListener listener) {
        this.secciones = secciones;
        this.listener = listener;
    }

    public void setSecciones(List<Seccion> secciones) {
        this.secciones = secciones;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return secciones != null ? secciones.size() + 1 : 1;
    }

    @Override
    public Object getItem(int position) {
        if (secciones != null && position < secciones.size()) {
            return secciones.get(position);
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

        ImageView btnAdd = convertView.findViewById(R.id.btn_item_add);
        TextView txtNombre = convertView.findViewById(R.id.tv_nombre_item);
        TextView tvOpciones = convertView.findViewById(R.id.tv_opciones);
        View ivImagen = convertView.findViewById(R.id.iv_item_imagen);

        // Ocultar la imagen y centrar el texto para Secciones
        if (ivImagen != null) ivImagen.setVisibility(View.GONE);

        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) txtNombre.getLayoutParams();
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        txtNombre.setLayoutParams(params);

        if (secciones == null || position == secciones.size()) {
            btnAdd.setVisibility(View.VISIBLE);
            txtNombre.setVisibility(View.GONE);
            tvOpciones.setVisibility(View.GONE);

            convertView.setOnClickListener(v -> {
                if (listener != null) listener.onAddClick();
            });
        } else {
            Seccion seccion = secciones.get(position);
            btnAdd.setVisibility(View.GONE);
            txtNombre.setVisibility(View.VISIBLE);
            tvOpciones.setVisibility(View.VISIBLE);

            txtNombre.setText(seccion.NombreSeccion);

            tvOpciones.setOnClickListener(v -> {
                if (listener != null) listener.onOptionsClick(v, seccion);
            });

            convertView.setOnClickListener(v -> {
                if (listener != null) listener.onSeccionClick(seccion);
            });
        }

        return convertView;
    }
}
package com.example.migymsito.adapter;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.migymsito.R;
import com.example.migymsito.data.Ejercicio;

import java.util.List;

public class EjerciciosAdapter extends BaseAdapter {

    private List<Ejercicio> ejercicios;
    private OnEjercicioClickListener listener;

    public interface OnEjercicioClickListener {
        void onAddClick();
        void onEjercicioClick(Ejercicio ejercicio);
        void onOptionsClick(View view, Ejercicio ejercicio);
    }

    public EjerciciosAdapter(List<Ejercicio> ejercicios, OnEjercicioClickListener listener) {
        this.ejercicios = ejercicios;
        this.listener = listener;
    }

    public void setEjercicios(List<Ejercicio> ejercicios) {
        this.ejercicios = ejercicios;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return ejercicios != null ? ejercicios.size() + 1 : 1;
    }

    @Override
    public Object getItem(int position) {
        if (ejercicios != null && position < ejercicios.size()) {
            return ejercicios.get(position);
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    private int dpToPx(int dp, View v) {
        return (int) (dp * v.getContext().getResources().getDisplayMetrics().density);
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
        ImageView ivImagen = convertView.findViewById(R.id.iv_item_imagen);
        View overlay = convertView.findViewById(R.id.view_overlay);

        // --- Ajuste de tamaño para Ejercicios ---
        if (container != null) {
            ViewGroup.LayoutParams layoutParams = container.getLayoutParams();
            layoutParams.height = dpToPx(220, convertView);
            container.setLayoutParams(layoutParams);
            container.setElevation(0);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                container.setOutlineAmbientShadowColor(Color.BLACK);
                container.setOutlineSpotShadowColor(Color.BLACK);
            }
        }

        // RESETEAR DISEÑO POR DEFECTO
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) txtNombre.getLayoutParams();
        params.removeRule(RelativeLayout.ALIGN_PARENT_TOP);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        params.addRule(RelativeLayout.CENTER_HORIZONTAL);
        params.setMargins(dpToPx(10, convertView), 0, dpToPx(10, convertView), 0);
        txtNombre.setTextSize(16f);
        txtNombre.setShadowLayer(0, 0, 0, 0);

        if (ejercicios == null || position == ejercicios.size()) {
            btnAdd.setVisibility(View.VISIBLE);
            txtNombre.setVisibility(View.GONE);
            tvOpciones.setVisibility(View.GONE);
            ivImagen.setVisibility(View.GONE);
            if (overlay != null) overlay.setVisibility(View.GONE);
            if (container != null) container.setBackgroundResource(R.drawable.card_border_white);

            convertView.setOnClickListener(v -> {
                if (listener != null) listener.onAddClick();
            });
        } else {
            Ejercicio ejercicio = ejercicios.get(position);
            btnAdd.setVisibility(View.GONE);
            txtNombre.setVisibility(View.VISIBLE);
            tvOpciones.setVisibility(View.VISIBLE);
            ivImagen.setVisibility(View.VISIBLE);

            txtNombre.setText(ejercicio.NombreEjercicio);

            if (ejercicio.ImagenEjercicio != null && !ejercicio.ImagenEjercicio.isEmpty()) {
                ivImagen.setImageURI(Uri.parse(ejercicio.ImagenEjercicio));
                ivImagen.setScaleType(ImageView.ScaleType.CENTER_CROP);
                
                // --- DISEÑO PARA EJERCICIOS CON IMAGEN ---
                params.removeRule(RelativeLayout.CENTER_IN_PARENT);
                params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                params.topMargin = dpToPx(14, convertView);
                params.leftMargin = dpToPx(45, convertView);
                params.rightMargin = dpToPx(45, convertView);
                
                txtNombre.setTextSize(19f);
                txtNombre.setShadowLayer(4, 2, 2, Color.parseColor("#CC000000"));
                
                if (overlay != null) overlay.setVisibility(View.VISIBLE);
                if (container != null) container.setBackgroundResource(R.drawable.card_border_white);
            } else {
                // --- RESTABLECIDO: DISEÑO PARA EJERCICIOS SIN IMAGEN ---
                ivImagen.setImageResource(android.R.color.transparent);
                if (overlay != null) overlay.setVisibility(View.GONE);
                if (container != null) container.setBackgroundResource(R.drawable.card_border_white);
                
                // Texto en el centro para los que no tienen foto
                params.addRule(RelativeLayout.CENTER_IN_PARENT);
                txtNombre.setTextSize(16f);
                txtNombre.setShadowLayer(0, 0, 0, 0);
            }
            txtNombre.setLayoutParams(params);

            tvOpciones.setOnClickListener(v -> {
                if (listener != null) listener.onOptionsClick(v, ejercicio);
            });

            convertView.setOnClickListener(v -> {
                if (listener != null) listener.onEjercicioClick(ejercicio);
            });
        }

        return convertView;
    }
}

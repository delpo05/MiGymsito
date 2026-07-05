package com.example.migymsito.adapter;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
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
    private boolean mostrarBotonAdd = true;
    private boolean isModoPopup = false;
    private int lastPosition = -1;

    public interface OnSeccionClickListener {
        void onAddClick();
        void onSeccionClick(Seccion seccion);
        void onOptionsClick(View view, Seccion seccion);
    }

    public SeccionesAdapter(List<Seccion> secciones, OnSeccionClickListener listener) {
        this.secciones = secciones;
        this.listener = listener;
    }

    public void setMostrarBotonAdd(boolean mostrar) {
        this.mostrarBotonAdd = mostrar;
    }

    public void setModoPopup(boolean modoPopup) {
        this.isModoPopup = modoPopup;
    }

    public void setSecciones(List<Seccion> secciones) {
        this.secciones = secciones;
        this.lastPosition = -1;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        if (mostrarBotonAdd) {
            return secciones != null ? secciones.size() + 1 : 1;
        } else {
            return secciones != null ? secciones.size() : 0;
        }
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
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_seccion, parent, false);
        }

        View container = convertView.findViewById(R.id.container_item);
        ImageView btnAdd = convertView.findViewById(R.id.btn_item_add);
        TextView txtNombre = convertView.findViewById(R.id.tv_nombre_item);
        TextView tvOpciones = convertView.findViewById(R.id.tv_opciones);
        TextView tvExerciseCount = convertView.findViewById(R.id.tv_exercise_count);
        ImageView ivBackgroundIcon = convertView.findViewById(R.id.iv_background_icon);

        if (isModoPopup) {
            txtNombre.setTextColor(Color.BLACK);
            tvOpciones.setTextColor(Color.BLACK);
            if (tvExerciseCount != null) tvExerciseCount.setTextColor(Color.DKGRAY);
            if (ivBackgroundIcon != null) ivBackgroundIcon.setColorFilter(Color.BLACK);
            
            GradientDrawable shape = new GradientDrawable();
            shape.setShape(GradientDrawable.RECTANGLE);
            shape.setCornerRadius(12 * parent.getContext().getResources().getDisplayMetrics().density);
            shape.setStroke(3, Color.BLACK);
            shape.setColor(Color.TRANSPARENT);
            container.setBackground(shape);
        } else {
            txtNombre.setTextColor(Color.WHITE);
            tvOpciones.setTextColor(Color.WHITE);
            if (tvExerciseCount != null) tvExerciseCount.setTextColor(Color.parseColor("#CCCCCC"));
            if (ivBackgroundIcon != null) ivBackgroundIcon.setColorFilter(Color.WHITE);
            container.setBackgroundColor(Color.TRANSPARENT); // MaterialCardView handles background
        }

        if (mostrarBotonAdd && (secciones == null || position == secciones.size())) {
            btnAdd.setVisibility(View.VISIBLE);
            txtNombre.setVisibility(View.GONE);
            tvOpciones.setVisibility(View.GONE);
            if (tvExerciseCount != null) tvExerciseCount.setVisibility(View.GONE);
            if (ivBackgroundIcon != null) ivBackgroundIcon.setVisibility(View.GONE);
            convertView.setOnClickListener(v -> { if (listener != null) listener.onAddClick(); });
        } else {
            Seccion seccion = secciones.get(position);
            btnAdd.setVisibility(View.GONE);
            txtNombre.setVisibility(View.VISIBLE);
            tvOpciones.setVisibility(mostrarBotonAdd ? View.VISIBLE : View.GONE);
            if (tvExerciseCount != null) {
                tvExerciseCount.setVisibility(View.VISIBLE);
                tvExerciseCount.setText(seccion.ejercicioCount + " ejercicios");
            }
            if (ivBackgroundIcon != null) ivBackgroundIcon.setVisibility(View.VISIBLE);

            if (seccion.nombreRutina != null && !seccion.nombreRutina.isEmpty()) {
                txtNombre.setText(seccion.NombreSeccion + "\n(" + seccion.nombreRutina + ")");
            } else {
                txtNombre.setText(seccion.NombreSeccion);
            }

            tvOpciones.setOnClickListener(v -> { if (listener != null) listener.onOptionsClick(v, seccion); });
            convertView.setOnClickListener(v -> { if (listener != null) listener.onSeccionClick(seccion); });
        }

        // Animación de entrada escalonada
        if (position > lastPosition) {
            android.view.animation.Animation animation = android.view.animation.AnimationUtils.loadAnimation(parent.getContext(), R.anim.item_entrance);
            animation.setStartOffset(position * 50L);
            convertView.startAnimation(animation);
            lastPosition = position;
        }

        return convertView;
    }
}

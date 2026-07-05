package com.example.migymsito.adapter;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.migymsito.R;
import com.example.migymsito.data.Seccion;

import java.util.List;

public class SeccionesAdapter extends RecyclerView.Adapter<SeccionesAdapter.SeccionViewHolder> {

    private List<Seccion> secciones;
    private final OnSeccionClickListener listener;
    private boolean isModoPopup = false;
    private int lastPosition = -1;

    public interface OnSeccionClickListener {
        void onSeccionClick(Seccion seccion);
        void onOptionsClick(View view, Seccion seccion);
    }

    public SeccionesAdapter(List<Seccion> secciones, OnSeccionClickListener listener) {
        this.secciones = secciones;
        this.listener = listener;
    }

    public void setModoPopup(boolean modoPopup) {
        this.isModoPopup = modoPopup;
    }

    public void setSecciones(List<Seccion> secciones) {
        this.secciones = secciones;
        this.lastPosition = -1;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SeccionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_seccion, parent, false);
        return new SeccionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SeccionViewHolder holder, int position) {
        Seccion seccion = secciones.get(position);

        if (isModoPopup) {
            holder.txtNombre.setTextColor(Color.BLACK);
            holder.tvOpciones.setTextColor(Color.BLACK);
            if (holder.tvExerciseCount != null) holder.tvExerciseCount.setTextColor(Color.DKGRAY);
            if (holder.ivBackgroundIcon != null) holder.ivBackgroundIcon.setColorFilter(Color.BLACK);
            
            GradientDrawable shape = new GradientDrawable();
            shape.setShape(GradientDrawable.RECTANGLE);
            shape.setCornerRadius(12 * holder.itemView.getContext().getResources().getDisplayMetrics().density);
            shape.setStroke(3, Color.BLACK);
            shape.setColor(Color.TRANSPARENT);
            holder.container.setBackground(shape);
            holder.tvOpciones.setVisibility(View.GONE);
        } else {
            holder.txtNombre.setTextColor(Color.WHITE);
            holder.tvOpciones.setTextColor(Color.WHITE);
            if (holder.tvExerciseCount != null) holder.tvExerciseCount.setTextColor(Color.parseColor("#CCCCCC"));
            if (holder.ivBackgroundIcon != null) holder.ivBackgroundIcon.setColorFilter(Color.WHITE);
            holder.container.setBackgroundResource(0); // Let MaterialCardView handle it
            holder.tvOpciones.setVisibility(View.VISIBLE);
        }

        if (seccion.nombreRutina != null && !seccion.nombreRutina.isEmpty()) {
            holder.txtNombre.setText(String.format("%s\n(%s)", seccion.NombreSeccion, seccion.nombreRutina));
        } else {
            holder.txtNombre.setText(seccion.NombreSeccion);
        }

        if (holder.tvExerciseCount != null) {
            holder.tvExerciseCount.setText(String.format("%d ejercicios", seccion.ejercicioCount));
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onSeccionClick(seccion);
        });

        holder.tvOpciones.setOnClickListener(v -> {
            if (listener != null) listener.onOptionsClick(v, seccion);
        });

        setAnimation(holder.itemView, position);
    }

    @Override
    public int getItemCount() {
        return secciones != null ? secciones.size() : 0;
    }

    private void setAnimation(View viewToAnimate, int position) {
        if (position > lastPosition) {
            Animation animation = AnimationUtils.loadAnimation(viewToAnimate.getContext(), R.anim.item_entrance);
            animation.setStartOffset(position * 50L);
            viewToAnimate.startAnimation(animation);
            lastPosition = position;
        }
    }

    public static class SeccionViewHolder extends RecyclerView.ViewHolder {
        View container;
        TextView txtNombre;
        TextView tvOpciones;
        TextView tvExerciseCount;
        ImageView ivBackgroundIcon;

        public SeccionViewHolder(@NonNull View itemView) {
            super(itemView);
            container = itemView.findViewById(R.id.container_item);
            txtNombre = itemView.findViewById(R.id.tv_nombre_item);
            tvOpciones = itemView.findViewById(R.id.tv_opciones);
            tvExerciseCount = itemView.findViewById(R.id.tv_exercise_count);
            ivBackgroundIcon = itemView.findViewById(R.id.iv_background_icon);
        }
    }
}

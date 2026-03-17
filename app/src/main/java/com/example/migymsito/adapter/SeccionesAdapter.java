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
    // Participa en SeccionesActivity para ocultar el botón de añadir cuando se usa en el popup de secciones previas
    private boolean mostrarBotonAdd = true;
    // Participa en SeccionesActivity para adaptar el diseño al popup blanco (colores oscuros)
    private boolean isModoPopup = false;

    public interface OnSeccionClickListener {
        void onAddClick();
        void onSeccionClick(Seccion seccion);
        void onOptionsClick(View view, Seccion seccion);
    }

    public SeccionesAdapter(List<Seccion> secciones, OnSeccionClickListener listener) {
        this.secciones = secciones;
        this.listener = listener;
    }

    // Participa en SeccionesActivity para configurar si se muestra el botón de añadir (útil para el popup)
    public void setMostrarBotonAdd(boolean mostrar) {
        this.mostrarBotonAdd = mostrar;
    }

    // Participa en SeccionesActivity para cambiar a colores oscuros en popup blanco
    public void setModoPopup(boolean modoPopup) {
        this.isModoPopup = modoPopup;
    }

    public void setSecciones(List<Seccion> secciones) {
        this.secciones = secciones;
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
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_gv, parent, false);
        }

        View container = convertView.findViewById(R.id.container_item);
        ImageView btnAdd = convertView.findViewById(R.id.btn_item_add);
        TextView txtNombre = convertView.findViewById(R.id.tv_nombre_item);
        TextView tvOpciones = convertView.findViewById(R.id.tv_opciones);
        View ivImagen = convertView.findViewById(R.id.iv_item_imagen);

        // Participa en SeccionesActivity: Ocultar la imagen y centrar el texto para Secciones
        if (ivImagen != null) ivImagen.setVisibility(View.GONE);

        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) txtNombre.getLayoutParams();
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        txtNombre.setLayoutParams(params);

        // Participa en SeccionesActivity: Ajustar diseño según si es popup blanco o pantalla negra
        if (isModoPopup) {
            txtNombre.setTextColor(Color.BLACK);
            tvOpciones.setTextColor(Color.BLACK);
            
            GradientDrawable shape = new GradientDrawable();
            shape.setShape(GradientDrawable.RECTANGLE);
            shape.setCornerRadius(12 * parent.getContext().getResources().getDisplayMetrics().density);
            shape.setStroke(3, Color.BLACK);
            shape.setColor(Color.TRANSPARENT);
            container.setBackground(shape);
        } else {
            txtNombre.setTextColor(Color.WHITE);
            tvOpciones.setTextColor(Color.WHITE);
            container.setBackgroundResource(R.drawable.card_border_white);
        }

        if (mostrarBotonAdd && (secciones == null || position == secciones.size())) {
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
            
            // Participa en SeccionesActivity: Ocultar opciones si no se muestra el botón add (es el popup)
            tvOpciones.setVisibility(mostrarBotonAdd ? View.VISIBLE : View.GONE);

            // Participa en SeccionesActivity: Mostrar nombre de sección y rutina si está disponible (para el popup)
            if (seccion.nombreRutina != null && !seccion.nombreRutina.isEmpty()) {
                txtNombre.setText(seccion.NombreSeccion + "\n(" + seccion.nombreRutina + ")");
            } else {
                txtNombre.setText(seccion.NombreSeccion);
            }

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

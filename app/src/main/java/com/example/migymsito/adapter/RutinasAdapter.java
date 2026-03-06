package com.example.migymsito.adapter;

import android.app.Dialog;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.migymsito.R;

import java.util.List;

public class RutinasAdapter extends BaseAdapter {

    private List<String> elementos;

    public RutinasAdapter(List<String> elementos) {
        this.elementos = elementos;
    }

    @Override
    public int getCount() {
        return elementos != null ? elementos.size() + 1 : 1;
    }

    @Override
    public String getItem(int position) {
        return position == 0 ? null : elementos.get(position - 1);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = convertView;

        if (view == null) {
            view = inflater.inflate(R.layout.boton_mas_crear_nueva_rutina_gv, null);
        }

        Button btnAdd = view.findViewById(R.id.btn_item_add);
        TextView txtNombre = view.findViewById(R.id.tv_nombre_rutina);

        if (position == 0) {
            // Es el botón de Añadir
            btnAdd.setVisibility(View.VISIBLE);
            txtNombre.setVisibility(View.GONE);

            btnAdd.setOnClickListener(v -> {
                Dialog dialog = new Dialog(v.getContext());
                dialog.setContentView(R.layout.pop_up_nueva_rutina);
                if (dialog.getWindow() != null) {
                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                }

                Button btnCancelar = dialog.findViewById(R.id.btnCancelarRutina);
                btnCancelar.setOnClickListener(view1 -> dialog.dismiss());

                Button btnCrear = dialog.findViewById(R.id.btnCrearRutina);
                btnCrear.setOnClickListener(view1 -> {
                    Toast.makeText(v.getContext(), "Rutina creada", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                });

                dialog.show();
            });
        } else {
            // Es una rutina
            btnAdd.setVisibility(View.GONE);
            txtNombre.setVisibility(View.VISIBLE);
            txtNombre.setText(getItem(position));
        }

        return view;
    }
}
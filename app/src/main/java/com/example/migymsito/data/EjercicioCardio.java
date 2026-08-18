package com.example.migymsito.data;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(tableName = "EjercicioCardio")
public class EjercicioCardio implements Serializable {
    @PrimaryKey(autoGenerate = true)
    public int IdEjercicioCardio;

    @NonNull
    public String TipoEjercicio;

    @NonNull
    public String NombreEjercicio;

    public String ImagenEjercicio;
}

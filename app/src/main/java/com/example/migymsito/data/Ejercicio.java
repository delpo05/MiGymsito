package com.example.migymsito.data;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(tableName = "Ejercicio")
public class Ejercicio implements Serializable {
    @PrimaryKey(autoGenerate = true)
    public int IdEjercicio;

    @NonNull
    public String TipoEjercicio;

    @NonNull
    public String NombreEjercicio;

    public String ImagenEjercicio;

    @NonNull
    public Boolean PesoCorporalEjercicio;

    // Determina si el ejercicio es preestablecido por el sistema o creado por el usuario
    @NonNull
    public boolean EsPreestablecido = false;
}

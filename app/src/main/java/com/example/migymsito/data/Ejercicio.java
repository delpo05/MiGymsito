package com.example.migymsito.data;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(tableName = "Ejercicio")
public class Ejercicio implements Serializable {
    @PrimaryKey(autoGenerate = true)
    public int IdEjercicio;

    /**
     * Define el origen del ejercicio.
     * Puede ser "Preestablecido" o "Personalizado".
     */
    @NonNull
    public String TipoEjercicio;

    @NonNull
    public String NombreEjercicio;

    public String ImagenEjercicio;

    @NonNull
    public Boolean PesoCorporalEjercicio;
}

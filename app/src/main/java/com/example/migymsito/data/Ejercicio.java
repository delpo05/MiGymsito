package com.example.migymsito.data;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

// He eliminado la relación directa con Seccion, ya que ahora se gestiona a través de SeccionesXEjercicios
@Entity(tableName = "Ejercicio")
public class Ejercicio implements Serializable {
    @PrimaryKey(autoGenerate = true)
    public int idEjercicio;

    @NonNull
    public Boolean EsCalistenico;

    @NonNull
    public String NombreEjercicio;

    public String ImagenEjercicio; // Puede ser null si no tiene imagen
}

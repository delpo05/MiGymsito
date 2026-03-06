package com.example.migymsito.data;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "Ejercicio",
        foreignKeys = @ForeignKey(
                entity = Seccion.class,                    // Relación con la tabla Seccion
                parentColumns = "idSeccion",               // PK de Seccion
                childColumns = "idSeccionEjercicio",       // FK en Ejercicio
                onDelete = ForeignKey.CASCADE              // Borrado en cascada
        ),
        indices = { @Index("idSeccionEjercicio") }         // Índice para optimizar búsquedas por sección
)
public class Ejercicio {
    @PrimaryKey(autoGenerate = true)
    public int idEjercicio;

    @NonNull
    public int idSeccionEjercicio;  // FK

    @NonNull
    public Boolean EsCalistenico;

    @NonNull
    public String NombreEjercicio;

    public String ImagenEjercicio; // Puede ser null si no tiene imagen
}

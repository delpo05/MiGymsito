package com.example.migymsito.data;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "Seccion",
        foreignKeys = @ForeignKey(
                entity = Rutina.class,                     // Relación con la tabla Rutina
                parentColumns = "idRutina",                // PK de Rutina
                childColumns = "IdRutinaSeccion",          // FK en Seccion
                onDelete = ForeignKey.CASCADE              // Si se borra la rutina → borrar sus secciones
        ),
        indices = { @Index("IdRutinaSeccion") }            // Index para mejorar queries por FK
)
public class Seccion {

    @PrimaryKey(autoGenerate = true)
    public int idSeccion;

    @NonNull
    public int IdRutinaSeccion;  // FK

    @NonNull
    public String NombreSeccion;

    @NonNull
    public String ColorSeccion;
}
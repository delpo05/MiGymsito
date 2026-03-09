package com.example.migymsito.data;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "Seccion",
        foreignKeys = @ForeignKey(
                entity = Rutina.class,
                parentColumns = "IdRutina",
                childColumns = "IdRutinaSeccion",
                onDelete = ForeignKey.CASCADE
        ),
        indices = { @Index("IdRutinaSeccion") }
)
public class Seccion {

    @PrimaryKey(autoGenerate = true)
    public int idSeccion;

    @NonNull
    public int IdRutinaSeccion;  // FK

    @NonNull
    public String NombreSeccion;
}
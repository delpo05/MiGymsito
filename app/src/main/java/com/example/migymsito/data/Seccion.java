package com.example.migymsito.data;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.io.Serializable;

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
public class Seccion implements Serializable {

    @PrimaryKey(autoGenerate = true)
    public int IdSeccion;

    // Se cambia a Integer para permitir nulos en secciones preestablecidas que no pertenecen a ninguna rutina de usuario
    public Integer IdRutinaSeccion;  // FK

    @NonNull
    public String NombreSeccion;

    // Participa en SeccionesActivity para mostrar el nombre de la rutina en el popup de secciones previas
    @Ignore
    public String nombreRutina;

    // Determina si la sección es preestablecida por el sistema o creada por el usuario
    @NonNull
    public boolean EsPreestablecido = false;
}

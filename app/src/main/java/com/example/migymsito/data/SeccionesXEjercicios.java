package com.example.migymsito.data;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.io.Serializable;

// Esta clase representa la tabla intermedia para la relación muchos a muchos entre Secciones y Ejercicios
@Entity(
        tableName = "SeccionesXEjercicios",
        foreignKeys = {
                @ForeignKey(
                        entity = Seccion.class,
                        parentColumns = "idSeccion",
                        childColumns = "idSeccion",
                        onDelete = ForeignKey.CASCADE
                ),
                @ForeignKey(
                        entity = Ejercicio.class,
                        parentColumns = "idEjercicio",
                        childColumns = "idEjercicio",
                        onDelete = ForeignKey.CASCADE
                )
        },
        indices = {
                @Index("idSeccion"),
                @Index("idEjercicio")
        }
)
public class SeccionesXEjercicios implements Serializable {
    @PrimaryKey(autoGenerate = true)
    public int IdSeccionXEjercicio;

    @NonNull
    public int idSeccion;  // FK que apunta a la sección

    @NonNull
    public int idEjercicio; // FK que apunta al ejercicio
}

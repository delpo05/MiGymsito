package com.example.migymsito.data;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(
        tableName = "SeccionXejercicio",
        foreignKeys = {
                @ForeignKey(
                        entity = Seccion.class,
                        parentColumns = "IdSeccion",
                        childColumns = "IdSeccion",
                        onDelete = ForeignKey.CASCADE
                ),
                @ForeignKey(
                        entity = Ejercicio.class,
                        parentColumns = "IdEjercicio",
                        childColumns = "IdEjercicio",
                        onDelete = ForeignKey.CASCADE
                )
        },
        indices = {
                @Index("IdSeccion"),
                @Index("IdEjercicio")
        }
)
public class SeccionXejercicio implements Serializable {

    @PrimaryKey(autoGenerate = true)
    public int IdSeccionXejercicio;

    @NonNull
    public int IdSeccion;

    @NonNull
    public int IdEjercicio;
}

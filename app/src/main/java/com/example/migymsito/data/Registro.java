package com.example.migymsito.data;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(
        tableName = "Registro",
        foreignKeys = {
                @ForeignKey(
                        entity = Entrenamiento.class,
                        parentColumns = "IdEntrenamiento",
                        childColumns = "IdEntrenamiento",
                        onDelete = ForeignKey.CASCADE
                ),
                @ForeignKey(
                        entity = SeccionXejercicio.class,
                        parentColumns = "IdSeccionXejercicio",
                        childColumns = "IdSeccionXejercicio",
                        onDelete = ForeignKey.CASCADE
                )
        },
        indices = {
                @Index("IdEntrenamiento"),
                @Index("IdSeccionXejercicio")
        }
)
public class Registro implements Serializable {

    @PrimaryKey(autoGenerate = true)
    public int IdRegistro;

    @NonNull
    public int IdEntrenamiento;

    @NonNull
    public int IdSeccionXejercicio;

    @NonNull
    public Double PesoRegistro;

    @NonNull
    public int NumSeriesRegistro;

    @NonNull
    public int Repeticiones;

    @NonNull
    public Long FechaRegistro; 
}

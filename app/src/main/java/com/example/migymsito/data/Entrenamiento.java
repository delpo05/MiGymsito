package com.example.migymsito.data;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(
        tableName = "Entrenamiento",
        foreignKeys = {
                @ForeignKey(
                        entity = Usuario.class,
                        parentColumns = "IdUsuario",
                        childColumns = "IdUsuario",
                        onDelete = ForeignKey.CASCADE
                ),
                @ForeignKey(
                        entity = Seccion.class,
                        parentColumns = "IdSeccion",
                        childColumns = "IdSeccion",
                        onDelete = ForeignKey.CASCADE
                )
        },
        indices = {
                @Index("IdUsuario"),
                @Index("IdSeccion")
        }
)
public class Entrenamiento implements Serializable {

    @PrimaryKey(autoGenerate = true)
    public int IdEntrenamiento;

    @NonNull
    public int IdUsuario;

    @NonNull
    public int IdSeccion;

    @NonNull
    public Integer NumeroEntrenamiento;

    @NonNull
    public Long FechaInicio;

    public Long FechaFin;
}

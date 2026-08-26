package com.example.migymsito.data;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(
        tableName = "RegistroCardio",
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
public class RegistroCardio implements Serializable {

    @PrimaryKey(autoGenerate = true)
    public int IdRegistroCardio;

    @NonNull
    public int IdEntrenamiento;

    @NonNull
    public int IdSeccionXejercicio;

    @NonNull
    public Long FechaRegistro;

    @NonNull
    public long DuracionSegundos;

    public Double Distancia;
    public String UnidadDistancia;

    public Double VelocidadPromedio;
    public Integer CaloriasQuemadas;

    public Integer RitmoCardiacoPromedio;
    public Integer RitmoCardiacoMaximo;

    public Double Inclinacion;
    public Integer NivelResistencia;
}

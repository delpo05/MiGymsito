package com.example.migymsito.data;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(
        tableName = "Historial",
        foreignKeys = @ForeignKey(
                entity = Usuario.class,
                parentColumns = "IdUsuario",
                childColumns = "IdUsuarioHistorial",
                onDelete = ForeignKey.CASCADE
        ),
        indices = { @Index("IdUsuarioHistorial") }
)
public class Historial implements Serializable {

    @PrimaryKey(autoGenerate = true)
    public int IdHistorial;

    @NonNull
    public int IdUsuarioHistorial; // FK a Usuario

    @NonNull
    public Double PesoHistorial;

    @NonNull
    public Double AlturaHistorial;

    @NonNull
    public Long FechaHistorial; // Timestamp
}

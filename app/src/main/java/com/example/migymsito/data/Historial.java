package com.example.migymsito.data;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "Historial",
        foreignKeys = @ForeignKey(
                entity = Usuario.class,
                parentColumns = "id",
                childColumns = "IdUsuario",
                onDelete = ForeignKey.CASCADE
        ),
        indices = { @Index("IdUsuario") }
)
public class Historial {

    @PrimaryKey(autoGenerate = true)
    public int IdHistorial;

    @NonNull
    public int IdUsuario; // FK a Usuario

    @NonNull
    public Double PesoHistorial;

    @NonNull
    public Double AlturaHistorial;

    @NonNull
    public Long FechaHistorial; // Timestamp
}

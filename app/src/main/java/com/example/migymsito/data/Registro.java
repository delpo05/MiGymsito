package com.example.migymsito.data;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "Registro",
        foreignKeys = {
                @ForeignKey(
                        entity = Usuario.class,
                        parentColumns = "id",
                        childColumns = "IdUsuarioRegistro",
                        onDelete = ForeignKey.CASCADE
                ),
                @ForeignKey(
                        entity = Ejercicio.class,
                        parentColumns = "idEjercicio",
                        childColumns = "IdEjercicioRegistro",
                        onDelete = ForeignKey.CASCADE
                )
        },
        indices = {
                @Index("IdUsuarioRegistro"),
                @Index("IdEjercicioRegistro")
        }
)
public class Registro {

    @PrimaryKey(autoGenerate = true)
    public int idRegistro;

    @NonNull
    public int IdUsuarioRegistro; // Relación con Usuario

    @NonNull
    public int IdEjercicioRegistro; // Relación con Ejercicio

    @NonNull
    public Long FechaRegistro; // Guardado como Timestamp (milisegundos)

    @NonNull
    public Double PesoRegistro; // Double por si usas medios kilos (ej: 12.5)

    @NonNull
    public int NumSeriesRegistro;

    @NonNull
    public int RepeticionesRegistro;
}

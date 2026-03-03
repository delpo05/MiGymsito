package com.example.migymsito.data;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

@Entity(
        tableName = "Rutina",
        indices = {
                @Index(value = {"NombreRutina"}, unique = false)
        },
        foreignKeys = {
                @ForeignKey(
                        entity = Usuario.class,
                        parentColumns = "id",
                        childColumns = "IdUsuarioRutina",
                        onDelete = ForeignKey.CASCADE
                )
        }
)
public class Rutina {

    @PrimaryKey(autoGenerate = true)
    public int idRutina;

    @NonNull
    public int IdUsuarioRutina; // Cambiado de Integer a int para consistencia

    @NonNull
    public String NombreRutina;

    @NonNull
    public String ColorRutina;
}

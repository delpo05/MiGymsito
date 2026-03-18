package com.example.migymsito.data;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(
        tableName = "Rutina",
        foreignKeys = @ForeignKey(
                entity = Usuario.class,
                parentColumns = "IdUsuario",
                childColumns = "IdUsuarioRutina",
                onDelete = ForeignKey.CASCADE
        ),
        indices = {@Index("IdUsuarioRutina")}
)
public class Rutina implements Serializable {

    @PrimaryKey(autoGenerate = true)
    public int IdRutina;

    @NonNull
    public String NombreRutina;

    @NonNull
    public int IdUsuarioRutina; // FK a Usuario
}

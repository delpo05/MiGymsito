package com.example.migymsito.data;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.io.Serializable;

// Definición de la estructura de la tabla Usuario
@Entity(tableName = "Usuario",
        indices = {
                @Index(value = {"CorreoElectronicoUsuario"}, unique = true),
                @Index(value = {"NombreUsuario"}, unique = true)
        }
)
public class Usuario implements Serializable {
    @PrimaryKey(autoGenerate = true)
    public int IdUsuario;

    @NonNull
    public String NombreUsuario;

    @NonNull
    public Long FechaNacimientoUsuario;

    @NonNull
    public String CorreoElectronicoUsuario;

    @NonNull
    public String GeneroUsuario;

    @NonNull
    public String ContraseniaUsuario;
}

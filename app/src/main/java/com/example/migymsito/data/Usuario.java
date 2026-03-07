package com.example.migymsito.data;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.io.Serializable;

//definicion de la estructura de la tabla Usuario
@Entity(tableName = "Usuario",
        indices = {
                @Index(value = {"correoElectronicoUsuario"}, unique = true),
                @Index(value = {"nombreUsuario"}, unique = true)
        }
)
public class Usuario implements Serializable {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @NonNull
    public String nombreUsuario;

    @NonNull
    public String correoElectronicoUsuario;

    @NonNull
    public Long fechaNacimiento;

    @NonNull
    public String generoUsuario;

    @NonNull
    public String contraseniaUsuario;

}

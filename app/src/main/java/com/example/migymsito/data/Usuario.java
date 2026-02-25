package com.example.migymsito.data;

//importacion de librerias

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

//definicion de la estructura de la tabla Usuario
@Entity(tableName = "Usuario",
        indices = {
                @Index(value = {"correoElectronicoUsuario"}, unique = true),
                @Index(value = {"nombreUsuario"}, unique = true)
        }
)

public class Usuario {
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
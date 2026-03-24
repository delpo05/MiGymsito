package com.example.migymsito.data;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.io.Serializable;
import java.util.Objects;

@Entity(
        tableName = "Seccion",
        foreignKeys = @ForeignKey(
                entity = Rutina.class,
                parentColumns = "IdRutina",
                childColumns = "IdRutinaSeccion",
                onDelete = ForeignKey.CASCADE
        ),
        indices = { @Index("IdRutinaSeccion") }
)
public class Seccion implements Serializable {

    @PrimaryKey(autoGenerate = true)
    public int IdSeccion;

    // Cambiado a Integer para permitir nulos en secciones preestablecidas (sin rutina inicial)
    public Integer IdRutinaSeccion;  

    @NonNull
    public String NombreSeccion;

    @NonNull
    public String TipoSeccion = "Personalizado"; 

    @Ignore
    public String nombreRutina;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Seccion seccion = (Seccion) o;
        return IdSeccion == seccion.IdSeccion &&
                Objects.equals(IdRutinaSeccion, seccion.IdRutinaSeccion) &&
                NombreSeccion.equals(seccion.NombreSeccion);
    }

    @Override
    public int hashCode() {
        return Objects.hash(IdSeccion, IdRutinaSeccion, NombreSeccion);
    }
}
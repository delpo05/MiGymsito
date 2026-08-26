package com.example.migymsito.dataDao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.migymsito.data.RegistroCardio;

import java.util.List;

@Dao
public interface RegistroCardioDao {

    @Insert
    long insertar(RegistroCardio registro);

    @Update
    void actualizar(RegistroCardio registro);

    @Delete
    void eliminar(RegistroCardio registro);

    @Query("SELECT * FROM RegistroCardio WHERE IdEntrenamiento = :idEntrenamiento")
    List<RegistroCardio> obtenerPorEntrenamiento(int idEntrenamiento);

    @Query("SELECT * FROM RegistroCardio WHERE IdSeccionXejercicio = :idSeccionXejercicio ORDER BY FechaRegistro DESC")
    List<RegistroCardio> obtenerPorSeccionXejercicio(int idSeccionXejercicio);
}

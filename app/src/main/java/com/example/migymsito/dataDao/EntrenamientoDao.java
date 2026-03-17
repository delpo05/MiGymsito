package com.example.migymsito.dataDao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.migymsito.data.Entrenamiento;

import java.util.List;

@Dao
public interface EntrenamientoDao {
    @Insert
    long insert(Entrenamiento entrenamiento);

    @Update
    void update(Entrenamiento entrenamiento);

    @Delete
    void delete(Entrenamiento entrenamiento);

    @Query("SELECT * FROM Entrenamiento WHERE IdUsuario = :idUsuario")
    List<Entrenamiento> getEntrenamientosByUsuario(int idUsuario);

    @Query("SELECT * FROM Entrenamiento WHERE IdEntrenamiento = :id")
    Entrenamiento getEntrenamientoById(int id);

    @Query("SELECT * FROM Entrenamiento WHERE IdUsuario = :idUsuario AND FechaFin IS NULL LIMIT 1")
    Entrenamiento getEntrenamientoActivo(int idUsuario);

    @Query("SELECT * FROM Entrenamiento WHERE IdUsuario = :idUsuario AND IdSeccion = :idSeccion AND FechaFin IS NULL LIMIT 1")
    Entrenamiento getEntrenamientoActivoPorSeccion(int idUsuario, int idSeccion);

    @Query("SELECT Entrenamiento.* FROM Entrenamiento " +
           "JOIN Seccion ON Entrenamiento.IdSeccion = Seccion.IdSeccion " +
           "WHERE Entrenamiento.IdUsuario = :idUsuario AND Seccion.IdRutinaSeccion = :idRutina AND Entrenamiento.FechaFin IS NULL")
    List<Entrenamiento> getEntrenamientosActivosPorRutina(int idUsuario, int idRutina);
}

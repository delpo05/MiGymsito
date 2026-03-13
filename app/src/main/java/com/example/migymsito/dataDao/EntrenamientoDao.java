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
}

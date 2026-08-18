package com.example.migymsito.dataDao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Delete;
import androidx.room.Update;

import com.example.migymsito.data.EjercicioCardio;

import java.util.List;

@Dao
public interface EjercicioCardioDao {

    @Insert
    long insertarEjercicio(EjercicioCardio ejercicio);

    @Update
    void actualizarEjercicio(EjercicioCardio ejercicio);

    @Delete
    void eliminarEjercicio(EjercicioCardio ejercicio);

    @Query("SELECT * FROM EjercicioCardio")
    List<EjercicioCardio> obtenerTodosLosEjercicios();

    @Query("SELECT * FROM EjercicioCardio WHERE IdEjercicioCardio = :id")
    EjercicioCardio obtenerEjercicioPorId(int id);

    @Query("SELECT * FROM EjercicioCardio WHERE TipoEjercicio = 'Preestablecido'")
    List<EjercicioCardio> obtenerEjerciciosPreestablecidos();

    @Query("DELETE FROM EjercicioCardio")
    void borrarTodo();
}

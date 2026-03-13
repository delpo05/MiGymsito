package com.example.migymsito.dataDao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Delete;
import androidx.room.Update;

import com.example.migymsito.data.Ejercicio;

import java.util.List;

@Dao
public interface EjercicioDao {

    @Insert
    long insertarEjercicio(Ejercicio ejercicio);

    @Update
    void actualizarEjercicio(Ejercicio ejercicio);

    @Delete
    void eliminarEjercicio(Ejercicio ejercicio);

    @Query("SELECT * FROM Ejercicio")
    List<Ejercicio> obtenerTodosLosEjercicios();

    @Query("SELECT * FROM Ejercicio WHERE IdEjercicio = :id")
    Ejercicio obtenerEjercicioPorId(int id);

    @Query("DELETE FROM Ejercicio")
    void borrarTodo();
}

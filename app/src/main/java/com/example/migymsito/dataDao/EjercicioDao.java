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
    void insertarEjercicio(Ejercicio ejercicio);

    @Update
    void actualizarEjercicio(Ejercicio ejercicio);

    @Delete
    void eliminarEjercicio(Ejercicio ejercicio);

    // Obtener todos los ejercicios de una sección (ej. Pecho, Espalda, etc.)
    @Query("SELECT * FROM Ejercicio WHERE idSeccionEjercicio = :idSeccion")
    List<Ejercicio> obtenerEjerciciosPorSeccion(int idSeccion);

    @Query("DELETE FROM Ejercicio")
    void borrarTodo();
}

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

    // Obtiene ejercicios que pertenecen a una sección específica
    @Query("SELECT e.* FROM Ejercicio e INNER JOIN SeccionXejercicio sxe ON e.IdEjercicio = sxe.IdEjercicio WHERE sxe.IdSeccion = :idSeccion")
    List<Ejercicio> obtenerEjerciciosPorSeccion(int idSeccion);

    // Obtiene todos los ejercicios preestablecidos usando el campo TipoEjercicio
    @Query("SELECT * FROM Ejercicio WHERE TipoEjercicio = 'Preestablecido'")
    List<Ejercicio> obtenerEjerciciosPreestablecidos();

    @Query("SELECT nombreEjercicio FROM Ejercicio JOIN SeccionXejercicio ON Ejercicio.IdEjercicio = SeccionXejercicio.IdEjercicio WHERE SeccionXejercicio.IdSeccion = :idSeccion")
    List<String> obtenerNombresEjerciciosPorSeccion(int idSeccion);



    @Query("DELETE FROM Ejercicio")
    void borrarTodo();
}

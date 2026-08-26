package com.example.migymsito.dataDao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Delete;
import androidx.room.Update;

import com.example.migymsito.data.EjercicioPeso;

import java.util.List;

@Dao
public interface EjercicioPesoDao {

    @Insert
    long insertarEjercicioPeso(EjercicioPeso ejercicio);

    @Update
    void actualizarEjercicioPeso(EjercicioPeso ejercicio);

    @Delete
    void eliminarEjercicioPeso(EjercicioPeso ejercicio);

    @Query("SELECT * FROM Ejercicio")
    List<EjercicioPeso> obtenerTodosLosEjercicios();

    @Query("SELECT * FROM Ejercicio WHERE IdEjercicio = :id")
    EjercicioPeso obtenerEjercicioPorId(int id);

    // Obtiene ejercicios que pertenecen a una sección específica
    @Query("SELECT e.* FROM Ejercicio e INNER JOIN SeccionXejercicio sxe ON e.IdEjercicio = sxe.IdEjercicio WHERE sxe.IdSeccion = :idSeccion")
    List<EjercicioPeso> obtenerEjerciciosPorSeccion(int idSeccion);

    // Obtiene solo los ejercicios que el usuario ha entrenado al menos una vez
    @Query("SELECT DISTINCT e.* FROM Ejercicio e " +
           "INNER JOIN SeccionXejercicio sxe ON e.IdEjercicio = sxe.IdEjercicio " +
           "INNER JOIN Registro r ON sxe.IdSeccionXejercicio = r.IdSeccionXejercicio " +
           "INNER JOIN Entrenamiento ent ON r.IdEntrenamiento = ent.IdEntrenamiento " +
           "WHERE ent.IdUsuario = :idUsuario")
    List<EjercicioPeso> obtenerEjerciciosEnUsoPorUsuario(int idUsuario);

    // Obtiene todos los ejercicios preestablecidos usando el campo TipoEjercicio
    @Query("SELECT * FROM Ejercicio WHERE TipoEjercicio = 'Preestablecido'")
    List<EjercicioPeso> obtenerEjerciciosPreestablecidos();

    @Query("SELECT nombreEjercicio FROM Ejercicio JOIN SeccionXejercicio ON Ejercicio.IdEjercicio = SeccionXejercicio.IdEjercicio WHERE SeccionXejercicio.IdSeccion = :idSeccion")
    List<String> obtenerNombresEjerciciosPorSeccion(int idSeccion);

    @Query("DELETE FROM Ejercicio")
    void borrarTodo();
}

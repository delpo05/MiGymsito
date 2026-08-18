package com.example.migymsito.dataDao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Delete;
import androidx.room.Update;

import com.example.migymsito.data.EjercicioStrength;

import java.util.List;

@Dao
public interface EjercicioDao {

    @Insert
    long insertarEjercicio(EjercicioStrength ejercicio);

    @Update
    void actualizarEjercicio(EjercicioStrength ejercicio);

    @Delete
    void eliminarEjercicio(EjercicioStrength ejercicio);

    @Query("SELECT * FROM EjerciciosStrength")
    List<EjercicioStrength> obtenerTodosLosEjercicios();

    @Query("SELECT * FROM EjerciciosStrength WHERE IdEjercicio = :id")
    EjercicioStrength obtenerEjercicioPorId(int id);

    // Obtiene ejercicios que pertenecen a una sección específica
    @Query("SELECT e.* FROM EjerciciosStrength e INNER JOIN SeccionXejercicio sxe ON e.IdEjercicio = sxe.IdEjercicio WHERE sxe.IdSeccion = :idSeccion")
    List<EjercicioStrength> obtenerEjerciciosPorSeccion(int idSeccion);

    // Obtiene solo los ejercicios que el usuario ha entrenado al menos una vez
    @Query("SELECT DISTINCT e.* FROM EjerciciosStrength e " +
           "INNER JOIN SeccionXejercicio sxe ON e.IdEjercicio = sxe.IdEjercicio " +
           "INNER JOIN Registro r ON sxe.IdSeccionXejercicio = r.IdSeccionXejercicio " +
           "INNER JOIN Entrenamiento ent ON r.IdEntrenamiento = ent.IdEntrenamiento " +
           "WHERE ent.IdUsuario = :idUsuario")
    List<EjercicioStrength> obtenerEjerciciosEnUsoPorUsuario(int idUsuario);

    // Obtiene todos los ejercicios preestablecidos usando el campo TipoEjercicio
    @Query("SELECT * FROM EjerciciosStrength WHERE TipoEjercicio = 'Preestablecido'")
    List<EjercicioStrength> obtenerEjerciciosPreestablecidos();

    @Query("SELECT nombreEjercicio FROM EjerciciosStrength JOIN SeccionXejercicio ON EjerciciosStrength.IdEjercicio = SeccionXejercicio.IdEjercicio WHERE SeccionXejercicio.IdSeccion = :idSeccion")
    List<String> obtenerNombresEjerciciosPorSeccion(int idSeccion);

    @Query("DELETE FROM EjerciciosStrength")
    void borrarTodo();
}

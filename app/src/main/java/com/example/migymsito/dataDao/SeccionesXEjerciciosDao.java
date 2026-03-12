package com.example.migymsito.dataDao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.migymsito.data.Ejercicio;
import com.example.migymsito.data.SeccionesXEjercicios;

import java.util.List;

@Dao
public interface SeccionesXEjerciciosDao {

    @Insert
    void insertar(SeccionesXEjercicios seccionXEjercicio);

    @Update
    void actualizar(SeccionesXEjercicios seccionXEjercicio);

    @Delete
    void eliminar(SeccionesXEjercicios seccionXEjercicio);

    // obtenemos los objetos Ejercicio filtrando por el id de la sección
    // a través de la relación en esta tabla intermedia.
    @Query("SELECT Ejercicio.* FROM Ejercicio " +
           "INNER JOIN SeccionesXEjercicios ON Ejercicio.idEjercicio = SeccionesXEjercicios.idEjercicio " +
           "WHERE SeccionesXEjercicios.idSeccion = :idSeccion")
    List<Ejercicio> obtenerEjerciciosPorSeccion(int idSeccion);

    @Query("DELETE FROM SeccionesXEjercicios")
    void borrarTodo();
}

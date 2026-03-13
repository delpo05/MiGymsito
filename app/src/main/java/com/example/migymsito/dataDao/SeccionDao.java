package com.example.migymsito.dataDao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Delete;
import androidx.room.Update;

import com.example.migymsito.data.Seccion;

import java.util.List;

@Dao
public interface SeccionDao {

    @Insert
    long insertarSeccion(Seccion seccion);

    @Update
    void actualizarSeccion(Seccion seccion);

    @Delete
    void eliminarSeccion(Seccion seccion);

    @Query("SELECT * FROM Seccion WHERE IdRutinaSeccion = :idRutina")
    List<Seccion> obtenerSeccionesPorRutina(int idRutina);

    // Participa en SeccionesActivity para traer todas las secciones y el nombre de su rutina para el popup de secciones previas
    @Query("SELECT s.*, r.NombreRutina as nombreRutina FROM Seccion s INNER JOIN Rutina r ON s.IdRutinaSeccion = r.IdRutina")
    List<Seccion> obtenerTodasLasSeccionesConRutina();

    @Query("DELETE FROM Seccion")
    void borrarTodo();
}
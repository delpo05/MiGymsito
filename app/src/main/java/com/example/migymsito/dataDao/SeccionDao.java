package com.example.migymsito.dataDao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Delete;
import androidx.room.Update;

import com.example.migymsito.data.Rutina;
import com.example.migymsito.data.Seccion;

import java.util.List;
import java.util.Map;

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

    // Cambiado a LEFT JOIN para incluir secciones sin rutina (Preestablecidas)
    @Query("SELECT * FROM Seccion LEFT JOIN Rutina ON Seccion.IdRutinaSeccion = Rutina.IdRutina")
    Map<Seccion, Rutina> obtenerTodasLasSeccionesConRutina();

    @Query("SELECT * FROM Seccion WHERE TipoSeccion = 'Preestablecido'")
    List<Seccion> obtenerSeccionesPreestablecidas();

    @Query("SELECT * FROM Seccion WHERE TipoSeccion = 'Personalizado'")
    List<Seccion> obtenerSeccionesPersonalizadas();

    @Query("SELECT Seccion.* FROM Seccion " +
           "INNER JOIN Rutina ON Seccion.IdRutinaSeccion = Rutina.IdRutina " +
           "WHERE Rutina.IdUsuarioRutina = :idUsuario")
    List<Seccion> obtenerSeccionesPorUsuario(int idUsuario);

    @Query("DELETE FROM Seccion")
    void borrarTodo();
}
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

    /**
     * MODIFICACIÓN: Ahora devuelve un Mapa que relaciona la Sección con su Rutina.
     * Room se encarga de hacer el JOIN internamente y mapear ambos objetos.
     */
    @Query("SELECT * FROM Seccion JOIN Rutina ON Seccion.IdRutinaSeccion = Rutina.IdRutina")
    Map<Seccion, Rutina> obtenerTodasLasSeccionesConRutina();

    // Obtiene todas las secciones que son preestablecidas por el sistema
    @Query("SELECT * FROM Seccion WHERE TipoSeccion = 'Preestablecido'")
    List<Seccion> obtenerSeccionesPreestablecidas();

    // Obtiene todas las secciones que no son preestablecidas (personalizadas)
    @Query("SELECT * FROM Seccion WHERE TipoSeccion = 'Personalizado'")
    List<Seccion> obtenerSeccionesPersonalizadas();

    @Query("DELETE FROM Seccion")
    void borrarTodo();
}

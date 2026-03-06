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
    void insertarSeccion(Seccion seccion);

    @Update
    void actualizarSeccion(Seccion seccion);

    @Delete
    void eliminarSeccion(Seccion seccion);

    @Query("SELECT * FROM Seccion WHERE IdRutinaSeccion = :idRutina")
    List<Seccion> obtenerSeccionesPorRutina(int idRutina);

    @Query("DELETE FROM Seccion")
    void borrarTodo();
}
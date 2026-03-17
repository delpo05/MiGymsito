package com.example.migymsito.dataDao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Delete;
import androidx.room.Update;

import com.example.migymsito.data.Historial;

import java.util.List;

@Dao
public interface HistorialDao {

    @Insert
    void insertarHistorial(Historial historial);

    @Update
    void actualizarHistorial(Historial historial);

    @Delete
    void eliminarHistorial(Historial historial);

    @Query("SELECT * FROM Historial WHERE IdUsuarioHistorial = :idUsuario ORDER BY FechaHistorial DESC")
    List<Historial> obtenerHistorialPorUsuario(int idUsuario);

    @Query("DELETE FROM Historial")
    void borrarTodo();
}

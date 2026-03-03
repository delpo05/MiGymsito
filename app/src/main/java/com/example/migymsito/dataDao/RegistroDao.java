package com.example.migymsito.dataDao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Delete;
import androidx.room.Update;

import com.example.migymsito.data.Registro;

import java.util.List;

@Dao
public interface RegistroDao {

    @Insert
    void insertarRegistro(Registro registro);

    @Update
    void actualizarRegistro(Registro registro);

    @Delete
    void eliminarRegistro(Registro registro);

    // Obtener todos los registros de un usuario para un ejercicio específico, ordenados por fecha (más reciente primero)
    @Query("SELECT * FROM Registro WHERE IdUsuarioRegistro = :idUsuario AND IdEjercicioRegistro = :idEjercicio ORDER BY FechaRegistro DESC")
    List<Registro> obtenerHistorialPorEjercicio(int idUsuario, int idEjercicio);

    // Obtener todos los registros de un usuario
    @Query("SELECT * FROM Registro WHERE IdUsuarioRegistro = :idUsuario ORDER BY FechaRegistro DESC")
    List<Registro> obtenerTodosLosRegistrosDelUsuario(int idUsuario);

    @Query("DELETE FROM Registro")
    void borrarTodo();
}

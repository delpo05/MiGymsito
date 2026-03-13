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

    @Query("SELECT * FROM Registro WHERE IdEntrenamiento = :idEntrenamiento")
    List<Registro> obtenerRegistrosPorEntrenamiento(int idEntrenamiento);

    @Query("SELECT * FROM Registro WHERE IdSeccionXejercicio = :idSeccionXejercicio")
    List<Registro> obtenerRegistrosPorSeccionXejercicio(int idSeccionXejercicio);

    @Query("SELECT Registro.* FROM Registro " +
           "JOIN SeccionXejercicio ON Registro.IdSeccionXejercicio = SeccionXejercicio.IdSeccionXejercicio " +
           "JOIN Entrenamiento ON Registro.IdEntrenamiento = Entrenamiento.IdEntrenamiento " +
           "WHERE SeccionXejercicio.IdEjercicio = :idEjercicio AND Entrenamiento.IdUsuario = :idUsuario " +
           "ORDER BY Registro.FechaRegistro DESC")
    List<Registro> obtenerHistorialPorEjercicioYUsuario(int idUsuario, int idEjercicio);

    @Query("SELECT Registro.* FROM Registro " +
           "JOIN Entrenamiento ON Registro.IdEntrenamiento = Entrenamiento.IdEntrenamiento " +
           "WHERE Entrenamiento.IdUsuario = :idUsuario " +
           "ORDER BY Registro.FechaRegistro DESC")
    List<Registro> obtenerTodosLosRegistrosDelUsuario(int idUsuario);

    @Query("DELETE FROM Registro")
    void borrarTodo();
}

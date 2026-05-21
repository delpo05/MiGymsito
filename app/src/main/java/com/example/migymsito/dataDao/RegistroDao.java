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

    // Query corregida: Obtenemos solo los campos necesarios para el progreso
    @Query("SELECT r.* FROM Registro r " +
            "JOIN SeccionXejercicio sxe ON r.IdSeccionXejercicio = sxe.IdSeccionXejercicio " +
            "WHERE sxe.IdEjercicio = :idEjercicio " +
            "AND r.PesoRegistro = (" +
            "   SELECT MAX(r2.PesoRegistro) " +
            "   FROM Registro r2 " +
            "   JOIN SeccionXejercicio sxe2 ON r2.IdSeccionXejercicio = sxe2.IdSeccionXejercicio " +
            "   WHERE sxe2.IdEjercicio = :idEjercicio " +
            "   AND (r2.FechaRegistro / 86400000) = (r.FechaRegistro / 86400000) " +
            ") " +
            "ORDER BY r.FechaRegistro ASC")
    List<Registro> obtenerProgresoCargas(int idEjercicio);

    @Query("SELECT r.* FROM Registro r " +
            "JOIN SeccionXejercicio sxe ON r.IdSeccionXejercicio = sxe.IdSeccionXejercicio " +
            "WHERE sxe.IdEjercicio = :idEjercicio " +
            "ORDER BY r.FechaRegistro ASC")
    List<Registro> obtenerRegistrosParaVolumen(int idEjercicio);

    @Query("SELECT Registro.* FROM Registro " +
           "JOIN Entrenamiento ON Registro.IdEntrenamiento = Entrenamiento.IdEntrenamiento " +
           "WHERE Entrenamiento.IdUsuario = :idUsuario " +
           "ORDER BY Registro.FechaRegistro DESC")
    List<Registro> obtenerTodosLosRegistrosDelUsuario(int idUsuario);

    @Query("SELECT r.* FROM Registro r " +
           "JOIN SeccionXejercicio sxe ON r.IdSeccionXejercicio = sxe.IdSeccionXejercicio " +
           "WHERE sxe.IdEjercicio = :idEjercicio " +
           "AND r.IdEntrenamiento = (" +
           "    SELECT MAX(r2.IdEntrenamiento) FROM Registro r2 " +
           "    JOIN SeccionXejercicio sxe2 ON r2.IdSeccionXejercicio = sxe2.IdSeccionXejercicio " +
           "    JOIN Entrenamiento e2 ON r2.IdEntrenamiento = e2.IdEntrenamiento " +
           "    WHERE sxe2.IdEjercicio = :idEjercicio " +
           "    AND e2.IdUsuario = :idUsuario " +
           "    AND r2.IdEntrenamiento < :idEntrenamientoActual" +
           ") " +
           "ORDER BY r.NumSeriesRegistro ASC")
    List<Registro> obtenerRegistrosUltimoEntrenamientoPrevio(int idUsuario, int idEjercicio, int idEntrenamientoActual);

    @Query("DELETE FROM Registro")
    void borrarTodo();
}

package com.example.migymsito.dataDao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Delete;
import androidx.room.Update;

import com.example.migymsito.data.Registro;
import com.example.migymsito.data.RegistroDetallado;

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
           "    SELECT e2.IdEntrenamiento FROM Entrenamiento e2 " +
           "    JOIN Registro r2 ON e2.IdEntrenamiento = r2.IdEntrenamiento " +
           "    JOIN SeccionXejercicio sxe2 ON r2.IdSeccionXejercicio = sxe2.IdSeccionXejercicio " +
           "    WHERE sxe2.IdEjercicio = :idEjercicio " +
           "    AND e2.IdUsuario = :idUsuario " +
           "    AND e2.FechaInicio < IFNULL((SELECT e3.FechaInicio FROM Entrenamiento e3 WHERE e3.IdEntrenamiento = :idEntrenamientoActual), 9223372036854775807) " +
           "    ORDER BY e2.FechaInicio DESC LIMIT 1" +
           ") " +
           "ORDER BY r.NumSeriesRegistro DESC")
    List<Registro> obtenerRegistrosUltimoEntrenamientoPrevio(int idUsuario, int idEjercicio, int idEntrenamientoActual);

    @Query("SELECT r.FechaRegistro as fecha, rut.NombreRutina as nombreRutina, s.NombreSeccion as nombreSeccion, " +
           "ej.NombreEjercicio as nombreEjercicio, r.NumSeriesRegistro as numSerie, " +
           "r.Repeticiones as repeticiones, r.PesoRegistro as peso, " +
           "ej.PesoCorporalEjercicio as esPesoCorporal, ej.TipoDeBarra as tipoBarra, ej.PesoBarra as pesoBarra " +
           "FROM Registro r " +
           "JOIN SeccionXejercicio sxe ON r.IdSeccionXejercicio = sxe.IdSeccionXejercicio " +
           "JOIN Ejercicio ej ON sxe.IdEjercicio = ej.IdEjercicio " +
           "JOIN Seccion s ON sxe.IdSeccion = s.IdSeccion " +
           "JOIN Rutina rut ON s.IdRutinaSeccion = rut.IdRutina " +
           "WHERE rut.IdUsuarioRutina = :idUsuario " +
           "AND (:idRutina = -1 OR rut.IdRutina = :idRutina) " +
           "AND (:idSeccion = -1 OR s.IdSeccion = :idSeccion) " +
           "AND (:idEjercicio = -1 OR ej.IdEjercicio = :idEjercicio) " +
           "ORDER BY r.FechaRegistro DESC")
    List<RegistroDetallado> buscarRegistrosDetallados(int idUsuario, int idRutina, int idSeccion, int idEjercicio);

    @Query("SELECT * FROM Registro")
    List<Registro> obtenerTodosLosRegistros();

    @Query("DELETE FROM Registro")
    void borrarTodo();
}

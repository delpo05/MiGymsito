package com.example.migymsito.dataDao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.migymsito.data.RegistroCardio;
import com.example.migymsito.data.RegistroDetallado;

import java.util.List;

@Dao
public interface RegistroCardioDao {

    @Insert
    long insertar(RegistroCardio registro);

    @Update
    void actualizar(RegistroCardio registro);

    @Delete
    void eliminar(RegistroCardio registro);

    @Query("SELECT * FROM RegistroCardio WHERE IdEntrenamiento = :idEntrenamiento")
    List<RegistroCardio> obtenerPorEntrenamiento(int idEntrenamiento);

    @Query("SELECT * FROM RegistroCardio WHERE IdSeccionXejercicio = :idSeccionXejercicio ORDER BY FechaRegistro DESC")
    List<RegistroCardio> obtenerPorSeccionXejercicio(int idSeccionXejercicio);

    @Query("SELECT rc.* FROM RegistroCardio rc " +
           "JOIN SeccionXejercicio sxe ON rc.IdSeccionXejercicio = sxe.IdSeccionXejercicio " +
           "JOIN Entrenamiento e ON rc.IdEntrenamiento = e.IdEntrenamiento " +
           "WHERE sxe.IdEjercicio = :idEjercicio AND e.IdUsuario = :idUsuario " +
           "ORDER BY rc.FechaRegistro DESC " +
           "LIMIT :limit OFFSET :offset")
    List<RegistroCardio> obtenerPorEjercicioYUsuarioPaginado(int idUsuario, int idEjercicio, int limit, int offset);

    @Query("SELECT rc.* FROM RegistroCardio rc " +
           "JOIN SeccionXejercicio sxe ON rc.IdSeccionXejercicio = sxe.IdSeccionXejercicio " +
           "JOIN Entrenamiento e ON rc.IdEntrenamiento = e.IdEntrenamiento " +
           "WHERE sxe.IdEjercicio = :idEjercicio AND e.IdUsuario = :idUsuario " +
           "ORDER BY rc.FechaRegistro DESC")
    List<RegistroCardio> obtenerPorEjercicioYUsuario(int idUsuario, int idEjercicio);

    @Query("SELECT 'CARDIO' as categoriaEjercicio, rc.FechaRegistro as fecha, rut.NombreRutina as nombreRutina, " +
           "s.NombreSeccion as nombreSeccion, ej.NombreEjercicio as nombreEjercicio, " +
           "0 as numSerie, 0 as repeticiones, 0.0 as peso, 0 as esPesoCorporal, " +
           "rc.DuracionSegundos as duracionSegundos, rc.Distancia as distancia, rc.UnidadDistancia as unidadDistancia, " +
           "rc.CaloriasQuemadas as caloriasQuemadas, rc.RitmoCardiacoPromedio as ritmoCardiacoPromedio, " +
           "rc.VelocidadPromedio as velocidadPromedio, rc.CadenciaPromedio as cadenciaPromedio, " +
           "rc.Inclinacion as inclinacion, rc.NivelResistencia as nivelResistencia, rc.Notas as notas " +
           "FROM RegistroCardio rc " +
           "JOIN SeccionXejercicio sxe ON rc.IdSeccionXejercicio = sxe.IdSeccionXejercicio " +
           "JOIN Ejercicio ej ON sxe.IdEjercicio = ej.IdEjercicio " +
           "JOIN Seccion s ON sxe.IdSeccion = s.IdSeccion " +
           "JOIN Rutina rut ON s.IdRutinaSeccion = rut.IdRutina " +
           "WHERE rut.IdUsuarioRutina = :idUsuario " +
           "AND (:idRutina = -1 OR rut.IdRutina = :idRutina) " +
           "AND (:idSeccion = -1 OR s.IdSeccion = :idSeccion) " +
           "AND (:idEjercicio = -1 OR ej.IdEjercicio = :idEjercicio) " +
           "AND (:fechaDesde = -1 OR rc.FechaRegistro >= :fechaDesde) " +
           "AND (:fechaHasta = -1 OR rc.FechaRegistro <= :fechaHasta) " +
           "ORDER BY rc.FechaRegistro DESC " +
           "LIMIT :limit OFFSET :offset")
    List<RegistroDetallado> buscarRegistrosCardioDetalladosPaginado(int idUsuario, int idRutina, int idSeccion, int idEjercicio, long fechaDesde, long fechaHasta, int limit, int offset);

    @Query("SELECT 'CARDIO' as categoriaEjercicio, rc.FechaRegistro as fecha, rut.NombreRutina as nombreRutina, " +
           "s.NombreSeccion as nombreSeccion, ej.NombreEjercicio as nombreEjercicio, " +
           "0 as numSerie, 0 as repeticiones, 0.0 as peso, 0 as esPesoCorporal, " +
           "rc.DuracionSegundos as duracionSegundos, rc.Distancia as distancia, rc.UnidadDistancia as unidadDistancia, " +
           "rc.CaloriasQuemadas as caloriasQuemadas, rc.RitmoCardiacoPromedio as ritmoCardiacoPromedio, " +
           "rc.VelocidadPromedio as velocidadPromedio, rc.CadenciaPromedio as cadenciaPromedio, " +
           "rc.Inclinacion as inclinacion, rc.NivelResistencia as nivelResistencia, rc.Notas as notas " +
           "FROM RegistroCardio rc " +
           "JOIN SeccionXejercicio sxe ON rc.IdSeccionXejercicio = sxe.IdSeccionXejercicio " +
           "JOIN Ejercicio ej ON sxe.IdEjercicio = ej.IdEjercicio " +
           "JOIN Seccion s ON sxe.IdSeccion = s.IdSeccion " +
           "JOIN Rutina rut ON s.IdRutinaSeccion = rut.IdRutina " +
           "WHERE rut.IdUsuarioRutina = :idUsuario " +
           "AND (:idRutina = -1 OR rut.IdRutina = :idRutina) " +
           "AND (:idSeccion = -1 OR s.IdSeccion = :idSeccion) " +
           "AND (:idEjercicio = -1 OR ej.IdEjercicio = :idEjercicio) " +
           "AND (:fechaDesde = -1 OR rc.FechaRegistro >= :fechaDesde) " +
           "AND (:fechaHasta = -1 OR rc.FechaRegistro <= :fechaHasta) " +
           "ORDER BY rc.FechaRegistro DESC")
    List<RegistroDetallado> buscarRegistrosCardioDetallados(int idUsuario, int idRutina, int idSeccion, int idEjercicio, long fechaDesde, long fechaHasta);
}

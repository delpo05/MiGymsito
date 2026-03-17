package com.example.migymsito.dataDao;

import androidx.room.Dao;
import androidx.room.Query;

import com.example.migymsito.data.ProgresoData;

import java.util.List;

@Dao
public interface ProgresoDao {

    @Query("SELECT (Registro.FechaRegistro / 86400000) * 86400000 as fecha, MAX(Registro.PesoRegistro) as valor " +
           "FROM Registro " +
           "JOIN SeccionXejercicio ON Registro.IdSeccionXejercicio = SeccionXejercicio.IdSeccionXejercicio " +
           "JOIN Entrenamiento ON Registro.IdEntrenamiento = Entrenamiento.IdEntrenamiento " +
           "WHERE SeccionXejercicio.IdEjercicio = :idEjercicio AND Entrenamiento.IdUsuario = :idUsuario " +
           "GROUP BY (Registro.FechaRegistro / 86400000) " +
           "ORDER BY Registro.FechaRegistro ASC")
    List<ProgresoData> obtenerProgresoPesoMaximo(int idUsuario, int idEjercicio);

    @Query("SELECT SUM(Repeticiones) FROM Registro " +
           "JOIN SeccionXejercicio ON Registro.IdSeccionXejercicio = SeccionXejercicio.IdSeccionXejercicio " +
           "JOIN Entrenamiento ON Registro.IdEntrenamiento = Entrenamiento.IdEntrenamiento " +
           "WHERE SeccionXejercicio.IdEjercicio = :idEjercicio AND Entrenamiento.IdUsuario = :idUsuario")
    Integer obtenerTotalRepeticiones(int idUsuario, int idEjercicio);
}

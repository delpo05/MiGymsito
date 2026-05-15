package com.example.migymsito.dataDao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Delete;
import androidx.room.Update;
import com.example.migymsito.data.Rutina;

import java.util.List;

@Dao
public interface RutinaDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    long insertarRutina(Rutina rutina);

    @Update
    void actualizarRutina(Rutina rutina);

    @Delete
    void eliminarRutina(Rutina rutina);

    @Query("SELECT * FROM Rutina WHERE IdUsuarioRutina = :idUsuario")
    List<Rutina> obtenerRutinasPorUsuario(int idUsuario);

    @Query("SELECT * FROM Rutina WHERE IdRutina = :idRutina LIMIT 1")
    Rutina obtenerRutinaPorId(int idRutina);

    @Query("DELETE FROM Rutina")
    void borrarTodo();
}
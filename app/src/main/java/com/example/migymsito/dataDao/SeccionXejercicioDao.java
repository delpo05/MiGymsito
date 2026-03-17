package com.example.migymsito.dataDao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.migymsito.data.SeccionXejercicio;

import java.util.List;

@Dao
public interface SeccionXejercicioDao {
    @Insert
    long insert(SeccionXejercicio seccionXejercicio);

    @Update
    void update(SeccionXejercicio seccionXejercicio);

    @Delete
    void delete(SeccionXejercicio seccionXejercicio);

    @Query("SELECT * FROM SeccionXejercicio WHERE IdSeccion = :idSeccion")
    List<SeccionXejercicio> getEjerciciosBySeccion(int idSeccion);

    @Query("SELECT * FROM SeccionXejercicio WHERE IdSeccion = :idSeccion AND IdEjercicio = :idEjercicio LIMIT 1")
    SeccionXejercicio getRelacion(int idSeccion, int idEjercicio);
}

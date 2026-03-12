package com.example.migymsito.dataDao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Delete;
import androidx.room.Update;

import com.example.migymsito.data.Ejercicio;

// Se eliminó la importación de List ya que no se usa en este DAO.
// Este DAO ahora solo maneja operaciones CRUD básicas de la tabla Ejercicio.
@Dao
public interface EjercicioDao {

    // Devuelve long para obtener el ID generado (necesario para la tabla intermedia).
    @Insert
    long insertarEjercicio(Ejercicio ejercicio);

    @Update
    void actualizarEjercicio(Ejercicio ejercicio);

    @Delete
    void eliminarEjercicio(Ejercicio ejercicio);

    @Query("DELETE FROM Ejercicio")
    void borrarTodo();
}

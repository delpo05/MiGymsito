package com.example.migymsito.dataDao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.migymsito.data.Usuario;
import java.util.List;

@Dao
public interface UsuarioDao {

    @Insert
    long registrarUsuario(Usuario usuario);

    // MÉTODO AÑADIDO PARA SOLUCIONAR EL ERROR EN EL REPOSITORIO
    @Update
    void actualizarUsuario(Usuario usuario);

    @Query("SELECT * FROM Usuario WHERE correoElectronicoUsuario = :correo AND contraseniaUsuario = :password LIMIT 1")
    Usuario login(String correo, String password);

    @Query("SELECT * FROM Usuario WHERE correoElectronicoUsuario = :correo LIMIT 1")
    Usuario validarCorreoUsuario(String correo);

    @Query("SELECT * FROM Usuario WHERE nombreUsuario = :nombre LIMIT 1")
    Usuario validarNombreUsuario(String nombre);

    @Query("SELECT * FROM Usuario")
    List<Usuario> obtenerTodosLosUsuarios();

    @Query("DELETE FROM Usuario")
    void deleteAll();
}
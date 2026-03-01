package com.example.migymsito.dataDao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import com.example.migymsito.data.Usuario;

//Conexion con la base de datos !

@Dao
public interface UsuarioDao {

    // REGISTRAR USUARIO: Guarda al usuario con todos sus campos
    @Insert(onConflict = OnConflictStrategy.ABORT)
    void registrarUsuario(Usuario usuario);

    // LOGIN: Busca un usuario que coincida con el correo Y la contraseña.
    @Query("SELECT * FROM Usuario WHERE correoElectronicoUsuario = :correo AND contraseniaUsuario = :password LIMIT 1")
    Usuario login(String correo, String password);

    // VERIFICAR CORREO: Por si solo quieres saber si el correo ya existe antes de registrar.
    @Query("SELECT * FROM Usuario WHERE correoElectronicoUsuario = :correo LIMIT 1")
    Usuario validarCorreoUsuario(String correo);

    // VERIFICAR NOMBRE: Para saber si el nombre ya está ocupado antes de registrar
    @Query("SELECT * FROM Usuario WHERE nombreUsuario = :nombre LIMIT 1")
    Usuario validarNombreUsuario(String nombre);

    // BORRAR TODOS: Elimina todos los usuarios de la tabla.
    @Query("DELETE FROM Usuario")
    void deleteAll();
}
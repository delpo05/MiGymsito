package com.example.migymsito.dataDao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import com.example.migymsito.data.Usuario;

//Conexion con la base de datos ! aa aa aa  aa

@Dao
public interface UsuarioDao {

    // REGISTRAR USUARIO: Guarda al usuario con todos sus campos
    // En caso de existir un usuario con el mismo correo, no lo guarda, para esto usa el
    // onConflict ABORT (para no guardarlo) por el "unique" que pusiste.
    @Insert(onConflict = OnConflictStrategy.ABORT)
    void registrarUsuario(Usuario usuario);

    // 2. LOGIN: Busca un usuario que coincida con el correo Y la contraseña.
    // Si encuentra uno, te devuelve el objeto Usuario. Si no, devuelve null.
    @Query("SELECT * FROM Usuario WHERE correoElectronicoUsuario = :correo AND contraseniaUsuario = :password LIMIT 1")
    Usuario login(String correo, String password);

    // 3. VERIFICAR CORREO: Por si solo quieres saber si el correo ya existe antes de registrar.
    @Query("SELECT * FROM Usuario WHERE correoElectronicoUsuario = :correo LIMIT 1")
    Usuario validarCorreoUsuario(String correo);

    // 4. VERIFICAR NOMBRE: Para saber si el nombre ya está ocupado antes de registrar
    @Query("SELECT * FROM Usuario WHERE nombreUsuario = :nombre LIMIT 1")
    Usuario validarNombreUsuario(String nombre);

}
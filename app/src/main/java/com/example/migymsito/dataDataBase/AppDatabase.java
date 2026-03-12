package com.example.migymsito.dataDataBase;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.migymsito.data.Ejercicio;
import com.example.migymsito.data.Historial;
import com.example.migymsito.data.Registro;
import com.example.migymsito.data.Rutina;
import com.example.migymsito.data.Seccion;
import com.example.migymsito.data.SeccionesXEjercicios;
import com.example.migymsito.data.Usuario;
import com.example.migymsito.dataDao.EjercicioDao;
import com.example.migymsito.dataDao.HistorialDao;
import com.example.migymsito.dataDao.RegistroDao;
import com.example.migymsito.dataDao.RutinaDao;
import com.example.migymsito.dataDao.SeccionDao;
import com.example.migymsito.dataDao.SeccionesXEjerciciosDao;
import com.example.migymsito.dataDao.UsuarioDao;

// Subimos la versión a 8 para incluir la nueva tabla SeccionesXEjercicios
@Database(entities = {Usuario.class, Rutina.class, Seccion.class, Ejercicio.class, Registro.class, Historial.class, SeccionesXEjercicios.class}, version = 8, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    public abstract UsuarioDao usuarioDao();
    public abstract RutinaDao rutinaDao();
    public abstract SeccionDao seccionDao();
    public abstract EjercicioDao ejercicioDao();
    public abstract RegistroDao registroDao();
    public abstract HistorialDao historialDao();
    public abstract SeccionesXEjerciciosDao seccionesXEjerciciosDao(); // Agregado el nuevo DAO

    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "migymsito_db")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}

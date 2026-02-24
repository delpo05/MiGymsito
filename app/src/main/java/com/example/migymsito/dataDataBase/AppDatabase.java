package com.example.migymsito.dataDataBase;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.migymsito.data.Usuario;
import com.example.migymsito.dataDao.UsuarioDao;

// En "entities" agregamos las nuevas tablas (ej: Rutina.class)
@Database(entities = {Usuario.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {

    // Aquí declaras todos los DAOs de tu app
    public abstract UsuarioDao usuarioDao();

    // --- ESTO ES EL "SINGLETON" (Para que no se creen mil archivos de base de datos) ---
    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "migymsito_db")
                            .fallbackToDestructiveMigration() // Si cambias algo, borra y recrea la DB
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
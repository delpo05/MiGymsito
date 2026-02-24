package com.example.migymsito.dataDataBase;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

// Verifica que estos imports coincidan con tus nuevas carpetas
import com.example.migymsito.data.Usuario;
import com.example.migymsito.dataDao.UsuarioDao;

@Database(entities = {Usuario.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {

    public abstract UsuarioDao usuarioDao();

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
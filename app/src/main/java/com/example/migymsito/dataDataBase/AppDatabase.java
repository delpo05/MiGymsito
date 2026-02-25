package com.example.migymsito.dataDataBase;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.migymsito.data.Usuario;
import com.example.migymsito.dataDao.UsuarioDao;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {Usuario.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    public abstract UsuarioDao usuarioDao();

    private static volatile AppDatabase INSTANCE;

    private static final int NUMBER_OF_THREADS = 4;
    static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "migymsito_db")
                            .addCallback(sRoomDatabaseCallback)
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    private static final RoomDatabase.Callback sRoomDatabaseCallback = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);

            databaseWriteExecutor.execute(() -> {
                UsuarioDao dao = INSTANCE.usuarioDao();
                dao.deleteAll();

                Usuario usuario = new Usuario();
                usuario.nombreUsuario = "Test User";
                usuario.correoElectronicoUsuario = "test@test.com";
                usuario.contraseniaUsuario = "test";
                // Asigna valores por defecto para los campos no nulos
                usuario.fechaNacimiento = System.currentTimeMillis(); 
                usuario.generoUsuario = "No especificado"; 
                
                dao.registrarUsuario(usuario);
            });
        }
    };
}

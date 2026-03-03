package com.example.migymsito.dataDataBase;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.migymsito.data.Ejercicio;
import com.example.migymsito.data.Historial;
import com.example.migymsito.data.Registro;
import com.example.migymsito.data.Rutina;
import com.example.migymsito.data.Seccion;
import com.example.migymsito.data.Usuario;
import com.example.migymsito.dataDao.EjercicioDao;
import com.example.migymsito.dataDao.HistorialDao;
import com.example.migymsito.dataDao.RegistroDao;
import com.example.migymsito.dataDao.RutinaDao;
import com.example.migymsito.dataDao.SeccionDao;
import com.example.migymsito.dataDao.UsuarioDao;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {Usuario.class, Rutina.class, Seccion.class, Ejercicio.class, Registro.class, Historial.class}, version = 6, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    public abstract UsuarioDao usuarioDao();
    public abstract RutinaDao rutinaDao();
    public abstract SeccionDao seccionDao();
    public abstract EjercicioDao ejercicioDao();
    public abstract RegistroDao registroDao();
    public abstract HistorialDao historialDao();

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
                            .fallbackToDestructiveMigration()
                            .addCallback(sRoomDatabaseCallback)
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    private static final RoomDatabase.Callback sRoomDatabaseCallback = new RoomDatabase.Callback() {
        @Override
        public void onOpen(@NonNull SupportSQLiteDatabase db) {
            super.onOpen(db);

            // Usamos onOpen para que se asegure de insertar el usuario cada vez que se abre la DB durante las pruebas
            databaseWriteExecutor.execute(() -> {
                UsuarioDao dao = INSTANCE.usuarioDao();
                
                // Opcional: Limpiamos para evitar conflictos y asegurar que los datos de prueba sean correctos
                dao.deleteAll();

                Usuario usuario = new Usuario();
                usuario.nombreUsuario = "Test User";
                usuario.correoElectronicoUsuario = "test@test.com";
                usuario.contraseniaUsuario = "test";
                usuario.fechaNacimiento = System.currentTimeMillis(); 
                usuario.generoUsuario = "No especificado"; 
                
                dao.registrarUsuario(usuario);
            });
        }
    };
}

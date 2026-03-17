package com.example.migymsito.dataDataBase;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.migymsito.data.Ejercicio;
import com.example.migymsito.data.Entrenamiento;
import com.example.migymsito.data.Historial;
import com.example.migymsito.data.Registro;
import com.example.migymsito.data.Rutina;
import com.example.migymsito.data.Seccion;
import com.example.migymsito.data.SeccionXejercicio;
import com.example.migymsito.data.Usuario;
import com.example.migymsito.dataDao.EjercicioDao;
import com.example.migymsito.dataDao.EntrenamientoDao;
import com.example.migymsito.dataDao.HistorialDao;
import com.example.migymsito.dataDao.RegistroDao;
import com.example.migymsito.dataDao.RutinaDao;
import com.example.migymsito.dataDao.SeccionDao;
import com.example.migymsito.dataDao.SeccionXejercicioDao;
import com.example.migymsito.dataDao.UsuarioDao;

import java.util.concurrent.Executors;

@Database(entities = {
        Usuario.class,
        Rutina.class,
        Seccion.class,
        Ejercicio.class,
        Registro.class,
        Historial.class,
        Entrenamiento.class,
        SeccionXejercicio.class
}, version = 9, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    public abstract UsuarioDao usuarioDao();
    public abstract RutinaDao rutinaDao();
    public abstract SeccionDao seccionDao();
    public abstract EjercicioDao ejercicioDao();
    public abstract RegistroDao registroDao();
    public abstract HistorialDao historialDao();
    public abstract EntrenamientoDao entrenamientoDao();
    public abstract SeccionXejercicioDao seccionXejercicioDao();

    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "migymsito_db")
                            .addCallback(sRoomDatabaseCallback)
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    /**
     * Callback para inicializar la base de datos con ejercicios y secciones preestablecidas.
     * Se ejecuta cada vez que se abre la base de datos para asegurar que los datos existan.
     */
    private static final RoomDatabase.Callback sRoomDatabaseCallback = new RoomDatabase.Callback() {
        @Override
        public void onOpen(@NonNull SupportSQLiteDatabase db) {
            super.onOpen(db);
            Executors.newSingleThreadExecutor().execute(() -> {
                AppDatabase database = INSTANCE;
                if (database != null) {
                    inicializarDatosPreestablecidos(database);
                }
            });
        }
    };

    /**
     * Inserta las secciones y ejercicios preestablecidos si no existen en la base de datos.
     */
    private static void inicializarDatosPreestablecidos(AppDatabase db) {
        if (db.seccionDao().obtenerSeccionesPreestablecidas().isEmpty()) {
            
            // Sección Pecho
            long idPecho = insertarSeccionPreestablecida(db, "Pecho");
            insertarEjercicioPreestablecido(db, "Preestablecido", "Press de banca", idPecho);
            insertarEjercicioPreestablecido(db, "Preestablecido", "Apertura de pecho", idPecho);

            // Sección Espalda
            long idEspalda = insertarSeccionPreestablecida(db, "Espalda");
            insertarEjercicioPreestablecido(db, "Preestablecido", "Dominada", idEspalda);
            insertarEjercicioPreestablecido(db, "Preestablecido", "Remo con barra", idEspalda);

            // Sección Bicep
            long idBicep = insertarSeccionPreestablecida(db, "Bicep");
            insertarEjercicioPreestablecido(db, "Preestablecido", "Curl de bicep con barra", idBicep);

            // Sección Tricep
            long idTricep = insertarSeccionPreestablecida(db, "Tricep");
            insertarEjercicioPreestablecido(db, "Preestablecido", "Extensión de tricep", idTricep);
        }
    }

    private static long insertarSeccionPreestablecida(AppDatabase db, String nombre) {
        Seccion s = new Seccion();
        s.NombreSeccion = nombre;
        s.EsPreestablecido = true;
        s.IdRutinaSeccion = null; // No pertenece a ninguna rutina de usuario inicial
        return db.seccionDao().insertarSeccion(s);
    }

    private static void insertarEjercicioPreestablecido(AppDatabase db, String tipo, String nombre, long idSeccion) {
        Ejercicio e = new Ejercicio();
        e.TipoEjercicio = tipo;
        e.NombreEjercicio = nombre;
        e.EsPreestablecido = true;
        e.PesoCorporalEjercicio = false;
        long idEjercicio = db.ejercicioDao().insertarEjercicio(e);

        SeccionXejercicio sxe = new SeccionXejercicio();
        sxe.IdSeccion = (int) idSeccion;
        sxe.IdEjercicio = (int) idEjercicio;
        db.seccionXejercicioDao().insert(sxe);
    }
}

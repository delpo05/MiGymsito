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
}, version = 12, exportSchema = false)
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
     * Utiliza los DAOs correspondientes para realizar las inserciones.
     */
    private static void inicializarDatosPreestablecidos(AppDatabase db) {
        SeccionDao seccionDao = db.seccionDao();
        EjercicioDao ejercicioDao = db.ejercicioDao();
        SeccionXejercicioDao sxeDao = db.seccionXejercicioDao();

        if (seccionDao.obtenerSeccionesPreestablecidas().isEmpty()) {

            // ================= PECHO =================
            long idPecho = insertarSeccion(seccionDao, "Pecho");
            insertarEjercicio(ejercicioDao, sxeDao, "Press de banca", idPecho);
            insertarEjercicio(ejercicioDao, sxeDao, "Press inclinado con barra", idPecho);
            insertarEjercicio(ejercicioDao, sxeDao, "Press inclinado con mancuernas", idPecho);
            insertarEjercicio(ejercicioDao, sxeDao, "Aperturas con mancuernas", idPecho);
            insertarEjercicio(ejercicioDao, sxeDao, "Aperturas en máquina", idPecho);
            insertarEjercicio(ejercicioDao, sxeDao, "Cruce de poleas", idPecho);
            insertarEjercicio(ejercicioDao, sxeDao, "Fondos en paralelas (pecho)", idPecho);

// ================= ESPALDA =================
            long idEspalda = insertarSeccion(seccionDao, "Espalda");
            insertarEjercicio(ejercicioDao, sxeDao, "Dominadas", idEspalda);
            insertarEjercicio(ejercicioDao, sxeDao, "Jalón al pecho", idEspalda);
            insertarEjercicio(ejercicioDao, sxeDao, "Remo con barra", idEspalda);
            insertarEjercicio(ejercicioDao, sxeDao, "Remo con mancuernas", idEspalda);
            insertarEjercicio(ejercicioDao, sxeDao, "Remo en máquina", idEspalda);
            insertarEjercicio(ejercicioDao, sxeDao, "Peso muerto", idEspalda);
            insertarEjercicio(ejercicioDao, sxeDao, "Pullover con mancuerna", idEspalda);

// ================= BICEPS =================
            long idBicep = insertarSeccion(seccionDao, "Biceps");
            insertarEjercicio(ejercicioDao, sxeDao, "Curl con barra", idBicep);
            insertarEjercicio(ejercicioDao, sxeDao, "Curl con mancuernas", idBicep);
            insertarEjercicio(ejercicioDao, sxeDao, "Curl alternado", idBicep);
            insertarEjercicio(ejercicioDao, sxeDao, "Curl martillo", idBicep);
            insertarEjercicio(ejercicioDao, sxeDao, "Curl concentrado", idBicep);
            insertarEjercicio(ejercicioDao, sxeDao, "Curl en banco inclinado", idBicep);

// ================= TRICEPS =================
            long idTricep = insertarSeccion(seccionDao, "Triceps");
            insertarEjercicio(ejercicioDao, sxeDao, "Extensión en polea", idTricep);
            insertarEjercicio(ejercicioDao, sxeDao, "Fondos en paralelas (triceps)", idTricep);
            insertarEjercicio(ejercicioDao, sxeDao, "Press cerrado", idTricep);
            insertarEjercicio(ejercicioDao, sxeDao, "Patada de triceps", idTricep);
            insertarEjercicio(ejercicioDao, sxeDao, "Extensión con mancuerna", idTricep);

// ================= HOMBROS =================
            long idHombros = insertarSeccion(seccionDao, "Hombros");
            insertarEjercicio(ejercicioDao, sxeDao, "Press militar", idHombros);
            insertarEjercicio(ejercicioDao, sxeDao, "Elevaciones laterales", idHombros);
            insertarEjercicio(ejercicioDao, sxeDao, "Elevaciones frontales", idHombros);
            insertarEjercicio(ejercicioDao, sxeDao, "Pájaros", idHombros);
            insertarEjercicio(ejercicioDao, sxeDao, "Face pull", idHombros);
            insertarEjercicio(ejercicioDao, sxeDao, "Press con mancuernas", idHombros);

// ================= PIERNAS =================
            long idPiernas = insertarSeccion(seccionDao, "Piernas");
            insertarEjercicio(ejercicioDao, sxeDao, "Sentadilla", idPiernas);
            insertarEjercicio(ejercicioDao, sxeDao, "Prensa", idPiernas);
            insertarEjercicio(ejercicioDao, sxeDao, "Zancadas", idPiernas);
            insertarEjercicio(ejercicioDao, sxeDao, "Peso muerto rumano", idPiernas);
            insertarEjercicio(ejercicioDao, sxeDao, "Extensión de cuadriceps", idPiernas);
            insertarEjercicio(ejercicioDao, sxeDao, "Curl femoral", idPiernas);
            insertarEjercicio(ejercicioDao, sxeDao, "Hip thrust", idPiernas);

// ================= ABDOMEN =================
            long idAbdomen = insertarSeccion(seccionDao, "Abdomen");
            insertarEjercicio(ejercicioDao, sxeDao, "Crunch", idAbdomen);
            insertarEjercicio(ejercicioDao, sxeDao, "Crunch en máquina", idAbdomen);
            insertarEjercicio(ejercicioDao, sxeDao, "Elevaciones de piernas", idAbdomen);
            insertarEjercicio(ejercicioDao, sxeDao, "Plancha", idAbdomen);
            insertarEjercicio(ejercicioDao, sxeDao, "Russian twist", idAbdomen);
            insertarEjercicio(ejercicioDao, sxeDao, "Bicicleta abdominal", idAbdomen);

        }
    }

    /**
     * Método auxiliar para insertar una sección preestablecida usando el DAO.
     */
    private static long insertarSeccion(SeccionDao dao, String nombre) {
        Seccion s = new Seccion();
        s.NombreSeccion = nombre;
        s.TipoSeccion = "Preestablecido";
        s.IdRutinaSeccion = null;
        return dao.insertarSeccion(s);
    }

    /**
     * Método auxiliar para insertar un ejercicio preestablecido y su relación usando los DAOs.
     */
    private static void insertarEjercicio(EjercicioDao ejDao, SeccionXejercicioDao sxeDao, String nombre, long idSeccion) {
        Ejercicio e = new Ejercicio();
        e.TipoEjercicio = "Preestablecido";
        e.NombreEjercicio = nombre;
        e.PesoCorporalEjercicio = false;
        long idEjercicio = ejDao.insertarEjercicio(e);

        SeccionXejercicio sxe = new SeccionXejercicio();
        sxe.IdSeccion = (int) idSeccion;
        sxe.IdEjercicio = (int) idEjercicio;
        sxeDao.insert(sxe);
    }
}

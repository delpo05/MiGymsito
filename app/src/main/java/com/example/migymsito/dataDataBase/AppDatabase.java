package com.example.migymsito.dataDataBase;

import android.content.Context;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.migymsito.data.Ejercicio;
import com.example.migymsito.data.Entrenamiento;
import com.example.migymsito.data.Historial;
import com.example.migymsito.data.Registro;
import com.example.migymsito.data.RegistroCardio;
import com.example.migymsito.data.Rutina;
import com.example.migymsito.data.Seccion;
import com.example.migymsito.data.SeccionXejercicio;
import com.example.migymsito.data.Usuario;
import com.example.migymsito.dataDao.EjercicioDao;
import com.example.migymsito.dataDao.EntrenamientoDao;
import com.example.migymsito.dataDao.HistorialDao;
import com.example.migymsito.dataDao.RegistroDao;
import com.example.migymsito.dataDao.RegistroCardioDao;
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
        RegistroCardio.class,
        Historial.class,
        Entrenamiento.class,
        SeccionXejercicio.class
}, version = 19, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    public abstract UsuarioDao usuarioDao();
    public abstract RutinaDao rutinaDao();
    public abstract SeccionDao seccionDao();
    public abstract EjercicioDao ejercicioDao();
    public abstract RegistroDao registroDao();
    public abstract RegistroCardioDao registroCardioDao();
    public abstract HistorialDao historialDao();
    public abstract EntrenamientoDao entrenamientoDao();
    public abstract SeccionXejercicioDao seccionXejercicioDao();

    private static volatile AppDatabase INSTANCE;

    public static final Migration MIGRATION_14_15 = new Migration(14, 15) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE Registro ADD COLUMN UnidadPeso TEXT DEFAULT 'kg'");
        }
    };

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "migymsito_db")
                            .addMigrations(MIGRATION_14_15)
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

        // ================= PECHO =================
        long idPecho = obtenerOInsertarSeccion(seccionDao, "Pecho");
        insertarEjercicioFuerza(ejercicioDao, sxeDao, "Press de banca", idPecho, false, false, "Olímpica (20kg)", 20f, "gif_press_banca");
        insertarEjercicioFuerza(ejercicioDao, sxeDao, "Press inclinado con barra", idPecho, false, false, "Olímpica (20kg)", 20f, "gif_press_inclinado_barra");
        insertarEjercicioFuerza(ejercicioDao, sxeDao, "Press inclinado con mancuernas", idPecho, false, true, null, null, "gif_press_inclinado_mancuernas");
        insertarEjercicioFuerza(ejercicioDao, sxeDao, "Press declinado con barra", idPecho, false, false, "Olímpica (20kg)", 20f, "gif_press_declinado");
        insertarEjercicioFuerza(ejercicioDao, sxeDao, "Aperturas con mancuernas", idPecho, false, true, null, null, "gif_aperturas_mancuernas");
        insertarEjercicioFuerza(ejercicioDao, sxeDao, "Aperturas en máquina (Peck Deck)", idPecho, false, false, null, null, "gif_peck_deck");
        insertarEjercicioFuerza(ejercicioDao, sxeDao, "Cruce de poleas", idPecho, false, true, null, null, "gif_cruce_poleas");
        insertarEjercicioFuerza(ejercicioDao, sxeDao, "Fondos en paralelas (pecho)", idPecho, true, false, null, null, "gif_fondos_pecho");
        insertarEjercicioFuerza(ejercicioDao, sxeDao, "Flexiones / Push-ups", idPecho, true, false, null, null, "gif_flexiones");

        // ================= ESPALDA =================
        long idEspalda = obtenerOInsertarSeccion(seccionDao, "Espalda");
        insertarEjercicioFuerza(ejercicioDao, sxeDao, "Dominadas", idEspalda, true, false, null, null, "gif_dominadas");
        insertarEjercicioFuerza(ejercicioDao, sxeDao, "Jalón al pecho", idEspalda, false, false, null, null, "gif_jalon_pecho");
        insertarEjercicioFuerza(ejercicioDao, sxeDao, "Jalón agarre estrecho / neutro", idEspalda, false, false, null, null, "gif_jalon_estrecho");
        insertarEjercicioFuerza(ejercicioDao, sxeDao, "Remo con barra", idEspalda, false, false, "Olímpica (20kg)", 20f, "gif_remo_barra");
        insertarEjercicioFuerza(ejercicioDao, sxeDao, "Remo con mancuerna a una mano", idEspalda, false, true, null, null, "gif_remo_mancuerna");
        insertarEjercicioFuerza(ejercicioDao, sxeDao, "Remo T / Remo apoyado", idEspalda, false, false, null, null, "gif_remo_t");
        insertarEjercicioFuerza(ejercicioDao, sxeDao, "Remo en máquina", idEspalda, false, false, null, null, "gif_remo_maquina");
        insertarEjercicioFuerza(ejercicioDao, sxeDao, "Peso muerto convencional", idEspalda, false, false, "Olímpica (20kg)", 20f, "gif_peso_muerto");
        insertarEjercicioFuerza(ejercicioDao, sxeDao, "Pullover con mancuerna", idEspalda, false, false, null, null, "gif_pullover");
        insertarEjercicioFuerza(ejercicioDao, sxeDao, "Extensión lumbar / Hiperextensiones", idEspalda, true, false, null, null, "gif_hiperextensiones");

        // ================= HOMBROS =================
        long idHombros = obtenerOInsertarSeccion(seccionDao, "Hombros");
        insertarEjercicioFuerza(ejercicioDao, sxeDao, "Press militar con barra", idHombros, false, false, "Olímpica (20kg)", 20f, "gif_press_militar");
        insertarEjercicioFuerza(ejercicioDao, sxeDao, "Press de hombros con mancuernas", idHombros, false, true, null, null, "gif_press_hombros_mancuernas");
        insertarEjercicioFuerza(ejercicioDao, sxeDao, "Press Arnold", idHombros, false, true, null, null, "gif_press_arnold");
        insertarEjercicioFuerza(ejercicioDao, sxeDao, "Elevaciones laterales", idHombros, false, true, null, null, "gif_elevaciones_laterales");
        insertarEjercicioFuerza(ejercicioDao, sxeDao, "Elevaciones laterales en polea", idHombros, false, true, null, null, "gif_elevaciones_laterales_polea");
        insertarEjercicioFuerza(ejercicioDao, sxeDao, "Elevaciones frontales", idHombros, false, true, null, null, "gif_elevaciones_frontales");
        insertarEjercicioFuerza(ejercicioDao, sxeDao, "Pájaros / Deltoides posterior", idHombros, false, true, null, null, "gif_pajaros");
        insertarEjercicioFuerza(ejercicioDao, sxeDao, "Face pull", idHombros, false, false, null, null, "gif_face_pull");
        insertarEjercicioFuerza(ejercicioDao, sxeDao, "Encogimientos de hombros (Trapecio)", idHombros, false, true, null, null, "gif_encogimientos");

        // ================= BÍCEPS =================
        long idBicep = obtenerOInsertarSeccion(seccionDao, "Bíceps");
        insertarEjercicioFuerza(ejercicioDao, sxeDao, "Curl con barra recta", idBicep, false, false, "Estándar (10kg)", 10f, "gif_curl_barra");
        insertarEjercicioFuerza(ejercicioDao, sxeDao, "Curl con barra Z", idBicep, false, false, "Z (10kg)", 10f, "gif_curl_barra_z");
        insertarEjercicioFuerza(ejercicioDao, sxeDao, "Curl con mancuernas", idBicep, false, true, null, null, "gif_curl_mancuernas");
        insertarEjercicioFuerza(ejercicioDao, sxeDao, "Curl alternado", idBicep, false, true, null, null, "gif_curl_alternado");
        insertarEjercicioFuerza(ejercicioDao, sxeDao, "Curl martillo", idBicep, false, true, null, null, "gif_curl_martillo");
        insertarEjercicioFuerza(ejercicioDao, sxeDao, "Curl concentrado", idBicep, false, false, null, null, "gif_curl_concentrado");
        insertarEjercicioFuerza(ejercicioDao, sxeDao, "Curl en banco Scott / Predicador", idBicep, false, false, "Z (10kg)", 10f, "gif_curl_scott");
        insertarEjercicioFuerza(ejercicioDao, sxeDao, "Curl en banco inclinado", idBicep, false, true, null, null, "gif_curl_inclinado");
        insertarEjercicioFuerza(ejercicioDao, sxeDao, "Curl en polea baja", idBicep, false, false, null, null, "gif_curl_polea");

        // ================= TRÍCEPS =================
        long idTricep = obtenerOInsertarSeccion(seccionDao, "Tríceps");
        insertarEjercicioFuerza(ejercicioDao, sxeDao, "Extensión en polea con cuerda", idTricep, false, false, null, null, "gif_extension_cuerda");
        insertarEjercicioFuerza(ejercicioDao, sxeDao, "Extensión en polea con barra V", idTricep, false, false, null, null, "gif_extension_barra_v");
        insertarEjercicioFuerza(ejercicioDao, sxeDao, "Press cerrado", idTricep, false, false, "Olímpica (20kg)", 20f, "gif_press_cerrado");
        insertarEjercicioFuerza(ejercicioDao, sxeDao, "Fondos en paralelas (tríceps)", idTricep, true, false, null, null, "gif_fondos_triceps");
        insertarEjercicioFuerza(ejercicioDao, sxeDao, "Fondos entre bancos", idTricep, true, false, null, null, "gif_fondos_bancos");
        insertarEjercicioFuerza(ejercicioDao, sxeDao, "Patada de tríceps", idTricep, false, true, null, null, "gif_patada_triceps");
        insertarEjercicioFuerza(ejercicioDao, sxeDao, "Extensión copa a dos manos", idTricep, false, false, null, null, "gif_extension_copa");
        insertarEjercicioFuerza(ejercicioDao, sxeDao, "Press francés / Extensión tras nuca", idTricep, false, false, "Z (10kg)", 10f, "gif_press_frances");

        // ================= ANTEBRAZOS =================
        long idAntebrazos = obtenerOInsertarSeccion(seccionDao, "Antebrazos");
        insertarEjercicioFuerza(ejercicioDao, sxeDao, "Curl de antebrazo con barra", idAntebrazos, false, false, "Estándar (10kg)", 10f, "gif_curl_antebrazo");
        insertarEjercicioFuerza(ejercicioDao, sxeDao, "Curl inverso con barra", idAntebrazos, false, false, "Estándar (10kg)", 10f, "gif_curl_inverso");
        insertarEjercicioFuerza(ejercicioDao, sxeDao, "Paseo del granjero con mancuernas", idAntebrazos, false, true, null, null, "gif_paseo_granjero");

        // ================= CUÁDRICEPS =================
        long idCuadriceps = obtenerOInsertarSeccion(seccionDao, "Cuádriceps");
        insertarEjercicioFuerza(ejercicioDao, sxeDao, "Sentadilla libre", idCuadriceps, false, false, "Olímpica (20kg)", 20f, "gif_sentadilla");
        insertarEjercicioFuerza(ejercicioDao, sxeDao, "Sentadilla frontal", idCuadriceps, false, false, "Olímpica (20kg)", 20f, "gif_sentadilla_frontal");
        insertarEjercicioFuerza(ejercicioDao, sxeDao, "Sentadilla Hack", idCuadriceps, false, false, null, null, "gif_sentadilla_hack");
        insertarEjercicioFuerza(ejercicioDao, sxeDao, "Prensa de piernas 45°", idCuadriceps, false, false, null, null, "gif_prensa");
        insertarEjercicioFuerza(ejercicioDao, sxeDao, "Zancadas / Lunges", idCuadriceps, false, true, null, null, "gif_zancadas");
        insertarEjercicioFuerza(ejercicioDao, sxeDao, "Sentadilla Búlgara", idCuadriceps, false, true, null, null, "gif_sentadilla_bulgara");
        insertarEjercicioFuerza(ejercicioDao, sxeDao, "Extensión de cuadríiceps", idCuadriceps, false, false, null, null, "gif_extension_cuadriceps");

        // ================= ISQUIOTIBIALES =================
        long idIsquiotibiales = obtenerOInsertarSeccion(seccionDao, "Isquiotibiales");
        insertarEjercicioFuerza(ejercicioDao, sxeDao, "Peso muerto rumano con barra", idIsquiotibiales, false, false, "Olímpica (20kg)", 20f, "gif_peso_muerto_rumano");
        insertarEjercicioFuerza(ejercicioDao, sxeDao, "Peso muerto rumano con mancuernas", idIsquiotibiales, false, true, null, null, "gif_peso_muerto_rumano_mancuernas");
        insertarEjercicioFuerza(ejercicioDao, sxeDao, "Curl femoral tumbado", idIsquiotibiales, false, false, null, null, "gif_curl_femoral_tumbado");
        insertarEjercicioFuerza(ejercicioDao, sxeDao, "Curl femoral sentado", idIsquiotibiales, false, false, null, null, "gif_curl_femoral_sentado");
        insertarEjercicioFuerza(ejercicioDao, sxeDao, "Buenos días con barra", idIsquiotibiales, false, false, "Estándar (10kg)", 10f, "gif_buenos_dias");

        // ================= GLÚTEOS =================
        long idGluteos = obtenerOInsertarSeccion(seccionDao, "Glúteos");
        insertarEjercicioFuerza(ejercicioDao, sxeDao, "Hip thrust con barra", idGluteos, false, false, "Olímpica (20kg)", 20f, "gif_hip_thrust");
        insertarEjercicioFuerza(ejercicioDao, sxeDao, "Puente de glúteos en suelo", idGluteos, true, false, null, null, "gif_puente_gluteos");
        insertarEjercicioFuerza(ejercicioDao, sxeDao, "Patada de glúteo en polea", idGluteos, false, true, null, null, "gif_patada_gluteo");
        insertarEjercicioFuerza(ejercicioDao, sxeDao, "Abducción de cadera en máquina", idGluteos, false, false, null, null, "gif_abduccion_cadera");
        insertarEjercicioFuerza(ejercicioDao, sxeDao, "Subidas al banco con mancuernas", idGluteos, false, true, null, null, "gif_subidas_banco");

        // ================= GEMELOS =================
        long idGemelos = obtenerOInsertarSeccion(seccionDao, "Gemelos");
        insertarEjercicioFuerza(ejercicioDao, sxeDao, "Elevación de talones de pie", idGemelos, false, false, null, null, "gif_gemelos_pie");
        insertarEjercicioFuerza(ejercicioDao, sxeDao, "Elevación de talones sentado", idGemelos, false, false, null, null, "gif_gemelos_sentado");
        insertarEjercicioFuerza(ejercicioDao, sxeDao, "Elevación de talones en prensa", idGemelos, false, false, null, null, "gif_gemelos_prensa");

        // ================= ABDOMEN =================
        long idAbdomen = obtenerOInsertarSeccion(seccionDao, "Abdomen");
        insertarEjercicioFuerza(ejercicioDao, sxeDao, "Crunch abdominal", idAbdomen, true, false, null, null, "gif_crunch");
        insertarEjercicioFuerza(ejercicioDao, sxeDao, "Crunch en máquina / polea", idAbdomen, false, false, null, null, "gif_crunch_maquina");
        insertarEjercicioFuerza(ejercicioDao, sxeDao, "Elevaciones de piernas colgado", idAbdomen, true, false, null, null, "gif_elevaciones_piernas");
        insertarEjercicioFuerza(ejercicioDao, sxeDao, "Elevaciones de piernas en suelo", idAbdomen, true, false, null, null, "gif_elevaciones_piernas_suelo");
        insertarEjercicioFuerza(ejercicioDao, sxeDao, "Plancha isométrica", idAbdomen, true, false, null, null, "gif_plancha");
        insertarEjercicioFuerza(ejercicioDao, sxeDao, "Rueda abdominal / Ab Wheel", idAbdomen, true, false, null, null, "gif_rueda_abdominal");
        insertarEjercicioFuerza(ejercicioDao, sxeDao, "Giros rusos / Russian twist", idAbdomen, true, false, null, null, "gif_russian_twist");
        insertarEjercicioFuerza(ejercicioDao, sxeDao, "Bicicleta abdominal", idAbdomen, true, false, null, null, "gif_bicicleta_abdominal");

        // ================= CARDIO =================
        long idCardio = obtenerOInsertarSeccion(seccionDao, "Cardio");
        insertarEjercicioCardio(ejercicioDao, sxeDao, "Cinta de correr", idCardio, true, true, false, true, true, false, "gif_cinta_correr");
        insertarEjercicioCardio(ejercicioDao, sxeDao, "Bicicleta estática", idCardio, true, true, true, false, false, true, "gif_bicicleta_estatica");
        insertarEjercicioCardio(ejercicioDao, sxeDao, "Elíptica", idCardio, true, true, false, false, false, true, "gif_eliptica");
        insertarEjercicioCardio(ejercicioDao, sxeDao, "Remo ergómetro", idCardio, true, true, false, true, false, true, "gif_remo_ergometro");
        insertarEjercicioCardio(ejercicioDao, sxeDao, "Escaladora / Stairmaster", idCardio, true, true, false, false, false, true, "gif_escaladora");
        insertarEjercicioCardio(ejercicioDao, sxeDao, "Salto con cuerda", idCardio, false, true, false, false, false, false, "gif_salto_cuerda");

        // ================= FULL BODY / CALENTAMIENTO =================
        long idFullBody = obtenerOInsertarSeccion(seccionDao, "Full Body / Calentamiento");
        insertarEjercicioFuerza(ejercicioDao, sxeDao, "Burpees", idFullBody, true, false, null, null, "gif_burpees");
        insertarEjercicioCardio(ejercicioDao, sxeDao, "Jumping Jacks", idFullBody, false, true, false, false, false, false, "gif_jumping_jacks");
        insertarEjercicioFuerza(ejercicioDao, sxeDao, "Escaladores / Mountain Climbers", idFullBody, true, false, null, null, "gif_mountain_climbers");
        insertarEjercicioFuerza(ejercicioDao, sxeDao, "Swing con Pesa Rusa", idFullBody, false, false, null, null, "gif_kettlebell_swing");
    }

    /**
     * Método auxiliar para obtener o insertar una sección preestablecida usando el DAO.
     */
    private static long obtenerOInsertarSeccion(SeccionDao dao, String nombre) {
        Seccion existente = dao.obtenerSeccionPreestablecidaPorNombre(nombre);
        if (existente != null) {
            return existente.IdSeccion;
        }
        Seccion s = new Seccion();
        s.NombreSeccion = nombre;
        s.TipoSeccion = "Preestablecido";
        s.IdRutinaSeccion = null;
        return dao.insertarSeccion(s);
    }

    /**
     * Método auxiliar para insertar un ejercicio de Fuerza preestablecido y su relación si no existe.
     */
    private static void insertarEjercicioFuerza(
            EjercicioDao ejDao, SeccionXejercicioDao sxeDao, String nombre, long idSeccion,
            boolean pesoCorporal, Boolean pesoPorLado, String tipoBarra, Float pesoBarra, String imagen) {

        Ejercicio e = ejDao.obtenerEjercicioPorNombre(nombre);
        long idEjercicio;
        if (e == null) {
            e = new Ejercicio();
            e.TipoEjercicio = "Preestablecido";
            e.NombreEjercicio = nombre;
            e.CategoriaEjercicio = "FUERZA";
            e.PesoCorporalEjercicio = pesoCorporal;
            e.PesoPorLado = pesoPorLado;
            e.TipoDeBarra = tipoBarra;
            e.PesoBarra = pesoBarra;
            e.ImagenEjercicio = imagen;
            idEjercicio = ejDao.insertarEjercicio(e);
        } else {
            idEjercicio = e.IdEjercicio;
        }

        if (sxeDao.getRelacion((int) idSeccion, (int) idEjercicio) == null) {
            SeccionXejercicio sxe = new SeccionXejercicio();
            sxe.IdSeccion = (int) idSeccion;
            sxe.IdEjercicio = (int) idEjercicio;
            sxeDao.insert(sxe);
        }
    }

    /**
     * Método auxiliar para insertar un ejercicio de Cardio preestablecido y su relación si no existe.
     */
    private static void insertarEjercicioCardio(
            EjercicioDao ejDao, SeccionXejercicioDao sxeDao, String nombre, long idSeccion,
            boolean distancia, boolean calorias, boolean cadencia, boolean ritmo, boolean inclinacion, boolean resistencia, String imagen) {

        Ejercicio e = ejDao.obtenerEjercicioPorNombre(nombre);
        long idEjercicio;
        if (e == null) {
            e = new Ejercicio();
            e.TipoEjercicio = "Preestablecido";
            e.NombreEjercicio = nombre;
            e.CategoriaEjercicio = "CARDIO";
            e.PesoCorporalEjercicio = true;
            e.registraDistancia = distancia;
            e.registraCalorias = calorias;
            e.registraCadencia = cadencia;
            e.registraRitmo = ritmo;
            e.registraInclinacion = inclinacion;
            e.registraResistencia = resistencia;
            e.ImagenEjercicio = imagen;
            idEjercicio = ejDao.insertarEjercicio(e);
        } else {
            idEjercicio = e.IdEjercicio;
        }

        if (sxeDao.getRelacion((int) idSeccion, (int) idEjercicio) == null) {
            SeccionXejercicio sxe = new SeccionXejercicio();
            sxe.IdSeccion = (int) idSeccion;
            sxe.IdEjercicio = (int) idEjercicio;
            sxeDao.insert(sxe);
        }
    }
}

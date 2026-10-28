package com.example.migymsito.utils;

import android.content.Context;
import android.net.Uri;
import android.util.Base64;
import android.util.Log;

import androidx.core.content.FileProvider;

import com.example.migymsito.data.*;
import com.example.migymsito.dataDataBase.AppDatabase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BackupManager {

    private static final String TAG = "BackupManager";
    private final Context context;
    private final ExecutorService executorService;

    public interface BackupCallback {
        void onComplete(boolean success, Uri fileUri);
        void onError(String message);
    }

    public interface ImportCallback {
        void onComplete(boolean success);
        void onError(String message);
    }

    public BackupManager(Context context) {
        this.context = context.getApplicationContext();
        this.executorService = Executors.newSingleThreadExecutor();
    }

    public void exportFullBackup(BackupCallback callback) {
        executorService.execute(() -> {
            try {
                AppDatabase db = AppDatabase.getDatabase(context);
                JSONObject backup = new JSONObject();

                // 1. Usuarios
                JSONArray usersArray = new JSONArray();
                List<Usuario> usuarios = db.usuarioDao().obtenerTodosLosUsuarios();
                for (Usuario u : usuarios) {
                    JSONObject ju = new JSONObject();
                    ju.put("IdUsuario", u.IdUsuario);
                    ju.put("NombreUsuario", u.NombreUsuario);
                    ju.put("FechaNacimientoUsuario", u.FechaNacimientoUsuario);
                    ju.put("CorreoElectronicoUsuario", u.CorreoElectronicoUsuario);
                    ju.put("GeneroUsuario", u.GeneroUsuario);
                    usersArray.put(ju);
                }
                backup.put("usuarios", usersArray);

                // 2. Rutinas
                JSONArray rutinasArray = new JSONArray();
                List<Rutina> rutinas = db.rutinaDao().obtenerTodasLasRutinas();
                for (Rutina r : rutinas) {
                    JSONObject jr = new JSONObject();
                    jr.put("IdRutina", r.IdRutina);
                    jr.put("NombreRutina", r.NombreRutina);
                    jr.put("IdUsuarioRutina", r.IdUsuarioRutina);
                    rutinasArray.put(jr);
                }
                backup.put("rutinas", rutinasArray);

                // 3. Secciones
                JSONArray seccionesArray = new JSONArray();
                List<Seccion> secciones = db.seccionDao().obtenerTodasLasSecciones();
                for (Seccion s : secciones) {
                    JSONObject js = new JSONObject();
                    js.put("IdSeccion", s.IdSeccion);
                    js.put("NombreSeccion", s.NombreSeccion);
                    js.put("TipoSeccion", s.TipoSeccion);
                    js.put("IdRutinaSeccion", s.IdRutinaSeccion);
                    seccionesArray.put(js);
                }
                backup.put("secciones", seccionesArray);

                // 4. Ejercicios
                JSONArray ejerciciosArray = new JSONArray();
                List<Ejercicio> ejercicios = db.ejercicioDao().obtenerTodosLosEjercicios();
                for (Ejercicio e : ejercicios) {
                    JSONObject je = new JSONObject();
                    je.put("IdEjercicio", e.IdEjercicio);
                    je.put("TipoEjercicio", e.TipoEjercicio);
                    je.put("NombreEjercicio", e.NombreEjercicio);
                    je.put("CategoriaEjercicio", e.CategoriaEjercicio);
                    je.put("PesoCorporalEjercicio", e.PesoCorporalEjercicio);
                    je.put("PesoPorLado", e.PesoPorLado != null ? e.PesoPorLado : false);
                    je.put("TipoDeBarra", e.TipoDeBarra);
                    je.put("PesoBarra", e.PesoBarra != null ? e.PesoBarra : 0.0f);
                    
                    // Configuración Cardio
                    je.put("registraDistancia", e.registraDistancia != null ? e.registraDistancia : true);
                    je.put("registraCalorias", e.registraCalorias != null ? e.registraCalorias : true);
                    je.put("registraCadencia", e.registraCadencia != null ? e.registraCadencia : false);
                    je.put("registraRitmo", e.registraRitmo != null ? e.registraRitmo : false);
                    je.put("registraInclinacion", e.registraInclinacion != null ? e.registraInclinacion : false);
                    je.put("registraResistencia", e.registraResistencia != null ? e.registraResistencia : false);

                    if (e.ImagenEjercicio != null && !e.ImagenEjercicio.isEmpty()) {
                        String base64 = uriToBase64(Uri.parse(e.ImagenEjercicio));
                        if (base64 != null) {
                            je.put("imagenData", base64);
                        }
                    }
                    ejerciciosArray.put(je);
                }
                backup.put("ejercicios", ejerciciosArray);

                // 5. SeccionXejercicio
                JSONArray sxeArray = new JSONArray();
                List<SeccionXejercicio> sxeList = db.seccionXejercicioDao().obtenerTodasLasRelaciones();
                for (SeccionXejercicio sxe : sxeList) {
                    JSONObject jsxe = new JSONObject();
                    jsxe.put("IdSeccionXejercicio", sxe.IdSeccionXejercicio);
                    jsxe.put("IdSeccion", sxe.IdSeccion);
                    jsxe.put("IdEjercicio", sxe.IdEjercicio);
                    sxeArray.put(jsxe);
                }
                backup.put("relaciones", sxeArray);

                // 6. Entrenamientos
                JSONArray entArray = new JSONArray();
                List<Entrenamiento> entrenamientos = db.entrenamientoDao().obtenerTodosLosEntrenamientos();
                for (Entrenamiento ent : entrenamientos) {
                    JSONObject jent = new JSONObject();
                    jent.put("IdEntrenamiento", ent.IdEntrenamiento);
                    jent.put("IdUsuario", ent.IdUsuario);
                    jent.put("IdSeccion", ent.IdSeccion != null ? ent.IdSeccion : JSONObject.NULL);
                    jent.put("NumeroEntrenamiento", ent.NumeroEntrenamiento);
                    jent.put("FechaInicio", ent.FechaInicio);
                    jent.put("FechaFin", ent.FechaFin);
                    entArray.put(jent);
                }
                backup.put("entrenamientos", entArray);

                // 7. Registros (Fuerza)
                JSONArray regArray = new JSONArray();
                List<Registro> registros = db.registroDao().obtenerTodosLosRegistros();
                for (Registro r : registros) {
                    JSONObject jr = new JSONObject();
                    jr.put("IdRegistro", r.IdRegistro);
                    jr.put("IdEntrenamiento", r.IdEntrenamiento);
                    jr.put("IdSeccionXejercicio", r.IdSeccionXejercicio);
                    jr.put("PesoRegistro", r.PesoRegistro);
                    jr.put("NumSeriesRegistro", r.NumSeriesRegistro);
                    jr.put("Repeticiones", r.Repeticiones);
                    jr.put("FechaRegistro", r.FechaRegistro);
                    jr.put("UnidadPeso", r.UnidadPeso != null ? r.UnidadPeso : "kg");
                    jr.put("PesoCorporalMomento", r.PesoCorporalMomento);
                    regArray.put(jr);
                }
                backup.put("registros", regArray);

                // 8. Registros Cardio
                JSONArray regCardioArray = new JSONArray();
                List<RegistroCardio> registrosCardio = db.registroCardioDao().obtenerTodosLosRegistrosCardio();
                for (RegistroCardio rc : registrosCardio) {
                    JSONObject jrc = new JSONObject();
                    jrc.put("IdRegistroCardio", rc.IdRegistroCardio);
                    jrc.put("IdEntrenamiento", rc.IdEntrenamiento);
                    jrc.put("IdSeccionXejercicio", rc.IdSeccionXejercicio);
                    jrc.put("FechaRegistro", rc.FechaRegistro);
                    jrc.put("DuracionSegundos", rc.DuracionSegundos);
                    jrc.put("FechaInicio", rc.FechaInicio);
                    jrc.put("FechaFin", rc.FechaFin);
                    jrc.put("Distancia", rc.Distancia);
                    jrc.put("UnidadDistancia", rc.UnidadDistancia);
                    jrc.put("VelocidadPromedio", rc.VelocidadPromedio);
                    jrc.put("CadenciaPromedio", rc.CadenciaPromedio);
                    jrc.put("CaloriasQuemadas", rc.CaloriasQuemadas);
                    jrc.put("RitmoCardiacoPromedio", rc.RitmoCardiacoPromedio);
                    jrc.put("RitmoCardiacoMaximo", rc.RitmoCardiacoMaximo);
                    jrc.put("Inclinacion", rc.Inclinacion);
                    jrc.put("NivelResistencia", rc.NivelResistencia);
                    jrc.put("EsfuerzoPercibido", rc.EsfuerzoPercibido);
                    jrc.put("Notas", rc.Notas);
                    regCardioArray.put(jrc);
                }
                backup.put("registrosCardio", regCardioArray);

                // 9. Historial
                JSONArray histArray = new JSONArray();
                List<Historial> historiales = db.historialDao().obtenerTodoElHistorial();
                for (Historial h : historiales) {
                    JSONObject jh = new JSONObject();
                    jh.put("IdHistorial", h.IdHistorial);
                    jh.put("IdUsuarioHistorial", h.IdUsuarioHistorial);
                    jh.put("PesoHistorial", h.PesoHistorial);
                    jh.put("AlturaHistorial", h.AlturaHistorial);
                    jh.put("FechaHistorial", h.FechaHistorial);
                    histArray.put(jh);
                }
                backup.put("historial", histArray);

                // Guardar a archivo
                File path = new File(context.getCacheDir(), "backups");
                if (!path.exists()) {
                    if (!path.mkdirs()) {
                        callback.onError("No se pudo crear el directorio de backups");
                        return;
                    }
                }
                
                String fileName = "Backup_MiGymsito_" + System.currentTimeMillis() + ".json";
                File file = new File(path, fileName);
                
                try (FileOutputStream stream = new FileOutputStream(file)) {
                    stream.write(backup.toString().getBytes(StandardCharsets.UTF_8));
                }

                Uri contentUri = FileProvider.getUriForFile(context, "com.example.migymsito.fileprovider", file);
                callback.onComplete(true, contentUri);

            } catch (Exception e) {
                Log.e(TAG, "Error en exportación", e);
                callback.onError(e.getMessage());
            }
        });
    }

    public void importFullBackup(Uri backupUri, ImportCallback callback) {
        executorService.execute(() -> {
            try {
                InputStream is = context.getContentResolver().openInputStream(backupUri);
                if (is == null) {
                    callback.onError("No se pudo abrir el archivo");
                    return;
                }

                ByteArrayOutputStream result = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int length;
                while ((length = is.read(buffer)) != -1) {
                    result.write(buffer, 0, length);
                }
                String jsonString = result.toString("UTF-8");
                is.close();

                JSONObject backup = new JSONObject(jsonString);
                AppDatabase db = AppDatabase.getDatabase(context);

                // Borrar e Importar en una sola transacción para seguridad y velocidad
                db.runInTransaction(() -> {
                    try {
                        db.registroCardioDao().borrarTodo();
                        db.registroDao().borrarTodo();
                        db.entrenamientoDao().borrarTodo();
                        db.seccionXejercicioDao().borrarTodo();
                        db.seccionDao().borrarTodo();
                        db.rutinaDao().borrarTodo();
                        db.ejercicioDao().borrarTodo();
                        db.historialDao().borrarTodo();
                        db.usuarioDao().deleteAll();

                        // Limpiar imágenes
                        File folder = new File(context.getFilesDir(), "imagenes_ejercicios");
                        if (folder.exists()) {
                            File[] files = folder.listFiles();
                            if (files != null) {
                                for (File f : files) f.delete();
                            }
                        }

                        // Importar Usuarios
                        if (backup.has("usuarios")) {
                            JSONArray usersArray = backup.getJSONArray("usuarios");
                            for (int i = 0; i < usersArray.length(); i++) {
                                JSONObject ju = usersArray.getJSONObject(i);
                                Usuario u = new Usuario();
                                u.IdUsuario = ju.optInt("IdUsuario", 1);
                                u.NombreUsuario = ju.optString("NombreUsuario", "Usuario");
                                u.FechaNacimientoUsuario = ju.optLong("FechaNacimientoUsuario", System.currentTimeMillis());
                                u.CorreoElectronicoUsuario = ju.optString("CorreoElectronicoUsuario", "");
                                u.GeneroUsuario = ju.optString("GeneroUsuario", "");
                                db.usuarioDao().registrarUsuario(u);
                            }
                        }

                        // Importar Ejercicios
                        if (backup.has("ejercicios")) {
                            JSONArray ejArray = backup.getJSONArray("ejercicios");
                            for (int i = 0; i < ejArray.length(); i++) {
                                JSONObject je = ejArray.getJSONObject(i);
                                Ejercicio e = new Ejercicio();
                                e.IdEjercicio = je.optInt("IdEjercicio");
                                e.TipoEjercicio = je.optString("TipoEjercicio", "Personalizado");
                                e.NombreEjercicio = je.optString("NombreEjercicio", "Ejercicio");
                                e.CategoriaEjercicio = je.optString("CategoriaEjercicio", "FUERZA");
                                e.PesoCorporalEjercicio = je.optBoolean("PesoCorporalEjercicio", false);
                                e.PesoPorLado = je.optBoolean("PesoPorLado", false);
                                e.TipoDeBarra = je.optString("TipoDeBarra", "");
                                e.PesoBarra = (float) je.optDouble("PesoBarra", 0.0);

                                e.registraDistancia = je.optBoolean("registraDistancia", true);
                                e.registraCalorias = je.optBoolean("registraCalorias", true);
                                e.registraCadencia = je.optBoolean("registraCadencia", false);
                                e.registraRitmo = je.optBoolean("registraRitmo", false);
                                e.registraInclinacion = je.optBoolean("registraInclinacion", false);
                                e.registraResistencia = je.optBoolean("registraResistencia", false);

                                if (je.has("imagenData")) {
                                    e.ImagenEjercicio = guardarImagenDesdeBase64(je.optString("imagenData", ""));
                                }
                                db.ejercicioDao().insertarEjercicio(e);
                            }
                        }

                        // Importar Rutinas
                        if (backup.has("rutinas")) {
                            JSONArray rutinasArray = backup.getJSONArray("rutinas");
                            for (int i = 0; i < rutinasArray.length(); i++) {
                                JSONObject jr = rutinasArray.getJSONObject(i);
                                Rutina r = new Rutina();
                                r.IdRutina = jr.optInt("IdRutina");
                                r.NombreRutina = jr.optString("NombreRutina", "Rutina");
                                r.IdUsuarioRutina = jr.optInt("IdUsuarioRutina", 1);
                                db.rutinaDao().insertarRutina(r);
                            }
                        }

                        // Importar Secciones
                        if (backup.has("secciones")) {
                            JSONArray seccionesArray = backup.getJSONArray("secciones");
                            for (int i = 0; i < seccionesArray.length(); i++) {
                                JSONObject js = seccionesArray.getJSONObject(i);
                                Seccion s = new Seccion();
                                s.IdSeccion = js.optInt("IdSeccion");
                                s.NombreSeccion = js.optString("NombreSeccion", "Sección");
                                s.TipoSeccion = js.optString("TipoSeccion", "Personalizado");
                                if (js.has("IdRutinaSeccion") && !js.isNull("IdRutinaSeccion")) {
                                    s.IdRutinaSeccion = js.optInt("IdRutinaSeccion");
                                } else {
                                    s.IdRutinaSeccion = null;
                                }
                                db.seccionDao().insertarSeccion(s);
                            }
                        }

                        // Importar Relaciones
                        if (backup.has("relaciones")) {
                            JSONArray sxeArray = backup.getJSONArray("relaciones");
                            for (int i = 0; i < sxeArray.length(); i++) {
                                JSONObject jsxe = sxeArray.getJSONObject(i);
                                SeccionXejercicio sxe = new SeccionXejercicio();
                                sxe.IdSeccionXejercicio = jsxe.optInt("IdSeccionXejercicio");
                                sxe.IdSeccion = jsxe.optInt("IdSeccion");
                                sxe.IdEjercicio = jsxe.optInt("IdEjercicio");
                                db.seccionXejercicioDao().insert(sxe);
                            }
                        }

                        // Importar Entrenamientos
                        if (backup.has("entrenamientos")) {
                            JSONArray entArray = backup.getJSONArray("entrenamientos");
                            for (int i = 0; i < entArray.length(); i++) {
                                JSONObject jent = entArray.getJSONObject(i);
                                Entrenamiento ent = new Entrenamiento();
                                ent.IdEntrenamiento = jent.optInt("IdEntrenamiento");
                                ent.IdUsuario = jent.optInt("IdUsuario", 1);
                                if (jent.has("IdSeccion") && !jent.isNull("IdSeccion")) {
                                    ent.IdSeccion = jent.optInt("IdSeccion");
                                } else {
                                    ent.IdSeccion = null;
                                }
                                ent.NumeroEntrenamiento = jent.optInt("NumeroEntrenamiento", 1);
                                ent.FechaInicio = jent.optLong("FechaInicio", System.currentTimeMillis());
                                if (jent.has("FechaFin") && !jent.isNull("FechaFin")) {
                                    ent.FechaFin = jent.optLong("FechaFin");
                                }
                                db.entrenamientoDao().insert(ent);
                            }
                        }

                        // Importar Registros (Fuerza)
                        if (backup.has("registros")) {
                            JSONArray regArray = backup.getJSONArray("registros");
                            for (int i = 0; i < regArray.length(); i++) {
                                JSONObject jr = regArray.getJSONObject(i);
                                Registro r = new Registro();
                                r.IdRegistro = jr.optInt("IdRegistro");
                                r.IdEntrenamiento = jr.optInt("IdEntrenamiento");
                                r.IdSeccionXejercicio = jr.optInt("IdSeccionXejercicio");
                                r.PesoRegistro = jr.optDouble("PesoRegistro", 0.0);
                                r.NumSeriesRegistro = jr.optInt("NumSeriesRegistro", 1);
                                r.Repeticiones = jr.optInt("Repeticiones", 0);
                                r.FechaRegistro = jr.optLong("FechaRegistro", System.currentTimeMillis());
                                r.UnidadPeso = jr.optString("UnidadPeso", "kg");
                                if (jr.has("PesoCorporalMomento") && !jr.isNull("PesoCorporalMomento")) {
                                    r.PesoCorporalMomento = jr.optDouble("PesoCorporalMomento");
                                }
                                db.registroDao().insertarRegistro(r);
                            }
                        }

                        // Importar Registros Cardio
                        if (backup.has("registrosCardio")) {
                            JSONArray regCardioArray = backup.getJSONArray("registrosCardio");
                            for (int i = 0; i < regCardioArray.length(); i++) {
                                JSONObject jrc = regCardioArray.getJSONObject(i);
                                RegistroCardio rc = new RegistroCardio();
                                rc.IdRegistroCardio = jrc.optInt("IdRegistroCardio");
                                rc.IdEntrenamiento = jrc.optInt("IdEntrenamiento");
                                rc.IdSeccionXejercicio = jrc.optInt("IdSeccionXejercicio");
                                rc.FechaRegistro = jrc.optLong("FechaRegistro", System.currentTimeMillis());
                                rc.DuracionSegundos = jrc.optLong("DuracionSegundos", 0L);
                                if (jrc.has("FechaInicio") && !jrc.isNull("FechaInicio")) rc.FechaInicio = jrc.optLong("FechaInicio");
                                if (jrc.has("FechaFin") && !jrc.isNull("FechaFin")) rc.FechaFin = jrc.optLong("FechaFin");
                                if (jrc.has("Distancia") && !jrc.isNull("Distancia")) rc.Distancia = jrc.optDouble("Distancia");
                                rc.UnidadDistancia = jrc.optString("UnidadDistancia", "km");
                                if (jrc.has("VelocidadPromedio") && !jrc.isNull("VelocidadPromedio")) rc.VelocidadPromedio = jrc.optDouble("VelocidadPromedio");
                                if (jrc.has("CadenciaPromedio") && !jrc.isNull("CadenciaPromedio")) rc.CadenciaPromedio = jrc.optInt("CadenciaPromedio");
                                if (jrc.has("CaloriasQuemadas") && !jrc.isNull("CaloriasQuemadas")) rc.CaloriasQuemadas = jrc.optInt("CaloriasQuemadas");
                                if (jrc.has("RitmoCardiacoPromedio") && !jrc.isNull("RitmoCardiacoPromedio")) rc.RitmoCardiacoPromedio = jrc.optInt("RitmoCardiacoPromedio");
                                if (jrc.has("RitmoCardiacoMaximo") && !jrc.isNull("RitmoCardiacoMaximo")) rc.RitmoCardiacoMaximo = jrc.optInt("RitmoCardiacoMaximo");
                                if (jrc.has("Inclinacion") && !jrc.isNull("Inclinacion")) rc.Inclinacion = jrc.optDouble("Inclinacion");
                                if (jrc.has("NivelResistencia") && !jrc.isNull("NivelResistencia")) rc.NivelResistencia = jrc.optInt("NivelResistencia");
                                if (jrc.has("EsfuerzoPercibido") && !jrc.isNull("EsfuerzoPercibido")) rc.EsfuerzoPercibido = jrc.optInt("EsfuerzoPercibido");
                                rc.Notas = jrc.optString("Notas", "");
                                db.registroCardioDao().insertar(rc);
                            }
                        }

                        // Importar Historial
                        if (backup.has("historial")) {
                            JSONArray histArray = backup.getJSONArray("historial");
                            for (int i = 0; i < histArray.length(); i++) {
                                JSONObject jh = histArray.getJSONObject(i);
                                Historial h = new Historial();
                                h.IdHistorial = jh.optInt("IdHistorial");
                                h.IdUsuarioHistorial = jh.optInt("IdUsuarioHistorial", 1);
                                h.PesoHistorial = jh.optDouble("PesoHistorial", 0.0);
                                h.AlturaHistorial = jh.optDouble("AlturaHistorial", 0.0);
                                h.FechaHistorial = jh.optLong("FechaHistorial", System.currentTimeMillis());
                                db.historialDao().insertarHistorial(h);
                            }
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "Error parsing JSON data during transaction", e);
                        throw new RuntimeException(e);
                    }
                });

                callback.onComplete(true);

            } catch (Exception e) {
                Log.e(TAG, "Error en importación", e);
                callback.onError(e.getMessage());
            }
        });
    }

    private String uriToBase64(Uri uri) {
        try {
            InputStream inputStream;
            if (uri.getScheme() == null || uri.getScheme().equals("file")) {
                String path = uri.getPath();
                if (path == null) return null;
                File file = new File(path);
                if (!file.exists()) return null;
                inputStream = new FileInputStream(file);
            } else {
                inputStream = context.getContentResolver().openInputStream(uri);
            }

            if (inputStream == null) return null;

            try (InputStream is = inputStream;
                 ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream()) {
                byte[] buffer = new byte[8192];
                int len;
                while ((len = is.read(buffer)) != -1) {
                    byteBuffer.write(buffer, 0, len);
                }
                return Base64.encodeToString(byteBuffer.toByteArray(), Base64.NO_WRAP);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error convirtiendo URI a Base64: " + uri, e);
            return null;
        }
    }

    private String guardarImagenDesdeBase64(String base64Str) {
        if (base64Str == null || base64Str.isEmpty()) return null;
        try {
            byte[] data = Base64.decode(base64Str, Base64.DEFAULT);
            File folder = new File(context.getFilesDir(), "imagenes_ejercicios");
            if (!folder.exists()) {
                folder.mkdirs();
            }
            
            File file = new File(folder, "img_" + System.currentTimeMillis() + "_" + (int)(Math.random() * 1000) + ".jpg");
            try (FileOutputStream fos = new FileOutputStream(file)) {
                fos.write(data);
            }
            
            return Uri.fromFile(file).toString();
        } catch (Exception e) {
            return null;
        }
    }
}

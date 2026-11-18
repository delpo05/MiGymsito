package com.example.migymsito.utils;

import android.content.Context;
import android.net.Uri;

public class ImageUtils {

    /**
     * Resuelve de forma inteligente la ruta de una imagen o GIF para ser cargada por Glide.
     * Soporta URIs completas (android.resource://, file://, content://, http://, https://),
     * así como nombres directos de recursos de drawable o raw.
     *
     * @param context El contexto de la aplicación o vista.
     * @param path La cadena que representa la URI, URL o el nombre del recurso.
     * @return Objeto adecuado (Uri o Integer de recurso) para pasar directamente a Glide.with().load().
     */
    public static Object parseGlideLoadTarget(Context context, String path) {
        if (path == null || path.trim().isEmpty()) {
            return null;
        }
        String cleanPath = path.trim();

        if (cleanPath.startsWith("android.resource://") ||
                cleanPath.startsWith("content://") ||
                cleanPath.startsWith("file://") ||
                cleanPath.startsWith("http://") ||
                cleanPath.startsWith("https://")) {
            return Uri.parse(cleanPath);
        }

        if (context != null) {
            String pkgName = context.getPackageName();
            int drawableResId = context.getResources().getIdentifier(cleanPath, "drawable", pkgName);
            if (drawableResId != 0) {
                return drawableResId;
            }
            int rawResId = context.getResources().getIdentifier(cleanPath, "raw", pkgName);
            if (rawResId != 0) {
                return rawResId;
            }
        }

        return Uri.parse(cleanPath);
    }
}

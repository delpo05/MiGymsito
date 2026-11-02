package com.example.migymsito.utils;

public class HeaderUtils {

    /**
     * Devuelve solo el primer nombre/palabra si el nombre contiene espacios o es demasiado largo (> 10 caracteres).
     * Por ejemplo: "Felipe Delpjno" -> "Felipe"
     */
    public static String obtenerNombreCortoHeader(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            return "";
        }
        String trimmed = nombre.trim();
        String[] parts = trimmed.split("\\s+");
        if (parts.length > 1 || trimmed.length() > 10) {
            return parts[0];
        }
        return trimmed;
    }
}

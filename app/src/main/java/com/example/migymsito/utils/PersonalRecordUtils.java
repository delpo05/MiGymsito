package com.example.migymsito.utils;

import com.example.migymsito.data.Registro;
import com.example.migymsito.data.RegistroCardio;

import java.util.List;

public class PersonalRecordUtils {

    /**
     * Devuelve el registro de fuerza con el mayor peso levantado.
     * En caso de empate, prioriza mayor número de repeticiones y fecha más reciente.
     */
    public static Registro calcularMejorPeso(List<Registro> registros) {
        if (registros == null || registros.isEmpty()) return null;

        Registro mejor = null;
        for (Registro r : registros) {
            if (r == null || r.PesoRegistro == null) continue;
            if (mejor == null) {
                mejor = r;
                continue;
            }

            double pesoActual = r.PesoRegistro;
            double pesoMejor = mejor.PesoRegistro;

            if (pesoActual > pesoMejor) {
                mejor = r;
            } else if (Double.compare(pesoActual, pesoMejor) == 0) {
                if (r.Repeticiones > mejor.Repeticiones) {
                    mejor = r;
                } else if (r.Repeticiones == mejor.Repeticiones) {
                    if (r.FechaRegistro != null && mejor.FechaRegistro != null && r.FechaRegistro > mejor.FechaRegistro) {
                        mejor = r;
                    }
                }
            }
        }
        return mejor;
    }

    /**
     * Devuelve el registro de fuerza con el mayor producto de (Peso × Repeticiones) en una sola serie.
     * En caso de empate, prioriza mayor peso y fecha más reciente.
     */
    public static Registro calcularMejorVolumen(List<Registro> registros) {
        if (registros == null || registros.isEmpty()) return null;

        Registro mejor = null;
        double maxVolumen = -1.0;

        for (Registro r : registros) {
            if (r == null || r.PesoRegistro == null) continue;
            double volumenActual = r.PesoRegistro * r.Repeticiones;

            if (mejor == null || volumenActual > maxVolumen) {
                mejor = r;
                maxVolumen = volumenActual;
            } else if (Double.compare(volumenActual, maxVolumen) == 0) {
                if (r.PesoRegistro > mejor.PesoRegistro) {
                    mejor = r;
                    maxVolumen = volumenActual;
                } else if (Double.compare(r.PesoRegistro, mejor.PesoRegistro) == 0) {
                    if (r.FechaRegistro != null && mejor.FechaRegistro != null && r.FechaRegistro > mejor.FechaRegistro) {
                        mejor = r;
                        maxVolumen = volumenActual;
                    }
                }
            }
        }
        return mejor;
    }

    /**
     * Devuelve el registro de cardio con la mayor distancia recorrida.
     */
    public static RegistroCardio calcularMejorDistancia(List<RegistroCardio> registros) {
        if (registros == null || registros.isEmpty()) return null;

        RegistroCardio mejor = null;
        for (RegistroCardio rc : registros) {
            if (rc == null || rc.Distancia == null || rc.Distancia <= 0) continue;
            if (mejor == null || rc.Distancia > mejor.Distancia) {
                mejor = rc;
            }
        }
        return mejor;
    }

    /**
     * Devuelve el registro de cardio con la mayor duración en segundos.
     */
    public static RegistroCardio calcularMejorDuracion(List<RegistroCardio> registros) {
        if (registros == null || registros.isEmpty()) return null;

        RegistroCardio mejor = null;
        for (RegistroCardio rc : registros) {
            if (rc == null || rc.DuracionSegundos <= 0) continue;
            if (mejor == null || rc.DuracionSegundos > mejor.DuracionSegundos) {
                mejor = rc;
            }
        }
        return mejor;
    }

    /**
     * Devuelve el registro de cardio con las mayores calorías quemadas.
     */
    public static RegistroCardio calcularMejorCalorias(List<RegistroCardio> registros) {
        if (registros == null || registros.isEmpty()) return null;

        RegistroCardio mejor = null;
        for (RegistroCardio rc : registros) {
            if (rc == null || rc.CaloriasQuemadas == null || rc.CaloriasQuemadas <= 0) continue;
            if (mejor == null || rc.CaloriasQuemadas > mejor.CaloriasQuemadas) {
                mejor = rc;
            }
        }
        return mejor;
    }
}

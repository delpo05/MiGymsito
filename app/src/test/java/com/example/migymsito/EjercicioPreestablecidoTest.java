package com.example.migymsito;

import com.example.migymsito.data.Ejercicio;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Pruebas unitarias para el catálogo de Ejercicios Preestablecidos y sus atributos.
 */
public class EjercicioPreestablecidoTest {

    @Test
    public void testCreacionEjercicioFuerzaPreestablecido() {
        Ejercicio e = new Ejercicio();
        e.TipoEjercicio = "Preestablecido";
        e.NombreEjercicio = "Press de banca";
        e.CategoriaEjercicio = "FUERZA";
        e.PesoCorporalEjercicio = false;
        e.TipoDeBarra = "Olímpica (20kg)";
        e.PesoBarra = 20f;
        e.ImagenEjercicio = "gif_press_banca";

        assertEquals("Preestablecido", e.TipoEjercicio);
        assertEquals("Press de banca", e.NombreEjercicio);
        assertEquals("FUERZA", e.CategoriaEjercicio);
        assertFalse(e.PesoCorporalEjercicio);
        assertEquals(Float.valueOf(20f), e.PesoBarra);
        assertEquals("gif_press_banca", e.ImagenEjercicio);
    }

    @Test
    public void testCreacionEjercicioCardioPreestablecido() {
        Ejercicio e = new Ejercicio();
        e.TipoEjercicio = "Preestablecido";
        e.NombreEjercicio = "Cinta de correr";
        e.CategoriaEjercicio = "CARDIO";
        e.PesoCorporalEjercicio = true;
        e.registraDistancia = true;
        e.registraCalorias = true;
        e.registraInclinacion = true;
        e.ImagenEjercicio = "gif_cinta_correr";

        assertEquals("Preestablecido", e.TipoEjercicio);
        assertEquals("Cinta de correr", e.NombreEjercicio);
        assertEquals("CARDIO", e.CategoriaEjercicio);
        assertTrue(e.PesoCorporalEjercicio);
        assertTrue(e.registraDistancia);
        assertTrue(e.registraCalorias);
        assertTrue(e.registraInclinacion);
    }

    @Test
    public void testVerificacionPropiedadesBiomecanicas() {
        Ejercicio e = new Ejercicio();
        e.TipoEjercicio = "Preestablecido";
        e.NombreEjercicio = "Press inclinado con mancuernas";
        e.CategoriaEjercicio = "FUERZA";
        e.PesoCorporalEjercicio = false;
        e.PesoPorLado = true;
        e.ImagenEjercicio = "gif_press_inclinado_mancuernas";

        assertNotNull(e.PesoPorLado);
        assertTrue(e.PesoPorLado);
        assertEquals("FUERZA", e.CategoriaEjercicio);
    }
}

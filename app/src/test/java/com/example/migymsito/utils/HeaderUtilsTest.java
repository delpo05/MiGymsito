package com.example.migymsito.utils;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class HeaderUtilsTest {

    @Test
    public void testObtenerNombreCortoHeader_conNombreYApellido() {
        assertEquals("Felipe", HeaderUtils.obtenerNombreCortoHeader("Felipe Delpjno"));
    }

    @Test
    public void testObtenerNombreCortoHeader_conVariasPalabras() {
        assertEquals("Juan", HeaderUtils.obtenerNombreCortoHeader("Juan Carlos Gomez"));
    }

    @Test
    public void testObtenerNombreCortoHeader_nombreUnicoCorto() {
        assertEquals("Felipe", HeaderUtils.obtenerNombreCortoHeader("Felipe"));
    }

    @Test
    public void testObtenerNombreCortoHeader_nombreUnicoLargo() {
        assertEquals("Constantinopla", HeaderUtils.obtenerNombreCortoHeader("Constantinopla"));
    }

    @Test
    public void testObtenerNombreCortoHeader_nullYVacio() {
        assertEquals("", HeaderUtils.obtenerNombreCortoHeader(null));
        assertEquals("", HeaderUtils.obtenerNombreCortoHeader("   "));
    }
}

package com.example.migymsito.utils;

import com.example.migymsito.data.Registro;
import com.example.migymsito.data.RegistroCardio;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

public class PersonalRecordUtilsTest {

    @Test
    public void testCalcularMejorPeso_ListaVacia_RetornaNull() {
        assertNull(PersonalRecordUtils.calcularMejorPeso(null));
        assertNull(PersonalRecordUtils.calcularMejorPeso(Collections.emptyList()));
    }

    @Test
    public void testCalcularMejorPeso_ObtieneMaximoPeso() {
        List<Registro> lista = new ArrayList<>();

        Registro r1 = new Registro();
        r1.PesoRegistro = 60.0;
        r1.Repeticiones = 10;
        r1.FechaRegistro = 1000L;

        Registro r2 = new Registro();
        r2.PesoRegistro = 80.0;
        r2.Repeticiones = 5;
        r2.FechaRegistro = 2000L;

        Registro r3 = new Registro();
        r3.PesoRegistro = 75.0;
        r3.Repeticiones = 8;
        r3.FechaRegistro = 3000L;

        lista.add(r1);
        lista.add(r2);
        lista.add(r3);

        Registro mejor = PersonalRecordUtils.calcularMejorPeso(lista);
        assertNotNull(mejor);
        assertEquals(80.0, mejor.PesoRegistro, 0.001);
        assertEquals(5, mejor.Repeticiones);
    }

    @Test
    public void testCalcularMejorVolumen_ObtieneMaximoVolumenSerie() {
        List<Registro> lista = new ArrayList<>();

        // 60kg x 10 reps = 600kg
        Registro r1 = new Registro();
        r1.PesoRegistro = 60.0;
        r1.Repeticiones = 10;
        r1.FechaRegistro = 1000L;

        // 80kg x 5 reps = 400kg
        Registro r2 = new Registro();
        r2.PesoRegistro = 80.0;
        r2.Repeticiones = 5;
        r2.FechaRegistro = 2000L;

        // 70kg x 10 reps = 700kg (Máximo)
        Registro r3 = new Registro();
        r3.PesoRegistro = 70.0;
        r3.Repeticiones = 10;
        r3.FechaRegistro = 3000L;

        lista.add(r1);
        lista.add(r2);
        lista.add(r3);

        Registro mejor = PersonalRecordUtils.calcularMejorVolumen(lista);
        assertNotNull(mejor);
        assertEquals(70.0, mejor.PesoRegistro, 0.001);
        assertEquals(10, mejor.Repeticiones);
        assertEquals(700.0, mejor.PesoRegistro * mejor.Repeticiones, 0.001);
    }

    @Test
    public void testCalcularMejorCardio_DistanciaDuracionCalorias() {
        List<RegistroCardio> lista = new ArrayList<>();

        RegistroCardio rc1 = new RegistroCardio();
        rc1.Distancia = 5.0;
        rc1.DuracionSegundos = 1800L; // 30 mins
        rc1.CaloriasQuemadas = 300;

        RegistroCardio rc2 = new RegistroCardio();
        rc2.Distancia = 10.0;
        rc2.DuracionSegundos = 3600L; // 60 mins
        rc2.CaloriasQuemadas = 650;

        lista.add(rc1);
        lista.add(rc2);

        RegistroCardio mejorDistancia = PersonalRecordUtils.calcularMejorDistancia(lista);
        assertNotNull(mejorDistancia);
        assertEquals(10.0, mejorDistancia.Distancia, 0.001);

        RegistroCardio mejorDuracion = PersonalRecordUtils.calcularMejorDuracion(lista);
        assertNotNull(mejorDuracion);
        assertEquals(3600L, mejorDuracion.DuracionSegundos);

        RegistroCardio mejorCalorias = PersonalRecordUtils.calcularMejorCalorias(lista);
        assertNotNull(mejorCalorias);
        assertEquals(650, (int) mejorCalorias.CaloriasQuemadas);
    }
}

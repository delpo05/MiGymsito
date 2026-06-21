package com.example.migymsito.data;

public class RegistroDetallado {
    public String nombreRutina;
    public String nombreSeccion;
    public String nombreEjercicio;
    public int numSerie;
    public int repeticiones;
    public double peso;
    public boolean esPesoCorporal;
    public String tipoBarra;
    public Float pesoBarra;
    public long fecha;

    public RegistroDetallado(String nombreRutina, String nombreSeccion, String nombreEjercicio, 
                            int numSerie, int repeticiones, double peso, boolean esPesoCorporal, 
                            String tipoBarra, Float pesoBarra, long fecha) {
        this.nombreRutina = nombreRutina;
        this.nombreSeccion = nombreSeccion;
        this.nombreEjercicio = nombreEjercicio;
        this.numSerie = numSerie;
        this.repeticiones = repeticiones;
        this.peso = peso;
        this.esPesoCorporal = esPesoCorporal;
        this.tipoBarra = tipoBarra;
        this.pesoBarra = pesoBarra;
        this.fecha = fecha;
    }
}

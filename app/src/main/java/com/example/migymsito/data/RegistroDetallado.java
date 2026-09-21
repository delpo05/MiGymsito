package com.example.migymsito.data;

public class RegistroDetallado {
    public String categoriaEjercicio = "FUERZA";
    public String nombreRutina;
    public String nombreSeccion;
    public String nombreEjercicio;

    // Fuerza fields
    public Integer numSerie;
    public Integer repeticiones;
    public Double peso;
    public String unidadPeso;
    public Boolean esPesoCorporal;
    public String tipoBarra;
    public Float pesoBarra;

    // Cardio fields
    public Long duracionSegundos;
    public Double distancia;
    public String unidadDistancia;
    public Integer caloriasQuemadas;
    public Integer ritmoCardiacoPromedio;
    public Double velocidadPromedio;
    public Integer cadenciaPromedio;
    public Double inclinacion;
    public Integer nivelResistencia;
    public String notas;

    public long fecha;

    public RegistroDetallado() {
    }

    public RegistroDetallado(String nombreRutina, String nombreSeccion, String nombreEjercicio, 
                            int numSerie, int repeticiones, double peso, String unidadPeso, boolean esPesoCorporal, 
                            String tipoBarra, Float pesoBarra, long fecha) {
        this.categoriaEjercicio = "FUERZA";
        this.nombreRutina = nombreRutina;
        this.nombreSeccion = nombreSeccion;
        this.nombreEjercicio = nombreEjercicio;
        this.numSerie = numSerie;
        this.repeticiones = repeticiones;
        this.peso = peso;
        this.unidadPeso = unidadPeso;
        this.esPesoCorporal = esPesoCorporal;
        this.tipoBarra = tipoBarra;
        this.pesoBarra = pesoBarra;
        this.fecha = fecha;
    }
}

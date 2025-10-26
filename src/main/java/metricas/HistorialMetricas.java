package metricas;

import estructura_datos.ListaEnteros;
import estructura_datos.ListaDoubles;

/**
 * Clase para almacenar el historial de métricas del sistema a lo largo del tiempo.
 * Permite registrar valores de métricas en cada ciclo para su posterior visualización.
 */
public class HistorialMetricas {

    // Listas para almacenar el historial de cada métrica
    private final ListaEnteros ciclos;
    private final ListaDoubles utilizacionCPU;
    private final ListaDoubles throughput;
    private final ListaDoubles tiempoEsperaPromedio;
    private final ListaDoubles tiempoRespuestaPromedio;
    private final ListaEnteros procesosCompletados;
    private final ListaEnteros procesosActivos;

    /**
     * Constructor que inicializa las listas de historial
     */
    public HistorialMetricas() {
        this.ciclos = new ListaEnteros();
        this.utilizacionCPU = new ListaDoubles();
        this.throughput = new ListaDoubles();
        this.tiempoEsperaPromedio = new ListaDoubles();
        this.tiempoRespuestaPromedio = new ListaDoubles();
        this.procesosCompletados = new ListaEnteros();
        this.procesosActivos = new ListaEnteros();
    }

    /**
     * Registra las métricas del ciclo actual
     *
     * @param ciclo Número del ciclo actual
     * @param utilCPU Utilización del CPU (%)
     * @param tput Throughput (procesos/ciclo)
     * @param tEspera Tiempo de espera promedio
     * @param tRespuesta Tiempo de respuesta promedio
     * @param pCompletados Número de procesos completados
     * @param pActivos Número de procesos activos
     */
    public void registrarMetricas(int ciclo, double utilCPU, double tput,
                                  double tEspera, double tRespuesta,
                                  int pCompletados, int pActivos) {
        this.ciclos.agregar(ciclo);
        this.utilizacionCPU.agregar(utilCPU);
        this.throughput.agregar(tput);
        this.tiempoEsperaPromedio.agregar(tEspera);
        this.tiempoRespuestaPromedio.agregar(tRespuesta);
        this.procesosCompletados.agregar(pCompletados);
        this.procesosActivos.agregar(pActivos);
    }

    /**
     * Reinicia todo el historial de métricas
     */
    public void reiniciar() {
        ciclos.limpiar();
        utilizacionCPU.limpiar();
        throughput.limpiar();
        tiempoEsperaPromedio.limpiar();
        tiempoRespuestaPromedio.limpiar();
        procesosCompletados.limpiar();
        procesosActivos.limpiar();
    }

    // Getters para acceder a las listas de historial

    public int[] getCiclos() {
        return ciclos.toArray();
    }

    public double[] getUtilizacionCPU() {
        return utilizacionCPU.toArray();
    }

    public double[] getThroughput() {
        return throughput.toArray();
    }

    public double[] getTiempoEsperaPromedio() {
        return tiempoEsperaPromedio.toArray();
    }

    public double[] getTiempoRespuestaPromedio() {
        return tiempoRespuestaPromedio.toArray();
    }

    public int[] getProcesosCompletados() {
        return procesosCompletados.toArray();
    }

    public int[] getProcesosActivos() {
        return procesosActivos.toArray();
    }

    /**
     * Obtiene el número de registros en el historial
     *
     * @return Número de registros
     */
    public int getTamanio() {
        return ciclos.obtenerTamanio();
    }

    /**
     * Verifica si el historial está vacío
     *
     * @return true si está vacío, false en caso contrario
     */
    public boolean estaVacio() {
        return ciclos.estaVacia();
    }
}

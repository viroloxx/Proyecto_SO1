package metricas;

import java.util.ArrayList;
import java.util.List;

/**
 * Clase para almacenar el historial de métricas del sistema a lo largo del tiempo.
 * Permite registrar valores de métricas en cada ciclo para su posterior visualización.
 */
public class HistorialMetricas {

    // Listas para almacenar el historial de cada métrica
    private final List<Integer> ciclos;
    private final List<Double> utilizacionCPU;
    private final List<Double> throughput;
    private final List<Double> tiempoEsperaPromedio;
    private final List<Double> tiempoRespuestaPromedio;
    private final List<Integer> procesosCompletados;
    private final List<Integer> procesosActivos;

    /**
     * Constructor que inicializa las listas de historial
     */
    public HistorialMetricas() {
        this.ciclos = new ArrayList<>();
        this.utilizacionCPU = new ArrayList<>();
        this.throughput = new ArrayList<>();
        this.tiempoEsperaPromedio = new ArrayList<>();
        this.tiempoRespuestaPromedio = new ArrayList<>();
        this.procesosCompletados = new ArrayList<>();
        this.procesosActivos = new ArrayList<>();
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
        this.ciclos.add(ciclo);
        this.utilizacionCPU.add(utilCPU);
        this.throughput.add(tput);
        this.tiempoEsperaPromedio.add(tEspera);
        this.tiempoRespuestaPromedio.add(tRespuesta);
        this.procesosCompletados.add(pCompletados);
        this.procesosActivos.add(pActivos);
    }

    /**
     * Reinicia todo el historial de métricas
     */
    public void reiniciar() {
        ciclos.clear();
        utilizacionCPU.clear();
        throughput.clear();
        tiempoEsperaPromedio.clear();
        tiempoRespuestaPromedio.clear();
        procesosCompletados.clear();
        procesosActivos.clear();
    }

    // Getters para acceder a las listas de historial

    public List<Integer> getCiclos() {
        return new ArrayList<>(ciclos);
    }

    public List<Double> getUtilizacionCPU() {
        return new ArrayList<>(utilizacionCPU);
    }

    public List<Double> getThroughput() {
        return new ArrayList<>(throughput);
    }

    public List<Double> getTiempoEsperaPromedio() {
        return new ArrayList<>(tiempoEsperaPromedio);
    }

    public List<Double> getTiempoRespuestaPromedio() {
        return new ArrayList<>(tiempoRespuestaPromedio);
    }

    public List<Integer> getProcesosCompletados() {
        return new ArrayList<>(procesosCompletados);
    }

    public List<Integer> getProcesosActivos() {
        return new ArrayList<>(procesosActivos);
    }

    /**
     * Obtiene el número de registros en el historial
     *
     * @return Número de registros
     */
    public int getTamanio() {
        return ciclos.size();
    }

    /**
     * Verifica si el historial está vacío
     *
     * @return true si está vacío, false en caso contrario
     */
    public boolean estaVacio() {
        return ciclos.isEmpty();
    }
}

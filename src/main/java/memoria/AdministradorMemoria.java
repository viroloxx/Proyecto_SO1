package memoria;

import modelo.PCB;
import estructura_datos.MapaMemoria;

/**
 * Simula la gestión de memoria principal del sistema operativo.
 * Controla cuántos procesos pueden estar activos en memoria simultáneamente.
 * Los procesos que no tengan espacio se suspenderán para liberar recursos.
 */
public class AdministradorMemoria {

    private final int capacidadMaxima; // Número máximo de procesos en memoria
    private final MapaMemoria procesosEnMemoria; // ID -> Tamaño en memoria (simulado)
    private int memoriaUsada;
    private final int memoriaTotal;

    /**
     * Constructor del administrador de memoria
     *
     * @param capacidadMaxProcesos Número máximo de procesos simultáneos en memoria
     * @param memoriaTotal Tamaño total de memoria (en unidades arbitrarias)
     */
    public AdministradorMemoria(int capacidadMaxProcesos, int memoriaTotal) {
        this.capacidadMaxima = capacidadMaxProcesos;
        this.memoriaTotal = memoriaTotal;
        this.procesosEnMemoria = new MapaMemoria();
        this.memoriaUsada = 0;
    }

    /**
     * Intenta cargar un proceso en memoria
     *
     * @param proceso PCB del proceso a cargar
     * @return true si se pudo cargar, false si no hay espacio
     */
    public synchronized boolean cargarProceso(PCB proceso) {
        // Calcular tamaño del proceso basado en sus instrucciones
        int tamanioProceso = calcularTamanioProceso(proceso);

        // Verificar si hay espacio disponible
        if (procesosEnMemoria.obtenerTamanio() < capacidadMaxima &&
            (memoriaUsada + tamanioProceso) <= memoriaTotal) {

            procesosEnMemoria.poner(proceso.getIdProceso(), tamanioProceso);
            memoriaUsada += tamanioProceso;
            return true;
        }

        return false;
    }

    /**
     * Libera un proceso de la memoria
     *
     * @param proceso PCB del proceso a liberar
     */
    public synchronized void liberarProceso(PCB proceso) {
        int tamanio = procesosEnMemoria.remover(proceso.getIdProceso());
        if (tamanio != -1) {
            memoriaUsada -= tamanio;
        }
    }

    /**
     * Verifica si un proceso está cargado en memoria
     *
     * @param proceso PCB del proceso
     * @return true si está en memoria, false en caso contrario
     */
    public synchronized boolean estaEnMemoria(PCB proceso) {
        return procesosEnMemoria.contieneClave(proceso.getIdProceso());
    }

    /**
     * Verifica si hay espacio disponible en memoria
     *
     * @return true si hay espacio, false si está llena
     */
    public synchronized boolean hayEspacioDisponible() {
        return procesosEnMemoria.obtenerTamanio() < capacidadMaxima;
    }

    /**
     * Calcula el espacio disponible en memoria
     *
     * @return Cantidad de espacio libre
     */
    public synchronized int getEspacioDisponible() {
        return memoriaTotal - memoriaUsada;
    }

    /**
     * Calcula el tamaño que ocuparía un proceso en memoria
     * Basado en el número de instrucciones del proceso
     *
     * @param proceso PCB del proceso
     * @return Tamaño estimado en unidades de memoria
     */
    private int calcularTamanioProceso(PCB proceso) {
        // Fórmula simple: cada instrucción ocupa 10 unidades de memoria
        // más 100 unidades base para el PCB y datos del proceso
        return 100 + (proceso.getTiempoEjecucion() * 10);
    }

    /**
     * Obtiene el número de procesos actualmente en memoria
     *
     * @return Número de procesos cargados
     */
    public synchronized int getProcesosEnMemoria() {
        return procesosEnMemoria.obtenerTamanio();
    }

    /**
     * Obtiene la capacidad máxima de procesos
     *
     * @return Capacidad máxima
     */
    public int getCapacidadMaxima() {
        return capacidadMaxima;
    }

    /**
     * Obtiene el porcentaje de memoria utilizada
     *
     * @return Porcentaje de uso (0-100)
     */
    public synchronized double getPorcentajeUso() {
        return (double) memoriaUsada / memoriaTotal * 100.0;
    }

    /**
     * Obtiene la memoria total del sistema
     *
     * @return Memoria total en unidades
     */
    public int getMemoriaTotal() {
        return memoriaTotal;
    }

    /**
     * Obtiene la memoria usada actualmente
     *
     * @return Memoria usada en unidades
     */
    public synchronized int getMemoriaUsada() {
        return memoriaUsada;
    }

    /**
     * Reinicia el administrador de memoria
     */
    public synchronized void reiniciar() {
        procesosEnMemoria.limpiar();
        memoriaUsada = 0;
    }

    @Override
    public synchronized String toString() {
        return String.format("Memoria: %d/%d procesos, %.1f%% usado (%d/%d unidades)",
                           procesosEnMemoria.obtenerTamanio(), capacidadMaxima,
                           getPorcentajeUso(), memoriaUsada, memoriaTotal);
    }
}

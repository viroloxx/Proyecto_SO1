package metricas;
import estructura_datos.Lista;
import modelo.PCB;

public class Metricas {
    private int procesosCompletados;
    private double tiempoTotalCPU;
    private double tiempoOcupadoCPU;
    private Lista listaTiemposEspera;
    private Lista listaTiemposRespuesta;
    private Lista listaThroughput;
    
    public Metricas() {
        this.procesosCompletados = 0;
        this.tiempoTotalCPU = 0;
        this.tiempoOcupadoCPU = 0;
        this.listaTiemposEspera = new Lista();
        this.listaTiemposRespuesta = new Lista();
        this.listaThroughput = new Lista();
    }
    
    public void registrarCiclo(boolean cpuOcupada) {
        tiempoTotalCPU++;
        if (cpuOcupada) {
            tiempoOcupadoCPU++;
        }
    }
    
    public void registrarProcesoCompletado(PCB proceso, int cicloActual) {
        procesosCompletados++;
        proceso.registrarTiempoRetorno(cicloActual);
    }
    
    public double calcularThroughput(int cicloActual) {
        if (cicloActual == 0) return 0.0;
        return (double) procesosCompletados / cicloActual;
    }
    
    public double calcularUtilizacionCPU() {
        if (tiempoTotalCPU == 0) return 0.0;
        return (tiempoOcupadoCPU / tiempoTotalCPU) * 100.0;
    }
    
    public double calcularTiempoEsperaPromedio(PCB[] todosLosProcesos) {
        if (todosLosProcesos.length == 0) return 0.0;
        double suma = 0;
        for (PCB p : todosLosProcesos) {
            suma += p.getTiempoEsperaTotal();
        }
        return suma / todosLosProcesos.length;
    }
    
    public double calcularTiempoRespuestaPromedio(PCB[] procesosTerminados) {
        if (procesosTerminados.length == 0) return 0.0;
        double suma = 0;
        for (PCB p : procesosTerminados) {
            suma += p.getTiempoRespuesta();
        }
        return suma / procesosTerminados.length;
    }
    
    public void reiniciar() {
        procesosCompletados = 0;
        tiempoTotalCPU = 0;
        tiempoOcupadoCPU = 0;
        listaTiemposEspera.limpiar();
        listaTiemposRespuesta.limpiar();
        listaThroughput.limpiar();
    }
    
    public int getProcesosCompletados() {
        return procesosCompletados;
    }
}

package cpu;
import modelo.PCB;
import modelo.EstadoProceso;

public class CPU {
    private PCB procesoActual;
    private boolean ejecutandoSO;
    private int ciclosProcesoActual;
    
    public CPU() {
        this.procesoActual = null;
        this.ejecutandoSO = false;
        this.ciclosProcesoActual = 0;
    }
    
    public synchronized void cargarProceso(PCB proceso) {
        this.procesoActual = proceso;
        this.ciclosProcesoActual = 0;
        if (proceso != null) {
            proceso.setEstado(EstadoProceso.EJECUCION);
        }
    }
    
    public synchronized boolean ejecutarCiclo(int cicloActual) {
        if (procesoActual == null) {
            ejecutandoSO = true;
            return false;
        }
        ejecutandoSO = false;
        procesoActual.registrarInicioPrimeraEjecucion(cicloActual);
        boolean generaExcepcion = procesoActual.ejecutarCiclo();
        ciclosProcesoActual++;
        return generaExcepcion;
    }
    
    public synchronized PCB liberarProceso() {
        PCB temp = procesoActual;
        procesoActual = null;
        ciclosProcesoActual = 0;
        return temp;
    }
    
    public PCB getProcesoActual() {
        return procesoActual;
    }
    
    public boolean isEjecutandoSO() {
        return ejecutandoSO;
    }
    
    public int getCiclosProcesoActual() {
        return ciclosProcesoActual;
    }
    
    public void resetCiclosProcesoActual() {
        this.ciclosProcesoActual = 0;
    }
}

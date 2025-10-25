package planificacion;
import estructura_datos.Cola;
import modelo.PCB;

public class RoundRobin implements Planificador {
    private int quantum;
    
    public RoundRobin(int quantum) {
        this.quantum = quantum;
    }
    
    public RoundRobin() {
        this(3);
    }
    
    @Override
    public PCB seleccionarSiguienteProceso(Cola colaListos) {
        return colaListos.desencolar();
    }
    
    @Override
    public void reorganizarCola(Cola colaListos) {}
    
    @Override
    public String obtenerNombre() {
        return "Round Robin (Q=" + quantum + ")";
    }
    
    @Override
    public boolean esExpropriativo() {
        return true;
    }
    
    @Override
    public int obtenerQuantum() {
        return quantum;
    }
    
    @Override
    public void establecerQuantum(int quantum) {
        if (quantum > 0) this.quantum = quantum;
    }
    
    @Override
    public boolean debeExpropriar(PCB procesoActual, Cola colaListos, int ciclosEjecutados) {
        return ciclosEjecutados >= quantum;
    }
}

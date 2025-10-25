package planificacion;
import estructura_datos.Cola;
import modelo.PCB;

public class FCFS implements Planificador {
    @Override
    public PCB seleccionarSiguienteProceso(Cola colaListos) {
        return colaListos.desencolar();
    }
    
    @Override
    public void reorganizarCola(Cola colaListos) {}
    
    @Override
    public String obtenerNombre() {
        return "FCFS";
    }
    
    @Override
    public boolean esExpropriativo() {
        return false;
    }
}

package planificacion;
import estructura_datos.Cola;
import modelo.PCB;

public class MultilevelQueue implements Planificador {
    private int quantumBase;
    
    public MultilevelQueue() {
        this.quantumBase = 2;
    }
    
    @Override
    public PCB seleccionarSiguienteProceso(Cola colaListos) {
        if (colaListos.estaVacia()) return null;
        reorganizarCola(colaListos);
        return colaListos.desencolar();
    }
    
    @Override
    public void reorganizarCola(Cola colaListos) {
        if (colaListos.obtenerTamanio() <= 1) return;
        PCB[] procesos = colaListos.toArray();
        for (int i = 0; i < procesos.length - 1; i++) {
            for (int j = 0; j < procesos.length - i - 1; j++) {
                int nivel1 = procesos[j].getPrioridad();
                int nivel2 = procesos[j + 1].getPrioridad();
                if (nivel1 > nivel2) {
                    PCB temp = procesos[j];
                    procesos[j] = procesos[j + 1];
                    procesos[j + 1] = temp;
                }
            }
        }
        colaListos.fromArray(procesos);
    }
    
    @Override
    public String obtenerNombre() {
        return "Multilevel FB Queue";
    }
    
    @Override
    public boolean esExpropriativo() {
        return true;
    }
    
    @Override
    public boolean debeExpropriar(PCB procesoActual, Cola colaListos, int ciclosEjecutados) {
        int quantumActual = quantumBase * (procesoActual.getPrioridad() + 1);
        return ciclosEjecutados >= quantumActual;
    }
}

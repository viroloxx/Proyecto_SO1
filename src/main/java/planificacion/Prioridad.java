package planificacion;
import estructura_datos.Cola;
import modelo.PCB;

public class Prioridad implements Planificador {
    private boolean expropriativo;
    
    public Prioridad(boolean expropriativo) {
        this.expropriativo = expropriativo;
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
                if (procesos[j].getPrioridad() > procesos[j + 1].getPrioridad()) {
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
        return expropriativo ? "Prioridad Exp" : "Prioridad No-Exp";
    }
    
    @Override
    public boolean esExpropriativo() {
        return expropriativo;
    }
    
    @Override
    public boolean debeExpropriar(PCB procesoActual, Cola colaListos, int ciclosEjecutados) {
        if (!expropriativo || colaListos.estaVacia()) return false;
        PCB[] procesos = colaListos.toArray();
        for (PCB p : procesos) {
            if (p.getPrioridad() < procesoActual.getPrioridad()) {
                return true;
            }
        }
        return false;
    }
}

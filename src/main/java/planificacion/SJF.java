package planificacion;
import estructura_datos.Cola;
import modelo.PCB;

public class SJF implements Planificador {
    private boolean expropriativo;
    
    public SJF(boolean expropriativo) {
        this.expropriativo = expropriativo;
    }
    
    public SJF() {
        this(false);
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
                if (procesos[j].getTiempoRestante() > procesos[j + 1].getTiempoRestante()) {
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
        return expropriativo ? "SRTF" : "SJF";
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
            if (p.getTiempoRestante() < procesoActual.getTiempoRestante()) {
                return true;
            }
        }
        return false;
    }
}

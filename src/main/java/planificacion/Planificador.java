package planificacion;
import estructura_datos.Cola;
import modelo.PCB;

public interface Planificador {
    PCB seleccionarSiguienteProceso(Cola colaListos);
    void reorganizarCola(Cola colaListos);
    String obtenerNombre();
    boolean esExpropriativo();
    default int obtenerQuantum() { return -1; }
    default void establecerQuantum(int quantum) {}
    default boolean debeExpropriar(PCB procesoActual, Cola colaListos, int ciclosEjecutados) { return false; }
}

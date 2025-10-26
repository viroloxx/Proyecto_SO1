package planificacion;
import estructura_datos.Cola;
import modelo.PCB;

/**
 * Planificador Multilevel Queue (sin Feedback).
 *
 * Características:
 * - Múltiples colas separadas por prioridad
 * - Los procesos NO cambian de cola (sin feedback)
 * - Prioridades:
 *   - Cola Alta (0-2): Procesos de sistema/interactivos
 *   - Cola Media (3-6): Procesos normales
 *   - Cola Baja (7-10): Procesos batch/background
 * - Dentro de cada cola se usa FCFS
 * - Se sirven colas de alta prioridad primero
 */
public class MultilevelQueue implements Planificador {

    // Límites de prioridad para cada cola
    private static final int PRIORIDAD_ALTA_MIN = 0;
    private static final int PRIORIDAD_ALTA_MAX = 2;
    private static final int PRIORIDAD_MEDIA_MIN = 3;
    private static final int PRIORIDAD_MEDIA_MAX = 6;
    private static final int PRIORIDAD_BAJA_MIN = 7;
    private static final int PRIORIDAD_BAJA_MAX = 10;

    public MultilevelQueue() {
        // No necesita estado interno adicional
    }

    /**
     * Determina el nivel de cola según la prioridad del proceso
     * @param prioridad Prioridad del proceso (0-10)
     * @return 0=Alta, 1=Media, 2=Baja
     */
    private int determinarNivelCola(int prioridad) {
        if (prioridad >= PRIORIDAD_ALTA_MIN && prioridad <= PRIORIDAD_ALTA_MAX) {
            return 0; // Cola Alta
        } else if (prioridad >= PRIORIDAD_MEDIA_MIN && prioridad <= PRIORIDAD_MEDIA_MAX) {
            return 1; // Cola Media
        } else {
            return 2; // Cola Baja
        }
    }

    @Override
    public PCB seleccionarSiguienteProceso(Cola colaListos) {
        if (colaListos.estaVacia()) return null;

        // Reorganizar para agrupar por nivel de cola
        reorganizarCola(colaListos);

        // Seleccionar el primero (que será de la cola de mayor prioridad disponible)
        return colaListos.desencolar();
    }

    @Override
    public void reorganizarCola(Cola colaListos) {
        if (colaListos.obtenerTamanio() <= 1) return;

        PCB[] procesos = colaListos.toArray();

        // Ordenar procesos por nivel de cola (primero) y luego por orden FCFS dentro de cada nivel
        // Bubble sort estable para mantener orden FCFS dentro de cada nivel
        for (int i = 0; i < procesos.length - 1; i++) {
            for (int j = 0; j < procesos.length - i - 1; j++) {
                int nivelActual = determinarNivelCola(procesos[j].getPrioridad());
                int nivelSiguiente = determinarNivelCola(procesos[j + 1].getPrioridad());

                // Ordenar por nivel (menor nivel = mayor prioridad)
                if (nivelActual > nivelSiguiente) {
                    PCB temp = procesos[j];
                    procesos[j] = procesos[j + 1];
                    procesos[j + 1] = temp;
                }
                // Si están en el mismo nivel, mantener orden FCFS (no intercambiar)
                // El bubble sort estable mantiene el orden original para elementos iguales
            }
        }

        colaListos.fromArray(procesos);
    }

    @Override
    public String obtenerNombre() {
        return "Multilevel Queue";
    }

    @Override
    public boolean esExpropriativo() {
        // No expropriativo: una vez que un proceso empieza, termina su ejecución
        // (o se bloquea por E/S)
        return false;
    }

    @Override
    public boolean debeExpropriar(PCB procesoActual, Cola colaListos, int ciclosEjecutados) {
        // Multilevel Queue tradicional NO es expropriativo
        return false;
    }
}

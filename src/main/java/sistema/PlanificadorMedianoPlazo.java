package sistema;

import memoria.AdministradorMemoria;
import modelo.PCB;
import modelo.EstadoProceso;
import estructura_datos.Cola;

/**
 * Planificador a Mediano Plazo (Swapper)
 *
 * Responsabilidades:
 * - Suspender procesos cuando la memoria está llena
 * - Reactivar procesos suspendidos cuando hay espacio disponible
 * - Gestionar la transición entre memoria principal y disco (simulado)
 *
 * Este componente implementa el concepto de "swapping" donde los procesos
 * pueden moverse entre memoria principal y almacenamiento secundario.
 */
public class PlanificadorMedianoPlazo {

    private final AdministradorProcesos adminProcesos;
    private final AdministradorMemoria adminMemoria;
    private int ciclosDesdeUltimaSuspension;
    private int ciclosDesdeUltimaReactivacion;
    private final int umbralSuspension;
    private final int umbralReactivacion;

    /**
     * Constructor del planificador a mediano plazo
     *
     * @param adminProcesos Administrador de procesos del sistema
     * @param adminMemoria Administrador de memoria del sistema
     */
    public PlanificadorMedianoPlazo(AdministradorProcesos adminProcesos,
                                    AdministradorMemoria adminMemoria) {
        this.adminProcesos = adminProcesos;
        this.adminMemoria = adminMemoria;
        this.ciclosDesdeUltimaSuspension = 0;
        this.ciclosDesdeUltimaReactivacion = 0;
        // Umbrales más agresivos para suspensión/reactivación
        this.umbralSuspension = 1; // Suspender rápidamente cuando sea necesario
        this.umbralReactivacion = 2; // Esperar un poco más para reactivar
    }

    /**
     * Ejecuta la lógica de planificación a mediano plazo
     * Debe llamarse periódicamente desde el ciclo principal del SO
     *
     * @param cicloActual Número del ciclo actual
     * @return Mensaje descriptivo de las acciones realizadas (para logging)
     */
    public synchronized String ejecutarCiclo(int cicloActual) {
        StringBuilder log = new StringBuilder();

        ciclosDesdeUltimaSuspension++;
        ciclosDesdeUltimaReactivacion++;

        // 1. Intentar reactivar procesos suspendidos si hay espacio
        if (ciclosDesdeUltimaReactivacion >= umbralReactivacion) {
            String msgReactivacion = intentarReactivarProcesos();
            if (!msgReactivacion.isEmpty()) {
                log.append(msgReactivacion);
                ciclosDesdeUltimaReactivacion = 0;
            }
        }

        // 2. Suspender procesos si la memoria está llena o cerca de estarlo
        if (ciclosDesdeUltimaSuspension >= umbralSuspension) {
            String msgSuspension = intentarSuspenderProcesos();
            if (!msgSuspension.isEmpty()) {
                if (log.length() > 0) log.append("; ");
                log.append(msgSuspension);
                ciclosDesdeUltimaSuspension = 0;
            }
        }

        return log.toString();
    }

    /**
     * Intenta suspender procesos para liberar memoria
     * Prioriza suspender procesos bloqueados sobre procesos listos
     *
     * @return Mensaje descriptivo de las acciones realizadas
     */
    private String intentarSuspenderProcesos() {
        // Verificar si necesitamos suspender
        double porcentajeUso = adminMemoria.getPorcentajeUso();
        int procesosEnMemoria = adminMemoria.getProcesosEnMemoria();
        int capacidadMaxima = adminMemoria.getCapacidadMaxima();

        // Suspender si: memoria > 70% O capacidad >= máxima
        if (porcentajeUso < 70.0 && procesosEnMemoria < capacidadMaxima) {
            return ""; // No es necesario suspender
        }

        StringBuilder log = new StringBuilder();
        int procesosSupendidos = 0;

        // Continuar suspendiendo hasta que la memoria esté por debajo del 60%
        // o no haya más procesos que suspender
        while ((adminMemoria.getPorcentajeUso() >= 60.0 ||
                adminMemoria.getProcesosEnMemoria() >= capacidadMaxima) &&
                procesosSupendidos < 5) { // Límite de seguridad: máx 5 por ciclo

            // 1. Prioridad: Suspender procesos BLOQUEADOS
            Cola colaBloqueados = adminProcesos.getColaBloqueados();
            PCB[] bloqueados = colaBloqueados.toArray();

            if (bloqueados.length > 0) {
                // Suspender el proceso bloqueado con menor prioridad (mayor número)
                PCB candidato = encontrarMenorPrioridad(bloqueados);
                if (candidato != null) {
                    adminMemoria.liberarProceso(candidato);
                    adminProcesos.suspenderProceso(candidato);
                    if (log.length() > 0) log.append("; ");
                    log.append(String.format("Suspendido bloqueado %s(ID:%d)",
                                            candidato.getNombre(), candidato.getIdProceso()));
                    procesosSupendidos++;
                    continue;
                }
            }

            // 2. Si no hay bloqueados, suspender procesos LISTOS
            Cola colaListos = adminProcesos.getColaListos();
            PCB[] listos = colaListos.toArray();

            if (listos.length > 1) { // Mantener al menos 1 proceso listo
                // Suspender el proceso listo con menor prioridad (mayor número)
                PCB candidato = encontrarMenorPrioridad(listos);
                if (candidato != null) {
                    adminMemoria.liberarProceso(candidato);
                    adminProcesos.suspenderProceso(candidato);
                    if (log.length() > 0) log.append("; ");
                    log.append(String.format("Suspendido listo %s(ID:%d)",
                                            candidato.getNombre(), candidato.getIdProceso()));
                    procesosSupendidos++;
                    continue;
                }
            }

            // Si llegamos aquí, no hay más procesos para suspender
            break;
        }

        if (procesosSupendidos > 0) {
            return String.format("Swapper: %d procesos suspendidos. Memoria: %.1f%%",
                               procesosSupendidos, adminMemoria.getPorcentajeUso());
        }

        return "";
    }

    /**
     * Intenta reactivar procesos suspendidos cuando hay memoria disponible
     * Prioriza reactivar procesos suspendidos-listos sobre suspendidos-bloqueados
     *
     * @return Mensaje descriptivo de las acciones realizadas
     */
    private String intentarReactivarProcesos() {
        // Verificar si hay espacio disponible (memoria < 60%)
        double porcentajeUso = adminMemoria.getPorcentajeUso();

        if (porcentajeUso >= 60.0 || !adminMemoria.hayEspacioDisponible()) {
            return ""; // No hay suficiente espacio
        }

        StringBuilder log = new StringBuilder();

        // 1. Prioridad: Reactivar procesos SUSPENDIDOS-LISTOS
        Cola colaSuspListos = adminProcesos.getColaSuspendidosListos();
        PCB[] suspListos = colaSuspListos.toArray();

        if (suspListos.length > 0) {
            // Reactivar el proceso con mayor prioridad (menor número)
            PCB candidato = encontrarMayorPrioridad(suspListos);
            if (candidato != null && adminMemoria.cargarProceso(candidato)) {
                adminProcesos.reactivarSuspendido(candidato);
                log.append(String.format("Reactivado proceso suspendido-listo %s(ID:%d)",
                                        candidato.getNombre(), candidato.getIdProceso()));
                return log.toString();
            }
        }

        // 2. Si no hay suspendidos-listos, reactivar SUSPENDIDOS-BLOQUEADOS
        Cola colaSuspBloq = adminProcesos.getColaSuspendidosBloqueados();
        PCB[] suspBloq = colaSuspBloq.toArray();

        if (suspBloq.length > 0) {
            // Reactivar el proceso con mayor prioridad (menor número)
            PCB candidato = encontrarMayorPrioridad(suspBloq);
            if (candidato != null && adminMemoria.cargarProceso(candidato)) {
                adminProcesos.reactivarSuspendido(candidato);
                log.append(String.format("Reactivado proceso suspendido-bloqueado %s(ID:%d)",
                                        candidato.getNombre(), candidato.getIdProceso()));
                return log.toString();
            }
        }

        return "";
    }

    /**
     * Encuentra el proceso con menor prioridad (mayor número de prioridad)
     *
     * @param procesos Array de procesos
     * @return PCB con menor prioridad, o null si el array está vacío
     */
    private PCB encontrarMenorPrioridad(PCB[] procesos) {
        if (procesos.length == 0) return null;

        PCB menor = procesos[0];
        for (PCB p : procesos) {
            if (p.getPrioridad() > menor.getPrioridad()) {
                menor = p;
            }
        }
        return menor;
    }

    /**
     * Encuentra el proceso con mayor prioridad (menor número de prioridad)
     *
     * @param procesos Array de procesos
     * @return PCB con mayor prioridad, o null si el array está vacío
     */
    private PCB encontrarMayorPrioridad(PCB[] procesos) {
        if (procesos.length == 0) return null;

        PCB mayor = procesos[0];
        for (PCB p : procesos) {
            if (p.getPrioridad() < mayor.getPrioridad()) {
                mayor = p;
            }
        }
        return mayor;
    }

    /**
     * Reinicia el planificador a mediano plazo
     */
    public void reiniciar() {
        ciclosDesdeUltimaSuspension = 0;
        ciclosDesdeUltimaReactivacion = 0;
    }
}

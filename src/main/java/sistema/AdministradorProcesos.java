package sistema;
import modelo.*;
import estructura_datos.*;
import java.util.concurrent.Semaphore;

public class AdministradorProcesos {
    private Cola colaNuevos;
    private Cola colaListos;
    private Cola colaBloqueados;
    private Cola colaSuspendidosListos;
    private Cola colaSuspendidosBloqueados;
    private Lista listaTerminados;
    private int contadorID;
    private Semaphore semaforo;
    
    public AdministradorProcesos() {
        this.colaNuevos = new Cola();
        this.colaListos = new Cola();
        this.colaBloqueados = new Cola();
        this.colaSuspendidosListos = new Cola();
        this.colaSuspendidosBloqueados = new Cola();
        this.listaTerminados = new Lista();
        this.contadorID = 1;
        this.semaforo = new Semaphore(1);
    }
    
    public synchronized PCB crearProceso(String nombre, TipoProceso tipo, int numInstrucciones, 
                                        int prioridad, int tiempoLlegada) {
        PCB nuevoProceso;
        
        // Distinguir constructores de PCB (uno para CPU_BOUND, otro para IO_BOUND)
        if (tipo == TipoProceso.CPU_BOUND) {
            // Constructor para CPU_BOUND (sin E/S)
            nuevoProceso = new PCB(contadorID++, nombre, tipo, numInstrucciones, prioridad, tiempoLlegada);
        } else {
            // Constructor para I/O_BOUND (con E/S)
            // Asumimos valores por defecto para E/S (ej: cada 5 ciclos, dura 8)
            // --- ESTA ES LA LÍNEA CORREGIDA ---
            // Se eliminó el argumento 'tipo' de esta llamada al constructor
            nuevoProceso = new PCB(contadorID++, nombre, numInstrucciones, prioridad, tiempoLlegada, 5, 8);
        }
        colaNuevos.encolar(nuevoProceso);
        return nuevoProceso;
    }
    
    public synchronized void admitirProcesosNuevos(int cicloActual) {
        try {
            semaforo.acquire();
            PCB[] nuevos = colaNuevos.toArray();
            for (PCB proceso : nuevos) {
                if (proceso.getTiempoLlegada() <= cicloActual) {
                    colaNuevos.eliminarPorId(proceso.getIdProceso());
                    proceso.setEstado(EstadoProceso.LISTO);
                    colaListos.encolar(proceso);
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            semaforo.release();
        }
    }

    public synchronized void actualizarTiemposEspera(int cicloActual) {
        try {
            semaforo.acquire();
            PCB[] listos = colaListos.toArray();
            for (PCB proceso : listos) {
                // Llama al método en PCB para incrementar el tiempo de espera
                proceso.incrementarTiempoEspera();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            semaforo.release();
        }
    }
    
    public synchronized void suspenderProceso(PCB proceso) {
        try {
            semaforo.acquire();
            if (proceso.getEstado() == EstadoProceso.LISTO) {
                colaListos.eliminarPorId(proceso.getIdProceso());
                proceso.setEstado(EstadoProceso.SUSPENDIDO_LISTO);
                colaSuspendidosListos.encolar(proceso);
            } else if (proceso.getEstado() == EstadoProceso.BLOQUEADO) {
                colaBloqueados.eliminarPorId(proceso.getIdProceso());
                proceso.setEstado(EstadoProceso.SUSPENDIDO_BLOQUEADO);
                colaSuspendidosBloqueados.encolar(proceso);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            semaforo.release();
        }
    }
    
    public synchronized void reactivarSuspendido(PCB proceso) {
        try {
            semaforo.acquire();
            if (proceso.getEstado() == EstadoProceso.SUSPENDIDO_LISTO) {
                colaSuspendidosListos.eliminarPorId(proceso.getIdProceso());
                proceso.setEstado(EstadoProceso.LISTO);
                colaListos.encolar(proceso);
            } else if (proceso.getEstado() == EstadoProceso.SUSPENDIDO_BLOQUEADO) {
                colaSuspendidosBloqueados.eliminarPorId(proceso.getIdProceso());
                proceso.setEstado(EstadoProceso.BLOQUEADO);
                colaBloqueados.encolar(proceso);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            semaforo.release();
        }
    }
    
    public void reiniciar() {
        colaNuevos.limpiar();
        colaListos.limpiar();
        colaBloqueados.limpiar();
        colaSuspendidosListos.limpiar();
        colaSuspendidosBloqueados.limpiar();
        listaTerminados.limpiar();
        contadorID = 1;
    }
    
    // Getters
    public Cola getColaNuevos() { return colaNuevos; }
    public Cola getColaListos() { return colaListos; }
    public Cola getColaBloqueados() { return colaBloqueados; }
    public Cola getColaSuspendidosListos() { return colaSuspendidosListos; }
    public Cola getColaSuspendidosBloqueados() { return colaSuspendidosBloqueados; }
    public Lista getListaTerminados() { return listaTerminados; }
}
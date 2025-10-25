package excepciones;
import modelo.PCB;
import modelo.EstadoProceso;
import estructura_datos.Cola;
import java.util.concurrent.Semaphore;

public class ManejadorExcepciones {
    private Cola colaBloqueados;
    private Cola colaListos;
    private Semaphore semaforoExcepciones;
    
    public ManejadorExcepciones(Cola colaBloqueados, Cola colaListos) {
        this.colaBloqueados = colaBloqueados;
        this.colaListos = colaListos;
        this.semaforoExcepciones = new Semaphore(1);
    }
    
    public void procesarExcepciones() {
        try {
            semaforoExcepciones.acquire();
            PCB[] bloqueados = colaBloqueados.toArray();
            for (PCB proceso : bloqueados) {
                if (proceso.avanzarExcepcion()) {
                    colaBloqueados.eliminarPorId(proceso.getIdProceso());
                    proceso.setEstado(EstadoProceso.LISTO);
                    colaListos.encolar(proceso);
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            semaforoExcepciones.release();
        }
    }
    
    public void agregarProcesoBloqueado(PCB proceso) {
        try {
            semaforoExcepciones.acquire();
            proceso.setEstado(EstadoProceso.BLOQUEADO);
            colaBloqueados.encolar(proceso);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            semaforoExcepciones.release();
        }
    }
}

package Clases;

/*
 *
 * @author Diego A. Vivolo
 */

public class PCB {

    /**
     * ESTADOS POSIBLES DE UN PROCESO
     */
    public enum Estado {
        NUEVO,
        LISTO,
        EJECUCION,
        BLOQUEADO,
        TERMINADO,
        LISTO_SUSPENDIDO,
        BLOQUEADO_SUSPENDIDO
    }

    /**
     * TIPO DE PROCESO
     */
    public enum TipoProceso {
        CPU_BOUND,
        IO_BOUND
    }

    private static int contadorId = 0;
    private final int id;
    private final String nombre;
    private Estado estado;
    private int programCounter;
    private int memoryAddressRegister;
    private final int totalInstrucciones;
    private final TipoProceso tipoProceso;

    private final int ciclosParaExcepcion;
    private final int ciclosParaCompletarExcepcion;
    private int ciclosEjecutadosDesdeIO;
    private final int prioridad;
    

    public PCB(String nombre, int totalInstrucciones, int prioridad) {
        this.id = ++contadorId;
        this.nombre = nombre;
        this.totalInstrucciones = totalInstrucciones;
        this.tipoProceso = TipoProceso.CPU_BOUND;
        this.prioridad = prioridad;
        

        this.ciclosParaExcepcion = -1;
        this.ciclosParaCompletarExcepcion = -1; 
        
        // Valores iniciales
        this.estado = Estado.NUEVO;
        this.programCounter = 0;
        this.memoryAddressRegister = 0;
        this.ciclosEjecutadosDesdeIO = 0;
    }


    public PCB(String nombre, int totalInstrucciones, int ciclosParaExcepcion, int ciclosParaCompletarExcepcion, int prioridad) {
        this.id = ++contadorId;
        this.nombre = nombre;
        this.totalInstrucciones = totalInstrucciones;
        this.tipoProceso = TipoProceso.IO_BOUND;
        
        this.ciclosParaExcepcion = ciclosParaExcepcion;
        this.ciclosParaCompletarExcepcion = ciclosParaCompletarExcepcion;
        this.prioridad = prioridad; // Esta asignación ahora es válida
        
        // Valores iniciales
        this.estado = Estado.NUEVO;
        this.programCounter = 0;
        this.memoryAddressRegister = 0;
        this.ciclosEjecutadosDesdeIO = 0;
    }

    public void ejecutarCiclo() {
        if (this.estado == Estado.EJECUCION) {
            this.programCounter++;
            this.memoryAddressRegister++; 
            
            if (this.tipoProceso == TipoProceso.IO_BOUND) {
                this.ciclosEjecutadosDesdeIO++;
            }
        }
    }

    /**
     * REVISA QUE EL PROCESO HAYA TERMINADO SUS INSTRUCCIONES
     */
    public boolean haTerminado() {
        return this.programCounter >= this.totalInstrucciones;
    }
    /**
     *REVISA QUE SE HAYA HECHO UNA EXCEPCION DE E/S
     */
    public boolean debeGenerarExcepcionIO() {
        return this.tipoProceso == TipoProceso.IO_BOUND && 
               this.ciclosEjecutadosDesdeIO >= this.ciclosParaExcepcion;
    }

    /**
     *REINICIA CONTADOR DE CICOS
     */
    public void reiniciarContadorIO() {
        this.ciclosEjecutadosDesdeIO = 0;
    }
    
    /**
     *DEVUELVE TIEMPO RESTANTE
     */
    public int getTiempoRestante() {
        return this.totalInstrucciones - this.programCounter;
    }

    public int getPrioridad() {
            return this.prioridad;
    }
    
    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public Estado getEstado() {
        return estado;
    }

    public void setEstado(Estado estado) {
        this.estado = estado;
    }

    public int getProgramCounter() {
        return programCounter;
    }

    public void setProgramCounter(int programCounter) {
        this.programCounter = programCounter;
    }

    public int getMemoryAddressRegister() {
        return memoryAddressRegister;
    }

    public void setMemoryAddressRegister(int memoryAddressRegister) {
        this.memoryAddressRegister = memoryAddressRegister;
    }

    public int getTotalInstrucciones() {
        return totalInstrucciones;
    }

    public TipoProceso getTipoProceso() {
        return tipoProceso;
    }

    public int getCiclosParaExcepcion() {
        return ciclosParaExcepcion;
    }

    public int getCiclosParaCompletarExcepcion() {
        return ciclosParaCompletarExcepcion;
    }


    @Override
    public String toString() {

        String info = String.format("PCB %d [%s] - %s | PC: %d/%d",
                id,
                nombre,
                estado,
                programCounter,
                totalInstrucciones 
        );
        if (tipoProceso == TipoProceso.IO_BOUND) {
            info += String.format(" | IO (Ciclos: %d/%d)", 
                    ciclosEjecutadosDesdeIO, ciclosParaExcepcion);
        }
        return info;
    }
}
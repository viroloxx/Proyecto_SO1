package Clases;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
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
    
    /**
     * Constructor para un proceso CPU-Bound.
     * @param nombre El nombre del proceso.
     * @param totalInstrucciones La cantidad total de instrucciones.
     */
    public PCB(String nombre, int totalInstrucciones) {
        this.id = ++contadorId;
        this.nombre = nombre;
        this.totalInstrucciones = totalInstrucciones;
        this.tipoProceso = TipoProceso.CPU_BOUND;
        
        // Valores no aplicables para CPU-bound
        this.ciclosParaExcepcion = -1;
        this.ciclosParaCompletarExcepcion = -1; 
        
        // Valores iniciales
        this.estado = Estado.NUEVO;
        this.programCounter = 0;
        this.memoryAddressRegister = 0;
        this.ciclosEjecutadosDesdeIO = 0;
    }

    /**
     * Constructor para un proceso I/O-Bound.
     * @param nombre El nombre del proceso.
     * @param totalInstrucciones La cantidad total de instrucciones.
     * @param ciclosParaExcepcion Ciclos para generar una E/S.
     * @param ciclosParaCompletarExcepcion Ciclos para resolver la E/S.
     */
    public PCB(String nombre, int totalInstrucciones, int ciclosParaExcepcion, int ciclosParaCompletarExcepcion) {
        this.id = ++contadorId;
        this.nombre = nombre;
        this.totalInstrucciones = totalInstrucciones;
        this.tipoProceso = TipoProceso.IO_BOUND;
        
        this.ciclosParaExcepcion = ciclosParaExcepcion;
        this.ciclosParaCompletarExcepcion = ciclosParaCompletarExcepcion;
        
        // Valores iniciales
        this.estado = Estado.NUEVO;
        this.programCounter = 0;
        this.memoryAddressRegister = 0;
        this.ciclosEjecutadosDesdeIO = 0;
    }

    // --- Métodos de Simulación ---

    /**
     * Simula la ejecución de un ciclo de reloj para este proceso.
     * Incrementa el PC y el MAR en una unidad.
     * También incrementa el contador de ciclos para la próxima E/S si aplica.
     */
    public void ejecutarCiclo() {
        if (this.estado == Estado.EJECUCION) {
            this.programCounter++;
            this.memoryAddressRegister++; // PC y MAR incrementan linealmente
            
            if (this.tipoProceso == TipoProceso.IO_BOUND) {
                this.ciclosEjecutadosDesdeIO++;
            }
        }
    }

    /**
     * Verifica si el proceso ha completado todas sus instrucciones.
     * @return true si PC >= totalInstrucciones, false de lo contrario.
     */
    public boolean haTerminado() {
        return this.programCounter >= this.totalInstrucciones;
    }
    
    /**
     * Verifica si el proceso debe generar una excepción de E/S en este ciclo.
     * @return true si es I/O-bound y ha cumplido sus ciclos, false de lo contrario.
     */
    public boolean debeGenerarExcepcionIO() {
        return this.tipoProceso == TipoProceso.IO_BOUND && 
               this.ciclosEjecutadosDesdeIO >= this.ciclosParaExcepcion;
    }

    /**
     * Reinicia el contador de ciclos de E/S.
     * Se debe llamar después de que una excepción de E/S es manejada.
     */
    public void reiniciarContadorIO() {
        this.ciclosEjecutadosDesdeIO = 0;
    }

    // --- Getters y Setters ---
    
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

    /**
     * Representación en String del PCB, útil para debugging y logs.
     */
    @Override
    public String toString() {
        String info = String.format("PCB %d [%s] - %s | PC: %d/%d",
                id,
                nombre,
                estado,
                programCounter,
                totalInstruE
        if (tipoProceso == TipoProceso.IO_BOUND) {
            info += String.format(" | IO (Ciclos: %d/%d)", 
                    ciclosEjecutadosDesdeIO, ciclosParaExcepcion);
        }
        return info;
    }
}
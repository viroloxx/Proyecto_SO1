package modelo;

public class PCB {
    private final int idProceso;
    private final String nombre;
    private EstadoProceso estado;
    private final TipoProceso tipo;
    private int programCounter;
    private int memoryAddressRegister;
    private int prioridad;
    private int tiempoLlegada;
    private int tiempoEjecucion;
    private int tiempoRestante;
    private int tiempoEsperaTotal;
    private int ciclosParaExcepcion;
    private int ciclosExcepcionActual;
    private int ciclosParaSatisfacerExcepcion;
    private int ciclosExcepcionRestantes;
    private int tiempoRespuesta;
    private int tiempoRetorno;
    private boolean primeraEjecucion;
    private int tiempoInicioPrimeraEjecucion;
    
    public PCB(int id, String nombre, TipoProceso tipo, int numInstrucciones, int prioridad, int tiempoLlegada) {
        this.idProceso = id;
        this.nombre = nombre;
        this.tipo = tipo;
        this.estado = EstadoProceso.NUEVO;
        this.programCounter = 0;
        this.memoryAddressRegister = 0;
        this.prioridad = prioridad;
        this.tiempoLlegada = tiempoLlegada;
        this.tiempoEjecucion = numInstrucciones;
        this.tiempoRestante = numInstrucciones;
        this.tiempoEsperaTotal = 0;
        this.primeraEjecucion = true;
        this.ciclosParaExcepcion = 0;
        this.ciclosExcepcionActual = 0;
        this.ciclosParaSatisfacerExcepcion = 0;
        this.ciclosExcepcionRestantes = 0;
        this.tiempoInicioPrimeraEjecucion = -1;
    }
    
    public PCB(int id, String nombre, int numInstrucciones, int prioridad, int tiempoLlegada, 
               int ciclosParaExcepcion, int ciclosParaSatisfacerExcepcion) {
        this(id, nombre, TipoProceso.IO_BOUND, numInstrucciones, prioridad, tiempoLlegada);
        this.ciclosParaExcepcion = ciclosParaExcepcion;
        this.ciclosParaSatisfacerExcepcion = ciclosParaSatisfacerExcepcion;
    }
    
    public boolean ejecutarCiclo() {
        if (estado != EstadoProceso.EJECUCION) return false;
        programCounter++;
        memoryAddressRegister++;
        tiempoRestante--;
        if (tipo == TipoProceso.IO_BOUND && ciclosParaExcepcion > 0) {
            ciclosExcepcionActual++;
            if (ciclosExcepcionActual >= ciclosParaExcepcion) {
                ciclosExcepcionActual = 0;
                ciclosExcepcionRestantes = ciclosParaSatisfacerExcepcion;
                return true;
            }
        }
        return false;
    }
    
    public boolean avanzarExcepcion() {
        if (ciclosExcepcionRestantes > 0) {
            ciclosExcepcionRestantes--;
            return ciclosExcepcionRestantes == 0;
        }
        return true;
    }
    
    public boolean haTerminado() {
        return tiempoRestante <= 0;
    }
    
    public void incrementarTiempoEspera() {
        if (estado == EstadoProceso.LISTO) tiempoEsperaTotal++;
    }
    
    public void registrarInicioPrimeraEjecucion(int cicloActual) {
        if (primeraEjecucion && tiempoInicioPrimeraEjecucion == -1) {
            tiempoInicioPrimeraEjecucion = cicloActual;
            tiempoRespuesta = cicloActual - tiempoLlegada;
        }
    }
    
    public void registrarTiempoRetorno(int cicloActual) {
        this.tiempoRetorno = cicloActual - tiempoLlegada;
    }
    
    // Getters
    public int getIdProceso() { return idProceso; }
    public String getNombre() { return nombre; }
    public EstadoProceso getEstado() { return estado; }
    public TipoProceso getTipo() { return tipo; }
    public int getProgramCounter() { return programCounter; }
    public int getMemoryAddressRegister() { return memoryAddressRegister; }
    public int getPrioridad() { return prioridad; }
    public int getTiempoLlegada() { return tiempoLlegada; }
    public int getTiempoEjecucion() { return tiempoEjecucion; }
    public int getTiempoRestante() { return tiempoRestante; }
    public int getTiempoEsperaTotal() { return tiempoEsperaTotal; }
    public int getTiempoRespuesta() { return tiempoRespuesta; }
    public int getTiempoRetorno() { return tiempoRetorno; }
    public int getCiclosExcepcionRestantes() { return ciclosExcepcionRestantes; }
    public int getCiclosParaExcepcion() { return ciclosParaExcepcion; }
    public int getCiclosParaSatisfacerExcepcion() { return ciclosParaSatisfacerExcepcion; }
    public boolean isPrimeraEjecucion() { return primeraEjecucion; }
    
    // Setters
    public void setEstado(EstadoProceso estado) { this.estado = estado; }
    public void setPrioridad(int prioridad) { this.prioridad = prioridad; }
    public void setPrimeraEjecucion(boolean primeraEjecucion) { this.primeraEjecucion = primeraEjecucion; }
    
    @Override
    public String toString() {
        return String.format("%s(ID:%d,PC:%d,Rest:%d)", nombre, idProceso, programCounter, tiempoRestante);
    }
}

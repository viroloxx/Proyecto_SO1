package modelo;

public enum TipoProceso {
    CPU_BOUND, IO_BOUND;
    
    @Override
    public String toString() {
        return this == CPU_BOUND ? "CPU Bound" : "I/O Bound";
    }
}

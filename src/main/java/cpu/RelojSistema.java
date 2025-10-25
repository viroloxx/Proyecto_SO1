package cpu;

public class RelojSistema {
    private int cicloActual;
    private int duracionCicloMs;
    private boolean pausado;
    
    public RelojSistema(int duracionCicloMs) {
        this.cicloActual = 0;
        this.duracionCicloMs = duracionCicloMs;
        this.pausado = false;
    }
    
    public void incrementarCiclo() {
        cicloActual++;
    }
    
    public void reiniciar() {
        cicloActual = 0;
    }
    
    public int getCicloActual() {
        return cicloActual;
    }
    
    public void setDuracionCicloMs(int duracionMs) {
        if (duracionMs > 0) this.duracionCicloMs = duracionMs;
    }
    
    public int getDuracionCicloMs() {
        return duracionCicloMs;
    }
    
    public void pausar() {
        this.pausado = true;
    }
    
    public void reanudar() {
        this.pausado = false;
    }
    
    public boolean estaPausado() {
        return pausado;
    }
    
    public void esperarCiclo() {
        try {
            Thread.sleep(duracionCicloMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

package persistencia;

public class ConfiguracionSistema {
    private int duracionCicloMs;
    private String planificadorInicial;
    private int quantumRR;
    
    public ConfiguracionSistema() {
        this.duracionCicloMs = 500;
        this.planificadorInicial = "FCFS";
        this.quantumRR = 3;
    }
    
    public ConfiguracionSistema(int duracionCicloMs, String planificadorInicial, int quantumRR) {
        this.duracionCicloMs = duracionCicloMs;
        this.planificadorInicial = planificadorInicial;
        this.quantumRR = quantumRR;
    }
    
    public int getDuracionCicloMs() { return duracionCicloMs; }
    public void setDuracionCicloMs(int duracionCicloMs) { this.duracionCicloMs = duracionCicloMs; }
    public String getPlanificadorInicial() { return planificadorInicial; }
    public void setPlanificadorInicial(String planificadorInicial) { this.planificadorInicial = planificadorInicial; }
    public int getQuantumRR() { return quantumRR; }
    public void setQuantumRR(int quantumRR) { this.quantumRR = quantumRR; }
}

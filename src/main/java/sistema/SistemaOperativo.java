package sistema;
import modelo.*;
import cpu.*;
import planificacion.*;
import excepciones.*;
import metricas.*;
import persistencia.*;
import estructura_datos.*;

public class SistemaOperativo implements Runnable {
    private RelojSistema reloj;
    private CPU cpu;
    private AdministradorProcesos adminProcesos;
    private ManejadorExcepciones manejadorExc;
    private Metricas metricas;
    private Planificador planificadorActual;
    private boolean ejecutando;
    private StringBuilder logEventos;
    private int quantumPorDefecto; 

    public SistemaOperativo(ConfiguracionSistema config) {
        this.reloj = new RelojSistema(config.getDuracionCicloMs());
        this.cpu = new CPU();
        this.adminProcesos = new AdministradorProcesos();
        this.manejadorExc = new ManejadorExcepciones(
            adminProcesos.getColaBloqueados(),
            adminProcesos.getColaListos()
        );
        this.metricas = new Metricas();
        this.planificadorActual = new FCFS(); // El planificador inicial se establece en el GUI
        this.ejecutando = false;
        this.logEventos = new StringBuilder();
        this.quantumPorDefecto = config.getQuantumRR(); // 
    }
    
    public synchronized void cambiarPlanificador(Planificador nuevoPlanificador) {
        this.planificadorActual = nuevoPlanificador;
        // Reorganizar la cola según el nuevo algoritmo (ej. SJF o Prioridad)
        planificadorActual.reorganizarCola(adminProcesos.getColaListos());
        agregarLog("Planificador cambiado a: " + nuevoPlanificador.obtenerNombre());
    }
    
    public void agregarProceso(String nombre, TipoProceso tipo, int numInstrucciones, int prioridad) {
        PCB proceso = adminProcesos.crearProceso(nombre, tipo, numInstrucciones, prioridad, reloj.getCicloActual());
        agregarLog(String.format("Nuevo proceso %s(ID:%d) creado.", proceso.getNombre(), proceso.getIdProceso()));
    }
    
    @Override
    public void run() {
        agregarLog("Sistema iniciado");
        while (ejecutando) {
            try {
                // Pausa
                while (reloj.estaPausado() && ejecutando) {
                    Thread.sleep(200);
                }
                if (!ejecutando) break;
                
                // 1. Ejecutar ciclo de reloj
                reloj.esperarCiclo();
                reloj.incrementarCiclo();
                int ciclo = reloj.getCicloActual();
                
                // 2. Admitir nuevos procesos
                adminProcesos.admitirProcesosNuevos(ciclo);
                
                // 3. Procesar E/S (Excepciones)
                manejadorExc.procesarExcepciones();
                
                // 4. Incrementar tiempo de espera de procesos en cola de listos
                adminProcesos.actualizarTiemposEspera(ciclo);
                
                // 5. Ejecución de CPU
                PCB procesoEnCPU = cpu.getProcesoActual();
                
                // 5a. ¿Hay que expropiar el proceso actual?
                if (procesoEnCPU != null && planificadorActual.esExpropriativo()) {
                    boolean expropiar = planificadorActual.debeExpropriar(
                        procesoEnCPU, 
                        adminProcesos.getColaListos(), 
                        cpu.getCiclosProcesoActual()
                    );
                    
                    if (expropiar) {
                        agregarLog(String.format("Planificador expropia a %s(ID:%d)", procesoEnCPU.getNombre(), procesoEnCPU.getIdProceso()));
                        PCB p = cpu.liberarProceso();
                        p.setEstado(EstadoProceso.LISTO);
                        adminProcesos.getColaListos().encolar(p);
                        procesoEnCPU = null;
                    }
                }
                
                // 5b. Si CPU está libre, planificar
                if (procesoEnCPU == null) {
                    procesoEnCPU = planificadorActual.seleccionarSiguienteProceso(adminProcesos.getColaListos());
                    if (procesoEnCPU != null) {
                        cpu.cargarProceso(procesoEnCPU);
                        agregarLog(String.format("CPU carga a %s(ID:%d)", procesoEnCPU.getNombre(), procesoEnCPU.getIdProceso()));
                        procesoEnCPU.registrarInicioPrimeraEjecucion(ciclo); // Registrar tiempo de respuesta
                    }
                }
                
                // 5c. Ejecutar ciclo de CPU
                if (procesoEnCPU != null) {
                    boolean generoExcepcion = cpu.ejecutarCiclo(ciclo);
                    
                    if (generoExcepcion) {
                        // Proceso genera E/S (Bloqueado)
                        agregarLog(String.format("%s(ID:%d) genera E/S y se bloquea.", procesoEnCPU.getNombre(), procesoEnCPU.getIdProceso()));
                        PCB p = cpu.liberarProceso();
                        manejadorExc.agregarProcesoBloqueado(p);
                    } else if (procesoEnCPU.getTiempoRestante() == 0) {
                        // Proceso terminado
                        agregarLog(String.format("%s(ID:%d) ha terminado.", procesoEnCPU.getNombre(), procesoEnCPU.getIdProceso()));
                        PCB p = cpu.liberarProceso();
                        p.setEstado(EstadoProceso.TERMINADO);
                        adminProcesos.getListaTerminados().agregar(p);
                        metricas.registrarProcesoCompletado(p, ciclo);
                    }
                    metricas.registrarCiclo(true); // CPU ocupada
                } else {
                    metricas.registrarCiclo(false); // CPU libre
                }
                
            } catch (Exception e) {
                System.err.println("Error en el ciclo principal del SO: " + e);
                e.printStackTrace();
            }
        }
    }
    
    public void iniciar() {
        if (!ejecutando) {
            ejecutando = true;
            new Thread(this).start();
        }
    }
    
    public void pausar() {
        reloj.pausar();
        agregarLog("Sistema pausado");
    }
    
    public void reanudar() {
        reloj.reanudar();
        agregarLog("Sistema reanudado");
    }
    
    public void detener() {
        ejecutando = false;
        agregarLog("Sistema detenido");
    }
    
    public void reiniciar() {
        detener();
        // Esperar un momento a que el hilo termine
        try { Thread.sleep(reloj.getDuracionCicloMs() + 50); } catch (InterruptedException e) {}
        
        reloj.reiniciar();
        cpu = new CPU();
        adminProcesos.reiniciar();
        metricas.reiniciar();
        logEventos = new StringBuilder();
        agregarLog("Sistema reiniciado");
    }
    
    private void agregarLog(String mensaje) {
        String logCompleto = String.format("[Ciclo %04d] %s\n", reloj.getCicloActual(), mensaje);
        logEventos.append(logCompleto);
        // Limitar el log para no consumir memoria infinita
        if (logEventos.length() > 10000) {
            logEventos.delete(0, 5000);
        }
    }
    
    public String getLog() {
        return logEventos.toString();
    }
    
    public String getUltimasLineasLog(int lineas) {
        String[] todasLineas = logEventos.toString().split("\n");
        int inicio = Math.max(0, todasLineas.length - lineas);
        StringBuilder resultado = new StringBuilder();
        for (int i = inicio; i < todasLineas.length; i++) {
            resultado.append(todasLineas[i]).append("\n");
        }
        return resultado.toString();
    }
    
    // Getters
    public RelojSistema getReloj() { return reloj; }
    public CPU getCpu() { return cpu; }
    public AdministradorProcesos getAdminProcesos() { return adminProcesos; }
    public Metricas getMetricas() { return metricas; }
    public Planificador getPlanificadorActual() { return planificadorActual; }
    public boolean isEjecutando() { return ejecutando; }
    public boolean estaPausado() { return reloj.estaPausado(); }
    
    // <-- CAMBIO 3: Getter añadido -->
    public int getQuantumPorDefecto() {
        return this.quantumPorDefecto;
    }
}
package sistema;
import modelo.*;
import cpu.*;
import planificacion.*;
import excepciones.*;
import metricas.*;
import persistencia.*;
import estructura_datos.*;
import memoria.AdministradorMemoria;

public class SistemaOperativo implements Runnable {
    private RelojSistema reloj;
    private CPU cpu;
    private AdministradorProcesos adminProcesos;
    private ManejadorExcepciones manejadorExc;
    private Metricas metricas;
    private Planificador planificadorActual;
    private AdministradorMemoria adminMemoria;
    private PlanificadorMedianoPlazo planificadorMP;
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

        // Inicializar gestión de memoria
        // Valores por defecto: 10 procesos máximo, 5000 unidades de memoria
        this.adminMemoria = new AdministradorMemoria(10, 5000);
        this.planificadorMP = new PlanificadorMedianoPlazo(adminProcesos, adminMemoria);

        this.ejecutando = false;
        this.logEventos = new StringBuilder();
        this.quantumPorDefecto = config.getQuantumRR();
    }
    
    public synchronized void cambiarPlanificador(Planificador nuevoPlanificador) {
        this.planificadorActual = nuevoPlanificador;
        // Reorganizar la cola según el nuevo algoritmo (ej. SJF o Prioridad)
        planificadorActual.reorganizarCola(adminProcesos.getColaListos());
        agregarLog("Planificador cambiado a: " + nuevoPlanificador.obtenerNombre());
    }

    /**
     * Planificador a Largo Plazo: Admite procesos de la cola de nuevos
     * Solo los admite si hay espacio en memoria disponible
     *
     * @param cicloActual Ciclo actual del sistema
     */
    private void admitirProcesosConMemoria(int cicloActual) {
        PCB[] nuevos = adminProcesos.getColaNuevos().toArray();

        for (PCB proceso : nuevos) {
            if (proceso.getTiempoLlegada() <= cicloActual) {
                // Intentar cargar el proceso en memoria
                if (adminMemoria.cargarProceso(proceso)) {
                    // Si hay espacio, admitir SOLO este proceso específico
                    adminProcesos.admitirProceso(proceso);
                    agregarLog(String.format("Proceso %s(ID:%d) admitido y cargado en memoria",
                                           proceso.getNombre(), proceso.getIdProceso()));
                } else {
                    // Si no hay espacio, el proceso permanece en la cola de nuevos
                    // Solo logueamos una vez por ciclo para evitar spam
                    if (adminProcesos.getColaNuevos().obtenerTamanio() > 0 &&
                        proceso == nuevos[0]) {
                        agregarLog(String.format("Memoria llena: %d procesos esperando admisión",
                                               nuevos.length));
                    }
                    break; // No intentar admitir más si la memoria está llena
                }
            }
        }
    }
    
    public void agregarProceso(String nombre, TipoProceso tipo, int numInstrucciones, int prioridad) {
        PCB proceso = adminProcesos.crearProceso(nombre, tipo, numInstrucciones, prioridad, reloj.getCicloActual());
        agregarLog(String.format("Nuevo proceso %s(ID:%d) creado.", proceso.getNombre(), proceso.getIdProceso()));
    }

    /**
     * Agrega un nuevo proceso I/O Bound con parámetros personalizados de E/S
     *
     * @param nombre Nombre del proceso
     * @param tipo Tipo de proceso (debe ser IO_BOUND)
     * @param numInstrucciones Número de instrucciones
     * @param prioridad Prioridad del proceso
     * @param ciclosParaExcepcion Ciclos necesarios para generar una excepción
     * @param ciclosParaSatisfacerExcepcion Ciclos necesarios para satisfacer la excepción
     */
    public void agregarProceso(String nombre, TipoProceso tipo, int numInstrucciones, int prioridad,
                              int ciclosParaExcepcion, int ciclosParaSatisfacerExcepcion) {
        PCB proceso = adminProcesos.crearProceso(nombre, tipo, numInstrucciones, prioridad,
                                                reloj.getCicloActual(), ciclosParaExcepcion,
                                                ciclosParaSatisfacerExcepcion);
        agregarLog(String.format("Nuevo proceso %s(ID:%d) creado. E/S cada %d ciclos, duración %d ciclos.",
                                proceso.getNombre(), proceso.getIdProceso(),
                                ciclosParaExcepcion, ciclosParaSatisfacerExcepcion));
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
                
                // 2. Planificador a Largo Plazo: Admitir nuevos procesos
                admitirProcesosConMemoria(ciclo);

                // 3. Planificador a Mediano Plazo: Gestionar suspensiones
                String logMP = planificadorMP.ejecutarCiclo(ciclo);
                if (!logMP.isEmpty()) {
                    agregarLog(logMP);
                }

                // 4. Procesar E/S (Excepciones)
                manejadorExc.procesarExcepciones();
                
                // 5. Incrementar tiempo de espera de procesos en cola de listos
                adminProcesos.actualizarTiemposEspera(ciclo);

                // 6. Planificador a Corto Plazo: Ejecución de CPU
                PCB procesoEnCPU = cpu.getProcesoActual();

                // 6a. ¿Hay que expropiar el proceso actual?
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
                
                // 6b. Si CPU está libre, planificar
                if (procesoEnCPU == null) {
                    procesoEnCPU = planificadorActual.seleccionarSiguienteProceso(adminProcesos.getColaListos());
                    if (procesoEnCPU != null) {
                        cpu.cargarProceso(procesoEnCPU);
                        agregarLog(String.format("CPU carga a %s(ID:%d)", procesoEnCPU.getNombre(), procesoEnCPU.getIdProceso()));
                        procesoEnCPU.registrarInicioPrimeraEjecucion(ciclo); // Registrar tiempo de respuesta
                    }
                }
                
                // 6c. Ejecutar ciclo de CPU
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
                        // Liberar memoria del proceso terminado
                        adminMemoria.liberarProceso(p);
                    }
                    metricas.registrarCiclo(true); // CPU ocupada
                } else {
                    metricas.registrarCiclo(false); // CPU libre
                }

                // Registrar historial de métricas para el gráfico
                PCB[] todosLosProcesos = adminProcesos.obtenerTodosLosProcesos();
                PCB[] procesosTerminados = adminProcesos.getListaTerminados().toArray();
                int procesosActivos = todosLosProcesos.length - procesosTerminados.length;
                metricas.registrarHistorial(ciclo, todosLosProcesos, procesosTerminados, procesosActivos);

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
        adminMemoria.reiniciar();
        planificadorMP.reiniciar();
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
    public AdministradorMemoria getAdminMemoria() { return adminMemoria; }
    public PlanificadorMedianoPlazo getPlanificadorMP() { return planificadorMP; }
    public boolean isEjecutando() { return ejecutando; }
    public boolean estaPausado() { return reloj.estaPausado(); }

    public int getQuantumPorDefecto() {
        return this.quantumPorDefecto;
    }
}
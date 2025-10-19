package Clases;

/**
 *
 * @author Diego A. Vivolo

 */
import Planificadores.P_FCFS;
import Planificadores.P_PA;
import Planificadores.P_RoundRobin;
import Planificadores.P_PNA;
import Planificadores.P_SJF;
import Planificadores.P_SRTF;


public class CPU {

    private int cicloGlobal;

    // Colas de estado de procesos
    private Cola<PCB> colaNuevos;
    private Cola<PCB> colaListos;
    private Cola<PCB> colaBloqueados;
    private Cola<PCB> colaTerminados;
    private Cola<PCB> colaListosSuspendidos;
    private Cola<PCB> colaBloqueadosSuspendidos;
    
    private PCB procesoEnEjecucion;

    // --- VARIABLES AÑADIDAS (Faltaban en tu archivo) ---
    private IPlanificador planificadorActual;
    private int quantum;
    private int ciclosEnQuantumActual;

    public CPU() {
        this.cicloGlobal = 0;
        this.procesoEnEjecucion = null;

        this.colaNuevos = new Cola<>();
        this.colaListos = new Cola<>();
        this.colaBloqueados = new Cola<>();
        this.colaTerminados = new Cola<>();
        this.colaListosSuspendidos = new Cola<>();
        this.colaBloqueadosSuspendidos = new Cola<>();
        
        // --- INICIALIZACIONES AÑADIDAS ---
        // Iniciar con FCFS por defecto
        this.planificadorActual = new P_FCFS(); 
        this.quantum = 5; // Quantum por defecto para Round Robin
        this.ciclosEnQuantumActual = 0;
    }
    
    public void setQuantum(int duracion) {
         this.quantum = duracion;
     }


    public void setPlanificador(IPlanificador nuevaPolitica) {
        System.out.println("--- CAMBIO DE PLANIFICADOR A: " + nuevaPolitica.getClass().getSimpleName() + " ---");
        this.planificadorActual = nuevaPolitica;
        this.ciclosEnQuantumActual = 0; // Reiniciar contador de quantum
    }

    public void agregarProceso(PCB nuevoProceso) {
        nuevoProceso.setEstado(PCB.Estado.NUEVO);
        this.colaNuevos.encolar(nuevoProceso);
        System.out.println("Proceso " + nuevoProceso.getNombre() + " (ID: " + nuevoProceso.getId() + ") ha entrado al sistema en cola NUEVOS.");
    }

    // --- NUEVO MÉTODO DE AYUDA (Para Apropiación) ---
    

    private void manejarLlegadaProcesoListo(PCB pcbQueLlega) {
        // Poner el proceso en la cola de listos
        pcbQueLlega.setEstado(PCB.Estado.LISTO);
        this.colaListos.encolar(pcbQueLlega);
        System.out.println("Proceso " + pcbQueLlega.getNombre() + " movido a LISTOS.");

        // --- LÓGICA DE APROPIACIÓN ---
        
        // Si la CPU está ociosa, no hay nada que interrumpir.
        if (this.procesoEnEjecucion == null) {
            return;
        }

        boolean interrumpir = false;

        // 1. Verificar apropiación por SRTF
        if (this.planificadorActual instanceof P_SRTF) {
            if (pcbQueLlega.getTiempoRestante() < this.procesoEnEjecucion.getTiempoRestante()) {
                System.out.println("INTERRUPCIÓN (SRTF): " + pcbQueLlega.getNombre() + 
                                   " (TR:" + pcbQueLlega.getTiempoRestante() + 
                                   ") es más corto que " + this.procesoEnEjecucion.getNombre() +
                                   " (TR:" + this.procesoEnEjecucion.getTiempoRestante() + ")");
                interrumpir = true;
            }
        }
        // 2. Verificar apropiación por Prioridad
        else if (this.planificadorActual instanceof P_PA) {
            if (pcbQueLlega.getPrioridad() < this.procesoEnEjecucion.getPrioridad()) {
                 System.out.println("INTERRUPCIÓN (Prioridad): " + pcbQueLlega.getNombre() + 
                                   " (P:" + pcbQueLlega.getPrioridad() + 
                                   ") tiene más prioridad que " + this.procesoEnEjecucion.getNombre() +
                                   " (P:" + this.procesoEnEjecucion.getPrioridad() + ")");
                interrumpir = true;
            }
        }

        // Si se decidió interrumpir
        if (interrumpir) {
            System.out.println("Proceso " + this.procesoEnEjecucion.getNombre() + " interrumpido y devuelto a LISTOS.");
            // Devolver el proceso en ejecución a la cola de listos
            this.procesoEnEjecucion.setEstado(PCB.Estado.LISTO);
            this.colaListos.encolar(this.procesoEnEjecucion);
            this.procesoEnEjecucion = null; // Dejar la CPU ociosa para el siguiente ciclo
        }
    }

    /**
     * Simula un único ciclo de reloj ("tick") en el sistema.
     * (ACTUALIZADO para incluir apropiación y lógica completa)
     */
    public void ejecutarCiclo() {
            this.cicloGlobal++;
            System.out.println("--- CICLO GLOBAL: " + this.cicloGlobal + " ---");

            // 1. Mover procesos de NUEVO a LISTO (ACTUALIZADO)
            if (!this.colaNuevos.estaVacia()) {
                PCB pcb = this.colaNuevos.desencolar();
                // Usamos el nuevo método que contiene la lógica de apropiación
                manejarLlegadaProcesoListo(pcb);
            }
            
            // (Recordatorio: FASE 4 - cuando un proceso de E/S termine,
            // su hilo también debe llamar a manejarLlegadaProcesoListo() )

            // 2. Lógica del Planificador de Corto Plazo (sin cambios)
            if (this.procesoEnEjecucion == null && !this.colaListos.estaVacia()) {

                this.procesoEnEjecucion = this.planificadorActual.seleccionarSiguienteProceso(this.colaListos);

                this.procesoEnEjecucion.setEstado(PCB.Estado.EJECUCION);
                this.ciclosEnQuantumActual = 0; // Reiniciar contador de quantum
                System.out.println("CPU selecciona Proceso " + this.procesoEnEjecucion.getNombre() + 
                                   " (Política: " + this.planificadorActual.getClass().getSimpleName() + ")");
            }

            // 3. Ejecutar el ciclo del proceso actual
            if (this.procesoEnEjecucion != null) {
                this.procesoEnEjecucion.ejecutarCiclo();
                System.out.println("Ejecutando Proceso " + this.procesoEnEjecucion.getNombre() + 
                                   ", PC: " + this.procesoEnEjecucion.getProgramCounter());

                this.ciclosEnQuantumActual++;

                // 3a. Verificar si el proceso terminó (COMPLETADO)
                if (this.procesoEnEjecucion.haTerminado()) {
                    System.out.println("Proceso " + this.procesoEnEjecucion.getNombre() + " ha TERMINADO.");
                    this.procesoEnEjecucion.setEstado(PCB.Estado.TERMINADO);
                    this.colaTerminados.encolar(this.procesoEnEjecucion);
                    this.procesoEnEjecucion = null; // CPU queda ociosa
                }
                // 3b. Verificar si genera E/S (COMPLETADO)
                else if (this.procesoEnEjecucion.debeGenerarExcepcionIO()) {
                    System.out.println("Proceso " + this.procesoEnEjecucion.getNombre() + " genera E/S.");
                    this.procesoEnEjecucion.setEstado(PCB.Estado.BLOQUEADO);
                    // (Aquí se iniciaría el Thread de E/S - Fase 4)
                    this.colaBloqueados.encolar(this.procesoEnEjecucion);
                    this.procesoEnEjecucion = null; // CPU queda ociosa
                }
                // 3c. Verificar si se acabó el Quantum (Lógica de Round Robin)
                else if (this.planificadorActual instanceof P_RoundRobin && 
                         this.ciclosEnQuantumActual >= this.quantum) {

                    System.out.println("Proceso " + this.procesoEnEjecucion.getNombre() + 
                                       " interrumpido por fin de QUANTUM.");

                    this.procesoEnEjecucion.setEstado(PCB.Estado.LISTO);
                    this.colaListos.encolar(this.procesoEnEjecucion);
                    this.procesoEnEjecucion = null; 
                }

            } else {
                System.out.println("CPU Ociosa");
            }

        // 4. Manejar procesos bloqueados (Fase 4 - Hilos de E/S)
        // ... Lógica para verificar si los hilos de E/S terminaron ...

        // 5. Manejar procesos suspendidos (Fase 7)
        // ... Lógica del Planificador de Mediano Plazo ...
    }

    // --- Getters (sin cambios) ---
    
    public int getCicloGlobal() {
        return cicloGlobal;
    }

    public PCB getProcesoEnEjecucion() {
        return procesoEnEjecucion;
    }

    public Cola<PCB> getColaNuevos() {
        return colaNuevos;
    }

    public Cola<PCB> getColaListos() {
        return colaListos;
    }

    public Cola<PCB> getColaBloqueados() {
        return colaBloqueados;
    }

    public Cola<PCB> getColaTerminados() {
        return colaTerminados;
    }

    public Cola<PCB> getColaListosSuspendidos() {
        return colaListosSuspendidos;
    }

    public Cola<PCB> getColaBloqueadosSuspendidos() {
        return colaBloqueadosSuspendidos;
    }
}
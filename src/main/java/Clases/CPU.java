package Clases;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author Diego A. Vivolo
 */
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


    public CPU() {
        this.cicloGlobal = 0;
        this.procesoEnEjecucion = null;

        this.colaNuevos = new Cola<>();
        this.colaListos = new Cola<>();
        this.colaBloqueados = new Cola<>();
        this.colaTerminados = new Cola<>();
        this.colaListosSuspendidos = new Cola<>();
        this.colaBloqueadosSuspendidos = new Cola<>();
        
        
    }
    public void setQuantum(int duracion) {
         this.quantum = duracion;
     }


    public void agregarProceso(PCB nuevoProceso) {
        nuevoProceso.setEstado(PCB.Estado.NUEVO);
        this.colaNuevos.encolar(nuevoProceso);
        System.out.println("Proceso " + nuevoProceso.getNombre() + " (ID: " + nuevoProceso.getId() + ") ha entrado al sistema en cola NUEVOS.");
    }

    public void ejecutarCiclo() {
            this.cicloGlobal++;
            System.out.println("--- CICLO GLOBAL: " + this.cicloGlobal + " ---");

            // 1. Mover procesos de NUEVO a LISTO (sin cambios)
            if (!this.colaNuevos.estaVacia()) {
                PCB pcb = this.colaNuevos.desencolar();
                pcb.setEstado(PCB.Estado.LISTO);
                this.colaListos.encolar(pcb);
                System.out.println("Proceso " + pcb.getNombre() + " movido a LISTOS.");
            }

            // 2. Lógica del Planificador de Corto Plazo (ACTUALIZADO)
            // Si la CPU está ociosa y hay procesos listos, seleccionar uno
            // usando la política actual.
            if (this.procesoEnEjecucion == null && !this.colaListos.estaVacia()) {

                // ¡Aquí ocurre la magia del polimorfismo!
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

                // Contar el ciclo para el quantum
                this.ciclosEnQuantumActual++;

                // 3a. Verificar si el proceso terminó (sin cambios)
                if (this.procesoEnEjecucion.haTerminado()) {
                    // ... (código de Fase 2) ...
                    this.procesoEnEjecucion = null; 
                }
                // 3b. Verificar si genera E/S (sin cambios)
                else if (this.procesoEnEjecucion.debeGenerarExcepcionIO()) {
                    // ... (código de Fase 2) ...
                    this.procesoEnEjecucion = null; 
                }
                // 3c. ¡NUEVO! Verificar si se acabó el Quantum (Lógica de Round Robin)
                else if (this.planificadorActual instanceof PlanificadorRoundRobin && 
                         this.ciclosEnQuantumActual >= this.quantum) {

                    System.out.println("Proceso " + this.procesoEnEjecucion.getNombre() + 
                                       " interrumpido por fin de QUANTUM.");

                    // El proceso es interrumpido y regresa al final de la cola de listos
                    this.procesoEnEjecucion.setEstado(PCB.Estado.LISTO);
                    this.colaListos.encolar(this.procesoEnEjecucion);
                    this.procesoEnEjecucion = null; // CPU queda ociosa
                }

            } else {
                System.out.println("CPU Ociosa");
            }

        // 4. Manejar procesos bloqueados (Fase 4 - Hilos de E/S)
        // ... Lógica para verificar si los hilos de E/S terminaron ...

        // 5. Manejar procesos suspendidos (Fase 7)
        // ... Lógica del Planificador de Mediano Plazo ...

        // --- FIN DE LÓGICA DE PLANIFICACIÓN ---
    }

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

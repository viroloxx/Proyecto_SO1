package Clases;

import Clases.PCB;
import Clases.Cola;

import Clases.Manejador_Hilos; 

import Planificadores.P_RoundRobin;
import Planificadores.P_FCFS;
import Planificadores.P_PA;
import Planificadores.P_PNA;
import Planificadores.P_SJF;
import Planificadores.P_SRTF;
import UI.UI_planificador;


import java.util.concurrent.Semaphore;

/**
 *
 * @author Diego A. Vivolo

 */
public class CPU {

    private int cicloGlobal;

    private Cola<PCB> colaNuevos;
    private Cola<PCB> colaListos;
    private Cola<PCB> colaBloqueados;
    private Cola<PCB> colaTerminados;
    private Cola<PCB> colaListosSuspendidos;
    private Cola<PCB> colaBloqueadosSuspendidos;
    private PCB procesoEnEjecucion;
    private UI_planificador planificadorActual;
    private int quantum;
    private int ciclosEnQuantumActual;
    private int duracionCicloMs;      
    private Semaphore semaforoColaListos;     
    private Semaphore semaforoColaBloqueados; 
    private Semaphore semaforoColaTerminados;  
    private Semaphore semaforoColaNuevos;   
    

    public CPU() {
        this.cicloGlobal = 0;
        this.procesoEnEjecucion = null;
        this.colaNuevos = new Cola<>();
        this.colaListos = new Cola<>();
        this.colaBloqueados = new Cola<>();
        this.colaTerminados = new Cola<>();
        this.colaListosSuspendidos = new Cola<>();
        this.colaBloqueadosSuspendidos = new Cola<>();
        this.planificadorActual = new P_FCFS(); 
        this.quantum = 5; 
        this.ciclosEnQuantumActual = 0;
        this.duracionCicloMs = 1000; 
        this.semaforoColaListos = new Semaphore(1); 
        this.semaforoColaBloqueados = new Semaphore(1);
        this.semaforoColaTerminados = new Semaphore(1);
        this.semaforoColaNuevos = new Semaphore(1);
    }

    public void setQuantum(int duracion) {
         this.quantum = duracion;
    }

    public void setPlanificador(UI_planificador nuevaPolitica) {
        System.out.println("--- CAMBIO DE PLANIFICADOR A: " + nuevaPolitica.getClass().getSimpleName() + " ---");
        this.planificadorActual = nuevaPolitica;
        this.ciclosEnQuantumActual = 0; 
    } 

    public void setDuracionCicloMs(int ms) {
        this.duracionCicloMs = ms;
    }

    public int getDuracionCicloMs() {
        return this.duracionCicloMs;
    }

    public Semaphore getSemaforoColaListos() {
        return semaforoColaListos;
    }

    public Semaphore getSemaforoColaBloqueados() {
        return semaforoColaBloqueados;
    }
        public Semaphore getSemaforoColaTerminados() {
        return semaforoColaTerminados;
    }

    public void agregarProceso(PCB nuevoProceso) {
        try{
            this.semaforoColaNuevos.acquire();
        
            nuevoProceso.setEstado(PCB.Estado.NUEVO);
            this.colaNuevos.encolar(nuevoProceso);
            System.out.println("Proceso " + nuevoProceso.getNombre() + " (ID: " + nuevoProceso.getId() + ") ha entrado al sistema en cola NUEVOS.");

        this.semaforoColaNuevos.release();
        } catch (InterruptedException e){
            System.err.println("Error de semaforo en agregarProceso:" + e.getMessage());
            Thread.currentThread().interrupt();
            
            
        }
    }
    
    

    public void manejarLlegadaProcesoListo(PCB pcbQueLlega) {
        try {
            // Adquirimos el bloqueo de la cola de Listos
            this.semaforoColaListos.acquire();
            
            //Inicio de Zona Crítica (colaListos)
            
            pcbQueLlega.setEstado(PCB.Estado.LISTO);
            this.colaListos.encolar(pcbQueLlega);
            System.out.println("Proceso " + pcbQueLlega.getNombre() + " movido a LISTOS.");

            if (this.procesoEnEjecucion == null) {
                // Liberamos antes de salir si no hay nada que hacer
                this.semaforoColaListos.release();
                return;
            }

            boolean interrumpir = false;

            // 1. Verificar SRTF
            if (this.planificadorActual instanceof P_SRTF) {
                if (pcbQueLlega.getTiempoRestante() < this.procesoEnEjecucion.getTiempoRestante()) {
                     System.out.println("INTERRUPCIÓN (SRTF): " + pcbQueLlega.getNombre() + 
                                       " (TR:" + pcbQueLlega.getTiempoRestante() + 
                                       ") es más corto que " + this.procesoEnEjecucion.getNombre() +
                                       " (TR:" + this.procesoEnEjecucion.getTiempoRestante() + ")");
                    interrumpir = true;
                }
            }
            // 2. Verificar Prioridad
            // --- CORRECCIÓN 2: El nombre de la clase era incorrecto (debe ser P_PA según tus imports) ---
            else if (this.planificadorActual instanceof P_PA) {
                
                if (pcbQueLlega.getPrioridad() < this.procesoEnEjecucion.getPrioridad()) {
                     System.out.println("INTERRUPCIÓN (Prioridad): " + pcbQueLlega.getNombre() + 
                                       " (P:" + pcbQueLlega.getPrioridad() + 
                                       ") tiene más prioridad que " + this.procesoEnEjecucion.getNombre() +
                                       " (P:" + this.procesoEnEjecucion.getPrioridad() + ")");
                    interrumpir = true;
                } 

            }

            if (interrumpir) {
                System.out.println("Proceso " + this.procesoEnEjecucion.getNombre() + " interrumpido y devuelto a LISTOS.");
               
                this.procesoEnEjecucion.setEstado(PCB.Estado.LISTO);
                this.colaListos.encolar(this.procesoEnEjecucion);
                this.procesoEnEjecucion = null; 
            }
            
            // --- Fin de Zona Crítica (colaListos) ---
            this.semaforoColaListos.release();
            
        } catch (InterruptedException e) {
            System.err.println("Error de semáforo en manejarLlegadaProcesoListo: " + e.getMessage());
            Thread.currentThread().interrupt();
        }
    }

    /**
     *SIMULA RELOJ
     */
    public void ejecutarCiclo() {
            try {
                this.cicloGlobal++;
                System.out.println("--- CICLO GLOBAL: " + this.cicloGlobal + " ---");
                
                PCB pcb = null;
                
                this.semaforoColaNuevos.acquire();
                if (!this.colaNuevos.estaVacia()) {
                    pcb = this.colaNuevos.desencolar();
                    
                }
                this.semaforoColaNuevos.release();
                
                if (pcb !=null) {
                    manejarLlegadaProcesoListo(pcb); 
                }
                
                // 2. Despachador (Dispatcher): Seleccionar un proceso si la CPU está ociosa
                this.semaforoColaListos.acquire(); 
                if (this.procesoEnEjecucion == null && !this.colaListos.estaVacia()) {
                    // --- Inicio Zona Crítica (colaListos) ---
                    this.procesoEnEjecucion = this.planificadorActual.seleccionarSiguienteProceso(this.colaListos);
                    this.procesoEnEjecucion.setEstado(PCB.Estado.EJECUCION);
                    this.ciclosEnQuantumActual = 0; // Reiniciar contador de quantum
                    System.out.println("CPU selecciona Proceso " + this.procesoEnEjecucion.getNombre() + 
                                       " (Política: " + this.planificadorActual.getClass().getSimpleName() + ")");
                    //Fin Zona Crítica (colaListos)
                }
                this.semaforoColaListos.release(); 

                // 3. Ejecutar el proceso
                if (this.procesoEnEjecucion != null) {

                    this.procesoEnEjecucion.ejecutarCiclo();
                    System.out.println("Ejecutando Proceso " + this.procesoEnEjecucion.getNombre() + 
                                       ", PC: " + this.procesoEnEjecucion.getProgramCounter());
                    this.ciclosEnQuantumActual++;

                    // 4. Verificar eventos de fin de ejecución

                    if (this.procesoEnEjecucion.haTerminado()) {
                        System.out.println("Proceso " + this.procesoEnEjecucion.getNombre() + " ha TERMINADO.");
                        this.procesoEnEjecucion.setEstado(PCB.Estado.TERMINADO);


                        this.semaforoColaTerminados.acquire();
                        this.colaTerminados.encolar(this.procesoEnEjecucion);
                        this.semaforoColaTerminados.release();

                        this.procesoEnEjecucion = null;
                    }
                    else if (this.procesoEnEjecucion.debeGenerarExcepcionIO()) {
                        System.out.println("Proceso " + this.procesoEnEjecucion.getNombre() + " genera E/S. Moviendo a BLOQUEADOS.");

                        this.procesoEnEjecucion.setEstado(PCB.Estado.BLOQUEADO);

                        // Inicio de Zona Crítica (colaBloqueados)
                        this.semaforoColaBloqueados.acquire();
                        this.colaBloqueados.encolar(this.procesoEnEjecucion);
                        this.semaforoColaBloqueados.release();
                        // --- Fin de Zona Crítica ---


                        Manejador_Hilos hiloIO = new Manejador_Hilos(this.procesoEnEjecucion, this);
                        hiloIO.start();

                        this.procesoEnEjecucion = null; // CPU queda ociosa
                    }

                    else if (this.planificadorActual instanceof P_RoundRobin && 
                             this.ciclosEnQuantumActual >= this.quantum) {

                        System.out.println("Proceso " + this.procesoEnEjecucion.getNombre() + " interrumpido por fin de QUANTUM.");

                        this.procesoEnEjecucion.setEstado(PCB.Estado.LISTO);

                        // --- Inicio de Zona Crítica (colaListos) ---
                        this.semaforoColaListos.acquire();
                        this.colaListos.encolar(this.procesoEnEjecucion);
                        this.semaforoColaListos.release();
                        // --- Fin de Zona Crítica ---

                        this.procesoEnEjecucion = null; 
                    }

                } else {
                    System.out.println("CPU Ociosa");
                }

            } catch (InterruptedException e) {
                System.err.println("Error de semáforo en el ciclo principal: " + e.getMessage());
                Thread.currentThread().interrupt();
            }
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
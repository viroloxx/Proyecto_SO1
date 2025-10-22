package Clases;

import Clases.Manejador_Hilos;
import Planificadores.*; // Importa todos tus planificadores
import UI.UI_planificador;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;
import java.util.concurrent.Semaphore;

/**
 *
 * @author Diego A. Vivolo
 * @author Gabriel Orozco 
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
    
    // --- Lógica de Planificadores ---
    private UI_planificador planificadorActual;
    private HashMap<String, UI_planificador> planificadoresDisponibles;

    private int quantum;
    private int ciclosEnQuantumActual;
    private int duracionCicloMs;      
    
    // --- Semáforos ---
    private Semaphore semaforoColaListos;     
    private Semaphore semaforoColaBloqueados; 
    private Semaphore semaforoColaTerminados;  
    private Semaphore semaforoColaNuevos;   
    
    // --- Lógica de Suspendidos ---
    private final int MAX_PROCESOS_EN_MEMORIA = 5; // Límite de memoria simulado
    
    // --- Lógica de Métricas ---
    private int ciclosCPUOcupada;
    
    // --- CORRECCIÓN 1 (Línea 61) ---
    private Cola<Integer> turnaroundTimes; 

    public CPU() {
        this.cicloGlobal = 0;
        this.procesoEnEjecucion = null;
        this.colaNuevos = new Cola<>();
        this.colaListos = new Cola<>();
        this.colaBloqueados = new Cola<>();
        this.colaTerminados = new Cola<>();
        this.colaListosSuspendidos = new Cola<>();
        this.colaBloqueadosSuspendidos = new Cola<>();
        
        this.quantum = 5; 
        this.ciclosEnQuantumActual = 0;
        this.duracionCicloMs = 1000; // Valor por defecto
        
        this.semaforoColaListos = new Semaphore(1); 
        this.semaforoColaBloqueados = new Semaphore(1);
        this.semaforoColaTerminados = new Semaphore(1);
        this.semaforoColaNuevos = new Semaphore(1);
        
        // Métricas
        this.ciclosCPUOcupada = 0;
        
       
        this.turnaroundTimes = new Cola<>(); // Inicializar como Cola
        
        // Cargar configuración al iniciar
        cargarConfiguracion(); // Carga config.properties si existe

        // Inicializar Mapa de Planificadores
        this.planificadoresDisponibles = new HashMap<>();
        this.planificadoresDisponibles.put("FCFS", new P_FCFS());
        this.planificadoresDisponibles.put("SJF", new P_SJF());
        this.planificadoresDisponibles.put("SRTF", new P_SRTF());
        this.planificadoresDisponibles.put("Round Robin", new P_RoundRobin());
        this.planificadoresDisponibles.put("Prioridad (No Aprop.)", new P_PNA());
        this.planificadoresDisponibles.put("Prioridad (Aprop.)", new P_PA());

        // Establecer el planificador por defecto
        this.planificadorActual = this.planificadoresDisponibles.get("FCFS");
    }

    // --- Métodos para la GUI ---
    public String[] getNombresPlanificadores() {
        return this.planificadoresDisponibles.keySet().toArray(new String[0]);
    }
    
    public int getQuantum() {
        return this.quantum;
    }
    
    public void setPlanificador(String nombre) {
        UI_planificador nuevo = this.planificadoresDisponibles.getOrDefault(nombre, this.planificadorActual);
        if (nuevo != this.planificadorActual) {
            System.out.println("--- CAMBIO DE PLANIFICADOR A: " + nombre + " ---");
            this.planificadorActual = nuevo;
            this.ciclosEnQuantumActual = 0; 
        }
    } 

    public void setQuantum(int duracion) {
         this.quantum = duracion;
    }

    public void setDuracionCicloMs(int ms) {
        this.duracionCicloMs = ms;
    }

    public int getDuracionCicloMs() {
        return this.duracionCicloMs;
    }

    // --- Getters de Semáforos ---
    public Semaphore getSemaforoColaListos() {
        return semaforoColaListos;
    }
    public Semaphore getSemaforoColaBloqueados() {
        return semaforoColaBloqueados;
    }
    public Semaphore getSemaforoColaTerminados() {
        return semaforoColaTerminados;
    }

    // --- Lógica de Procesos ---

    public void agregarProceso(PCB nuevoProceso) {
        try{
            this.semaforoColaNuevos.acquire();
            
            // Settear ciclo de llegada para métricas
            nuevoProceso.setCicloLlegada(this.cicloGlobal);
            
            nuevoProceso.setEstado(PCB.Estado.NUEVO);
            this.colaNuevos.encolar(nuevoProceso);
            System.out.println("Proceso " + nuevoProceso.getNombre() + " (ID: " + nuevoProceso.getId() + ") ha entrado al sistema en cola NUEVOS.");

            this.semaforoColaNuevos.release();
        } catch (InterruptedException e){
            System.err.println("Error de semaforo en agregarProceso:" + e.getMessage());
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * Calcula cuántos procesos están "en memoria" (Listos + Bloqueados + Ejecución)
     */
    private int getProcesosActivosEnMemoria() {
        int enListos = (colaListos != null) ? colaListos.getTamano() : 0;
        int enBloqueados = (colaBloqueados != null) ? colaBloqueados.getTamano() : 0;
        int enEjecucion = (procesoEnEjecucion != null) ? 1 : 0;
        return enListos + enBloqueados + enEjecucion;
    }
    
    /**
     * Revisa si hay espacio en memoria para traer procesos de suspensión.
     */
    private void intentarAdmitirProcesoSuspendido() {
        // Revisa si hay espacio Y si hay alguien esperando
        if (getProcesosActivosEnMemoria() < MAX_PROCESOS_EN_MEMORIA && 
            !this.colaListosSuspendidos.estaVacia()) {
            
            System.out.println("Espacio liberado en memoria. Admitiendo desde LISTOS-SUSPENDIDOS.");
            
            // TODO: Añadir semáforo para colaListosSuspendidos si es necesario
            PCB pcbAdmitido = this.colaListosSuspendidos.desencolar();
            
            // Lo enviamos al flujo normal de llegada (que lo moverá a Listos)
            manejarLlegadaProcesoListo(pcbAdmitido);
        }
        // (Aquí también iría la lógica para Bloqueados-Suspendidos)
    }

    public void manejarLlegadaProcesoListo(PCB pcbQueLlega) {
        try {
            this.semaforoColaListos.acquire();
            
            // --- INICIO DE LÓGICA DE SUSPENSIÓN ---
            // Revisa si hay "espacio en memoria"
            if (getProcesosActivosEnMemoria() < MAX_PROCESOS_EN_MEMORIA) {
                // Hay espacio: va a Listos
                pcbQueLlega.setEstado(PCB.Estado.LISTO);
                this.colaListos.encolar(pcbQueLlega);
                System.out.println("Proceso " + pcbQueLlega.getNombre() + " movido a LISTOS.");
            } else {
                // No hay espacio: va a Listos-Suspendidos
                pcbQueLlega.setEstado(PCB.Estado.LISTO_SUSPENDIDO);
                // TODO: Añadir semáforo para colaListosSuspendidos
                this.colaListosSuspendidos.encolar(pcbQueLlega);
                System.out.println("MEMORIA LLENA. Proceso " + pcbQueLlega.getNombre() + " movido a LISTOS-SUSPENDIDOS.");
                
                this.semaforoColaListos.release();
                return; // No puede interrumpir al actual porque no entró a Listos
            }
            // --- FIN DE LÓGICA DE SUSPENSIÓN ---

            if (this.procesoEnEjecucion == null) {
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
            // 2. Verificar Prioridad Apropiativa
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
            
            this.semaforoColaListos.release();
            
        } catch (InterruptedException e) {
            System.err.println("Error de semáforo en manejarLlegadaProcesoListo: " + e.getMessage());
            Thread.currentThread().interrupt();
        }
    }

    /**
     * SIMULA EL CICLO DEL RELOJ
     */
    public void ejecutarCiclo() {
            try {
                this.cicloGlobal++;
                System.out.println("--- CICLO GLOBAL: " + this.cicloGlobal + " ---");
                
                PCB pcb = null;
                
                // 1. Admitir Nuevos (Planificador de Largo Plazo)
                this.semaforoColaNuevos.acquire();
                if (!this.colaNuevos.estaVacia()) {
                    pcb = this.colaNuevos.desencolar();
                }
                this.semaforoColaNuevos.release();
                
                if (pcb !=null) {
                    // Esta llamada ahora decide si va a Listos o Listos-Suspendidos
                    manejarLlegadaProcesoListo(pcb); 
                }
                
                // 2. Despachador (Dispatcher): Seleccionar un proceso si la CPU está ociosa
                this.semaforoColaListos.acquire(); 
                if (this.procesoEnEjecucion == null && !this.colaListos.estaVacia()) {
                    // Planificador de Corto Plazo
                    this.procesoEnEjecucion = this.planificadorActual.seleccionarSiguienteProceso(this.colaListos);
                    this.procesoEnEjecucion.setEstado(PCB.Estado.EJECUCION);
                    this.ciclosEnQuantumActual = 0; // Reiniciar contador de quantum
                    System.out.println("CPU selecciona Proceso " + this.procesoEnEjecucion.getNombre() + 
                                       " (Política: " + this.planificadorActual.getClass().getSimpleName() + ")");
                }
                this.semaforoColaListos.release(); 

                // 3. Ejecutar el proceso
                if (this.procesoEnEjecucion != null) {

                    // Contar ciclo para métrica de utilización
                    this.ciclosCPUOcupada++;

                    this.procesoEnEjecucion.ejecutarCiclo();
                    System.out.println("Ejecutando Proceso " + this.procesoEnEjecucion.getNombre() + 
                                       ", PC: " + this.procesoEnEjecucion.getProgramCounter());
                    this.ciclosEnQuantumActual++;

                    // 4. Verificar eventos de fin de ejecución

                    // --- Evento: Proceso Terminado ---
                    if (this.procesoEnEjecucion.haTerminado()) {
                        System.out.println("Proceso " + this.procesoEnEjecucion.getNombre() + " ha TERMINADO.");
                        this.procesoEnEjecucion.setEstado(PCB.Estado.TERMINADO);
                        
                        // Calcular Métricas
                        this.procesoEnEjecucion.setCicloFin(this.cicloGlobal);
                        // Esta línea ya es correcta porque turnaroundTimes es una Cola<Integer>
                        this.turnaroundTimes.encolar(this.procesoEnEjecucion.getTurnaroundTime());

                        this.semaforoColaTerminados.acquire();
                        this.colaTerminados.encolar(this.procesoEnEjecucion);
                        this.semaforoColaTerminados.release();

                        this.procesoEnEjecucion = null;
                        
                        // Se liberó memoria, intentar admitir un proceso suspendido
                        intentarAdmitirProcesoSuspendido(); 
                    }
                    // --- Evento: Excepción de E/S ---
                    else if (this.procesoEnEjecucion.debeGenerarExcepcionIO()) {
                        System.out.println("Proceso " + this.procesoEnEjecucion.getNombre() + " genera E/S. Moviendo a BLOQUEADOS.");

                        this.procesoEnEjecucion.setEstado(PCB.Estado.BLOQUEADO);
                        this.procesoEnEjecucion.reiniciarContadorIO();

                        this.semaforoColaBloqueados.acquire();
                        this.colaBloqueados.encolar(this.procesoEnEjecucion);
                        this.semaforoColaBloqueados.release();

                        // Iniciar el hilo de E/S
                        Manejador_Hilos hiloIO = new Manejador_Hilos(this.procesoEnEjecucion, this);
                        hiloIO.start();

                        this.procesoEnEjecucion = null; // CPU queda ociosa
                        
                    }
                    // --- Evento: Fin de Quantum (Round Robin) ---
                    else if (this.planificadorActual instanceof P_RoundRobin && 
                             this.ciclosEnQuantumActual >= this.quantum) {

                        System.out.println("Proceso " + this.procesoEnEjecucion.getNombre() + " interrumpido por fin de QUANTUM.");

                        this.procesoEnEjecucion.setEstado(PCB.Estado.LISTO);

                        this.semaforoColaListos.acquire();
                        this.colaListos.encolar(this.procesoEnEjecucion);
                        this.semaforoColaListos.release();

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

    // --- Métodos para Guardar/Cargar Configuración ---

    public void guardarConfiguracion() {
        Properties props = new Properties();
        props.setProperty("duracionCicloMs", String.valueOf(this.duracionCicloMs));
        props.setProperty("quantum", String.valueOf(this.quantum));
        
        try (FileOutputStream fos = new FileOutputStream("config.properties")) {
            props.store(fos, "Configuración del Simulador de CPU");
            System.out.println("Configuración guardada en config.properties");
        } catch (IOException e) {
            System.err.println("Error al guardar configuración: " + e.getMessage());
        }
    }
    
    public void cargarConfiguracion() {
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream("config.properties")) {
            props.load(fis);
            
            // Cargar y aplicar valores
            int duracionCargada = Integer.parseInt(props.getProperty("duracionCicloMs", "1000"));
            int quantumCargado = Integer.parseInt(props.getProperty("quantum", "5"));
            
            this.setDuracionCicloMs(duracionCargada);
            this.setQuantum(quantumCargado);
            
            System.out.println("Configuración cargada de config.properties");
            
        } catch (IOException e) {
            System.out.println("No se encontró config.properties. Usando valores por defecto.");
        } catch (NumberFormatException e) {
            System.err.println("Error al leer config.properties. Archivo corrupto. Usando valores por defecto.");
        }
    }
    
    // --- Métodos para Métricas ---

    /**
     * Devuelve la utilización del CPU como un porcentaje (0.0 a 100.0)
     */
    public double getUtilizacionCPU() {
        if (this.cicloGlobal == 0) {
            return 0.0;
        }
        // (ciclos ocupados / ciclos totales) * 100
        return (double) this.ciclosCPUOcupada / this.cicloGlobal * 100.0;
    }

    /**
     * Devuelve el tiempo de turnaround promedio
     */
    public double getTurnaroundPromedio() {
        if (this.turnaroundTimes.estaVacia()) {
            return 0.0;
        }
        
        double suma = 0;
        int totalProcesos = this.turnaroundTimes.getTamano();
        
        // --- CORRECCIÓN 3 (Línea 473) ---
        Cola<Integer> temp = new Cola<>(); // Debe ser Cola<Integer>
        
        while (!this.turnaroundTimes.estaVacia()) {
            // Esta línea ya es correcta
            int tiempo = this.turnaroundTimes.desencolar();
            suma += tiempo;
            // Esta línea ya es correcta
            temp.encolar(tiempo);
        }
        
        // Restaurar la cola original
        while (!temp.estaVacia()) {
            // Esta línea ya es correcta
            this.turnaroundTimes.encolar(temp.desencolar());
        }

        return suma / totalProcesos;
    }

    
    // --- Getters de Colas y Estado ---
    
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
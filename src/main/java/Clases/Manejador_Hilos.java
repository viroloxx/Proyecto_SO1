package Clases;

import Clases.CPU;
import Clases.PCB;

/**
 *
 * @author Diego A. Vivolo

 */
public class Manejador_Hilos extends Thread {

    private PCB pcb;         
    private int ciclosEspera; 
    private CPU cpu;         

    public Manejador_Hilos(PCB pcb, CPU cpu) {
        this.pcb = pcb;
        this.cpu = cpu;
        this.ciclosEspera = pcb.getCiclosParaCompletarExcepcion();
        
    }

    @Override
    public void run() {
        try {
            // 1. Calcular el tiempo real de espera en milisegundos
            long tiempoEsperaMs = (long) this.ciclosEspera * this.cpu.getDuracionCicloMs();
            
            // 2. Dormir el hilo para simular la E/S
            System.out.println("Proceso " + pcb.getNombre() + " (ID: " + pcb.getId() + ") inicia E/S. Duración: " + tiempoEsperaMs + "ms");
            
            Thread.sleep(tiempoEsperaMs);

            System.out.println(">>> E/S COMPLETADA para Proceso " + pcb.getNombre() + " <<<");

            // 3. Despertar y remover de Bloqueados (zona crítica)
            this.cpu.getSemaforoColaBloqueados().acquire();
            
            // Inicio de Zona Crítica (colaBloqueados)

            this.cpu.getColaBloqueados().remover(this.pcb);
            
            // Fin de Zona Crítica 
            
            this.cpu.getSemaforoColaBloqueados().release();


            // 4. Mover a 'Listos' (zona crítica)
            // Usamos el método de la CPU que ya es seguro para hilos y que maneja la lógica de apropiación.
            this.cpu.manejarLlegadaProcesoListo(this.pcb);

        } catch (InterruptedException e) {
            System.err.println("Hilo de E/S para PCB " + pcb.getId() + " fue interrumpido.");
            Thread.currentThread().interrupt();
        }
    }
}
package Clases;

/**
 *
 * @author Diego A. Vivolo
 * @author Gabriel Orozco
 */

import UI.InterfazSimulador; 

/**
 * Clase principal que inicia la simulación.
 */
public class Proyecto_OS {

    public static void main(String[] args) {
        
        // 1. Crear el cerebro (CPU)
        CPU cpu = new CPU();

        // 2. Crear la interfaz gráfica y pasarle la CPU
        InterfazSimulador gui = new InterfazSimulador(cpu);
        
        // 3. Hacer visible la GUI (Esto inicia el Hilo de Swing EDT)
        java.awt.EventQueue.invokeLater(() -> {
            gui.setVisible(true);
        });

        // ----- DATOS DE PRUEBA (Para ver si funciona) -----
        // (Puedes mover esto a un botón en la GUI luego)
        cpu.agregarProceso(new PCB("Proceso A (IO)", 20, 5, 10, 2)); // IO
        cpu.agregarProceso(new PCB("Proceso B (CPU)", 40, 1));      // CPU
        cpu.agregarProceso(new PCB("Proceso C (CPU)", 15, 3));      // CPU
        cpu.agregarProceso(new PCB("Proceso D (IO)", 25, 8, 8, 1));  // IO
        cpu.agregarProceso(new PCB("Proceso E (CPU)", 30, 2));
        cpu.agregarProceso(new PCB("Proceso F (CPU)", 10, 1));
        cpu.agregarProceso(new PCB("Proceso G (IO)", 18, 4, 12, 3));
        // ------------------------------------------------

        // 4. Crear y lanzar el Hilo de Simulación (Reloj)
        //    Este es el hilo que ejecuta el ciclo de la CPU
        Thread hiloSimulacion = new Thread(() -> {
            try {
                // Bucle infinito que simula el reloj
                while (true) {
                    
                    // 1. Ejecutar un ciclo de la CPU (lógica de planificación)
                    cpu.ejecutarCiclo();
                    
                    // 2. Actualizar la GUI
                    gui.actualizarEstado();
                    
                    // 3. Dormir el hilo según la duración del ciclo
                    Thread.sleep(cpu.getDuracionCicloMs());
                }
            } catch (InterruptedException e) {
                System.err.println("Hilo de simulación interrumpido.");
            }
        });

        hiloSimulacion.setName("Reloj-Simulador");
        hiloSimulacion.start();
    }
}
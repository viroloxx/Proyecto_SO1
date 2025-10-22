/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */

/**
 *
 * @author Gabriel Orozco
 */
package UI;

import Clases.Cola;
import Clases.PCB;

/**
 * Interfaz que deben implementar todas las políticas de planificación.
 */
public interface UI_planificador {
    
    /**
     * Selecciona el siguiente proceso a ejecutar de la cola de listos,
     * según la política de planificación.
     * @param colaListos La cola de procesos en estado LISTO.
     * @return El PCB del proceso seleccionado.
     */
    PCB seleccionarSiguienteProceso(Cola<PCB> colaListos);
    
}
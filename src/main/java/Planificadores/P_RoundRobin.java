package Planificadores;

import Clases.Cola;
import Clases.PCB;
import UI.UI_planificador;

/**
 *
 * @author Diego A. Vivolo
 */
public class P_RoundRobin implements UI_planificador {

    @Override
    public PCB seleccionarSiguienteProceso(Cola<PCB> colaListos) {
        // La selección de Round Robin es FCFS: toma el primero de la cola.
        return colaListos.desencolar();
    }
}
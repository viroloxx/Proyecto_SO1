package Planificadores;

import UI.UI_planificador;
import Clases.Cola;
import Clases.PCB;

/**
 *
 * @author Diego A. Vivolo
 */
public class P_FCFS implements UI_planificador {

    @Override
    public PCB seleccionarSiguienteProceso(Cola<PCB> colaListos) {

        return colaListos.desencolar();
    }
    
}

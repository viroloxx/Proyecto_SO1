package Planificadores;

import Clases.Cola;
import Clases.PCB;
import UI.UI_planificador;

/**
 *
 * @author Diego A. Vivolo
 */
public class P_PA implements UI_planificador {

    @Override
    public PCB seleccionarSiguienteProceso(Cola<PCB> colaListos) {
        // encontrar el de mayor prioridad en la cola.
        
        if (colaListos.estaVacia()) {
            return null;
        }

        Cola<PCB> colaTemporal = new Cola<>();
        PCB procesoPrioritario = null;
        int maxPrioridad = Integer.MAX_VALUE;

        while (!colaListos.estaVacia()) {
            PCB actual = colaListos.desencolar();
            if (actual.getPrioridad() < maxPrioridad) {
                maxPrioridad = actual.getPrioridad();
                procesoPrioritario = actual;
            }
            colaTemporal.encolar(actual);
        }

        while (!colaTemporal.estaVacia()) {
            PCB actual = colaTemporal.desencolar();
            if (actual.getId() != procesoPrioritario.getId()) {
                colaListos.encolar(actual);
            }
        }
        return procesoPrioritario;
    }
}
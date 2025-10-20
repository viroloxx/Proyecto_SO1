
package Planificadores;

import Clases.Cola;
import Clases.PCB;
import UI.UI_planificador;

/**
 *
 * @author Diego A. Vivolo
 */
public class P_PNA implements UI_planificador {

    @Override
    public PCB seleccionarSiguienteProceso(Cola<PCB> colaListos) {
        if (colaListos.estaVacia()) {
            return null;
        }

        Cola<PCB> colaTemporal = new Cola<>();
        PCB procesoPrioritario = null;
        int maxPrioridad = Integer.MAX_VALUE; 

        // 1. Encontrar el proceso con la prioridad más alta 
        while (!colaListos.estaVacia()) {
            PCB actual = colaListos.desencolar();
            
            if (actual.getPrioridad() < maxPrioridad) {
                maxPrioridad = actual.getPrioridad();
                procesoPrioritario = actual;
            }
            
            colaTemporal.encolar(actual);
        }

        // 2. Devolver los otros procesos a la cola de listos
        while (!colaTemporal.estaVacia()) {
            PCB actual = colaTemporal.desencolar();
            if (actual.getId() != procesoPrioritario.getId()) {
                colaListos.encolar(actual);
            }
        }

        // 3. Devolver el proceso seleccionado
        return procesoPrioritario;
    }
}
/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
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
        // La lógica de selección es la misma:
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
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
public class P_PNA implements UI_planificador {

    @Override
    public PCB seleccionarSiguienteProceso(Cola<PCB> colaListos) {
        if (colaListos.estaVacia()) {
            return null;
        }

        Cola<PCB> colaTemporal = new Cola<>();
        PCB procesoPrioritario = null;
        int maxPrioridad = Integer.MAX_VALUE; // (ej. 0 es más alto que 10)

        // 1. Encontrar el proceso con la prioridad más alta (número más bajo)
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
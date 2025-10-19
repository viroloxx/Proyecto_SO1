/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Clases;

import UI.UI_planificador;

public class P_SJF implements UI_planificador {

    @Override
    public PCB seleccionarSiguienteProceso(Cola<PCB> colaListos) {
        if (colaListos.estaVacia()) {
            return null;
        }

        // 1. Crear cola temporal y variables para encontrar el mínimo
        Cola<PCB> colaTemporal = new Cola<>();
        PCB procesoMasCorto = null;
        int minInstrucciones = Integer.MAX_VALUE;

        // 2. Primera pasada: vaciar colaListos, encontrar el más corto
        //    y llenar la colaTemporal
        while (!colaListos.estaVacia()) {
            PCB actual = colaListos.desencolar();
            
            if (actual.getTotalInstrucciones() < minInstrucciones) {
                minInstrucciones = actual.getTotalInstrucciones();
                procesoMasCorto = actual;
            }
            
            colaTemporal.encolar(actual);
        }

        // 3. Segunda pasada: devolver todos los procesos a colaListos,
        //    excepto el seleccionado (procesoMasCorto)
        while (!colaTemporal.estaVacia()) {
            PCB actual = colaTemporal.desencolar();
            
            if (actual.getId() != procesoMasCorto.getId()) {
                // Si NO es el que seleccionamos, se regresa a la cola original
                colaListos.encolar(actual);
            }
        }

        // 4. Devolver el proceso seleccionado
        return procesoMasCorto;
    }
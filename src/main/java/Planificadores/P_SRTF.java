/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Planificadores;

import Clases.Cola;
import UI.UI_planificador;
import Clases.PCB;

/**
 *
 * @author Diego A. Vivolo
 */
public class P_SRTF {
    public class PlanificadorSRTF implements UI_planificador {

    @Override
    public PCB seleccionarSiguienteProceso(Cola<PCB> colaListos) {
        if (colaListos.estaVacia()) {
            return null;
        }

        // 1. Crear cola temporal y variables para encontrar el mínimo
        Cola<PCB> colaTemporal = new Cola<>();
        PCB procesoMasCorto = null;
        int minTiempoRestante = Integer.MAX_VALUE;

        // 2. Primera pasada: encontrar el proceso con el menor tiempo restante
        while (!colaListos.estaVacia()) {
            PCB actual = colaListos.desencolar();
            
            // La única diferencia con SJF: usamos getTiempoRestante()
            if (actual.getTiempoRestante() < minTiempoRestante) {
                minTiempoRestante = actual.getTiempoRestante();
                procesoMasCorto = actual;
            }
            
            colaTemporal.encolar(actual);
        }

        // 3. Segunda pasada: devolver todos los procesos a colaListos,
        //    excepto el seleccionado (procesoMasCorto)
        while (!colaTemporal.estaVacia()) {
            PCB actual = colaTemporal.desencolar();
            if (actual.getId() != procesoMasCorto.getId()) {
                colaListos.encolar(actual);
            }
        }

        // 4. Devolver el proceso seleccionado
        return procesoMasCorto;
    }
}
    
}

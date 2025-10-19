/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package UI;

import Clases.Cola;
import Clases.PCB;

/**
 *
 * @author Diego A. Vivolo
 */
public interface UI_planificador {
    PCB seleccionarSiguienteProceso(Cola<PCB> colaListos);
    
}

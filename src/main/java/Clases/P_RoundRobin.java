/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Clases;

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
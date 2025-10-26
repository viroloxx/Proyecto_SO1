package gui;

import javax.swing.*;
import java.awt.*;
import sistema.SistemaOperativo;
import memoria.AdministradorMemoria; 

/**
 * Panel que muestra la cantidad de procesos en memoria
 */
public class PanelMemoria extends JPanel {

    private SistemaOperativo sistema;
    private JLabel lblProcesosEnMemoria;

    public PanelMemoria(SistemaOperativo sistema) {
        this.sistema = sistema;

        setLayout(new FlowLayout(FlowLayout.LEFT, 10, 5));
        setBorder(BorderFactory.createTitledBorder("Estado de Memoria"));

        add(new JLabel("Procesos en Memoria:"));
        lblProcesosEnMemoria = new JLabel("0/0");
        lblProcesosEnMemoria.setFont(new Font(lblProcesosEnMemoria.getFont().getName(), Font.BOLD, 12));
        add(lblProcesosEnMemoria);
    }

    /**
     * Actualiza la visualización del estado de memoria
     */
    public void actualizar() {
     
        AdministradorMemoria adminMemoria = sistema.getAdminMemoria(); 

        // Actualizar procesos en memoria
        int procesosEnMemoria = adminMemoria.getProcesosEnMemoria();
        int capacidadMaxima = adminMemoria.getCapacidadMaxima();
        lblProcesosEnMemoria.setText(String.format("%d/%d", procesosEnMemoria, capacidadMaxima));

    }
}
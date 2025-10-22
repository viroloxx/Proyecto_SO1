/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package UI;

/**
 *
 * @author gabrielorozco
 */
import Clases.CPU;
import Clases.PCB;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import javax.swing.*;
import javax.swing.event.ChangeEvent;

/**
 * Ventana principal de la GUI para el Simulador de Planificación.
 * Esta clase también se encarga de manejar los eventos de los controles.
 */
public class InterfazSimulador extends JFrame {

    private CPU cpu;

    // --- Componentes de la GUI ---
    private JTextArea areaListos, areaBloqueados, areaTerminados, areaNuevos, 
                      areaListosSuspendidos, areaBloqueadosSuspendidos;
    
    private JLabel labelCicloGlobal, labelProcesoEnCPU, labelPC, labelMAR;
    
    // --- Métricas ---
    private JLabel labelUtilizacionCPU, labelTurnaroundPromedio;

    // --- Controles ---
    private JComboBox<String> selectorPlanificador;
    private JSpinner spinnerDuracionCiclo;
    private JSpinner spinnerQuantum;
    private JButton botonGuardarConfig, botonCargarConfig;
    // (Faltaría el botón de "Agregar Proceso" con sus campos)

    public InterfazSimulador(CPU cpu) {
        this.cpu = cpu;
        
        // --- Configuración de la ventana ---
        setTitle("Simulador de Planificación de CPU");
        setSize(1600, 900);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // --- 1. Panel de Colas (Centro) ---
        JPanel panelColas = new JPanel(new GridLayout(2, 3));
        areaNuevos = new JTextArea("NUEVOS:\n");
        areaListos = new JTextArea("LISTOS:\n");
        areaBloqueados = new JTextArea("BLOQUEADOS:\n");
        areaTerminados = new JTextArea("TERMINADOS:\n");
        areaListosSuspendidos = new JTextArea("LISTOS-SUSP:\n");
        areaBloqueadosSuspendidos = new JTextArea("BLOQUEADOS-SUSP:\n");
        
        panelColas.add(crearPanelScroll("Nuevos", areaNuevos));
        panelColas.add(crearPanelScroll("Listos", areaListos));
        panelColas.add(crearPanelScroll("Bloqueados", areaBloqueados));
        panelColas.add(crearPanelScroll("Terminados", areaTerminados));
        panelColas.add(crearPanelScroll("Listos-Suspendidos", areaListosSuspendidos));
        panelColas.add(crearPanelScroll("Bloqueados-Suspendidos", areaBloqueadosSuspendidos));
        
        add(panelColas, BorderLayout.CENTER);

        // --- 2. Panel de Estado (Sur) ---
        JPanel panelEstado = new JPanel(new GridLayout(2, 4));
        labelCicloGlobal = new JLabel("Ciclo Global: 0");
        labelProcesoEnCPU = new JLabel("Proceso en CPU: Ocioso");
        labelPC = new JLabel("PC: N/A");
        labelMAR = new JLabel("MAR: N/A");
        labelUtilizacionCPU = new JLabel("Uso CPU: 0.0%");
        labelTurnaroundPromedio = new JLabel("Turnaround Prom: 0 ciclos");
        
        panelEstado.add(labelCicloGlobal);
        panelEstado.add(labelProcesoEnCPU);
        panelEstado.add(labelPC);
        panelEstado.add(labelMAR);
        panelEstado.add(labelUtilizacionCPU);
        panelEstado.add(labelTurnaroundPromedio);
        
        add(panelEstado, BorderLayout.SOUTH);

        // --- 3. Panel de Controles (Norte) ---
        JPanel panelControles = new JPanel();
        
        // Selector de Planificador
        selectorPlanificador = new JComboBox<>(cpu.getNombresPlanificadores());
        selectorPlanificador.addActionListener(this::onPlanificadorChange);
        panelControles.add(new JLabel("Planificador:"));
        panelControles.add(selectorPlanificador);

        // Spinner Duración Ciclo
        spinnerDuracionCiclo = new JSpinner(new SpinnerNumberModel(cpu.getDuracionCicloMs(), 50, 5000, 50));
        spinnerDuracionCiclo.addChangeListener(this::onDuracionCicloChange);
        panelControles.add(new JLabel("Duración Ciclo (ms):"));
        panelControles.add(spinnerDuracionCiclo);
        
        // Spinner Quantum
        spinnerQuantum = new JSpinner(new SpinnerNumberModel(cpu.getQuantum(), 1, 100, 1));
        spinnerQuantum.addChangeListener(this::onQuantumChange);
        panelControles.add(new JLabel("Quantum (RR):"));
        panelControles.add(spinnerQuantum);

        // Botones Guardar/Cargar
        botonGuardarConfig = new JButton("Guardar Config");
        botonCargarConfig = new JButton("Cargar Config");
        botonGuardarConfig.addActionListener(e -> cpu.guardarConfiguracion());
        botonCargarConfig.addActionListener(e -> {
            cpu.cargarConfiguracion();
            // Refrescar spinners con valores cargados
            spinnerDuracionCiclo.setValue(cpu.getDuracionCicloMs());
            spinnerQuantum.setValue(cpu.getQuantum());
        });
        panelControles.add(botonGuardarConfig);
        panelControles.add(botonCargarConfig);

        add(panelControles, BorderLayout.NORTH);
    }
    
    // --- Métodos de Eventos (Listeners) ---

    private void onPlanificadorChange(ActionEvent e) {
        String nombrePlanificador = (String) selectorPlanificador.getSelectedItem();
        cpu.setPlanificador(nombrePlanificador);
    }

    private void onDuracionCicloChange(ChangeEvent e) {
        int duracion = (int) spinnerDuracionCiclo.getValue();
        cpu.setDuracionCicloMs(duracion);
    }
    
    private void onQuantumChange(ChangeEvent e) {
        int quantum = (int) spinnerQuantum.getValue();
        cpu.setQuantum(quantum);
    }

    // Método auxiliar para crear paneles con scroll
    private JPanel crearPanelScroll(String titulo, JTextArea textArea) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(titulo));
        textArea.setEditable(false);
        panel.add(new JScrollPane(textArea), BorderLayout.CENTER);
        return panel;
    }

    // --- Actualizador de la GUI ---
    public void actualizarEstado() {
        // Se asegura de que las actualizaciones de la GUI se ejecuten 
        // en el Hilo de Despacho de Eventos (EDT) de Swing.
        SwingUtilities.invokeLater(() -> {
            // Etiquetas
            labelCicloGlobal.setText("Ciclo Global: " + cpu.getCicloGlobal());
            if (cpu.getProcesoEnEjecucion() != null) {
                PCB pcb = cpu.getProcesoEnEjecucion();
                labelProcesoEnCPU.setText("Proceso en CPU: " + pcb.getNombre() + " (ID: " + pcb.getId() + ")");
                labelPC.setText("PC: " + pcb.getProgramCounter());
                labelMAR.setText("MAR: " + pcb.getMemoryAddressRegister());
            } else {
                labelProcesoEnCPU.setText("Proceso en CPU: Ocioso");
                labelPC.setText("PC: N/A");
                labelMAR.setText("MAR: N/A");
            }

            // Colas (usamos el .toString() que ya definiste en tus clases)
            areaNuevos.setText(cpu.getColaNuevos().toString());
            areaListos.setText(cpu.getColaListos().toString());
            areaBloqueados.setText(cpu.getColaBloqueados().toString());
            areaTerminados.setText(cpu.getColaTerminados().toString());
            areaListosSuspendidos.setText(cpu.getColaListosSuspendidos().toString());
            areaBloqueadosSuspendidos.setText(cpu.getColaBloqueadosSuspendidos().toString());
            
            // Métricas
            labelUtilizacionCPU.setText(String.format("Uso CPU: %.2f%%", cpu.getUtilizacionCPU()));
            labelTurnaroundPromedio.setText(String.format("Turnaround Prom: %.2f ciclos", cpu.getTurnaroundPromedio()));
        });
    }
}
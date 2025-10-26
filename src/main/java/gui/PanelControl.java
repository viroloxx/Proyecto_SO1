package gui;
import javax.swing.*;
import java.awt.*;
import sistema.SistemaOperativo;
import planificacion.*;
import modelo.TipoProceso;
import javax.swing.JOptionPane;
import persistencia.ConfiguracionSistema;
import persistencia.GestorConfiguracion;

public class PanelControl extends JPanel {
    private SistemaOperativo sistema;
    private JButton btnIniciar, btnPausar, btnReiniciar, btnAgregarProceso;
    private JComboBox<String> comboPlanificador;
    private JLabel lblEstado, lblCiclo, lblPlanificador;


    private JButton btnCambiarVelocidad;
    private JLabel lblVelocidadActual;


    public PanelControl(SistemaOperativo sistema) {
        this.sistema = sistema;
        setLayout(new GridLayout(3, 1, 5, 5));
        setBorder(BorderFactory.createTitledBorder("Control del Sistema"));

        // Fila 1: Botones principales
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnIniciar = new JButton("Iniciar");
        btnPausar = new JButton("Pausar");
        btnReiniciar = new JButton("Reiniciar");
        btnAgregarProceso = new JButton("Agregar Proceso(s)");
        
        panelBotones.add(btnIniciar);
        panelBotones.add(btnPausar);
        panelBotones.add(btnReiniciar);
        panelBotones.add(btnAgregarProceso);
        
        add(panelBotones);

        // Fila 2: Planificación
        JPanel panelPlanificacion = new JPanel(new FlowLayout(FlowLayout.LEFT));
        lblPlanificador = new JLabel("Algoritmo:");
        comboPlanificador = new JComboBox<>(new String[]{"FCFS", "SJF", "SRTF", "Prioridad NP", "Prioridad P", "Round Robin", "Multilevel Queue", "Multilevel FB Queue"});
        panelPlanificacion.add(lblPlanificador);
        panelPlanificacion.add(comboPlanificador);
        add(panelPlanificacion);

        // Fila 3: Velocidad y Estado
        JPanel panelEstado = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));

        panelEstado.add(new JLabel("Velocidad (ms/ciclo):"));
        
        // Etiqueta para mostrar valor actual
        int velocidadInicial = sistema.getReloj().getDuracionCicloMs();
        lblVelocidadActual = new JLabel(velocidadInicial + " ms");
        panelEstado.add(lblVelocidadActual);
        
        // Botón para cambiar
        btnCambiarVelocidad = new JButton("Cambiar");
        panelEstado.add(btnCambiarVelocidad);
        
        lblCiclo = new JLabel("Ciclo: 0");
        panelEstado.add(lblCiclo);
        
        lblEstado = new JLabel("Detenido");
        lblEstado.setFont(new Font(lblEstado.getFont().getName(), Font.BOLD, lblEstado.getFont().getSize()));
        panelEstado.add(lblEstado);
        
        add(panelEstado);
        
        // --- Listeners ---
        
        btnIniciar.addActionListener(e -> {
            if (!sistema.isEjecutando()) {
                sistema.iniciar();
            } else if (sistema.estaPausado()) {
                sistema.reanudar();
            }
        });
        
        btnPausar.addActionListener(e -> sistema.pausar());
        btnReiniciar.addActionListener(e -> sistema.reiniciar());
        btnAgregarProceso.addActionListener(e -> mostrarDialogoAgregarProceso());
        
        comboPlanificador.addActionListener(e -> {
            String seleccion = (String) comboPlanificador.getSelectedItem();
            Planificador p;
            switch (seleccion) {
                case "SJF": p = new SJF(false); break;
                case "SRTF": p = new SJF(true); break;
                case "Prioridad NP": p = new Prioridad(false); break;
                case "Prioridad P": p = new Prioridad(true); break;
                case "Round Robin": p = new RoundRobin(sistema.getQuantumPorDefecto()); break;
                case "Multilevel Queue": p = new MultilevelQueue(); break;
                case "Multilevel FB Queue": p = new MultilevelFeedbackQueue(); break;
                default: p = new FCFS();
            }
            sistema.cambiarPlanificador(p);
        });
        
        btnCambiarVelocidad.addActionListener(e -> mostrarDialogoVelocidad());
        
        sistema.getReloj().setDuracionCicloMs(velocidadInicial);
        sistema.cambiarPlanificador(new FCFS());
    }

    private void mostrarDialogoVelocidad() {
        String valorActual = String.valueOf(sistema.getReloj().getDuracionCicloMs());
        
        String nuevoValorStr = (String) JOptionPane.showInputDialog(
            this,
            "Ingresa la nueva duración del ciclo (ms):",
            "Cambiar Velocidad",
            JOptionPane.PLAIN_MESSAGE,
            null,
            null,
            valorActual
        );

        if (nuevoValorStr != null && !nuevoValorStr.isEmpty()) {
            try {
                int nuevoValor = Integer.parseInt(nuevoValorStr);
                if (nuevoValor >= 0) { // Permitir 0 para velocidad máxima
                    sistema.getReloj().setDuracionCicloMs(nuevoValor);
                    lblVelocidadActual.setText(nuevoValor + " ms");

                    // Guardar la nueva configuración
                    ConfiguracionSistema config = GestorConfiguracion.cargarConfiguracion();
                    config.setDuracionCicloMs(nuevoValor);
                    if (GestorConfiguracion.guardarConfiguracion(config)) {
                        System.out.println("Duración del ciclo actualizada y guardada: " + nuevoValor + " ms");
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "El valor debe ser positivo.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Por favor, ingresa un número válido.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void mostrarDialogoAgregarProceso() {
        JDialog dialogo = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Agregar Proceso(s)", true);

        dialogo.setLayout(new GridLayout(9, 2, 10, 10));
        dialogo.setSize(500, 400);
        dialogo.setLocationRelativeTo(this);

        // Fila 1: Nombre
        dialogo.add(new JLabel("Nombre Base:"));
        JTextField txtNombre = new JTextField("Proc");
        dialogo.add(txtNombre);

        // Fila 2: Cantidad
        dialogo.add(new JLabel("Cantidad:"));
        JSpinner spinCantidad = new JSpinner(new SpinnerNumberModel(1, 1, 50, 1));
        dialogo.add(spinCantidad);

        // Fila 3: Tipo
        dialogo.add(new JLabel("Tipo:"));
        JComboBox<String> comboTipo = new JComboBox<>(new String[]{"CPU Bound", "I/O Bound"});
        dialogo.add(comboTipo);

        // Fila 4: Instrucciones
        dialogo.add(new JLabel("Instrucciones (Ciclos):"));
        JSpinner spinInstrucciones = new JSpinner(new SpinnerNumberModel(10, 5, 100, 5));
        dialogo.add(spinInstrucciones);

        // Fila 5: Prioridad
        dialogo.add(new JLabel("Prioridad (0=Alta):"));
        JSpinner spinPrioridad = new JSpinner(new SpinnerNumberModel(5, 0, 10, 1));
        dialogo.add(spinPrioridad);

        // Fila 6: Ciclos para generar E/S (solo I/O Bound)
        JLabel lblCiclosExcepcion = new JLabel("Ciclos para E/S:");
        JSpinner spinCiclosExcepcion = new JSpinner(new SpinnerNumberModel(5, 1, 50, 1));
        lblCiclosExcepcion.setEnabled(false);
        spinCiclosExcepcion.setEnabled(false);
        dialogo.add(lblCiclosExcepcion);
        dialogo.add(spinCiclosExcepcion);

        // Fila 7: Ciclos para satisfacer E/S (solo I/O Bound)
        JLabel lblDuracionExcepcion = new JLabel("Duración E/S (ciclos):");
        JSpinner spinDuracionExcepcion = new JSpinner(new SpinnerNumberModel(8, 1, 50, 1));
        lblDuracionExcepcion.setEnabled(false);
        spinDuracionExcepcion.setEnabled(false);
        dialogo.add(lblDuracionExcepcion);
        dialogo.add(spinDuracionExcepcion);

        // Listener para habilitar/deshabilitar campos de I/O
        comboTipo.addActionListener(e -> {
            boolean esIOBound = comboTipo.getSelectedIndex() == 1;
            lblCiclosExcepcion.setEnabled(esIOBound);
            spinCiclosExcepcion.setEnabled(esIOBound);
            lblDuracionExcepcion.setEnabled(esIOBound);
            spinDuracionExcepcion.setEnabled(esIOBound);
        });

        // Fila 8: Espacio vacío
        dialogo.add(new JLabel(""));
        dialogo.add(new JLabel(""));

        // Fila 9: Botones
        JButton btnCrear = new JButton("Crear");
        JButton btnCancelar = new JButton("Cancelar");

        btnCrear.addActionListener(e -> {
            int cantidad = (Integer) spinCantidad.getValue();
            String nombreBase = txtNombre.getText();
            TipoProceso tipo = comboTipo.getSelectedIndex() == 0 ? TipoProceso.CPU_BOUND : TipoProceso.IO_BOUND;
            int instrucciones = (Integer) spinInstrucciones.getValue();
            int prioridad = (Integer) spinPrioridad.getValue();
            int ciclosExcepcion = (Integer) spinCiclosExcepcion.getValue();
            int duracionExcepcion = (Integer) spinDuracionExcepcion.getValue();

            for (int i = 0; i < cantidad; i++) {
                String nombreProceso = cantidad > 1 ? nombreBase + "_" + (i + 1) : nombreBase;

                if (tipo == TipoProceso.IO_BOUND) {
                    sistema.agregarProceso(nombreProceso, tipo, instrucciones, prioridad,
                                          ciclosExcepcion, duracionExcepcion);
                } else {
                    sistema.agregarProceso(nombreProceso, tipo, instrucciones, prioridad);
                }
            }
            dialogo.dispose();
        });

        btnCancelar.addActionListener(e -> dialogo.dispose());

        dialogo.add(btnCrear);
        dialogo.add(btnCancelar);
        dialogo.setVisible(true);
    }
    
    public void actualizar() {
        lblCiclo.setText("Ciclo: " + sistema.getReloj().getCicloActual());

        if (!sistema.isEjecutando()) {
            lblEstado.setText("Detenido");
        } else if (sistema.estaPausado()) {
            lblEstado.setText("Pausado");
        } else {
            lblEstado.setText("Ejecutando");
        }
    }

    /**
     * Cambia el planificador del sistema y actualiza el combo box
     * @param nombrePlanificador Nombre del planificador (como aparece en el JSON)
     * @param quantum Quantum para Round Robin y MLFQ
     */
    public void cambiarPlanificadorDesdeJSON(String nombrePlanificador, int quantum) {
        // Mapear nombre del JSON a nombre del combo box
        String nombreCombo = mapearNombrePlanificador(nombrePlanificador);

        // Actualizar combo box
        comboPlanificador.setSelectedItem(nombreCombo);

        // Crear y aplicar el planificador
        Planificador p = crearPlanificador(nombreCombo, quantum);
        if (p != null) {
            sistema.cambiarPlanificador(p);
        }
    }

    /**
     * Mapea el nombre del planificador del JSON al nombre del combo box
     */
    private String mapearNombrePlanificador(String nombreJSON) {
        switch (nombreJSON) {
            case "FCFS": return "FCFS";
            case "SJF": return "SJF";
            case "SRTF": return "SRTF";
            case "Prioridad NP": return "Prioridad NP";
            case "Prioridad P": return "Prioridad P";
            case "Round Robin": return "Round Robin";
            case "Multilevel Queue": return "Multilevel Queue";
            case "Multilevel FB Queue": return "Multilevel FB Queue";
            default: return "FCFS"; // Por defecto
        }
    }

    /**
     * Crea una instancia del planificador según el nombre
     */
    private Planificador crearPlanificador(String nombre, int quantum) {
        switch (nombre) {
            case "SJF": return new SJF(false);
            case "SRTF": return new SJF(true);
            case "Prioridad NP": return new Prioridad(false);
            case "Prioridad P": return new Prioridad(true);
            case "Round Robin": return new RoundRobin(quantum);
            case "Multilevel Queue": return new MultilevelQueue();
            case "Multilevel FB Queue": return new MultilevelFeedbackQueue();
            default: return new FCFS();
        }
    }
}
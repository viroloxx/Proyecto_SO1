package gui;
import javax.swing.*;
import java.awt.*;
import sistema.SistemaOperativo;

public class PanelCentral extends JPanel {
    private SistemaOperativo sistema;
    private PanelControl panelControl;
    private PanelCPU panelCPU;
    private PanelColas panelColas;
    private PanelMetricas panelMetricas;
    private PanelMemoria panelMemoria;
    private JTextArea areaLog;
    
    public PanelCentral(SistemaOperativo sistema) {
        this.sistema = sistema;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Panel superior: Control
        panelControl = new PanelControl(sistema);
        add(panelControl, BorderLayout.NORTH);
        
        // Panel central: CPU y Colas
        JPanel panelMedio = new JPanel(new GridLayout(1, 2, 10, 10));
        
        // Izquierda: CPU
        panelCPU = new PanelCPU(sistema);
        panelMedio.add(panelCPU);
        
        // Derecha: Colas
        panelColas = new PanelColas(sistema);
        panelMedio.add(panelColas);
        
        add(panelMedio, BorderLayout.CENTER);
        
        // Panel inferior: Métricas, Memoria y Log
        JPanel panelInferior = new JPanel(new GridLayout(1, 3, 10, 10));

        // Métricas
        panelMetricas = new PanelMetricas(sistema);
        panelInferior.add(panelMetricas);

        // Memoria
        panelMemoria = new PanelMemoria(sistema);
        panelInferior.add(panelMemoria);

        // Log de eventos
        JPanel panelLog = new JPanel(new BorderLayout());
        panelLog.setBorder(BorderFactory.createTitledBorder("Log de Eventos"));
        areaLog = new JTextArea();
        areaLog.setEditable(false);
        areaLog.setFont(new Font("Monospaced", Font.PLAIN, 11));
        JScrollPane scrollLog = new JScrollPane(areaLog);
        panelLog.add(scrollLog, BorderLayout.CENTER);
        panelInferior.add(panelLog);

        // Se establece una altura preferida para el panel inferior (250px)
        panelInferior.setPreferredSize(new Dimension(100, 250));

        add(panelInferior, BorderLayout.SOUTH);
    }
    
    public void actualizar() {
        panelControl.actualizar();
        panelCPU.actualizar();
        panelColas.actualizar();
        panelMetricas.actualizar();
        panelMemoria.actualizar();
        // Actualizar el log
        areaLog.setText(sistema.getUltimasLineasLog(100));
        areaLog.setCaretPosition(areaLog.getDocument().getLength());
    }
}
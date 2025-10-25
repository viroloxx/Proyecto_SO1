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
        
        // Panel inferior: Métricas y Log
        JPanel panelInferior = new JPanel(new GridLayout(1, 2, 10, 10));
        
        // Métricas
        panelMetricas = new PanelMetricas(sistema);
        panelInferior.add(panelMetricas);
        
        // Log de eventos
        JPanel panelLog = new JPanel(new BorderLayout());
        panelLog.setBorder(BorderFactory.createTitledBorder("Log de Eventos"));
        areaLog = new JTextArea();
        areaLog.setEditable(false);
        areaLog.setFont(new Font("Monospaced", Font.PLAIN, 11));
        JScrollPane scrollLog = new JScrollPane(areaLog);
        panelLog.add(scrollLog, BorderLayout.CENTER);
        panelInferior.add(panelLog);
        
        // --- CORRECCIÓN AQUÍ ---
        // Se establece una altura preferida para el panel inferior (250px)
        // El ancho (100) es ignorado por BorderLayout.SOUTH, pero la altura es respetada.
        panelInferior.setPreferredSize(new Dimension(100, 250));
        // --- FIN DE LA CORRECCIÓN ---
        
        add(panelInferior, BorderLayout.SOUTH);
    }
    
    public void actualizar() {
        panelControl.actualizar();
        panelCPU.actualizar();
        panelColas.actualizar();
        panelMetricas.actualizar();
        // Actualizar el log (tomado del archivo original)
        areaLog.setText(sistema.getUltimasLineasLog(100));
        areaLog.setCaretPosition(areaLog.getDocument().getLength());
    }
}
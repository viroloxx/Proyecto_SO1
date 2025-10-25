package gui;
import javax.swing.*;
import java.awt.*;
import sistema.SistemaOperativo;
import modelo.PCB;

public class PanelCPU extends JPanel {
    private SistemaOperativo sistema;
    private JLabel lblEstadoCPU, lblProcesoActual, lblPC, lblMAR, lblTipoEjecucion;
    private JTextArea areaPCB;
    
    public PanelCPU(SistemaOperativo sistema) {
        this.sistema = sistema;
        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createTitledBorder("Estado del CPU"));
        
        // Panel superior: Indicadores
        JPanel panelIndicadores = new JPanel(new GridLayout(5, 1, 2, 2));
        lblEstadoCPU = new JLabel("CPU: Libre");
        lblEstadoCPU.setFont(new Font("Arial", Font.BOLD, 16));
        lblEstadoCPU.setForeground(Color.GRAY);
        panelIndicadores.add(lblEstadoCPU);
        
        lblTipoEjecucion = new JLabel("Ejecutando: Sistema Operativo");
        lblTipoEjecucion.setFont(new Font("Arial", Font.PLAIN, 12));
        panelIndicadores.add(lblTipoEjecucion);
        
        lblProcesoActual = new JLabel("Proceso: Ninguno");
        lblProcesoActual.setFont(new Font("Arial", Font.PLAIN, 14));
        panelIndicadores.add(lblProcesoActual);
        
        lblPC = new JLabel("PC: --");
        lblPC.setFont(new Font("Monospaced", Font.PLAIN, 13));
        panelIndicadores.add(lblPC);
        
        lblMAR = new JLabel("MAR: --");
        lblMAR.setFont(new Font("Monospaced", Font.PLAIN, 13));
        panelIndicadores.add(lblMAR);
        
        add(panelIndicadores, BorderLayout.NORTH);
        
        // Panel central: PCB
        areaPCB = new JTextArea();
        areaPCB.setEditable(false);
        // Se eliminó la fuente personalizada y el fondo
        JScrollPane scrollPCB = new JScrollPane(areaPCB);
        add(scrollPCB, BorderLayout.CENTER);
    }
    
    public void actualizar() {
        PCB proceso = sistema.getCpu().getProcesoActual();
        if (proceso != null) {
            lblEstadoCPU.setText("CPU: Ocupado");
            lblEstadoCPU.setForeground(new Color(0, 128, 0)); // Verde oscuro
            lblTipoEjecucion.setText("Ejecutando: Proceso de Usuario");
            lblProcesoActual.setText(String.format("Proceso: %s (ID:%d)", proceso.getNombre(), proceso.getIdProceso()));
            lblPC.setText(String.format("PC: %d", proceso.getProgramCounter()));
            lblMAR.setText(String.format("MAR: %d", proceso.getMemoryAddressRegister()));
            
            // Llenar el JTextArea con info del PCB
            StringBuilder pcbInfo = new StringBuilder();
            pcbInfo.append(String.format("ID: %d | Nombre: %s \n", proceso.getIdProceso(), proceso.getNombre()));
            pcbInfo.append(String.format("Estado: %s | Tipo: %s \n", proceso.getEstado(), proceso.getTipo()));
            pcbInfo.append("--- Registros ---\n");
            pcbInfo.append(String.format("PC:   %d / %d \n", proceso.getProgramCounter(), proceso.getTiempoEjecucion()));
            pcbInfo.append(String.format("MAR:  %d \n", proceso.getMemoryAddressRegister()));
            pcbInfo.append("--- Planificación ---\n");
            pcbInfo.append(String.format("Prioridad:       %d\n", proceso.getPrioridad()));
            pcbInfo.append(String.format("Tiempo Llegada:  %d\n", proceso.getTiempoLlegada()));
            pcbInfo.append(String.format("Tiempo Total:    %d ciclos\n", proceso.getTiempoEjecucion()));
            pcbInfo.append(String.format("Tiempo Restante: %d ciclos\n", proceso.getTiempoRestante()));
            pcbInfo.append(String.format("Tiempo Espera:   %d ciclos\n", proceso.getTiempoEsperaTotal()));
            pcbInfo.append(String.format("Progreso:        %.1f%%\n", 
                ((double)(proceso.getTiempoEjecucion() - proceso.getTiempoRestante()) / proceso.getTiempoEjecucion()) * 100));
            
            if (proceso.getCiclosExcepcionRestantes() > 0) {
                pcbInfo.append("--- E/S ---\n");
                pcbInfo.append(String.format("Ciclos E/S Rest: %d\n", proceso.getCiclosExcepcionRestantes()));
            }
            
            areaPCB.setText(pcbInfo.toString());
        } else {
            lblEstadoCPU.setText("CPU: Libre");
            lblEstadoCPU.setForeground(Color.GRAY);
            lblTipoEjecucion.setText("Ejecutando: Sistema Operativo");
            lblProcesoActual.setText("Proceso: Ninguno");
            lblPC.setText("PC: --");
            lblMAR.setText("MAR: --");
            areaPCB.setText("");
        }
    }
}
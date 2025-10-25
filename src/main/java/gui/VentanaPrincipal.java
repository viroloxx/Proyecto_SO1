package gui;
import javax.swing.*;
import java.awt.*;
import sistema.SistemaOperativo;
import persistencia.*;

public class VentanaPrincipal extends JFrame {
    private SistemaOperativo sistema;
    private PanelCentral panelCentral;
    private Timer actualizador;
    
    public VentanaPrincipal() {
        // Usamos la configuración por defecto
        ConfiguracionSistema config = new ConfiguracionSistema(300, "FCFS", 3);
        this.sistema = new SistemaOperativo(config);
        inicializarComponentes();
        iniciarActualizador();
    }
    
    private void inicializarComponentes() {
        // Título simplificado
        setTitle("Simulador de Planificación de Procesos");
        setSize(1400, 900);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        // Layout principal
        setLayout(new BorderLayout(10, 10));
        
        // Panel central con toda la simulación
        panelCentral = new PanelCentral(sistema);
        add(panelCentral, BorderLayout.CENTER);
        
        // Barra de menú
        JMenuBar menuBar = new JMenuBar();
        JMenu menuArchivo = new JMenu("Archivo");
        JMenuItem itemSalir = new JMenuItem("Salir");
        itemSalir.addActionListener(e -> System.exit(0));
        menuArchivo.add(itemSalir);
        menuBar.add(menuArchivo);
        setJMenuBar(menuBar);
    }
    
    private void iniciarActualizador() {
        actualizador = new Timer(100, e -> {
            panelCentral.actualizar();
        });
        actualizador.start();
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                // Usar el Look and Feel nativo del sistema operativo
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            VentanaPrincipal ventana = new VentanaPrincipal();
            ventana.setVisible(true);
        });
    }
}
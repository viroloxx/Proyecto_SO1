package gui;
import javax.swing.*;
import java.awt.*;
import sistema.SistemaOperativo;
import persistencia.*;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import persistencia.PersistenciaCSV; 
import persistencia.PersistenciaJSON; 
import java.io.File; 

public class VentanaPrincipal extends JFrame {
    private SistemaOperativo sistema;
    private PanelCentral panelCentral;
    private PanelGrafico panelGrafico;
    private Timer actualizador;

    public VentanaPrincipal() {
        
        ConfiguracionSistema config = GestorConfiguracion.cargarConfiguracion(); 
        this.sistema = new SistemaOperativo(config);
        inicializarComponentes();
        iniciarActualizador();
    }
    
    private void inicializarComponentes() {
        setTitle("Simulador de Planificación de Procesos");
        setSize(1400, 900);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        setLayout(new BorderLayout(10, 10));

        // Crear pestañas para organizar la interfaz
        JTabbedPane pestanas = new JTabbedPane();

        panelCentral = new PanelCentral(sistema);
        panelGrafico = new PanelGrafico();

        pestanas.addTab("Simulación", panelCentral);
        pestanas.addTab("Gráfico de Métricas", panelGrafico);

        add(pestanas, BorderLayout.CENTER);
        
    
        JMenuBar menuBar = new JMenuBar();
        JMenu menuArchivo = new JMenu("Carga/Guarda");

        
        JMenuItem itemCargarJSON = new JMenuItem("Cargar Procesos desde JSON...");
        itemCargarJSON.setEnabled(false); 
        
    
        JMenuItem itemGuardarCSV = new JMenuItem("Guardar Resultados en CSV...");
        JMenuItem itemGuardarJSON = new JMenuItem("Guardar Resultados en JSON...");
        
        JMenuItem itemSalir = new JMenuItem("Salir");
        
        
        menuArchivo.add(itemCargarJSON);
        menuArchivo.addSeparator();
        menuArchivo.add(itemGuardarCSV);
        menuArchivo.add(itemGuardarJSON);
        menuArchivo.addSeparator();
        menuArchivo.add(itemSalir);
        
        menuBar.add(menuArchivo);
        setJMenuBar(menuBar);

        // --- ACTION LISTENERS ---
        itemSalir.addActionListener(e -> System.exit(0));
        itemGuardarCSV.addActionListener(e -> guardarACSV());
        itemGuardarJSON.addActionListener(e -> guardarAJSON());
    }
    
    private void iniciarActualizador() {
        actualizador = new Timer(100, e -> {
            panelCentral.actualizar();
            panelGrafico.actualizar(sistema.getMetricas().getHistorial());
        });
        actualizador.start();
    }
    
    
    private void guardarACSV() {
    
        boolean estabaEjecutando = sistema.isEjecutando() && !sistema.estaPausado();
        if (estabaEjecutando) {
            sistema.pausar();
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Guardar Resultados como CSV");
        fileChooser.setFileFilter(new FileNameExtensionFilter("Archivos CSV (*.csv)", "csv"));
        
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                File archivo = fileChooser.getSelectedFile();
                String ruta = archivo.getAbsolutePath();
                if (!ruta.toLowerCase().endsWith(".csv")) {
                    ruta += ".csv";
                }
                

                PersistenciaCSV.guardarResultados(sistema.getAdminProcesos().getListaTerminados(), ruta);
                
                JOptionPane.showMessageDialog(this, "Resultados guardados en CSV:\n" + ruta, "Exportación Exitosa", JOptionPane.INFORMATION_MESSAGE);
                
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error al guardar CSV: " + ex.getMessage(), "Error de Exportación", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
        
    
        if (estabaEjecutando) {
            sistema.reanudar();
        }
    }

    // --- FUNCIÓN PARA GUARDAR EN JSON ---
    private void guardarAJSON() {
        boolean estabaEjecutando = sistema.isEjecutando() && !sistema.estaPausado();
        if (estabaEjecutando) {
            sistema.pausar();
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Guardar Resultados como JSON");
        fileChooser.setFileFilter(new FileNameExtensionFilter("Archivos JSON (*.json)", "json"));
        
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                File archivo = fileChooser.getSelectedFile();
                String ruta = archivo.getAbsolutePath();
                if (!ruta.toLowerCase().endsWith(".json")) {
                    ruta += ".json";
                }
                
            
                PersistenciaJSON.guardarResultados(sistema.getAdminProcesos().getListaTerminados(), ruta);

                JOptionPane.showMessageDialog(this, "Resultados guardados en JSON:\n" + ruta, "Exportación Exitosa", JOptionPane.INFORMATION_MESSAGE);

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error al guardar JSON: " + ex.getMessage(), "Error de Exportación", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
        
        if (estabaEjecutando) {
            sistema.reanudar();
        }
    }

    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            VentanaPrincipal ventana = new VentanaPrincipal();
            ventana.setVisible(true);
        });
    }
}

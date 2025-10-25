package gui;
import javax.swing.*;
import java.awt.*;
import sistema.SistemaOperativo;
import persistencia.*;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

public class VentanaPrincipal extends JFrame {
    private SistemaOperativo sistema;
    private PanelCentral panelCentral;
    private PanelGrafico panelGrafico;
    private Timer actualizador;

    public VentanaPrincipal() {
        ConfiguracionSistema config = new ConfiguracionSistema(300, "FCFS", 3);
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
        JMenu menuArchivo = new JMenu("Archivo");
        

        JMenuItem itemExportarCSV = new JMenuItem("Exportar a CSV...");
        JMenuItem itemExportarJSON = new JMenuItem("Exportar a JSON...");
        

        JMenuItem itemSalir = new JMenuItem("Salir");
        
        menuArchivo.add(itemExportarCSV);
        menuArchivo.add(itemExportarJSON);
        menuArchivo.addSeparator(); 
        menuArchivo.add(itemSalir);
        
        menuBar.add(menuArchivo);
        setJMenuBar(menuBar);

    
        itemSalir.addActionListener(e -> System.exit(0));
        
        itemExportarCSV.addActionListener(e -> exportarACSV());
        itemExportarJSON.addActionListener(e -> exportarAJSON());
    }
    
    private void iniciarActualizador() {
        actualizador = new Timer(100, e -> {
            panelCentral.actualizar();
            panelGrafico.actualizar(sistema.getMetricas().getHistorial());
        });
        actualizador.start();
    }
    
private void exportarACSV() {
 
        boolean estabaEjecutando = sistema.isEjecutando() && !sistema.estaPausado();
        if (estabaEjecutando) {
            sistema.pausar();
        }

      
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Guardar como CSV");
        fileChooser.setFileFilter(new FileNameExtensionFilter("Archivos CSV (*.csv)", "csv"));
        
        int seleccion = fileChooser.showSaveDialog(this);
        
        if (seleccion == JFileChooser.APPROVE_OPTION) {
            try {
                String ruta = fileChooser.getSelectedFile().getAbsolutePath();
                if (!ruta.toLowerCase().endsWith(".csv")) {
                    ruta += ".csv";
                }
                
         
                PersistenciaCSV.guardarResultados(sistema.getAdminProcesos().getListaTerminados(), ruta);
                
                JOptionPane.showMessageDialog(this, "Resultados guardados en:\n" + ruta, "Exportar CSV", JOptionPane.INFORMATION_MESSAGE);
            
                
            } catch (Exception ex) {
        
                JOptionPane.showMessageDialog(this, "Error al guardar CSV: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace(); // Bueno para depurar
            }
        }
        
        // 4. Reanudamos la simulación
        if (estabaEjecutando) {
            sistema.reanudar();
        }
    }


    private void exportarAJSON() {

        boolean estabaEjecutando = sistema.isEjecutando() && !sistema.estaPausado();
        if (estabaEjecutando) {
            sistema.pausar();
        }


        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Guardar como JSON");
        fileChooser.setFileFilter(new FileNameExtensionFilter("Archivos JSON (*.json)", "json"));
        
        int seleccion = fileChooser.showSaveDialog(this);
        
        if (seleccion == JFileChooser.APPROVE_OPTION) {
            try {
                String ruta = fileChooser.getSelectedFile().getAbsolutePath();
                if (!ruta.toLowerCase().endsWith(".json")) {
                    ruta += ".json";
                }
                

                PersistenciaJSON.guardarResultados(sistema.getAdminProcesos().getListaTerminados(), ruta);

                JOptionPane.showMessageDialog(this, "Resultados guardados en:\n" + ruta, "Exportar JSON", JOptionPane.INFORMATION_MESSAGE);


            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error al guardar JSON: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace(); // Bueno para depurar
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
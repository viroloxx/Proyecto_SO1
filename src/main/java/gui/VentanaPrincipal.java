package gui;
import javax.swing.*;
import java.awt.*;
import sistema.SistemaOperativo;
import persistencia.*;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import persistencia.PersistenciaCSV;
import persistencia.PersistenciaJSON;
import persistencia.GestorCargaProcesos;
import persistencia.ConfiguracionCompleta;
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
        // AHORA HABILITADO
        itemCargarJSON.setEnabled(true); 
        
    
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
        itemCargarJSON.addActionListener(e -> cargarProcesosDesdeJSON());
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

    // --- FUNCIÓN PARA CARGAR PROCESOS DESDE JSON ---
    private void cargarProcesosDesdeJSON() {
        // Pausar el sistema si está ejecutando
        boolean estabaEjecutando = sistema.isEjecutando() && !sistema.estaPausado();
        if (estabaEjecutando) {
            sistema.pausar();
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Cargar Procesos desde JSON");
        fileChooser.setFileFilter(new FileNameExtensionFilter("Archivos JSON (*.json)", "json"));
        fileChooser.setCurrentDirectory(new File(".")); // Directorio actual

        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                File archivo = fileChooser.getSelectedFile();
                String ruta = archivo.getAbsolutePath();

                // Cargar configuración desde JSON
                ConfiguracionCompleta config = GestorCargaProcesos.cargarConfiguracion(ruta);

                if (config == null) {
                    JOptionPane.showMessageDialog(this,
                        "Error al cargar el archivo JSON.\nVerifique el formato del archivo.",
                        "Error de Carga",
                        JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Cargar procesos en el sistema
                int procesosCarados = GestorCargaProcesos.cargarProcesosEnSistema(config, sistema);

                // Aplicar configuración del sistema si existe
                if (config.getConfiguracionSistema() != null) {
                    ConfiguracionSistema configSistema = config.getConfiguracionSistema();

                    // Mostrar diálogo de confirmación para aplicar configuración
                    int opcion = JOptionPane.showConfirmDialog(this,
                        String.format("¿Aplicar también la configuración del sistema?\n" +
                                    "- Duración ciclo: %d ms\n" +
                                    "- Planificador: %s\n" +
                                    "- Quantum: %d",
                                    configSistema.getDuracionCicloMs(),
                                    configSistema.getPlanificadorInicial(),
                                    configSistema.getQuantumRR()),
                        "Aplicar Configuración",
                        JOptionPane.YES_NO_OPTION);

                    if (opcion == JOptionPane.YES_OPTION) {
                        sistema.getReloj().setDuracionCicloMs(configSistema.getDuracionCicloMs());
                        // Guardar configuración para persistencia
                        GestorConfiguracion.guardarConfiguracion(configSistema);
                    }
                }

                // Mensaje de éxito
                JOptionPane.showMessageDialog(this,
                    String.format("Procesos cargados exitosamente:\n" +
                                "- %d procesos importados\n" +
                                "- Archivo: %s",
                                procesosCarados,
                                archivo.getName()),
                    "Carga Exitosa",
                    JOptionPane.INFORMATION_MESSAGE);

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                    "Error al cargar procesos: " + ex.getMessage(),
                    "Error de Carga",
                    JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }

        // Reanudar el sistema si estaba ejecutando
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

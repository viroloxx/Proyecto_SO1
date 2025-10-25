package gui;
import javax.swing.*;
import java.awt.*;
import sistema.SistemaOperativo;
import estructura_datos.*;
import modelo.PCB;

public class PanelMetricas extends JPanel {
    private SistemaOperativo sistema;
    private JLabel lblThroughput, lblUtilizacion, lblTiempoEspera, lblTiempoRespuesta;
    private JLabel lblProcesosCompletados, lblProcesosActivos;
    
    public PanelMetricas(SistemaOperativo sistema) {
        this.sistema = sistema;
        setLayout(new GridLayout(3, 2, 10, 10));
        // Título simplificado
        setBorder(BorderFactory.createTitledBorder("Métricas del Sistema"));
        // Se eliminó setBackground(...)
        
        // Panel para cada métrica
        add(crearPanelMetrica("Throughput", "0.00 proc/ciclo"));
        add(crearPanelMetrica("Utilización CPU", "0.00%"));
        add(crearPanelMetrica("Tiempo Espera Prom", "0.00 ciclos"));
        add(crearPanelMetrica("Tiempo Respuesta Prom", "0.00 ciclos"));
        add(crearPanelMetrica("Procesos Completados", "0"));
        add(crearPanelMetrica("Procesos Activos", "0"));
        
        // Obtener las etiquetas
        Component[] componentes = getComponents();
        lblThroughput = obtenerLabel(componentes[0]);
        lblUtilizacion = obtenerLabel(componentes[1]);
        lblTiempoEspera = obtenerLabel(componentes[2]);
        lblTiempoRespuesta = obtenerLabel(componentes[3]);
        lblProcesosCompletados = obtenerLabel(componentes[4]);
        lblProcesosActivos = obtenerLabel(componentes[5]);
    }
    
    private JLabel obtenerLabel(Component panel) {
        return (JLabel) ((JPanel) panel).getComponent(1);
    }
    
    private JPanel crearPanelMetrica(String titulo, String valorInicial) {
        JPanel panel = new JPanel(new BorderLayout());
        // Se eliminó panel.setOpaque(false)
        
        JLabel lblTitulo = new JLabel(titulo);
        // Se eliminaron las fuentes personalizadas
        lblTitulo.setForeground(new Color(60, 60, 60));
        panel.add(lblTitulo, BorderLayout.NORTH);
        
        JLabel lblValor = new JLabel(valorInicial);
        // Se eliminaron las fuentes personalizadas
        lblValor.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(lblValor, BorderLayout.CENTER);
        
        return panel;
    }
    
    public void actualizar() {
        int cicloActual = sistema.getReloj().getCicloActual();
        
        // Throughput
        double throughput = sistema.getMetricas().calcularThroughput(cicloActual);
        lblThroughput.setText(String.format("%.2f proc/ciclo", throughput));
        
        // Utilización CPU
        double utilizacion = sistema.getMetricas().calcularUtilizacionCPU();
        lblUtilizacion.setText(String.format("%.2f%%", utilizacion));
        
        // Tiempos (necesita coleccionar todos los procesos)
        Cola listos = sistema.getAdminProcesos().getColaListos();
        Cola bloqueados = sistema.getAdminProcesos().getColaBloqueados();
        Lista terminados = sistema.getAdminProcesos().getListaTerminados();
        
        int totalProcesos = listos.obtenerTamanio() + bloqueados.obtenerTamanio() + terminados.obtenerTamanio();
        if (sistema.getCpu().getProcesoActual() != null) {
            totalProcesos++;
        }
        
        PCB[] todosProcesos = new PCB[totalProcesos];
        int idx = 0;
        
        for (PCB p : listos.toArray()) {
            todosProcesos[idx++] = p;
        }
        for (PCB p : bloqueados.toArray()) {
            todosProcesos[idx++] = p;
        }
        for (PCB p : terminados.toArray()) {
            todosProcesos[idx++] = p;
        }
        if (sistema.getCpu().getProcesoActual() != null) {
            todosProcesos[idx] = sistema.getCpu().getProcesoActual();
        }
        
        // Tiempo de espera promedio
        double tiempoEspera = sistema.getMetricas().calcularTiempoEsperaPromedio(todosProcesos);
        lblTiempoEspera.setText(String.format("%.2f ciclos", tiempoEspera));
        
        // Tiempo de respuesta promedio (solo terminados)
        PCB[] procesosTerminados = terminados.toArray();
        double tiempoRespuesta = sistema.getMetricas().calcularTiempoRespuestaPromedio(procesosTerminados);
        lblTiempoRespuesta.setText(String.format("%.2f ciclos", tiempoRespuesta));
        
        // Procesos completados
        lblProcesosCompletados.setText(String.valueOf(sistema.getMetricas().getProcesosCompletados()));
        
        // Procesos activos
        int activos = totalProcesos - procesosTerminados.length;
        lblProcesosActivos.setText(String.valueOf(activos));
    }
}
package gui;

import metricas.HistorialMetricas;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Panel que muestra gráficos de las métricas del sistema a lo largo del tiempo.
 * Utiliza JFreeChart para visualizar la evolución de las métricas de rendimiento.
 */
public class PanelGrafico extends JPanel {

    private final XYSeries serieUtilizacionCPU;
    private final XYSeries serieThroughput;
    private final XYSeries serieTiempoEspera;
    private final XYSeries serieTiempoRespuesta;

    private final ChartPanel chartPanel;
    private final JComboBox<String> selectorMetrica;

    /**
     * Constructor que inicializa el panel de gráficos
     */
    public PanelGrafico() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("Gráfico de Métricas vs Tiempo"));

        // Inicializar las series de datos
        serieUtilizacionCPU = new XYSeries("Utilización CPU (%)");
        serieThroughput = new XYSeries("Throughput (x100)");
        serieTiempoEspera = new XYSeries("Tiempo Espera Promedio");
        serieTiempoRespuesta = new XYSeries("Tiempo Respuesta Promedio");

        // Crear el dataset inicial con todas las métricas
        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(serieUtilizacionCPU);
        dataset.addSeries(serieThroughput);
        dataset.addSeries(serieTiempoEspera);
        dataset.addSeries(serieTiempoRespuesta);

        // Crear el gráfico
        JFreeChart chart = ChartFactory.createXYLineChart(
                "Rendimiento del Sistema en el Tiempo",
                "Ciclos",
                "Valor",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );

        // Personalizar el gráfico
        customizeChart(chart);

        // Crear el panel del gráfico
        chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(800, 400));
        chartPanel.setMouseWheelEnabled(true);

        // Panel de control superior
        JPanel panelControl = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelControl.add(new JLabel("Métricas visibles:"));

        selectorMetrica = new JComboBox<>(new String[]{
            "Todas las métricas",
            "Solo Utilización CPU",
            "Solo Throughput",
            "Solo Tiempos de Espera/Respuesta"
        });

        selectorMetrica.addActionListener(e -> actualizarVisibilidadSeries());
        panelControl.add(selectorMetrica);

        // Agregar componentes al panel
        add(panelControl, BorderLayout.NORTH);
        add(chartPanel, BorderLayout.CENTER);
    }

    /**
     * Personaliza la apariencia del gráfico
     */
    private void customizeChart(JFreeChart chart) {
        XYPlot plot = chart.getXYPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setDomainGridlinePaint(Color.LIGHT_GRAY);
        plot.setRangeGridlinePaint(Color.LIGHT_GRAY);

        // Personalizar el renderer
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        renderer.setSeriesPaint(0, new Color(0, 102, 204)); // Azul para CPU
        renderer.setSeriesPaint(1, new Color(0, 153, 0)); // Verde para Throughput
        renderer.setSeriesPaint(2, new Color(204, 0, 0)); // Rojo para Tiempo Espera
        renderer.setSeriesPaint(3, new Color(255, 128, 0)); // Naranja para Tiempo Respuesta

        renderer.setSeriesStroke(0, new BasicStroke(2.0f));
        renderer.setSeriesStroke(1, new BasicStroke(2.0f));
        renderer.setSeriesStroke(2, new BasicStroke(2.0f));
        renderer.setSeriesStroke(3, new BasicStroke(2.0f));

        plot.setRenderer(renderer);
    }

    /**
     * Actualiza la visibilidad de las series según la selección del usuario
     */
    private void actualizarVisibilidadSeries() {
        JFreeChart chart = chartPanel.getChart();
        XYPlot plot = chart.getXYPlot();
        XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) plot.getRenderer();

        int seleccion = selectorMetrica.getSelectedIndex();

        switch (seleccion) {
            case 0: // Todas las métricas
                renderer.setSeriesVisible(0, true);
                renderer.setSeriesVisible(1, true);
                renderer.setSeriesVisible(2, true);
                renderer.setSeriesVisible(3, true);
                break;
            case 1: // Solo Utilización CPU
                renderer.setSeriesVisible(0, true);
                renderer.setSeriesVisible(1, false);
                renderer.setSeriesVisible(2, false);
                renderer.setSeriesVisible(3, false);
                break;
            case 2: // Solo Throughput
                renderer.setSeriesVisible(0, false);
                renderer.setSeriesVisible(1, true);
                renderer.setSeriesVisible(2, false);
                renderer.setSeriesVisible(3, false);
                break;
            case 3: // Solo Tiempos
                renderer.setSeriesVisible(0, false);
                renderer.setSeriesVisible(1, false);
                renderer.setSeriesVisible(2, true);
                renderer.setSeriesVisible(3, true);
                break;
        }
    }

    /**
     * Actualiza el gráfico con los datos del historial de métricas
     *
     * @param historial Historial de métricas a visualizar
     */
    public void actualizar(HistorialMetricas historial) {
        if (historial == null || historial.estaVacio()) {
            return;
        }

        // Limpiar las series
        serieUtilizacionCPU.clear();
        serieThroughput.clear();
        serieTiempoEspera.clear();
        serieTiempoRespuesta.clear();

        // Obtener los datos del historial
        List<Integer> ciclos = historial.getCiclos();
        List<Double> utilizacionCPU = historial.getUtilizacionCPU();
        List<Double> throughput = historial.getThroughput();
        List<Double> tiempoEspera = historial.getTiempoEsperaPromedio();
        List<Double> tiempoRespuesta = historial.getTiempoRespuestaPromedio();

        // Agregar los datos a las series
        for (int i = 0; i < ciclos.size(); i++) {
            int ciclo = ciclos.get(i);
            serieUtilizacionCPU.add(ciclo, utilizacionCPU.get(i));
            // Multiplicamos throughput por 100 para mejor visualización
            serieThroughput.add(ciclo, throughput.get(i) * 100);
            serieTiempoEspera.add(ciclo, tiempoEspera.get(i));
            serieTiempoRespuesta.add(ciclo, tiempoRespuesta.get(i));
        }
    }

    /**
     * Reinicia el gráfico, limpiando todas las series
     */
    public void reiniciar() {
        serieUtilizacionCPU.clear();
        serieThroughput.clear();
        serieTiempoEspera.clear();
        serieTiempoRespuesta.clear();
    }

    /**
     * Agrega un punto de datos al gráfico en tiempo real
     *
     * @param ciclo Número del ciclo
     * @param utilizacionCPU Utilización del CPU (%)
     * @param throughput Throughput
     * @param tiempoEspera Tiempo de espera promedio
     * @param tiempoRespuesta Tiempo de respuesta promedio
     */
    public void agregarPunto(int ciclo, double utilizacionCPU, double throughput,
                            double tiempoEspera, double tiempoRespuesta) {
        serieUtilizacionCPU.add(ciclo, utilizacionCPU);
        serieThroughput.add(ciclo, throughput * 100);
        serieTiempoEspera.add(ciclo, tiempoEspera);
        serieTiempoRespuesta.add(ciclo, tiempoRespuesta);

        // Limitar el número de puntos visibles para mejor rendimiento
        // Mantener solo los últimos 100 puntos
        if (serieUtilizacionCPU.getItemCount() > 100) {
            serieUtilizacionCPU.remove(0);
            serieThroughput.remove(0);
            serieTiempoEspera.remove(0);
            serieTiempoRespuesta.remove(0);
        }
    }
}

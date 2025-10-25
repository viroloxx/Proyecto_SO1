package gui;
import javax.swing.*;
import java.awt.*;
import sistema.SistemaOperativo;
import estructura_datos.*;
import modelo.PCB;
import modelo.EstadoProceso;

public class PanelColas extends JPanel {
    private SistemaOperativo sistema;
    private JTextArea areaNuevos, areaListos, areaBloqueados, areaSuspendidos, areaTerminados;
    
    public PanelColas(SistemaOperativo sistema) {
        this.sistema = sistema;
        setLayout(new GridLayout(5, 1, 5, 5));
        setBorder(BorderFactory.createTitledBorder("Colas del Sistema"));
        
        // --- CORRECCIÓN AQUÍ ---
        // Se eliminó el segundo parámetro (Color) de las llamadas
        
        // Cola de Nuevos
        JPanel panelNuevos = crearPanelCola("Nuevos");
        areaNuevos = (JTextArea) ((JScrollPane) panelNuevos.getComponent(0)).getViewport().getView();
        add(panelNuevos);
        
        // Cola de Listos
        JPanel panelListos = crearPanelCola("Listos");
        areaListos = (JTextArea) ((JScrollPane) panelListos.getComponent(0)).getViewport().getView();
        add(panelListos);
        
        // Cola de Bloqueados
        JPanel panelBloqueados = crearPanelCola("Bloqueados");
        areaBloqueados = (JTextArea) ((JScrollPane) panelBloqueados.getComponent(0)).getViewport().getView();
        add(panelBloqueados);
        
        // Cola de Suspendidos
        JPanel panelSuspendidos = crearPanelCola("Suspendidos");
        areaSuspendidos = (JTextArea) ((JScrollPane) panelSuspendidos.getComponent(0)).getViewport().getView();
        add(panelSuspendidos);
        
        // Lista de Terminados
        JPanel panelTerminados = crearPanelCola("Terminados");
        areaTerminados = (JTextArea) ((JScrollPane) panelTerminados.getComponent(0)).getViewport().getView();
        add(panelTerminados);
    }

    // --- CORRECCIÓN AQUÍ ---
    // Se eliminó el parámetro Color y la línea setBackground
    private JPanel crearPanelCola(String titulo) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(titulo));
        // panel.setBackground(colorFondo); // <--- LÍNEA ELIMINADA
        
        JTextArea areaTexto = new JTextArea("(vacío)", 3, 40);
        areaTexto.setEditable(false);
        areaTexto.setLineWrap(true);
        areaTexto.setWrapStyleWord(true);
        
        JScrollPane scroll = new JScrollPane(areaTexto);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }
    
    public void actualizar() {
        // Nuevos
        areaNuevos.setText(formatearCola(sistema.getAdminProcesos().getColaNuevos()));
        
        // Listos
        areaListos.setText(formatearCola(sistema.getAdminProcesos().getColaListos()));
        
        // Bloqueados
        areaBloqueados.setText(formatearCola(sistema.getAdminProcesos().getColaBloqueados()));
        
        // Suspendidos
        Cola suspListos = sistema.getAdminProcesos().getColaSuspendidosListos();
        Cola suspBloq = sistema.getAdminProcesos().getColaSuspendidosBloqueados();
        StringBuilder suspendidos = new StringBuilder();
        if (!suspListos.estaVacia() || !suspBloq.estaVacia()) {
            if (!suspListos.estaVacia()) {
                suspendidos.append("Susp-Listos: ").append(formatearCola(suspListos));
            }
            if (!suspBloq.estaVacia()) {
                if (suspendidos.length() > 0) suspendidos.append("\n");
                suspendidos.append("Susp-Bloq: ").append(formatearCola(suspBloq));
            }
        } else {
            suspendidos.append("(vacío)");
        }
        areaSuspendidos.setText(suspendidos.toString());
        
        // Terminados
        areaTerminados.setText(formatearLista(sistema.getAdminProcesos().getListaTerminados()));
    }
    
    private String formatearCola(Cola cola) {
        if (cola.estaVacia()) {
            return "(vacío)";
        }
        
        StringBuilder sb = new StringBuilder();
        PCB[] procesos = cola.toArray();
        for (int i = 0; i < procesos.length; i++) {
            PCB p = procesos[i];
            sb.append(String.format("%s[ID:%d,R:%d,P:%d]", 
                p.getNombre(), p.getIdProceso(), p.getTiempoRestante(), p.getPrioridad()));
            if (i < procesos.length - 1) {
                sb.append(" → ");
            }
        }
        return sb.toString();
    }
    
    private String formatearLista(Lista lista) {
        if (lista.estaVacia()) {
            return "(vacío)";
        }
        
        StringBuilder sb = new StringBuilder();
        PCB[] procesos = lista.toArray();
        for (int i = 0; i < procesos.length; i++) {
            PCB p = procesos[i];
            sb.append(String.format("%s[ID:%d,Ret:%d]", 
                p.getNombre(), p.getIdProceso(), p.getTiempoRetorno()));
            if (i < procesos.length - 1) {
                sb.append(", ");
            }
        }
        return sb.toString();
    }
}
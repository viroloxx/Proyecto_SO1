package UI; 


import Clases.PCB;
import Clases.PCB.Estado;
// --- FIN CAMBIOS ---

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/**
 *
 * @author Diego A. Vivolo 
 * */

@SuppressWarnings("serial")
class PanelProceso extends JPanel {

	PCB proceso;

	static final int PPANCHO = 12;
	static final int PPALTO = 150;


	static final int BARALTURA = 135;

	Color burstColor, initBurstColor = Color.darkGray, unarrivedColor,
			lblColor;

	JLabel pidLbl;
	
	static boolean showHidden = true;

	PanelProceso() {
		proceso = new PCB("Default", 100, 1);
		initPanel();
	}

	PanelProceso(PCB p) {
		proceso = p;
		initPanel();
	}

	void initPanel() {
		setAlignmentX(Component.LEFT_ALIGNMENT);
		setLayout(new BorderLayout());
		
		pidLbl = new JLabel("" + (int) proceso.getId());
		pidLbl.setFont(new Font("Helvetica", Font.BOLD, 8));
		pidLbl.setToolTipText(proceso.toString()); 
		pidLbl.setHorizontalAlignment(SwingConstants.CENTER);

		pidLbl.addMouseListener(new MouseListener() {
			
			@Override
			public void mouseReleased(MouseEvent arg0) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void mousePressed(MouseEvent arg0) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void mouseExited(MouseEvent arg0) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void mouseEntered(MouseEvent arg0) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void mouseClicked(MouseEvent arg0) {
				JOptionPane.showMessageDialog(null, proceso.toString(), "Informacion del Proceso", JOptionPane.INFORMATION_MESSAGE);
				
			}
		});
		
		
		setSize(PPANCHO, PPALTO);
		setBackground(Color.white);
		setOpaque(true);
		add(pidLbl, "South");
	}


	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		if (proceso.getEstado() == Estado.TERMINADO) {
			setVisible(false);
		} else {
			DrawBursts(g);
		}
	}

	void DrawBursts(Graphics g) {
		int initBurstHeight = 0, burstHeight = 0;
		int width = 0;

		initBurstHeight = (int) proceso.getTotalInstrucciones();
		burstHeight = (int) proceso.getTiempoRestante();
		width = (int) PPANCHO - 2;

        double escala = 1.0;
        if (initBurstHeight > BARALTURA) {
            escala = (double) BARALTURA / initBurstHeight;
        }
        
        int alturaTotalDibujada = (int) (initBurstHeight * escala);
        int alturaRestanteDibujada = (int) (burstHeight * escala);

        Estado estadoActual = proceso.getEstado();

        // Asignar colores basados en el estado
        switch (estadoActual) {
            case NUEVO:
                lblColor = showHidden ? Color.lightGray : Color.white;
                initBurstColor = Color.lightGray;
                burstColor = showHidden ? Color.darkGray : Color.white;
                pidLbl.setBackground(Color.white);
                break;
            case LISTO:
            case LISTO_SUSPENDIDO: // (Tratamos igual)
                lblColor = Color.black;
                initBurstColor = Color.lightGray;
                burstColor = Color.cyan; // Color para "Listo"
                pidLbl.setBackground(Color.white);
                break;
            case EJECUCION:
                lblColor = Color.black;
                initBurstColor = Color.lightGray;
                burstColor = Color.red; // Color para "Activo"
                pidLbl.setBackground(Color.red);
                break;
            case BLOQUEADO:
            case BLOQUEADO_SUSPENDIDO: // (Tratamos igual)
                lblColor = Color.black;
                initBurstColor = Color.green; // Color para "Bloqueado"
                burstColor = Color.green;
                pidLbl.setBackground(Color.white);
                break;
            default: // TERMINADO (aunque ya se filtra en paintComponent)
                lblColor = Color.white;
                initBurstColor = Color.white;
                burstColor = Color.white;
                pidLbl.setBackground(Color.white);
                break;
        }

		pidLbl.setForeground(lblColor);

		if (estadoActual == Estado.LISTO || estadoActual == Estado.EJECUCION || estadoActual == Estado.LISTO_SUSPENDIDO) {
			g.setColor(initBurstColor); // Dibuja el fondo gris (total)
			g.drawRect(0, BARALTURA - alturaTotalDibujada, width,
					alturaTotalDibujada);
			g.setColor(burstColor); // Dibuja la barra de restante (cyan o red)
			g.fillRect(1, BARALTURA - alturaRestanteDibujada + 1, width - 1,
					alturaRestanteDibujada - 1);
		} else if(estadoActual == Estado.BLOQUEADO || estadoActual == Estado.BLOQUEADO_SUSPENDIDO){
			g.setColor(burstColor); // Dibuja la barra completa en verde
			g.drawRect(0, BARALTURA - alturaTotalDibujada, width,
					alturaTotalDibujada);
			g.fillRect(1, BARALTURA - alturaTotalDibujada + 1, width - 1,
					alturaTotalDibujada - 1);
		} else if (estadoActual == Estado.NUEVO && showHidden) {
			g.setColor(initBurstColor); // Dibuja solo el contorno gris
			g.drawRect(0, BARALTURA - alturaTotalDibujada, width,
					alturaTotalDibujada);
		}
	}

	public PCB getProceso() {
		return proceso;
	}


	public void setProceso(PCB v) {
		this.proceso = v;
	}

	public Dimension getPreferredSize() {
		return (new Dimension(PPANCHO, PPALTO));
	}

	/**
	 * Obtener el valor de showHidden.
	 * * @return valor de showHidden.
	 */
	public static boolean getShowHidden() {
		return showHidden;
	}

	/**
	 * Ajustar el valor de showHidden.
	 * * @param v
	 * Valor a asignar a showHidden.
	 */
	public static void setShowHidden(boolean v) {
		showHidden = v;
	}

}
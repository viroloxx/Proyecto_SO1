package persistencia;

import estructura_datos.Lista;
import modelo.PCB;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class PersistenciaCSV {

    /**
     * Guarda la lista de procesos terminados en un archivo CSV.
     * @param terminados La lista de PCBs en estado TERMINADO.
     * @param ruta La ruta completa del archivo donde se guardará 
     * @throws java.io.IOException
     */
    public static void guardarResultados(Lista terminados, String ruta) throws IOException {

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(ruta))) {
            
            bw.write("ID,Nombre,Tipo,Prioridad,TiempoLlegada,TiempoEjecucion,TiempoRetorno,TiempoEspera\n");
            
            PCB[] procesos = terminados.toArray();
            
            for (PCB pcb : procesos) {
                String linea = String.format("%d,%s,%s,%d,%d,%d,%d,%d\n",
                        pcb.getIdProceso(),
                        pcb.getNombre(),
                        pcb.getTipo().toString(),
                        pcb.getPrioridad(),
                        pcb.getTiempoLlegada(),
                        pcb.getTiempoEjecucion(),
                        pcb.getTiempoRetorno(),  
                        pcb.getTiempoEsperaTotal()
                );
                bw.write(linea);
            }
        } 
       
    }
}
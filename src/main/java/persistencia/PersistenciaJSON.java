package persistencia;

import estructura_datos.Lista; 
import modelo.PCB;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.FileWriter;
import java.io.IOException;

/**
 *
 * @author Diego A. Vivolo
 */
public class PersistenciaJSON {

    /**
     * Guarda la lista de procesos terminados en un archivo JSON.
     * @param terminados La lista (de tu estructura_datos) de PCBs terminados.
     * @param ruta La ruta completa del archivo donde se guardará.
     */
    public static void guardarResultados(Lista terminados, String ruta) throws IOException {
        

        PCB[] procesosArray = terminados.toArray(); 
        
        
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        
        
        try (FileWriter writer = new FileWriter(ruta)) {
            
        
            gson.toJson(procesosArray, writer);
        }
        
    }

}

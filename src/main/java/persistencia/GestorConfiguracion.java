package persistencia;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Gestor para cargar y guardar la configuración del sistema en formato JSON.
 * La configuración se guarda en el archivo "config.json" en el directorio del proyecto.
 */
public class GestorConfiguracion {
    private static final String ARCHIVO_CONFIG = "config.json";
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    /**
     * Carga la configuración del sistema desde el archivo JSON.
     * Si el archivo no existe, retorna una configuración por defecto.
     *
     * @return Configuración del sistema
     */
    public static ConfiguracionSistema cargarConfiguracion() {
        File archivo = new File(ARCHIVO_CONFIG);

        if (!archivo.exists()) {
            System.out.println("Archivo de configuración no encontrado. Usando valores por defecto.");
            return new ConfiguracionSistema(500, "FCFS", 3);
        }

        try (FileReader reader = new FileReader(archivo)) {
            ConfiguracionSistema config = gson.fromJson(reader, ConfiguracionSistema.class);
            System.out.println("Configuración cargada exitosamente desde " + ARCHIVO_CONFIG);
            return config;
        } catch (IOException e) {
            System.err.println("Error al cargar configuración: " + e.getMessage());
            return new ConfiguracionSistema(500, "FCFS", 3);
        }
    }

    /**
     * Guarda la configuración del sistema en el archivo JSON.
     *
     * @param config Configuración a guardar
     * @return true si se guardó exitosamente, false en caso contrario
     */
    public static boolean guardarConfiguracion(ConfiguracionSistema config) {
        try (FileWriter writer = new FileWriter(ARCHIVO_CONFIG)) {
            gson.toJson(config, writer);
            System.out.println("Configuración guardada exitosamente en " + ARCHIVO_CONFIG);
            return true;
        } catch (IOException e) {
            System.err.println("Error al guardar configuración: " + e.getMessage());
            return false;
        }
    }
}

package persistencia;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import modelo.PlantillaProceso;
import modelo.TipoProceso;
import sistema.SistemaOperativo;
import estructura_datos.ListaPlantillas;
import estructura_datos.ListaStrings;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Gestor para cargar y guardar configuraciones de procesos desde/hacia JSON.
 */
public class GestorCargaProcesos {

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    /**
     * Carga una configuración completa desde un archivo JSON
     *
     * @param rutaArchivo Ruta del archivo JSON
     * @return ConfiguracionCompleta con sistema y procesos, o null si hay error
     */
    public static ConfiguracionCompleta cargarConfiguracion(String rutaArchivo) {
        File archivo = new File(rutaArchivo);

        if (!archivo.exists()) {
            System.err.println("Archivo no encontrado: " + rutaArchivo);
            return null;
        }

        try (FileReader reader = new FileReader(archivo)) {
            // Gson deserializa a una clase auxiliar que usa arrays
            ConfiguracionJSON configJSON = gson.fromJson(reader, ConfiguracionJSON.class);

            // Validar que la configuración sea válida
            if (configJSON == null) {
                System.err.println("Error al parsear el archivo JSON");
                return null;
            }

            // Convertir a nuestra estructura personalizada
            ConfiguracionCompleta config = new ConfiguracionCompleta();

            // Si no hay configuración de sistema, usar valores por defecto
            if (configJSON.getConfiguracionSistema() == null) {
                config.setConfiguracionSistema(new ConfiguracionSistema(500, "FCFS", 3));
            } else {
                config.setConfiguracionSistema(configJSON.getConfiguracionSistema());
            }

            // Convertir array de procesos a ListaPlantillas
            ListaPlantillas listaProcesos = new ListaPlantillas();
            if (configJSON.getProcesos() != null) {
                for (PlantillaProceso plantilla : configJSON.getProcesos()) {
                    if (plantilla != null) {
                        listaProcesos.agregar(plantilla);
                    }
                }
            }
            config.setProcesos(listaProcesos);

            System.out.println("Configuración cargada: " + listaProcesos.obtenerTamanio() + " procesos");
            return config;

        } catch (IOException e) {
            System.err.println("Error al leer archivo: " + e.getMessage());
            return null;
        } catch (Exception e) {
            System.err.println("Error al parsear JSON: " + e.getMessage());
            return null;
        }
    }

    /**
     * Guarda una configuración completa en un archivo JSON
     *
     * @param config Configuración a guardar
     * @param rutaArchivo Ruta donde guardar
     * @return true si se guardó correctamente
     */
    public static boolean guardarConfiguracion(ConfiguracionCompleta config, String rutaArchivo) {
        try (FileWriter writer = new FileWriter(rutaArchivo)) {
            // Convertir nuestra estructura personalizada a clase auxiliar para Gson
            ConfiguracionJSON configJSON = new ConfiguracionJSON();
            configJSON.setConfiguracionSistema(config.getConfiguracionSistema());

            // Convertir ListaPlantillas a array
            if (config.getProcesos() != null) {
                configJSON.setProcesos(config.getProcesos().toArray());
            }

            gson.toJson(configJSON, writer);
            System.out.println("Configuración guardada en: " + rutaArchivo);
            return true;
        } catch (IOException e) {
            System.err.println("Error al guardar configuración: " + e.getMessage());
            return false;
        }
    }

    /**
     * Carga los procesos de una configuración en el sistema operativo
     *
     * @param config Configuración con los procesos
     * @param sistema Sistema operativo donde cargar los procesos
     * @return Número de procesos cargados correctamente
     */
    public static int cargarProcesosEnSistema(ConfiguracionCompleta config, SistemaOperativo sistema) {
        if (config == null || config.getProcesos() == null) {
            return 0;
        }

        int procesosValidos = 0;
        ListaStrings errores = new ListaStrings();

        // Convertir ListaPlantillas a array para iterar
        PlantillaProceso[] plantillas = config.getProcesos().toArray();

        for (PlantillaProceso plantilla : plantillas) {
            if (plantilla == null) continue;

            // Validar plantilla
            if (!plantilla.esValido()) {
                errores.agregar("Proceso inválido: " + plantilla.getNombre());
                continue;
            }

            try {
                // Crear proceso en el sistema con tiempo de llegada especificado
                if (plantilla.getTipoProceso() == TipoProceso.IO_BOUND) {
                    // Proceso I/O Bound con parámetros personalizados
                    sistema.agregarProcesoConLlegada(
                        plantilla.getNombre(),
                        plantilla.getTipoProceso(),
                        plantilla.getNumeroInstrucciones(),
                        plantilla.getPrioridad(),
                        plantilla.getTiempoLlegada(),
                        plantilla.getCiclosParaExcepcion(),
                        plantilla.getCiclosParaSatisfacerExcepcion()
                    );
                } else {
                    // Proceso CPU Bound
                    sistema.agregarProcesoConLlegada(
                        plantilla.getNombre(),
                        plantilla.getTipoProceso(),
                        plantilla.getNumeroInstrucciones(),
                        plantilla.getPrioridad(),
                        plantilla.getTiempoLlegada()
                    );
                }

                procesosValidos++;

            } catch (Exception e) {
                errores.agregar("Error al crear proceso " + plantilla.getNombre() + ": " + e.getMessage());
            }
        }

        // Mostrar errores si los hay
        if (!errores.estaVacia()) {
            System.err.println("Errores al cargar procesos:");
            String[] arrayErrores = errores.toArray();
            for (String error : arrayErrores) {
                System.err.println("  - " + error);
            }
        }

        return procesosValidos;
    }

    /**
     * Crea un archivo de ejemplo con procesos predefinidos
     *
     * @param rutaArchivo Ruta donde crear el archivo
     * @return true si se creó correctamente
     */
    public static boolean crearArchivoEjemplo(String rutaArchivo) {
        // Crear configuración de sistema
        ConfiguracionSistema configSistema = new ConfiguracionSistema(300, "FCFS", 3);

        // Crear lista de procesos de ejemplo
        ListaPlantillas procesos = new ListaPlantillas();

        // Proceso 1: CPU Bound simple
        procesos.agregar(new PlantillaProceso(
            "Calc_Matematico",
            "CPU_BOUND",
            20,  // instrucciones
            5,   // prioridad
            0,   // tiempo llegada
            0,   // ciclos para excepción (no aplica en CPU_BOUND)
            0    // ciclos para satisfacer (no aplica en CPU_BOUND)
        ));

        // Proceso 2: I/O Bound con E/S frecuente
        procesos.agregar(new PlantillaProceso(
            "Lectura_Disco",
            "IO_BOUND",
            30,  // instrucciones
            3,   // prioridad
            5,   // tiempo llegada
            4,   // genera E/S cada 4 ciclos
            6    // E/S dura 6 ciclos
        ));

        // Proceso 3: I/O Bound con E/S poco frecuente
        procesos.agregar(new PlantillaProceso(
            "Red_Download",
            "IO_BOUND",
            40,  // instrucciones
            7,   // prioridad
            10,  // tiempo llegada
            10,  // genera E/S cada 10 ciclos
            15   // E/S dura 15 ciclos
        ));

        // Proceso 4: CPU Bound de alta prioridad
        procesos.agregar(new PlantillaProceso(
            "Sistema_Critico",
            "CPU_BOUND",
            15,  // instrucciones
            1,   // prioridad alta
            2,   // tiempo llegada
            0,
            0
        ));

        ConfiguracionCompleta config = new ConfiguracionCompleta(configSistema, procesos);

        return guardarConfiguracion(config, rutaArchivo);
    }
}

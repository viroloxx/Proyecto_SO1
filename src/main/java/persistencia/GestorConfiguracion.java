package persistencia;

public class GestorConfiguracion {
    public static ConfiguracionSistema cargarConfiguracion() {
        return new ConfiguracionSistema(500, "FCFS", 3);
    }
    
    public static void guardarConfiguracion(ConfiguracionSistema config) {
        System.out.println("Configuración guardada: " + config.getPlanificadorInicial());
    }
}

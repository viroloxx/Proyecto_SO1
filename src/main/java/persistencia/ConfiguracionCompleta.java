package persistencia;

import modelo.PlantillaProceso;
import estructura_datos.ListaPlantillas;

/**
 * Clase contenedora para la configuración completa del sistema.
 * Incluye tanto la configuración del sistema como la lista de procesos a cargar.
 */
public class ConfiguracionCompleta {

    private ConfiguracionSistema configuracionSistema;
    private ListaPlantillas procesos;

    /**
     * Constructor vacío para Gson
     */
    public ConfiguracionCompleta() {
        this.procesos = new ListaPlantillas();
    }

    /**
     * Constructor completo
     */
    public ConfiguracionCompleta(ConfiguracionSistema configuracionSistema,
                                ListaPlantillas procesos) {
        this.configuracionSistema = configuracionSistema;
        this.procesos = procesos;
    }

    // Getters y Setters

    public ConfiguracionSistema getConfiguracionSistema() {
        return configuracionSistema;
    }

    public void setConfiguracionSistema(ConfiguracionSistema configuracionSistema) {
        this.configuracionSistema = configuracionSistema;
    }

    public ListaPlantillas getProcesos() {
        return procesos;
    }

    public void setProcesos(ListaPlantillas procesos) {
        this.procesos = procesos;
    }
}

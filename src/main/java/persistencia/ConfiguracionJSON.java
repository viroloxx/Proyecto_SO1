package persistencia;

import modelo.PlantillaProceso;

/**
 * Clase auxiliar para deserialización JSON con Gson.
 * Usa arrays en lugar de estructuras personalizadas porque Gson los puede manejar.
 */
class ConfiguracionJSON {
    private ConfiguracionSistema configuracionSistema;
    private PlantillaProceso[] procesos;

    public ConfiguracionJSON() {
    }

    public ConfiguracionSistema getConfiguracionSistema() {
        return configuracionSistema;
    }

    public void setConfiguracionSistema(ConfiguracionSistema configuracionSistema) {
        this.configuracionSistema = configuracionSistema;
    }

    public PlantillaProceso[] getProcesos() {
        return procesos;
    }

    public void setProcesos(PlantillaProceso[] procesos) {
        this.procesos = procesos;
    }
}

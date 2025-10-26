# Guía de Configuración del Simulador de Planificación de Procesos

## Configuración de Procesos I/O Bound

### Especificación de Parámetros de E/S

Cuando se crea un proceso de tipo **I/O Bound**, ahora es posible especificar:

1. **Ciclos para generar E/S**: Cada cuántos ciclos de ejecución el proceso generará una excepción de E/S
2. **Duración de E/S**: Cuántos ciclos tarda en completarse la operación de E/S

#### Cómo Agregar Procesos I/O Bound

1. Haz clic en el botón **"Agregar Proceso(s)"**
2. Selecciona **"I/O Bound"** en el campo Tipo
3. Los siguientes campos se habilitarán automáticamente:
   - **Ciclos para E/S**: Frecuencia de las interrupciones (valor por defecto: 5)
   - **Duración E/S (ciclos)**: Tiempo de bloqueo (valor por defecto: 8)

**Ejemplo**: Si configuras un proceso con:
- Ciclos para E/S = 5
- Duración E/S = 8

El proceso ejecutará 5 ciclos en CPU, luego se bloqueará por 8 ciclos mientras espera que se complete la E/S, y luego volverá a la cola de listos.

---

## Configuración de Duración del Ciclo

### Modificar Durante la Ejecución

La duración de cada ciclo del sistema puede modificarse **en cualquier momento**:

1. En el panel de control, localiza "Velocidad (ms/ciclo)"
2. Haz clic en el botón **"Cambiar"**
3. Ingresa el nuevo valor en milisegundos (0 = velocidad máxima)
4. La configuración se guardará automáticamente

### Persistencia de la Configuración

La configuración del sistema se guarda automáticamente en el archivo **`config.json`** en el directorio raíz del proyecto.

#### Estructura del archivo config.json

```json
{
  "duracionCicloMs": 500,
  "planificadorInicial": "FCFS",
  "quantumRR": 3
}
```

**Parámetros**:
- `duracionCicloMs`: Duración de cada ciclo en milisegundos (100-1000 recomendado)
- `planificadorInicial`: Algoritmo de planificación inicial ("FCFS", "SJF", "SRTF", "Prioridad NP", "Prioridad P", "Round Robin", "Multilevel FB Queue")
- `quantumRR`: Quantum utilizado por Round Robin (número de ciclos)

### Carga Automática al Inicio

Al iniciar la aplicación:
1. El sistema busca el archivo `config.json`
2. Si existe, carga la configuración guardada
3. Si no existe, usa valores por defecto y crea el archivo al primer cambio

---

## Exportación de Resultados

### CSV con Parámetros de I/O

Los archivos CSV exportados ahora incluyen los parámetros de E/S de cada proceso:

**Columnas del CSV**:
```
ID, Nombre, Tipo, Prioridad, TiempoLlegada, TiempoEjecucion,
TiempoRetorno, TiempoEspera, CiclosParaExcepcion, CiclosParaSatisfacerExcepcion
```

Para procesos **CPU Bound**, las columnas de E/S tendrán valor 0.

### JSON

La exportación JSON incluye toda la información del PCB, incluyendo los parámetros de E/S.

---

## Visualización de Métricas

El proyecto incluye un **gráfico de métricas vs tiempo** que muestra:

- **Utilización CPU (%)**: Porcentaje de tiempo que el CPU está ocupado
- **Throughput (x100)**: Procesos completados por ciclo (multiplicado por 100 para visualización)
- **Tiempo Espera Promedio**: Tiempo promedio que los procesos pasan en la cola de listos
- **Tiempo Respuesta Promedio**: Tiempo desde llegada hasta primera ejecución

### Acceder al Gráfico

1. Ejecuta la simulación desde la pestaña **"Simulación"**
2. Cambia a la pestaña **"Gráfico de Métricas"**
3. Usa el selector para filtrar las métricas visibles

El gráfico se actualiza en tiempo real durante la ejecución.

---

## Ejemplos de Uso

### Ejemplo 1: Proceso CPU Bound Simple
- Nombre: ProcCPU
- Tipo: CPU Bound
- Instrucciones: 20 ciclos
- Prioridad: 5

### Ejemplo 2: Proceso I/O Bound con E/S Frecuente
- Nombre: ProcIO
- Tipo: I/O Bound
- Instrucciones: 30 ciclos
- Prioridad: 3
- Ciclos para E/S: 3 (genera E/S cada 3 ciclos)
- Duración E/S: 5 ciclos (bloqueado por 5 ciclos)

### Ejemplo 3: Proceso I/O Bound con E/S Poco Frecuente
- Nombre: ProcIO_Largo
- Tipo: I/O Bound
- Instrucciones: 50 ciclos
- Prioridad: 7
- Ciclos para E/S: 10 (genera E/S cada 10 ciclos)
- Duración E/S: 15 ciclos (bloqueado por 15 ciclos)

---

## Notas Técnicas

1. **Persistencia**: El archivo `config.json` se crea en el directorio raíz del proyecto
2. **Formato**: Se usa Gson con pretty-printing para JSON legible
3. **Thread-safe**: Los cambios de configuración son seguros durante la ejecución
4. **Validación**: Los valores ingresados se validan antes de aplicarse
5. **Historial**: El gráfico mantiene los últimos 100 puntos de datos para optimizar rendimiento

# Arquitectura de Planificación Multinivel

## Resumen Ejecutivo

El simulador implementa una **arquitectura de planificación multinivel robusta** que modela fielmente el comportamiento de un sistema operativo real. El sistema incorpora tres niveles de planificación y gestión automática de memoria con estados suspendidos.

---

## 1. Estados de Procesos

### Diagrama de Transición de Estados

```
     ┌─────────┐
     │  NUEVO  │ (Cola de Nuevos)
     └────┬────┘
          │ Admisión (LP + Carga en Memoria)
          ↓
     ┌─────────┐ ←──────────────────┐
     │  LISTO  │ (Cola de Listos)   │
     └────┬────┘                     │
          │ Dispatch (CP)            │
          ↓                          │
   ┌────────────┐                    │
   │ EJECUCION  │                    │
   └─┬────┬─────┘                    │
     │    │                          │
     │    └─→ E/S ──→ ┌───────────┐ │
     │              │ BLOQUEADO  │ │
     │              └─────┬──────┘ │
     │                    │ E/S completada
     │                    └──────────┘
     │
     │ Terminado
     ↓
┌───────────┐
│ TERMINADO │
└───────────┘

Estado Suspendido (Swapping - Mediano Plazo):
┌──────────────────┐     ┌─────────────────────────┐
│ SUSPENDIDO_LISTO │ ←→  │ SUSPENDIDO_BLOQUEADO    │
└──────────────────┘     └─────────────────────────┘
        ↕                             ↕
     LISTO                       BLOQUEADO
```

### Estados Implementados

**Archivo**: `modelo/EstadoProceso.java`

```java
public enum EstadoProceso {
    NUEVO,                  // Proceso creado, esperando admisión
    LISTO,                  // En memoria, listo para ejecutar
    EJECUCION,             // Ejecutando en CPU
    BLOQUEADO,             // Esperando E/S
    SUSPENDIDO_LISTO,      // Fuera de memoria, listo
    SUSPENDIDO_BLOQUEADO,  // Fuera de memoria, bloqueado
    TERMINADO              // Completado
}
```

---

## 2. Planificación a Largo Plazo (Long-Term Scheduler)

### Responsabilidades

El **Planificador a Largo Plazo** controla la admisión de procesos al sistema:

- Decide **qué procesos** de la cola de nuevos entran al sistema
- Decide **cuándo** admitir nuevos procesos
- **Gestiona la carga en memoria** de procesos admitidos
- Mantiene el **grado de multiprogramación** óptimo

### Implementación

**Archivo**: `sistema/SistemaOperativo.java:58-76`

```java
private void admitirProcesosConMemoria(int cicloActual) {
    PCB[] nuevos = adminProcesos.getColaNuevos().toArray();

    for (PCB proceso : nuevos) {
        if (proceso.getTiempoLlegada() <= cicloActual) {
            // Intentar cargar el proceso en memoria
            if (adminMemoria.cargarProceso(proceso)) {
                // Si hay espacio, admitir el proceso
                adminProcesos.admitirProcesosNuevos(cicloActual);
                agregarLog("Proceso admitido y cargado en memoria");
            } else {
                // Si no hay espacio, el proceso permanece esperando
                agregarLog("Proceso esperando espacio en memoria");
            }
        }
    }
}
```

### Características

✅ **Control de Admisión**: Solo admite procesos si hay espacio en memoria
✅ **Tiempo de Llegada**: Respeta el tiempo de llegada configurado
✅ **Logging**: Registra admisiones y rechazos
✅ **Thread-Safe**: Sincronizado para concurrencia

---

## 3. Planificación a Mediano Plazo (Medium-Term Scheduler / Swapper)

### Responsabilidades

El **Planificador a Mediano Plazo** gestiona el **swapping** (intercambio) de procesos:

- **Suspende** procesos cuando la memoria está llena (> 80% de uso)
- **Reactiva** procesos suspendidos cuando hay espacio disponible (< 70% de uso)
- Libera recursos de memoria principal mediante **suspensión**
- Prioriza suspender procesos **BLOQUEADOS** sobre **LISTOS**

### Implementación

**Archivo**: `sistema/PlanificadorMedianoPlazo.java`

#### Algoritmo de Suspensión

```java
private String intentarSuspenderProcesos() {
    // Verificar si necesitamos suspender
    if (porcentajeUso < 80.0 && procesosEnMemoria < capacidadMaxima) {
        return ""; // No es necesario
    }

    // 1. PRIORIDAD: Suspender procesos BLOQUEADOS
    PCB[] bloqueados = colaBloqueados.toArray();
    if (bloqueados.length > 0) {
        PCB candidato = encontrarMenorPrioridad(bloqueados);
        adminMemoria.liberarProceso(candidato);
        adminProcesos.suspenderProceso(candidato);
        return "Suspendido proceso bloqueado para liberar memoria";
    }

    // 2. Si no hay bloqueados, suspender procesos LISTOS
    PCB[] listos = colaListos.toArray();
    if (listos.length > 2) { // Mantener al menos 2 listos
        PCB candidato = encontrarMenorPrioridad(listos);
        adminMemoria.liberarProceso(candidato);
        adminProcesos.suspenderProceso(candidato);
        return "Suspendido proceso listo para liberar memoria";
    }
}
```

#### Algoritmo de Reactivación

```java
private String intentarReactivarProcesos() {
    // Verificar si hay espacio disponible
    if (porcentajeUso >= 70.0 || !adminMemoria.hayEspacioDisponible()) {
        return ""; // No hay espacio suficiente
    }

    // 1. PRIORIDAD: Reactivar SUSPENDIDOS-LISTOS
    PCB[] suspListos = colaSuspendidosListos.toArray();
    if (suspListos.length > 0) {
        PCB candidato = encontrarMayorPrioridad(suspListos);
        if (adminMemoria.cargarProceso(candidato)) {
            adminProcesos.reactivarSuspendido(candidato);
            return "Reactivado proceso suspendido-listo";
        }
    }

    // 2. Si no hay susp-listos, reactivar SUSPENDIDOS-BLOQUEADOS
    PCB[] suspBloq = colaSuspendidosBloqueados.toArray();
    if (suspBloq.length > 0) {
        PCB candidato = encontrarMayorPrioridad(suspBloq);
        if (adminMemoria.cargarProceso(candidato)) {
            adminProcesos.reactivarSuspendido(candidato);
            return "Reactivado proceso suspendido-bloqueado";
        }
    }
}
```

### Características

✅ **Umbrales Inteligentes**:
- Suspende cuando memoria > 80%
- Reactiva cuando memoria < 70%
- Evita oscilaciones (thrashing)

✅ **Priorización**:
- Suspende primero bloqueados (no están usando CPU)
- Reactiva primero listos (pueden ejecutar inmediatamente)
- Respeta prioridades de procesos

✅ **Protección**:
- Mantiene mínimo 2 procesos listos
- Espera ciclos entre suspensiones/reactivaciones
- Evita suspensiones/reactivaciones excesivas

---

## 4. Planificación a Corto Plazo (Short-Term Scheduler / CPU Scheduler)

### Responsabilidades

El **Planificador a Corto Plazo** selecciona el proceso que ejecutará en el CPU:

- Ejecuta en **cada ciclo** del sistema
- Selecciona el **siguiente proceso** de la cola de listos
- Implementa el **algoritmo de planificación** elegido
- Gestiona **expropiación** (preemption) si es aplicable

### Algoritmos Implementados

**Directorio**: `planificacion/`

1. **FCFS** (First Come First Served) - No expropiativo
2. **SJF** (Shortest Job First) - No expropiativo
3. **SRTF** (Shortest Remaining Time First) - Expropiativo
4. **Prioridad** - Variantes expropiativas y no expropiativas
5. **Round Robin** - Expropiativo con quantum configurable
6. **Multilevel Feedback Queue** - Expropiativo multinivel

### Implementación

**Archivo**: `sistema/SistemaOperativo.java:108-158`

```java
// 6. Planificador a Corto Plazo: Ejecución de CPU
PCB procesoEnCPU = cpu.getProcesoActual();

// 6a. ¿Hay que expropiar el proceso actual?
if (procesoEnCPU != null && planificadorActual.esExpropriativo()) {
    boolean expropiar = planificadorActual.debeExpropriar(
        procesoEnCPU,
        adminProcesos.getColaListos(),
        cpu.getCiclosProcesoActual()
    );

    if (expropiar) {
        agregarLog("Planificador expropia proceso");
        PCB p = cpu.liberarProceso();
        p.setEstado(EstadoProceso.LISTO);
        adminProcesos.getColaListos().encolar(p);
        procesoEnCPU = null;
    }
}

// 6b. Si CPU está libre, planificar
if (procesoEnCPU == null) {
    procesoEnCPU = planificadorActual.seleccionarSiguienteProceso(
        adminProcesos.getColaListos()
    );
    if (procesoEnCPU != null) {
        cpu.cargarProceso(procesoEnCPU);
        agregarLog("CPU carga proceso");
        procesoEnCPU.registrarInicioPrimeraEjecucion(ciclo);
    }
}

// 6c. Ejecutar ciclo de CPU
if (procesoEnCPU != null) {
    boolean generoExcepcion = cpu.ejecutarCiclo(ciclo);
    // ... manejo de terminación y E/S
}
```

---

## 5. Gestión de Memoria

### Simulación de Memoria Principal

**Archivo**: `memoria/AdministradorMemoria.java`

#### Características

- **Capacidad Limitada**: Máximo de procesos simultáneos configurables
- **Tamaño Total**: Memoria total en unidades arbitrarias
- **Cálculo Dinámico**: Tamaño del proceso = 100 + (instrucciones × 10)
- **Tracking**: Mapeo ID proceso → tamaño en memoria

#### Métodos Principales

```java
public boolean cargarProceso(PCB proceso) {
    int tamanioProceso = calcularTamanioProceso(proceso);

    if (procesosEnMemoria.size() < capacidadMaxima &&
        (memoriaUsada + tamanioProceso) <= memoriaTotal) {

        procesosEnMemoria.put(proceso.getIdProceso(), tamanioProceso);
        memoriaUsada += tamanioProceso;
        return true;
    }
    return false;
}

public void liberarProceso(PCB proceso) {
    Integer tamanio = procesosEnMemoria.remove(proceso.getIdProceso());
    if (tamanio != null) {
        memoriaUsada -= tamanio;
    }
}
```

### Visualización

**Archivo**: `gui/PanelMemoria.java`

- **Barra de Progreso**: Visualización del uso de memoria (0-100%)
- **Estadísticas**: Procesos en memoria, memoria usada, porcentaje
- **Códigos de Color**:
  - 🟢 Verde: 0-60% (Normal)
  - 🟡 Amarillo: 60-80% (Moderado)
  - 🟠 Naranja: 80-90% (Alto)
  - 🔴 Rojo: 90-100% (Crítico)

---

## 6. Colas del Sistema

### Clasificación de Colas

**Archivo**: `sistema/AdministradorProcesos.java`

#### Cola a Largo Plazo
```java
private Cola colaNuevos;  // Procesos esperando admisión
```

#### Colas a Corto Plazo
```java
private Cola colaListos;      // Procesos en memoria, listos
private Cola colaBloqueados;  // Procesos esperando E/S
```

#### Colas a Mediano Plazo (Swapping)
```java
private Cola colaSuspendidosListos;     // Suspendidos, listos
private Cola colaSuspendidosBloqueados; // Suspendidos, bloqueados
```

#### Lista Final
```java
private Lista listaTerminados;  // Procesos completados
```

### Métodos de Gestión de Suspensión

```java
public synchronized void suspenderProceso(PCB proceso) {
    if (proceso.getEstado() == EstadoProceso.LISTO) {
        colaListos.eliminarPorId(proceso.getIdProceso());
        proceso.setEstado(EstadoProceso.SUSPENDIDO_LISTO);
        colaSuspendidosListos.encolar(proceso);
    } else if (proceso.getEstado() == EstadoProceso.BLOQUEADO) {
        colaBloqueados.eliminarPorId(proceso.getIdProceso());
        proceso.setEstado(EstadoProceso.SUSPENDIDO_BLOQUEADO);
        colaSuspendidosBloqueados.encolar(proceso);
    }
}

public synchronized void reactivarSuspendido(PCB proceso) {
    if (proceso.getEstado() == EstadoProceso.SUSPENDIDO_LISTO) {
        colaSuspendidosListos.eliminarPorId(proceso.getIdProceso());
        proceso.setEstado(EstadoProceso.LISTO);
        colaListos.encolar(proceso);
    } else if (proceso.getEstado() == EstadoProceso.SUSPENDIDO_BLOQUEADO) {
        colaSuspendidosBloqueados.eliminarPorId(proceso.getIdProceso());
        proceso.setEstado(EstadoProceso.BLOQUEADO);
        colaBloqueados.encolar(proceso);
    }
}
```

---

## 7. Flujo del Ciclo Principal

**Archivo**: `sistema/SistemaOperativo.java:run()`

### Secuencia de Ejecución por Ciclo

```
1. ⏰ Esperar ciclo del reloj
2. 🎯 Planificador a Largo Plazo
   └─ Admitir procesos nuevos con verificación de memoria

3. 💾 Planificador a Mediano Plazo
   ├─ Suspender procesos si memoria > 80%
   └─ Reactivar procesos si memoria < 70%

4. 📥 Procesar Excepciones de E/S
   └─ Desbloquear procesos con E/S completada

5. ⏱️ Actualizar Tiempos de Espera
   └─ Incrementar tiempo de espera en procesos LISTOS

6. 🖥️ Planificador a Corto Plazo
   ├─ Verificar expropiación (si aplicable)
   ├─ Seleccionar siguiente proceso
   └─ Ejecutar ciclo de CPU

7. 📊 Registrar Métricas
   └─ Actualizar historial de rendimiento
```

---

## 8. Escenarios Complejos Soportados

### Escenario 1: Sobrecarga de Memoria

```
Situación: 15 procesos llegan simultáneamente
Capacidad: 10 procesos máximo en memoria

Comportamiento:
1. Planificador LP admite primeros 10 procesos
2. Restantes 5 esperan en cola de nuevos
3. Cuando memoria > 80%, Planificador MP suspende bloqueados
4. Procesos suspendidos liberan memoria
5. Planificador LP admite procesos pendientes
6. Cuando memoria < 70%, Planificador MP reactiva suspendidos
```

### Escenario 2: Proceso I/O Intensivo

```
Situación: Proceso genera E/S cada 3 ciclos, duración 8 ciclos
Estado de Memoria: 85% usado

Comportamiento:
1. Proceso ejecuta 3 ciclos en CPU
2. Genera excepción E/S → BLOQUEADO
3. Planificador MP detecta memoria alta + proceso bloqueado
4. BLOQUEADO → SUSPENDIDO_BLOQUEADO
5. Memoria liberada permite admitir nuevos procesos
6. E/S se completa → SUSPENDIDO_BLOQUEADO (aún fuera de memoria)
7. Cuando memoria < 70%, MP reactiva
8. SUSPENDIDO_BLOQUEADO → BLOQUEADO → LISTO
```

### Escenario 3: Mix de Procesos CPU y I/O Bound

```
Sistema balanceado:
- 40% CPU-Bound: Raramente bloquean, candidatos a suspensión
- 60% I/O-Bound: Bloquean frecuentemente, suspendidos primero

Resultado:
✅ CPU-Bound mantienen CPU ocupado
✅ I/O-Bound suspendidos mientras esperan E/S
✅ Uso óptimo de memoria y CPU
✅ Multiprogramación efectiva
```

---

## 9. Métricas y Monitoreo

### Panel de Memoria

**Visualización en Tiempo Real**:
- Procesos activos en memoria
- Memoria usada / Memoria total
- Porcentaje de utilización
- Alertas visuales por nivel de uso

### Log de Eventos

El sistema registra todos los eventos de planificación:

```
[Ciclo 0012] Proceso Proc_1(ID:1) admitido y cargado en memoria
[Ciclo 0023] Suspendido proceso bloqueado Proc_3(ID:3) para liberar memoria
[Ciclo 0045] Reactivado proceso suspendido-listo Proc_3(ID:3)
[Ciclo 0067] Proceso Proc_1(ID:1) ha terminado.
```

---

## 10. Ventajas de la Arquitectura

### Realismo

✅ **Modelo Fiel**: Replica comportamiento de SO reales (Unix, Linux, Windows)
✅ **Estados Completos**: 7 estados implementados (incluye suspendidos)
✅ **Swapping**: Simulación de intercambio memoria-disco

### Robustez

✅ **Manejo de Sobrecarga**: Sistema no colapsa con muchos procesos
✅ **Thread-Safe**: Sincronización en operaciones críticas
✅ **Protección**: Umbrales evitan thrashing

### Flexibilidad

✅ **Configurable**: Capacidad de memoria ajustable
✅ **Múltiples Algoritmos**: 6 algoritmos de planificación
✅ **Visualización**: Paneles dedicados para cada componente

### Educativo

✅ **Transparencia**: Logs detallados de cada decisión
✅ **Visualización**: Estado de colas y memoria en tiempo real
✅ **Métricas**: Rendimiento medible y graficable

---

## 11. Configuración del Sistema

### Parámetros de Memoria

**Archivo**: `sistema/SistemaOperativo.java:37-38`

```java
// Valores configurables
this.adminMemoria = new AdministradorMemoria(
    10,    // Capacidad: máximo 10 procesos en memoria
    5000   // Memoria total: 5000 unidades
);
```

### Ajuste de Umbrales

**Archivo**: `sistema/PlanificadorMedianoPlazo.java:25-26`

```java
// Umbrales de suspensión/reactivación
this.umbralSuspension = 5;     // Ciclos entre suspensiones
this.umbralReactivacion = 3;   // Ciclos entre reactivaciones
```

---

## 12. Conclusión

El simulador implementa una **arquitectura de planificación multinivel robusta y completa** que incluye:

1. ✅ **Planificador a Largo Plazo** con control de admisión basado en memoria
2. ✅ **Planificador a Mediano Plazo** con swapping inteligente
3. ✅ **Planificador a Corto Plazo** con 6 algoritmos implementados
4. ✅ **Estados Suspendidos** completamente funcionales
5. ✅ **Gestión de Memoria** simulada con umbrales inteligentes
6. ✅ **Visualización Completa** de todos los componentes

El sistema es capaz de modelar **escenarios complejos** que incluyen sobrecarga de memoria, procesos I/O intensivos, y mezclas de diferentes tipos de procesos, manteniendo un comportamiento estable y eficiente mediante la gestión automática de suspensiones y reactivaciones.

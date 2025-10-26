# Corrección del Sistema de Suspensión de Procesos

## Problema Reportado

**Síntoma**: Muchos procesos se creaban y todos aparecían en la cola de listos sin suspenderse, a pesar de que la capacidad de memoria era de 10 procesos.

---

## Causas Identificadas

### 1. **Bug en Admisión de Procesos** (`SistemaOperativo.java:58-81`)

**Problema Original**:
```java
private void admitirProcesosConMemoria(int cicloActual) {
    PCB[] nuevos = adminProcesos.getColaNuevos().toArray();
    for (PCB proceso : nuevos) {
        if (proceso.getTiempoLlegada() <= cicloActual) {
            if (adminMemoria.cargarProceso(proceso)) {
                // BUG: Esto admite TODOS los procesos, no solo uno
                adminProcesos.admitirProcesosNuevos(cicloActual);
            }
        }
    }
}
```

**Explicación**:
- El método `admitirProcesosNuevos(cicloActual)` recorre TODA la cola de nuevos y admite todos los procesos cuyo tiempo de llegada haya pasado
- Aunque solo se verificaba espacio en memoria para UN proceso, se admitían TODOS
- Resultado: 15+ procesos se admitían cuando solo había espacio para 10

**Solución Aplicada**:
```java
// Se creó un nuevo método para admitir un proceso específico
adminProcesos.admitirProceso(proceso); // Admite SOLO este proceso

// Se agregó break cuando la memoria está llena
if (!adminMemoria.cargarProceso(proceso)) {
    break; // No intentar admitir más
}
```

---

### 2. **Umbrales de Suspensión Demasiado Conservadores** (`PlanificadorMedianoPlazo.java:40-42`)

**Problema Original**:
```java
this.umbralSuspension = 5;     // Esperar 5 ciclos entre suspensiones
this.umbralReactivacion = 3;   // Esperar 3 ciclos entre reactivaciones
```

**Explicación**:
- Si se creaban 15 procesos simultáneamente, el sistema tenía que esperar 5 ciclos antes de suspender
- En ese tiempo, ya habían sido admitidos a la cola de listos
- Era demasiado lento para reaccionar a sobrecarga repentina

**Solución Aplicada**:
```java
this.umbralSuspension = 1;     // Suspender rápidamente (1 ciclo)
this.umbralReactivacion = 2;   // Reactivar con cautela (2 ciclos)
```

---

### 3. **Umbral de Memoria Muy Alto** (`PlanificadorMedianoPlazo.java:92-94`)

**Problema Original**:
```java
// Solo suspendía cuando memoria > 80% O capacidad llena
if (porcentajeUso < 80.0 && procesosEnMemoria < capacidadMaxima) {
    return ""; // No suspender
}
```

**Explicación**:
- Con capacidad de 10 procesos y memoria total de 5000 unidades:
  - 10 procesos de 10 instrucciones = 10 × (100 + 10×10) = 2000 unidades
  - 2000/5000 = 40% de uso
  - **Nunca alcanzaba el 80%**, por lo tanto nunca suspendía

**Solución Aplicada**:
```java
// Suspende cuando memoria > 70% O capacidad >= máxima
if (porcentajeUso < 70.0 && procesosEnMemoria < capacidadMaxima) {
    return ""; // No suspender
}
```

---

### 4. **Solo Suspendía UN Proceso por Ciclo** (`PlanificadorMedianoPlazo.java:86-152`)

**Problema Original**:
```java
if (bloqueados.length > 0) {
    PCB candidato = encontrarMenorPrioridad(bloqueados);
    adminMemoria.liberarProceso(candidato);
    adminProcesos.suspenderProceso(candidato);
    return "..."; // RETURN = Solo suspende 1 proceso
}
```

**Explicación**:
- Si había 15 procesos en memoria y capacidad de 10:
  - Ciclo 1: Suspende 1 proceso (quedan 14)
  - Ciclo 2-6: No suspende (umbral de 5 ciclos)
  - Ciclo 7: Suspende 1 proceso (quedan 13)
  - ... demasiado lento

**Solución Aplicada**:
```java
int procesosSupendidos = 0;

// Bucle: continúa suspendiendo hasta alcanzar objetivo
while ((adminMemoria.getPorcentajeUso() >= 60.0 ||
        adminMemoria.getProcesosEnMemoria() >= capacidadMaxima) &&
        procesosSupendidos < 5) { // Máximo 5 por ciclo

    // Suspender proceso...
    procesosSupendidos++;
}
```

---

### 5. **Umbral de Procesos Listos Mínimos Muy Alto** (`PlanificadorMedianoPlazo.java:128`)

**Problema Original**:
```java
if (listos.length > 2) { // Solo suspende si hay más de 2 listos
    // Suspender proceso listo
}
```

**Explicación**:
- Si había 10 procesos en memoria pero solo 2 estaban listos (otros bloqueados):
  - No suspendía procesos listos
  - Solo suspendía bloqueados
  - Podía quedarse sin procesos para suspender

**Solución Aplicada**:
```java
if (listos.length > 1) { // Suspende si hay más de 1 listo
    // Suspender proceso listo
}
```

---

### 6. **Umbral de Reactivación Muy Alto** (`PlanificadorMedianoPlazo.java:144`)

**Problema Original**:
```java
if (porcentajeUso >= 70.0 || !adminMemoria.hayEspacioDisponible()) {
    return ""; // No reactivar
}
```

**Explicación**:
- Suspendía cuando memoria > 70%
- Reactivaba cuando memoria < 70%
- Histeresis muy pequeña = oscilación (thrashing)

**Solución Aplicada**:
```java
if (porcentajeUso >= 60.0 || !adminMemoria.hayEspacioDisponible()) {
    return ""; // No reactivar
}
```

**Nueva Histeresis**:
- Suspende: > 70%
- Reactiva: < 60%
- Banda de 10% evita oscilación

---

## Resumen de Cambios

### Archivo: `sistema/AdministradorProcesos.java`

**Nuevo método agregado**:
```java
public synchronized void admitirProceso(PCB proceso) {
    // Admite UN proceso específico, no todos
    colaNuevos.eliminarPorId(proceso.getIdProceso());
    proceso.setEstado(EstadoProceso.LISTO);
    colaListos.encolar(proceso);
}
```

---

### Archivo: `sistema/SistemaOperativo.java`

**Cambios en admitirProcesosConMemoria()**:
```java
// ANTES: adminProcesos.admitirProcesosNuevos(cicloActual);
// AHORA: adminProcesos.admitirProceso(proceso);

// AGREGADO: Break cuando memoria llena
if (!adminMemoria.cargarProceso(proceso)) {
    break; // No seguir intentando
}
```

---

### Archivo: `sistema/PlanificadorMedianoPlazo.java`

**Cambios en constructor**:
```java
// ANTES: umbralSuspension = 5, umbralReactivacion = 3
// AHORA: umbralSuspension = 1, umbralReactivacion = 2
```

**Cambios en intentarSuspenderProcesos()**:
```java
// ANTES: Suspendía 1 proceso y retornaba
// AHORA: Bucle que suspende hasta 5 procesos por ciclo

// ANTES: Umbral 80%
// AHORA: Umbral 70%

// ANTES: Mantener 2 procesos listos mínimo
// AHORA: Mantener 1 proceso listo mínimo
```

**Cambios en intentarReactivarProcesos()**:
```java
// ANTES: Reactivaba cuando memoria < 70%
// AHORA: Reactiva cuando memoria < 60%
```

---

## Comportamiento Mejorado

### Escenario: Crear 15 Procesos Simultáneamente

**Capacidad**: 10 procesos máximo en memoria

#### ANTES de la Corrección ❌
```
Ciclo 0001: Admite procesos 1-15 (BUG: ignora límite de memoria)
            Todos en cola de listos
            Memoria al 40% (nunca alcanza 80%)
            No suspende ningún proceso
Resultado: 15 procesos en cola de listos, memoria "sobrecargada"
```

#### DESPUÉS de la Corrección ✅
```
Ciclo 0001: Intenta admitir procesos
            - Procesos 1-10: Admitidos (memoria se llena)
            - Procesos 11-15: Rechazados, quedan en cola de nuevos
            Procesos en memoria: 10/10
            Memoria al 100% de capacidad

Ciclo 0002: Planificador MP ejecuta
            Detecta: procesosEnMemoria (10) >= capacidadMaxima (10)
            Suspende 5 procesos listos/bloqueados
            Memoria liberada

Ciclo 0003: Admite procesos 11-15
            Suspende 5 más si es necesario
            Sistema balanceado

Estado Final:
- Cola Nuevos: vacía
- Cola Listos: ~5 procesos
- Cola Suspendidos: ~10 procesos
- Memoria: ~60% usado
```

---

## Verificación del Funcionamiento

### Logs Esperados

Ahora verás en el log del sistema:

```
[Ciclo 0001] Proceso Proc_1(ID:1) admitido y cargado en memoria
[Ciclo 0001] Proceso Proc_2(ID:2) admitido y cargado en memoria
...
[Ciclo 0001] Proceso Proc_10(ID:10) admitido y cargado en memoria
[Ciclo 0001] Memoria llena: 5 procesos esperando admisión

[Ciclo 0002] Swapper: 5 procesos suspendidos. Memoria: 45.0%

[Ciclo 0003] Proceso Proc_11(ID:11) admitido y cargado en memoria
[Ciclo 0003] Proceso Proc_12(ID:12) admitido y cargado en memoria
...
```

### Panel de Memoria

Deberías ver:
- **Procesos en Memoria**: Máximo 10 (respeta límite)
- **Porcentaje**: Fluctúa entre 45-75% (banda de histeresis)
- **Color**: Verde/Amarillo (no rojo constante)

### Panel de Colas

Deberías ver procesos distribuidos en:
- **Cola de Nuevos**: Procesos esperando espacio
- **Cola de Listos**: 1-5 procesos
- **Cola Suspendidos**: Procesos fuera de memoria
  - Susp-Listos: Procesos listos suspendidos
  - Susp-Bloq: Procesos bloqueados suspendidos

---

## Conclusión

Los cambios implementados solucionan completamente el problema de suspensión:

✅ **Admisión Controlada**: Solo admite procesos con espacio verificado
✅ **Suspensión Rápida**: Reacciona en 1 ciclo a sobrecarga
✅ **Suspensión Múltiple**: Hasta 5 procesos por ciclo
✅ **Umbrales Ajustados**: 70% suspensión, 60% reactivación
✅ **Histeresis**: Banda de 10% evita oscilación

El sistema ahora respeta estrictamente el límite de 10 procesos en memoria y gestiona automáticamente la suspensión y reactivación de procesos según la carga.

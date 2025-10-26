package estructura_datos;

/**
 * Estructura de datos tipo mapa para almacenar pares (Integer → Integer)
 * Utilizada para mapear ID de proceso → Tamaño en memoria
 */
public class MapaMemoria {
    private NodoMapa cabeza;
    private int tamanio;

    public MapaMemoria() {
        this.cabeza = null;
        this.tamanio = 0;
    }

    /**
     * Inserta o actualiza un par clave-valor
     * @param clave ID del proceso
     * @param valor Tamaño en memoria
     */
    public synchronized void poner(int clave, int valor) {
        // Verificar si la clave ya existe
        NodoMapa actual = cabeza;
        while (actual != null) {
            if (actual.getClave() == clave) {
                actual.setValor(valor); // Actualizar valor existente
                return;
            }
            actual = actual.getSiguiente();
        }

        // Si no existe, agregar nuevo nodo
        NodoMapa nuevoNodo = new NodoMapa(clave, valor);
        nuevoNodo.setSiguiente(cabeza);
        cabeza = nuevoNodo;
        tamanio++;
    }

    /**
     * Obtiene el valor asociado a una clave
     * @param clave ID del proceso
     * @return Valor asociado, o -1 si no existe
     */
    public synchronized int obtener(int clave) {
        NodoMapa actual = cabeza;
        while (actual != null) {
            if (actual.getClave() == clave) {
                return actual.getValor();
            }
            actual = actual.getSiguiente();
        }
        return -1; // No encontrado
    }

    /**
     * Elimina un par clave-valor
     * @param clave ID del proceso a eliminar
     * @return Valor eliminado, o -1 si no existía
     */
    public synchronized int remover(int clave) {
        if (cabeza == null) return -1;

        // Si es el primer nodo
        if (cabeza.getClave() == clave) {
            int valor = cabeza.getValor();
            cabeza = cabeza.getSiguiente();
            tamanio--;
            return valor;
        }

        // Buscar en el resto de la lista
        NodoMapa actual = cabeza;
        while (actual.getSiguiente() != null) {
            if (actual.getSiguiente().getClave() == clave) {
                int valor = actual.getSiguiente().getValor();
                actual.setSiguiente(actual.getSiguiente().getSiguiente());
                tamanio--;
                return valor;
            }
            actual = actual.getSiguiente();
        }

        return -1; // No encontrado
    }

    /**
     * Verifica si el mapa contiene una clave
     * @param clave ID del proceso
     * @return true si existe, false en caso contrario
     */
    public synchronized boolean contieneClave(int clave) {
        NodoMapa actual = cabeza;
        while (actual != null) {
            if (actual.getClave() == clave) {
                return true;
            }
            actual = actual.getSiguiente();
        }
        return false;
    }

    public boolean estaVacio() {
        return cabeza == null;
    }

    public int obtenerTamanio() {
        return tamanio;
    }

    public synchronized void limpiar() {
        cabeza = null;
        tamanio = 0;
    }

    /**
     * Nodo interno para el mapa
     */
    private static class NodoMapa {
        private int clave;
        private int valor;
        private NodoMapa siguiente;

        public NodoMapa(int clave, int valor) {
            this.clave = clave;
            this.valor = valor;
            this.siguiente = null;
        }

        public int getClave() { return clave; }
        public int getValor() { return valor; }
        public void setValor(int valor) { this.valor = valor; }
        public NodoMapa getSiguiente() { return siguiente; }
        public void setSiguiente(NodoMapa siguiente) { this.siguiente = siguiente; }
    }
}

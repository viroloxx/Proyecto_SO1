package estructura_datos;

/**
 * Lista enlazada simple para almacenar valores enteros
 */
public class ListaEnteros {
    private NodoEntero cabeza;
    private int tamanio;

    public ListaEnteros() {
        this.cabeza = null;
        this.tamanio = 0;
    }

    public synchronized void agregar(int valor) {
        NodoEntero nuevoNodo = new NodoEntero(valor);
        if (cabeza == null) {
            cabeza = nuevoNodo;
        } else {
            NodoEntero actual = cabeza;
            while (actual.getSiguiente() != null) {
                actual = actual.getSiguiente();
            }
            actual.setSiguiente(nuevoNodo);
        }
        tamanio++;
    }

    public synchronized int obtener(int indice) {
        if (indice < 0 || indice >= tamanio) {
            throw new IndexOutOfBoundsException("Índice fuera de rango: " + indice);
        }
        NodoEntero actual = cabeza;
        for (int i = 0; i < indice; i++) {
            actual = actual.getSiguiente();
        }
        return actual.getValor();
    }

    public boolean estaVacia() {
        return cabeza == null;
    }

    public int obtenerTamanio() {
        return tamanio;
    }

    public synchronized int[] toArray() {
        if (estaVacia()) return new int[0];
        int[] arreglo = new int[tamanio];
        NodoEntero actual = cabeza;
        int i = 0;
        while (actual != null) {
            arreglo[i++] = actual.getValor();
            actual = actual.getSiguiente();
        }
        return arreglo;
    }

    public synchronized void limpiar() {
        cabeza = null;
        tamanio = 0;
    }

    /**
     * Nodo interno para la lista de enteros
     */
    private static class NodoEntero {
        private int valor;
        private NodoEntero siguiente;

        public NodoEntero(int valor) {
            this.valor = valor;
            this.siguiente = null;
        }

        public int getValor() { return valor; }
        public NodoEntero getSiguiente() { return siguiente; }
        public void setSiguiente(NodoEntero siguiente) { this.siguiente = siguiente; }
    }
}

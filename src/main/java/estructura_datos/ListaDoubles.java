package estructura_datos;

/**
 * Lista enlazada simple para almacenar valores double
 */
public class ListaDoubles {
    private NodoDouble cabeza;
    private int tamanio;

    public ListaDoubles() {
        this.cabeza = null;
        this.tamanio = 0;
    }

    public synchronized void agregar(double valor) {
        NodoDouble nuevoNodo = new NodoDouble(valor);
        if (cabeza == null) {
            cabeza = nuevoNodo;
        } else {
            NodoDouble actual = cabeza;
            while (actual.getSiguiente() != null) {
                actual = actual.getSiguiente();
            }
            actual.setSiguiente(nuevoNodo);
        }
        tamanio++;
    }

    public synchronized double obtener(int indice) {
        if (indice < 0 || indice >= tamanio) {
            throw new IndexOutOfBoundsException("Índice fuera de rango: " + indice);
        }
        NodoDouble actual = cabeza;
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

    public synchronized double[] toArray() {
        if (estaVacia()) return new double[0];
        double[] arreglo = new double[tamanio];
        NodoDouble actual = cabeza;
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
     * Nodo interno para la lista de doubles
     */
    private static class NodoDouble {
        private double valor;
        private NodoDouble siguiente;

        public NodoDouble(double valor) {
            this.valor = valor;
            this.siguiente = null;
        }

        public double getValor() { return valor; }
        public NodoDouble getSiguiente() { return siguiente; }
        public void setSiguiente(NodoDouble siguiente) { this.siguiente = siguiente; }
    }
}

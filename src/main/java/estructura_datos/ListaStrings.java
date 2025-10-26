package estructura_datos;

/**
 * Lista enlazada simple para almacenar cadenas de texto
 */
public class ListaStrings {
    private NodoString cabeza;
    private int tamanio;

    public ListaStrings() {
        this.cabeza = null;
        this.tamanio = 0;
    }

    public synchronized void agregar(String valor) {
        NodoString nuevoNodo = new NodoString(valor);
        if (cabeza == null) {
            cabeza = nuevoNodo;
        } else {
            NodoString actual = cabeza;
            while (actual.getSiguiente() != null) {
                actual = actual.getSiguiente();
            }
            actual.setSiguiente(nuevoNodo);
        }
        tamanio++;
    }

    public synchronized String obtener(int indice) {
        if (indice < 0 || indice >= tamanio) {
            throw new IndexOutOfBoundsException("Índice fuera de rango: " + indice);
        }
        NodoString actual = cabeza;
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

    public synchronized String[] toArray() {
        if (estaVacia()) return new String[0];
        String[] arreglo = new String[tamanio];
        NodoString actual = cabeza;
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
     * Nodo interno para la lista de strings
     */
    private static class NodoString {
        private String valor;
        private NodoString siguiente;

        public NodoString(String valor) {
            this.valor = valor;
            this.siguiente = null;
        }

        public String getValor() { return valor; }
        public NodoString getSiguiente() { return siguiente; }
        public void setSiguiente(NodoString siguiente) { this.siguiente = siguiente; }
    }
}

package estructura_datos;

import modelo.PlantillaProceso;

/**
 * Lista enlazada simple para almacenar PlantillaProceso
 */
public class ListaPlantillas {
    private NodoPlantilla cabeza;
    private int tamanio;

    public ListaPlantillas() {
        this.cabeza = null;
        this.tamanio = 0;
    }

    public synchronized void agregar(PlantillaProceso plantilla) {
        NodoPlantilla nuevoNodo = new NodoPlantilla(plantilla);
        if (cabeza == null) {
            cabeza = nuevoNodo;
        } else {
            NodoPlantilla actual = cabeza;
            while (actual.getSiguiente() != null) {
                actual = actual.getSiguiente();
            }
            actual.setSiguiente(nuevoNodo);
        }
        tamanio++;
    }

    public synchronized PlantillaProceso obtener(int indice) {
        if (indice < 0 || indice >= tamanio) {
            throw new IndexOutOfBoundsException("Índice fuera de rango: " + indice);
        }
        NodoPlantilla actual = cabeza;
        for (int i = 0; i < indice; i++) {
            actual = actual.getSiguiente();
        }
        return actual.getPlantilla();
    }

    public boolean estaVacia() {
        return cabeza == null;
    }

    public int obtenerTamanio() {
        return tamanio;
    }

    public synchronized PlantillaProceso[] toArray() {
        if (estaVacia()) return new PlantillaProceso[0];
        PlantillaProceso[] arreglo = new PlantillaProceso[tamanio];
        NodoPlantilla actual = cabeza;
        int i = 0;
        while (actual != null) {
            arreglo[i++] = actual.getPlantilla();
            actual = actual.getSiguiente();
        }
        return arreglo;
    }

    public synchronized void limpiar() {
        cabeza = null;
        tamanio = 0;
    }

    /**
     * Nodo interno para la lista de plantillas
     */
    private static class NodoPlantilla {
        private PlantillaProceso plantilla;
        private NodoPlantilla siguiente;

        public NodoPlantilla(PlantillaProceso plantilla) {
            this.plantilla = plantilla;
            this.siguiente = null;
        }

        public PlantillaProceso getPlantilla() { return plantilla; }
        public NodoPlantilla getSiguiente() { return siguiente; }
        public void setSiguiente(NodoPlantilla siguiente) { this.siguiente = siguiente; }
    }
}

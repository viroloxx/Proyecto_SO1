package estructura_datos;
import modelo.PCB;


public class Lista {
    private Nodo cabeza;
    private int tamanio;
    
    public Lista() {
        this.cabeza = null;
        this.tamanio = 0;
    }
    
    public synchronized void agregar(PCB pcb) {
        Nodo nuevoNodo = new Nodo(pcb);
        if (cabeza == null) {
            cabeza = nuevoNodo;
        } else {
            Nodo actual = cabeza;
            while (actual.getSiguiente() != null) {
                actual = actual.getSiguiente();
            }
            actual.setSiguiente(nuevoNodo);
        }
        tamanio++;
    }
    
    public synchronized boolean eliminar(int id) {
        if (cabeza == null) return false;
        if (cabeza.getDato().getIdProceso() == id) {
            cabeza = cabeza.getSiguiente();
            tamanio--;
            return true;
        }
        Nodo actual = cabeza;
        while (actual.getSiguiente() != null) {
            if (actual.getSiguiente().getDato().getIdProceso() == id) {
                actual.setSiguiente(actual.getSiguiente().getSiguiente());
                tamanio--;
                return true;
            }
            actual = actual.getSiguiente();
        }
        return false;
    }
    
    public PCB buscar(int id) {
        Nodo actual = cabeza;
        while (actual != null) {
            if (actual.getDato().getIdProceso() == id) {
                return actual.getDato();
            }
            actual = actual.getSiguiente();
        }
        return null;
    }
    
    public boolean estaVacia() {
        return cabeza == null;
    }
    
    public int obtenerTamanio() {
        return tamanio;
    }
    
    public synchronized PCB[] toArray() {
        if (estaVacia()) return new PCB[0];
        PCB[] arreglo = new PCB[tamanio];
        Nodo actual = cabeza;
        int i = 0;
        while (actual != null) {
            arreglo[i++] = actual.getDato();
            actual = actual.getSiguiente();
        }
        return arreglo;
    }
    
    public void limpiar() {
        cabeza = null;
        tamanio = 0;
    }
    
    
}

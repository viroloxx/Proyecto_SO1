package estructura_datos;
import modelo.PCB;

public class Cola {
    private Nodo frente;
    private Nodo fin;
    private int tamanio;
    
    public Cola() {
        this.frente = null;
        this.fin = null;
        this.tamanio = 0;
    }
    
    public synchronized void encolar(PCB pcb) {
        Nodo nuevoNodo = new Nodo(pcb);
        if (estaVacia()) {
            frente = nuevoNodo;
            fin = nuevoNodo;
        } else {
            fin.setSiguiente(nuevoNodo);
            fin = nuevoNodo;
        }
        tamanio++;
    }
    
    public synchronized PCB desencolar() {
        if (estaVacia()) return null;
        PCB dato = frente.getDato();
        frente = frente.getSiguiente();
        if (frente == null) fin = null;
        tamanio--;
        return dato;
    }
    
    public PCB verFrente() {
        return estaVacia() ? null : frente.getDato();
    }
    
    public boolean estaVacia() {
        return frente == null;
    }
    
    public int obtenerTamanio() {
        return tamanio;
    }
    
    public synchronized boolean eliminarPorId(int id) {
        if (estaVacia()) return false;
        if (frente.getDato().getIdProceso() == id) {
            desencolar();
            return true;
        }
        Nodo actual = frente;
        while (actual.getSiguiente() != null) {
            if (actual.getSiguiente().getDato().getIdProceso() == id) {
                Nodo nodoAEliminar = actual.getSiguiente();
                actual.setSiguiente(nodoAEliminar.getSiguiente());
                if (nodoAEliminar == fin) fin = actual;
                tamanio--;
                return true;
            }
            actual = actual.getSiguiente();
        }
        return false;
    }
    
    public synchronized PCB[] toArray() {
        if (estaVacia()) return new PCB[0];
        PCB[] arreglo = new PCB[tamanio];
        Nodo actual = frente;
        int indice = 0;
        while (actual != null) {
            arreglo[indice++] = actual.getDato();
            actual = actual.getSiguiente();
        }
        return arreglo;
    }
    
    public synchronized void fromArray(PCB[] arreglo) {
        limpiar();
        for (PCB pcb : arreglo) {
            if (pcb != null) encolar(pcb);
        }
    }
    
    public synchronized void limpiar() {
        frente = null;
        fin = null;
        tamanio = 0;
    }
    
    @Override
    public String toString() {
        if (estaVacia()) return "[]";
        StringBuilder sb = new StringBuilder("[");
        Nodo actual = frente;
        while (actual != null) {
            sb.append(actual.getDato().getNombre());
            if (actual.getSiguiente() != null) sb.append(", ");
            actual = actual.getSiguiente();
        }
        sb.append("]");
        return sb.toString();
    }
}

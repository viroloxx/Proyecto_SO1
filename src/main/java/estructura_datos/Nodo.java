package estructura_datos;
import modelo.PCB;

public class Nodo {
    private PCB dato;
    private Nodo siguiente;
    
    public Nodo(PCB dato) {
        this.dato = dato;
        this.siguiente = null;
    }
    
    public PCB getDato() { return dato; }
    public void setDato(PCB dato) { this.dato = dato; }
    public Nodo getSiguiente() { return siguiente; }
    public void setSiguiente(Nodo siguiente) { this.siguiente = siguiente; }
}

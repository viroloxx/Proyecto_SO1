package Clases;


/**
 *
 * @author Diego A. Vivolo
 */
public class Cola<T> {

    private ListaEnlazada<T> listaInterna;

    public Cola() {
        this.listaInterna = new ListaEnlazada<>();
    }


    public void encolar(T dato) {
        this.listaInterna.agregarAlFinal(dato);
    }

    public T desencolar() {
        return this.listaInterna.eliminarDelFrente();
    }

    public T verFrente() {
        return this.listaInterna.verFrente();
    }

    public boolean estaVacia() {
        return this.listaInterna.estaVacia();
    }


    public int getTamano() {
        return this.listaInterna.getTamano();
    }

    @Override
    public String toString() {
        return "Frente -> " + listaInterna.toString() + " -> Final";
    }
}

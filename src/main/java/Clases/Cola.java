package Clases;


/**
 *
 * @author Diego A. Vivolo
 * @author Gabriel Orozco 
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
        // Modificado para mostrar mejor en la GUI (un proceso por línea)
        String s = listaInterna.toString();
        
        // Quita los corchetes
        if (s.length() > 2) {
            s = s.substring(1, s.length() - 1);
        } else {
            return ""; // Vacío
        }
        
        // Reemplaza la flecha por saltos de línea
        return s.replace(" -> ", "\n\n");
    }
    
    public void remover(T dato) {
            this.listaInterna.remover(dato);
    }

}
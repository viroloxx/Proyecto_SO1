package Clases;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author Diego A. Vivolo
 */
public class ListaEnlazada<T> {

    private class Nodo {
        public T dato;
        public Nodo siguiente;

        public Nodo(T dato) {
            this.dato = dato;
            this.siguiente = null;
        }
    }

    private Nodo cabeza;  
    private Nodo cola;   
    private int tamano;   


    public ListaEnlazada() {
        this.cabeza = null;
        this.cola = null;
        this.tamano = 0;
    }


    public boolean estaVacia() {
        return this.tamano == 0;
    }


    public int getTamano() {
        return this.tamano;
    }


    public void agregarAlFinal(T dato) {
        Nodo nuevoNodo = new Nodo(dato);
        
        if (estaVacia()) {
            this.cabeza = nuevoNodo;
            this.cola = nuevoNodo;
        } else {
            // Si no está vacía, se enlaza al final
            this.cola.siguiente = nuevoNodo;
            this.cola = nuevoNodo; // Se actualiza la referencia de la cola
        }
        this.tamano++;
    }

    public T eliminarDelFrente() {
        if (estaVacia()) {
            throw new RuntimeException("No se puede eliminar de una lista vacía.");
        }

        T datoEliminado = this.cabeza.dato;
        
        this.cabeza = this.cabeza.siguiente; // Mueve la cabeza al siguiente nodo
        this.tamano--;

        if (estaVacia()) {
            // Si la lista quedó vacía, también hay que actualizar la cola
            this.cola = null;
        }
        
        return datoEliminado;
    }


    public T verFrente() {
        if (estaVacia()) {
            throw new RuntimeException("No se puede ver el frente de una lista vacía.");
        }
        return this.cabeza.dato;
    }


    @Override
    public String toString() {
        if (estaVacia()) {
            return "[]";
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        Nodo actual = this.cabeza;
        while (actual != null) {
            sb.append(actual.dato.toString());
            if (actual.siguiente != null) {
                sb.append(" -> ");
            }
            actual = actual.siguiente;
        }
        sb.append("]");
        return sb.toString();
    }
}
package com.example.gastos;

/** Lista fija de categorías. */
public class Categories {
    public static final String[] ALL = {
            "Comida", "Transporte", "Servicios", "Ocio", "Salud", "Otros"
    };

    /** Posición de una categoría en la lista (si no existe, devuelve "Otros"). */
    public static int indexOf(String category) {
        for (int i = 0; i < ALL.length; i++) {
            if (ALL[i].equals(category)) return i;
        }
        return ALL.length - 1;
    }
}

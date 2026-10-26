package modelo;

/**
 * Representa una fila de un catálogo (tipo_documento o estado_civil).
 * Su toString devuelve el nombre para que el JComboBox muestre texto y no números.
 */
public class Catalogo {

    private final int id;
    private final String nombre;

    public Catalogo(int id, String nombre) {
        this.id = id;
        this.nombre = nombre;
    }

    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    @Override
    public String toString() {
        return nombre;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Catalogo)) {
            return false;
        }
        return ((Catalogo) o).id == id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}

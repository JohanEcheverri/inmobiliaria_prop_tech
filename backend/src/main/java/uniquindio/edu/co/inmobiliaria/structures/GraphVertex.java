package uniquindio.edu.co.inmobiliaria.structures;

import java.util.Objects;
import uniquindio.edu.co.inmobiliaria.models.enums.Zona;
import uniquindio.edu.co.inmobiliaria.models.entities.Cliente;
import uniquindio.edu.co.inmobiliaria.models.entities.Inmueble;
import uniquindio.edu.co.inmobiliaria.models.entities.Operacion;

/**
 * Representa un vértice del grafo de análisis comercial.
 */
public final class GraphVertex {

    public enum Type {
        CLIENTE,
        INMUEBLE,
        ZONA,
        OPERACION
    }

    private final Type type;
    private final String id;
    private final String label;
    private final String summary;

    private GraphVertex(Type type, String id, String label, String summary) {
        this.type = type;
        this.id = id;
        this.label = label;
        this.summary = summary;
    }

    public static GraphVertex ofCliente(Cliente cliente) {
        String nombre = cliente.getNombre() != null ? cliente.getNombre() : cliente.getId();
        return new GraphVertex(
                Type.CLIENTE,
                cliente.getId(),
                nombre,
                "Cliente " + cliente.getId() + " - " + nombre
        );
    }

    public static GraphVertex ofInmueble(Inmueble inmueble) {
        String label = inmueble.getCodigo();
        return new GraphVertex(
                Type.INMUEBLE,
                inmueble.getCodigo(),
                label,
                "Inmueble " + inmueble.getCodigo() + " (" + inmueble.getTipoInmueble() + ")"
        );
    }

    public static GraphVertex ofZona(Zona zona) {
        return new GraphVertex(
                Type.ZONA,
                zona.name(),
                zona.name(),
                "Zona " + zona.name()
        );
    }

    public static GraphVertex ofOperacion(Operacion operacion) {
        String label = operacion.getCodigo();
        return new GraphVertex(
                Type.OPERACION,
                operacion.getCodigo(),
                label,
                "Operacion " + label + " para cliente "
                        + (operacion.getCliente() != null ? operacion.getCliente().getId() : "?")
        );
    }

    public Type getType() {
        return type;
    }

    public String getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    public String getSummary() {
        return summary;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GraphVertex)) return false;
        GraphVertex that = (GraphVertex) o;
        return type == that.type && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, id);
    }

    @Override
    public String toString() {
        return type + "(" + id + "): " + label;
    }
}

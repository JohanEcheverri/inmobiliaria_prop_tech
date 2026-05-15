package uniquindio.edu.co.inmobiliaria.models.entities;
import uniquindio.edu.co.inmobiliaria.models.enums.TipoInmueble;
import uniquindio.edu.co.inmobiliaria.models.enums.Finalidad;
import uniquindio.edu.co.inmobiliaria.models.enums.Estado;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * Propiedad ofertada o gestionada por la inmobiliaria (venta o arrendamiento).
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder


public class Inmueble {

    /** Código interno o de referencia del inmueble. */
    private String codigo;
    /** Dirección física. */
    private String direccion;
    /** Ciudad. */
    private Ciudad ciudad;
    /** Barrio. */
    private Barrio barrio;
    /** Tipología (apartamento, casa, etc.). */
    private TipoInmueble tipoInmueble;
    /** Si se ofrece en venta o en arrendamiento. */
    private Finalidad finalidad;
    /** Precio de venta o canon según la finalidad. */
    private double precio;
    /** Área construida o útil según criterio de negocio. */
    private double area;
    /** Número de habitaciones. */
    private int numeroHabitaciones;
    /** Número de baños. */
    private int numeroBanios;
    /** Disponibilidad comercial del inmueble. */
    private Estado estado;
    /** Asesor responsable o de contacto. */
    private Asesor asesor;
    /**
     * Referencia a una imagen representativa; a futuro conviene usar una colección para varias imágenes.
     */
    private String imagen;

}

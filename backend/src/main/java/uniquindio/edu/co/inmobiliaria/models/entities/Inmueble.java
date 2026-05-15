package uniquindio.edu.co.inmobiliaria.models.entities;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
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
@Entity


public class Inmueble {

    /** Código interno o de referencia del inmueble. */
    @Id
    private String codigo;
    /** Dirección física. */
    private String direccion;
    /** Ciudad. */
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "nombre", column = @Column(name = "ciudad_nombre")),
            @AttributeOverride(name = "departamento", column = @Column(name = "ciudad_departamento"))
    })
    private Ciudad ciudad;
    /** Barrio. */
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "zona", column = @Column(name = "barrio_zona")),
            @AttributeOverride(name = "nombre", column = @Column(name = "barrio_nombre")),
            @AttributeOverride(name = "ciudad.nombre", column = @Column(name = "barrio_ciudad_nombre")),
            @AttributeOverride(name = "ciudad.departamento", column = @Column(name = "barrio_ciudad_departamento"))
    })
    private Barrio barrio;
    /** Tipología (apartamento, casa, etc.). */
    @Enumerated(EnumType.STRING)
    private TipoInmueble tipoInmueble;
    /** Si se ofrece en venta o en arrendamiento. */
    @Enumerated(EnumType.STRING)
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
    @Enumerated(EnumType.STRING)
    private Estado estado;
    /** Asesor responsable o de contacto. */
    @ManyToOne(fetch = FetchType.LAZY)
    private Asesor asesor;
    /**
     * Referencia a una imagen representativa; a futuro conviene usar una colección para varias imágenes.
     */
    private String imagen;

}

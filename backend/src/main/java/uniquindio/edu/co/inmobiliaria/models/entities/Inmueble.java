package uniquindio.edu.co.inmobiliaria.models.entities;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Transient;
import uniquindio.edu.co.inmobiliaria.models.converters.CiudadConverter;
import uniquindio.edu.co.inmobiliaria.models.converters.StringListJsonConverter;
import uniquindio.edu.co.inmobiliaria.models.enums.TipoInmueble;
import uniquindio.edu.co.inmobiliaria.models.enums.Finalidad;
import uniquindio.edu.co.inmobiliaria.models.enums.Estado;
import uniquindio.edu.co.inmobiliaria.models.enums.Zona;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

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
    /** Dirección o barrio principal del inmueble. */
    @Column(name = "direccion_barrio")
    private String direccionBarrio;
    /** Ciudad. */
    @Convert(converter = CiudadConverter.class)
    @Column(name = "ciudad")
    private Ciudad ciudad;
    /** Zona macro del inmueble. */
    @Enumerated(EnumType.STRING)
    private Zona zona;
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
    @Lob
    @Column(name = "imagen", columnDefinition = "LONGTEXT")
    @Convert(converter = StringListJsonConverter.class)
    @Builder.Default
    private List<String> imagen = new ArrayList<>();

    @Transient
    /**
     * Devuelve la dirección principal del inmueble (mapeada en campo {@code direccionBarrio}).
     *
     * Este método está marcado @Transient porque es una vista derivada del campo persistente
     * {@code direccionBarrio} y no crea una columna adicional en la entidad.
     */
    public String getDireccion() {
        return direccionBarrio;
    }

    /**
     * Establece la dirección principal del inmueble. Internamente actualiza el campo
     * {@code direccionBarrio} usado para persistencia.
     *
     * @param direccion texto representando la dirección o nombre de barrio
     */
    public void setDireccion(String direccion) {
        this.direccionBarrio = direccion;
    }

    @Transient
    /**
     * Construye y devuelve un objeto {@link Barrio} derivado de los campos internos
     * {@code zona}, {@code direccionBarrio} y {@code ciudad}.
     *
     * Nota: devuelve una nueva instancia en cada llamada; no está ligada al estado de
     * la entidad para persistencia directa.
     *
     * @return instancia de {@link Barrio} representando la ubicación del inmueble
     */
    public Barrio getBarrio() {
        return new Barrio(zona, direccionBarrio, ciudad);
    }

    /**
     * Actualiza los campos internos de ubicación a partir de un {@link Barrio}.
     *
     * La operación copia zona, nombre (como direcciónBarrio) y ciudad. Si se pasa
     * {@code null} no realiza cambios.
     *
     * @param barrio objeto Barrio con los datos de ubicación
     */
    public void setBarrio(Barrio barrio) {
        if (barrio == null) {
            return;
        }
        this.zona = barrio.getZona();
        this.direccionBarrio = barrio.getNombre();
        this.ciudad = barrio.getCiudad();
    }

}

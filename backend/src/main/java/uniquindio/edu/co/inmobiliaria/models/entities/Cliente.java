package uniquindio.edu.co.inmobiliaria.models.entities;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import uniquindio.edu.co.inmobiliaria.models.enums.TipoCliente;
import uniquindio.edu.co.inmobiliaria.models.enums.Zona;
import uniquindio.edu.co.inmobiliaria.models.enums.TipoInmueble;
import uniquindio.edu.co.inmobiliaria.models.enums.EstadoBusquedaCliente;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * Cliente de la inmobiliaria con preferencias de búsqueda y estado del proceso.
 */
@SuperBuilder
@Getter
@Setter
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Entity

public class Cliente extends Usuario{

    /** Indica si el cliente ya completó el formulario de preferencias en su primer inicio */
    @lombok.Builder.Default
    private Boolean primerInicioCompletado = false;

    /** Si busca comprar o arrendar. */
    @Enumerated(EnumType.STRING)
    private TipoCliente tipoCliente;
    /** Zona geográfica de interés principal. */
    @Enumerated(EnumType.STRING)
    private Zona zonaInteres;
    /** Presupuesto aproximado para la operación. */
    private Double presupuesto;
    /** Tipología de inmueble deseada. */
    @Enumerated(EnumType.STRING)
    private TipoInmueble tipoInmuebleDeseado;
    /** Cantidad de habitaciones deseadas. */
    private int numeroHabitacionesDeseadas;
    /** Etapa actual de la búsqueda o negociación. */
    @Enumerated(EnumType.STRING)
    private EstadoBusquedaCliente estadoBusqueda;

}

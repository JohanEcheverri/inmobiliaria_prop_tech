package uniquindio.edu.co.inmobiliaria.models.entities;
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

public class Cliente extends Usuario{

    /** Si busca comprar o arrendar. */
    private TipoCliente tipoCliente;
    /** Zona geográfica de interés principal. */
    private Zona zonaInteres;
    /** Presupuesto aproximado para la operación. */
    private Double presupuesto;
    /** Tipología de inmueble deseada. */
    private TipoInmueble tipoInmuebleDeseado;
    /** Cantidad de habitaciones deseadas. */
    private int numeroHabitacionesDeseadas;
    /** Etapa actual de la búsqueda o negociación. */
    private EstadoBusquedaCliente estadoBusqueda;


}

package uniquindio.edu.co.inmobiliaria.models.entities;
import lombok.*;
import lombok.experimental.SuperBuilder;
import java.time.LocalDate;

/**
 * Renovación o extensión de un arriendo previo, enlazada a la operación anterior.
 */
@SuperBuilder
@Getter
@Setter
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor

public class Renovacion extends Operacion {

    /** Código de la operación de arriendo que se renueva. */
    private String codigoOperacionAnterior;
    /** Meses agregados al contrato. */
    private int mesesAdicionales;
    /** Nueva fecha de vencimiento tras la renovación. */
    private LocalDate fechaNuevaVencimiento;

}

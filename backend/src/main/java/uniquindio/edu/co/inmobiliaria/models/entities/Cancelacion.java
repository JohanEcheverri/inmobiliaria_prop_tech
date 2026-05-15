package uniquindio.edu.co.inmobiliaria.models.entities;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * Cancelación de una operación en curso o reciente, con motivo registrado.
 */
@SuperBuilder
@Getter
@Setter
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor

public class Cancelacion extends Operacion {

    /** Texto que describe por qué se canceló la operación. */
    private String motivo;

}

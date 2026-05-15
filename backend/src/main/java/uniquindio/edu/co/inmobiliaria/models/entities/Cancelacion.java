package uniquindio.edu.co.inmobiliaria.models.entities;
import jakarta.persistence.Entity;
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
@Entity

public class Cancelacion extends Operacion {

    /** Texto que describe por qué se canceló la operación. */
    private String motivo;

}

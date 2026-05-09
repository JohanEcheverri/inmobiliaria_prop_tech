package uniquindio.edu.co.inmobiliaria.models.entities;
import lombok.*;
import lombok.experimental.SuperBuilder;
import java.time.LocalDate;

/**
 * Operación de arrendamiento con duración y fecha de vencimiento del contrato.
 */
@SuperBuilder
@Getter
@Setter
@ToString(callSuper = true) 
@NoArgsConstructor
@AllArgsConstructor

public class Arriendo extends Operacion {

    /** Duración del contrato en meses. */
    private int duracionMeses;
    /** Fecha en que vence el arriendo vigente. */
    private LocalDate fechaVencimiento;

}

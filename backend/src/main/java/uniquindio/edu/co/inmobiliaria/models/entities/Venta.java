package uniquindio.edu.co.inmobiliaria.models.entities;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * Operación de venta de un inmueble; hereda los datos comunes de {@link Operacion}.
 */
@SuperBuilder
@Getter
@Setter
@ToString(callSuper = true)

public class Venta extends Operacion {

}

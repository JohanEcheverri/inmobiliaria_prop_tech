package uniquindio.edu.co.inmobiliaria.models.entities;
import jakarta.persistence.Entity;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * Operación de venta de un inmueble; hereda los datos comunes de {@link Operacion}.
 */
@SuperBuilder
@Getter
@Setter
@ToString(callSuper = true)
@NoArgsConstructor
@Entity

public class Venta extends Operacion {

}

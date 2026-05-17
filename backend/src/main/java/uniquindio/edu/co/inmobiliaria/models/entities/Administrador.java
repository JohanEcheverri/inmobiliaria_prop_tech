package uniquindio.edu.co.inmobiliaria.models.entities;

import jakarta.persistence.Entity;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * Administrador de la plataforma que hereda los datos comunes de Usuario.
 */
@SuperBuilder
@Getter
@Setter
@ToString(callSuper = true)
@AllArgsConstructor
@Entity
public class Administrador extends Usuario {
}

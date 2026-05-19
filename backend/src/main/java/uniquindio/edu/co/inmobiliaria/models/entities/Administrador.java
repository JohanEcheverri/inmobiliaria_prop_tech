package uniquindio.edu.co.inmobiliaria.models.entities;

import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * Usuario administrador del sistema. Hereda todos sus datos de {@link Usuario}.
 */
@SuperBuilder
@Getter
@Setter
@ToString(callSuper = true)
@NoArgsConstructor
@Entity
public class Administrador extends Usuario {

    public Administrador(String nombre, String id, String email, String telefono, String contrasenia, String fotoPerfil) {
        super(nombre, id, email, telefono, contrasenia, fotoPerfil);
    }
}

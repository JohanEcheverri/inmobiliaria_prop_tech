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

    /**
     * Construye un Administrador con los datos básicos heredados de {@link Usuario}.
     *
     * @param nombre nombre completo o visible del administrador
     * @param id identificador único (p. ej. cédula o documento)
     * @param email correo electrónico
     * @param telefono número de teléfono de contacto
     * @param contrasenia contraseña (en producción debe guardarse como hash)
     * @param fotoPerfil referencia a la foto de perfil (URL o ruta)
     */
    public Administrador(String nombre, String id, String email, String telefono, String contrasenia, String fotoPerfil) {
        super(nombre, id, email, telefono, contrasenia, fotoPerfil);
    }
}

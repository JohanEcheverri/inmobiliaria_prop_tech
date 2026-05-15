package uniquindio.edu.co.inmobiliaria.models.entities;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * Datos comunes de identidad para cualquier usuario del sistema (cliente o asesor).
 */
@SuperBuilder
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor

public class Usuario {

    /** Nombre completo o visible del usuario. */
    private String nombre;

    private String id;
    /** Correo electrónico; suele usarse como identificador de acceso. */
    private String email;
    /** Número de teléfono de contacto. */
    private String telefono;
    /** Credencial de acceso; en producción debe almacenarse de forma segura (hash), no en texto plano. */
    private String password;
    /** Referencia a la imagen de perfil (URL, ruta o identificador según la implementación). */
    private String fotoPerfil;

}

package uniquindio.edu.co.inmobiliaria.models.entities;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
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
@Entity
@Inheritance(strategy = InheritanceType.JOINED)

public class Usuario {

    /** Nombre completo o visible del usuario. */
    private String nombre;

    @Id
    private String id;
    /** Correo electrónico; suele usarse como identificador de acceso. */
    @Column(unique = true)
    private String email;
    /** Número de teléfono de contacto. */
    private String telefono;
    /** Credencial de acceso; en producción debe almacenarse de forma segura (hash), no en texto plano. */
    private String password;
    /** Referencia a la imagen de perfil (URL, ruta o identificador según la implementación). */
    private String fotoPerfil;

}

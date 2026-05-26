package uniquindio.edu.co.inmobiliaria.models.entities;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import uniquindio.edu.co.inmobiliaria.models.enums.Zona;
import uniquindio.edu.co.inmobiliaria.models.enums.TipoInmueble;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * Asesor inmobiliario con zona de cobertura y especialidad en tipos de inmueble.
 */
@SuperBuilder
@Getter
@Setter
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Entity

public class Asesor extends Usuario {

    /** Zona geográfica asignada para gestión de propiedades o clientes. */
    @Enumerated(EnumType.STRING)
    public Zona zonaAsignada;
    /** Tipo de inmueble en el que el asesor tiene mayor foco. */
    @Enumerated(EnumType.STRING)
    public TipoInmueble especialidad;
    /** */
    @Builder.Default
    @Column(name = "numero_de_cierres")
    private Integer numeroDeCierres = 0;

    /**
     * Construye un Asesor inicializando datos de {@link Usuario} y atributos propios.
     *
     * @param nombre nombre completo o visible del asesor
     * @param id identificador único
     * @param email correo electrónico
     * @param telefono número de teléfono
     * @param contrasenia contraseña (almacenar como hash en producción)
     * @param fotoPerfil referencia a la imagen de perfil
     * @param zonaAsignada zona geográfica de cobertura del asesor
     * @param especialidad tipo de inmueble de mayor experiencia
     */
    public Asesor(String nombre, String id, String email, String telefono,String contrasenia, String fotoPerfil, Zona zonaAsignada, TipoInmueble especialidad) {
        super(nombre, id, email, telefono, contrasenia, fotoPerfil);
        this.zonaAsignada = zonaAsignada;
        this.especialidad = especialidad;
        this.numeroDeCierres = 0;
    }


}

package uniquindio.edu.co.inmobiliaria.models.entities;
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

public class Asesor extends Usuario {

    /** Zona geográfica asignada para gestión de propiedades o clientes. */
    public Zona zonaAsignada;
    /** Tipo de inmueble en el que el asesor tiene mayor foco. */
    public TipoInmueble especialidad;
    /** */
    public Integer numeroDeCierres;

    public Asesor(String nombre, String id, String email, String telefono,String password, String fotoPerfil, Zona zonaAsignada, TipoInmueble especialidad) {
        super(id, nombre, email, telefono, password, fotoPerfil);
        this.zonaAsignada = zonaAsignada;
        this.especialidad = especialidad;
        this.numeroDeCierres = 0; // Inicialmente sin cierres
    }


}

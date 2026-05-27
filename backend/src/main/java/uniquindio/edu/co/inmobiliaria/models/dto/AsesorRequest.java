package uniquindio.edu.co.inmobiliaria.models.dto;

import lombok.*;
import uniquindio.edu.co.inmobiliaria.models.enums.Zona;
import uniquindio.edu.co.inmobiliaria.models.enums.TipoInmueble;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
/**
 * DTO utilizado para la creación o actualización de Asesores.
 * Contiene todos los campos configurables por un administrador o el propio asesor,
 * incluyendo su credencial en texto plano (que luego es codificada por el servicio).
 */
public class AsesorRequest {
    private String id;
    private String nombre;
    private String email;
    private String telefono;
    private String password;
    private String fotoPerfil;
    private Zona zonaAsignada;
    private TipoInmueble especialidad;
}
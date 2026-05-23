package uniquindio.edu.co.inmobiliaria.models.dto;

import lombok.*;
import uniquindio.edu.co.inmobiliaria.models.enums.Zona;
import uniquindio.edu.co.inmobiliaria.models.enums.TipoInmueble;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AsesorResponse {
    private String id;
    private String nombre;
    private String email;
    private String telefono;
    private String fotoPerfil;
    private Zona zonaAsignada;
    private TipoInmueble especialidad;
    private Integer numeroDeCierres;
}
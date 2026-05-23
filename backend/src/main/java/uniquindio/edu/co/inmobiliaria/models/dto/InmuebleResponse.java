package uniquindio.edu.co.inmobiliaria.models.dto;

import uniquindio.edu.co.inmobiliaria.models.enums.Estado;
import uniquindio.edu.co.inmobiliaria.models.enums.Finalidad;
import uniquindio.edu.co.inmobiliaria.models.enums.TipoInmueble;

public record InmuebleResponse(
        String codigo,
        String direccion,
        String ciudad,
        String barrio,
        TipoInmueble tipoInmueble,
        Finalidad finalidad,
        double precio,
        double area,
        int habitaciones,
        int banos,
        String estadoInmueble,
        String disponibilidad,
        String asesorResponsable,
        String asesorId,
        String imagen,
        Estado estado
) {
}

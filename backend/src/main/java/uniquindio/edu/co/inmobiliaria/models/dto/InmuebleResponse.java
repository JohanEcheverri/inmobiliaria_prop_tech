package uniquindio.edu.co.inmobiliaria.models.dto;

import uniquindio.edu.co.inmobiliaria.models.enums.Estado;
import uniquindio.edu.co.inmobiliaria.models.enums.Finalidad;
import uniquindio.edu.co.inmobiliaria.models.enums.TipoInmueble;
import uniquindio.edu.co.inmobiliaria.models.enums.Zona;

import java.util.List;

public record InmuebleResponse(
        String codigo,
        String direccion,
        String direccionBarrio,
        String ciudad,
        String departamento,
        String barrio,
        Zona zona,
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
        List<String> imagenes,
        Estado estado
) {
}

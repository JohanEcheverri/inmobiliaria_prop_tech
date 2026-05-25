package uniquindio.edu.co.inmobiliaria.models.dto;

import java.util.List;

public record InmuebleRequest(
        String codigo,
        String direccion,
        String direccionBarrio,
        String ciudad,
        String departamento,
        String barrio,
        String zona,
        String tipoInmueble,
        String finalidad,
        Double precio,
        Double area,
        Integer habitaciones,
        Integer banos,
        String estadoInmueble,
        String disponibilidad,
        String asesorResponsable,
        String imagen,
        List<String> imagenes
) {
}

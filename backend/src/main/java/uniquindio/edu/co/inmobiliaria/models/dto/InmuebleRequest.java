package uniquindio.edu.co.inmobiliaria.models.dto;

public record InmuebleRequest(
        String codigo,
        String direccion,
        String ciudad,
        String barrio,
        String tipoInmueble,
        String finalidad,
        Double precio,
        Double area,
        Integer habitaciones,
        Integer banos,
        String estadoInmueble,
        String disponibilidad,
        String asesorResponsable,
        String imagen
) {
}

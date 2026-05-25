package uniquindio.edu.co.inmobiliaria.models.dto;

import java.time.LocalDateTime;

public record PropiedadAdquiridaResponse(
        String codigo,
        String direccion,
        String ciudad,
        String zona,
        String tipoInmueble,
        String finalidad,
        double precio,
        double area,
        int habitaciones,
        int banios,
        String asesorNombre,
        double comision,
        double valorAcordado,
        LocalDateTime fechaCompra
) {
}

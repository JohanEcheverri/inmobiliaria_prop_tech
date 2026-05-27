package uniquindio.edu.co.inmobiliaria.models.dto;

import java.time.LocalDateTime;
import java.util.List;

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
        String imagen,
        List<String> imagenes,
        double comision,
        double valorAcordado,
        LocalDateTime fechaCompra
) {
}

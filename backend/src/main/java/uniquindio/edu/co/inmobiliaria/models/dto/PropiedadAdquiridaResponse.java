package uniquindio.edu.co.inmobiliaria.models.dto;

import java.time.LocalDate;
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
        LocalDateTime fechaCompra,
        /** VENTA o ARRIENDO. */
        String tipoOperacion,
        /** Código de la operación (VENTA-... o ARRIENDO-...). */
        String operacionCodigo,
        /** Estado de la operación: COMPLETADA, EN_PROCESO, CANCELADA. */
        String operacionEstado,
        /** Meses de duración del contrato (solo arriendo). */
        Integer duracionMeses,
        /** Fecha de vencimiento del arriendo (solo arriendo). */
        LocalDate fechaVencimiento
) {
}

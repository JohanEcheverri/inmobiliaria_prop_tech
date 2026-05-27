package uniquindio.edu.co.inmobiliaria.models.dto;

import uniquindio.edu.co.inmobiliaria.models.enums.Estado;
import uniquindio.edu.co.inmobiliaria.models.dto.VentaRequest;

public record EstadoInmuebleRequest(
        Estado estado,
        VentaRequest venta,
        ArriendoRequest arriendo
) {
}

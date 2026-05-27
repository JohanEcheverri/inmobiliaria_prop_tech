package uniquindio.edu.co.inmobiliaria.models.dto;

import java.time.LocalDate;

public record ArriendoRequest(
        String inmuebleCodigo,
        String clienteId,
        String asesorId,
        double valorAcordado,
        double comision,
        int duracionMeses,
        LocalDate fechaVencimiento
) {
}

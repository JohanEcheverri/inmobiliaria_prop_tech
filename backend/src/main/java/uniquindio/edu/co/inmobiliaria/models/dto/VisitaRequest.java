package uniquindio.edu.co.inmobiliaria.models.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public record VisitaRequest(
        String clienteId,
        String inmuebleCodigo,
        String asesorId,
        LocalDate fecha,
        LocalTime hora,
        String observaciones
) {
}

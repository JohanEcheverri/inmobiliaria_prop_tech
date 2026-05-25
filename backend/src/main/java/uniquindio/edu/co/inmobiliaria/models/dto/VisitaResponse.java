package uniquindio.edu.co.inmobiliaria.models.dto;

import uniquindio.edu.co.inmobiliaria.models.enums.EstadoVisita;

import java.time.LocalDate;
import java.time.LocalTime;

public record VisitaResponse(
        String codigo,
        String clienteId,
        String clienteNombre,
        String inmuebleCodigo,
        String inmuebleDireccion,
        String asesorId,
        String asesorNombre,
        LocalDate fecha,
        LocalTime hora,
        EstadoVisita estado,
        String observaciones
) {
}

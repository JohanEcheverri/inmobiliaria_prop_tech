package uniquindio.edu.co.inmobiliaria.models.dto;

import uniquindio.edu.co.inmobiliaria.models.enums.TipoEventoHistorial;

import java.time.LocalDateTime;

public record EventoHistorialResponse(
        Long id,
        String clienteId,
        String clienteNombre,
        String inmuebleCodigo,
        String inmuebleDireccion,
        TipoEventoHistorial tipoEvento,
        LocalDateTime fechaEvento
) {
}

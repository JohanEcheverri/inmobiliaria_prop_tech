package uniquindio.edu.co.inmobiliaria.models.dto;

public record EventoHistorialRequest(
        String clienteId,
        String inmuebleCodigo,
        String tipoEvento
) {
}

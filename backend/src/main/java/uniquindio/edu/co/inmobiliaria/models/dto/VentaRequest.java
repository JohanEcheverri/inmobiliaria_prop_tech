package uniquindio.edu.co.inmobiliaria.models.dto;

public record VentaRequest(
        String inmuebleCodigo,
        String clienteId,
        String asesorId,
        double valorAcordado,
        double comision
) {
}

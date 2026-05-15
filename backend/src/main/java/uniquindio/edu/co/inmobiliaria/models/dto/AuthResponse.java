package uniquindio.edu.co.inmobiliaria.models.dto;

public record AuthResponse(
        String id,
        String nombre,
        String email,
        String telefono,
        String fotoPerfil,
        String rol
) {
}

package uniquindio.edu.co.inmobiliaria.models.dto;

public record AuthRequest(
        String identificacion,
        String email,
        String password
) {
}

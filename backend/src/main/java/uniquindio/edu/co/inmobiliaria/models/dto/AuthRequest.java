package uniquindio.edu.co.inmobiliaria.models.dto;

public record AuthRequest(
        String identificacion,
        String email,
        String password
) {

    public String credencial() {
        return email != null && !email.isBlank() ? email : identificacion;
    }
}

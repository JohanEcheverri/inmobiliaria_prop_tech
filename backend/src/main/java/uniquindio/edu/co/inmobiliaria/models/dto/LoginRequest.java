package uniquindio.edu.co.inmobiliaria.models.dto;

/**
 * DTO para la petición de inicio de sesión de cualquier usuario (Administrador, Asesor, Cliente).
 * Encapsula las credenciales de acceso básicas.
 */
public class LoginRequest {
    private String identificacion;
    private String contrasenia;

    public String getIdentificacion() { return identificacion; }
    public void setIdentificacion(String identificacion) { this.identificacion = identificacion; }

    public String getContrasenia() { return contrasenia; }
    public void setContrasenia(String contrasenia) { this.contrasenia = contrasenia; }
}
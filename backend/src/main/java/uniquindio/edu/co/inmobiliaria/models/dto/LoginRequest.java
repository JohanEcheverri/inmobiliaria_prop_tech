package uniquindio.edu.co.inmobiliaria.models.dto;

public class LoginRequest {
    private String identificacion;
    private String contrasenia;

    public String getIdentificacion() { return identificacion; }
    public void setIdentificacion(String identificacion) { this.identificacion = identificacion; }

    public String getContrasenia() { return contrasenia; }
    public void setContrasenia(String contrasenia) { this.contrasenia = contrasenia; }
}
package uniquindio.edu.co.inmobiliaria.models;

public class LoginRequest {
    private String identificacion; // Cambiado de email a identificacion
    private String password;

    // Getters y Setters
    public String getIdentificacion() { return identificacion; }
    public void setIdentificacion(String identificacion) { this.identificacion = identificacion; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
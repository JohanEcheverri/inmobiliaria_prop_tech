package uniquindio.edu.co.inmobiliaria.models.dto;
import uniquindio.edu.co.inmobiliaria.models.enums.Rol;

public record SesionDTO(
        String id,
        String nombre,
        String email,
        String rol, // "CLIENTE", "ASESOR" o "ADMINISTRADOR"
        String fotoPerfil
) {}

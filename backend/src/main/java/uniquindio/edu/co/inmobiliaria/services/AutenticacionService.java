package uniquindio.edu.co.inmobiliaria.services;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import uniquindio.edu.co.inmobiliaria.models.dto.LoginRequest;
import uniquindio.edu.co.inmobiliaria.models.dto.SesionDTO;
import uniquindio.edu.co.inmobiliaria.models.entities.*;
import uniquindio.edu.co.inmobiliaria.repositories.UsuarioRepository;

@Service
@RequiredArgsConstructor
public class AutenticacionService {

    private final UsuarioRepository usuarioRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public SesionDTO login(LoginRequest request) throws Exception {
        // 1. Buscar al usuario por id de forma global en la tabla de usuarios
        Usuario usuario = usuarioRepository.buscarPorId(request.getIdentificacion())
                .orElseThrow(() -> new Exception("Usuario no encontrado"));

        // 2. Comparar la contraseña ingresada con el hash de la base de datos usando BCrypt
        if (!passwordEncoder.matches(request.getContrasenia(), usuario.getContrasenia())) {
            throw new Exception("Contraseña incorrecta");
        }

        // 3. Determinar el rol dinámicamente según la instancia de la clase
        String rol = "USUARIO";
        if (usuario instanceof Cliente) {
            rol = "CLIENTE";
        } else if (usuario instanceof Asesor) {
            rol = "ASESOR";
        } else if (usuario instanceof Administrador) {
            rol = "ADMINISTRADOR";
        }

        // 4. Retornar el DTO con la información necesaria para la sesión en el Frontend
        return new SesionDTO(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getEmail(),
                rol,
                usuario.getFotoPerfil()
        );
    }
}
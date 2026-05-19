package uniquindio.edu.co.inmobiliaria.services;

import org.springframework.stereotype.Service;
import uniquindio.edu.co.inmobiliaria.models.dto.AuthResponse;
import uniquindio.edu.co.inmobiliaria.models.entities.Administrador;
import uniquindio.edu.co.inmobiliaria.repositories.AdministradorRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;


import java.util.Optional;
@Service
public class AdministradorService {

    private final AdministradorRepository administradorRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    // Se añadió el encoder al constructor
    public AdministradorService(AdministradorRepository administradorRepository, BCryptPasswordEncoder passwordEncoder) {
        this.administradorRepository = administradorRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Optional<AuthResponse> autenticar(String id, String password) {
        if (estaVacio(id) || estaVacio(password)) {
            return Optional.empty();
        }
        return administradorRepository.findById(id)
                .filter(administrador -> passwordCoincide(administrador.getContrasenia(), password))
                .map(this::mapearAuth);
    }

    private AuthResponse mapearAuth(Administrador administrador) {
        return new AuthResponse(
                administrador.getId(),
                administrador.getNombre(),
                administrador.getEmail(),
                administrador.getTelefono(),
                administrador.getFotoPerfil(),
                "ADMINISTRADOR"
        );
    }

    private boolean passwordCoincide(String passwordGuardada, String passwordIngresada) {
        // Validación con BCrypt obligatoria para el administrador registrado
        return passwordGuardada != null && passwordEncoder.matches(passwordIngresada, passwordGuardada);
    }

    private boolean estaVacio(String valor) {
        return valor == null || valor.isBlank();
    }
}
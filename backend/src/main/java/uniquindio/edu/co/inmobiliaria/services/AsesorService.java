package uniquindio.edu.co.inmobiliaria.services;

import org.springframework.stereotype.Service;
import uniquindio.edu.co.inmobiliaria.models.dto.AuthResponse;
import uniquindio.edu.co.inmobiliaria.models.entities.Asesor;
import uniquindio.edu.co.inmobiliaria.repositories.AsesorRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;
@Service
public class AsesorService {

    private final AsesorRepository asesorRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    // Se añadió el encoder al constructor para la inyección de Spring
    public AsesorService(AsesorRepository asesorRepository, BCryptPasswordEncoder passwordEncoder) {
        this.asesorRepository = asesorRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Optional<AuthResponse> autenticar(String id, String password) {
        if (estaVacio(id) || estaVacio(password)) {
            return Optional.empty();
        }
        // Nota: Asegúrate de que findById devuelva Optional en tu repositorio
        return asesorRepository.findById(id)
                .filter(asesor -> passwordCoincide(asesor.getContrasenia(), password))
                .map(this::mapearAuth);
    }

    private AuthResponse mapearAuth(Asesor asesor) {
        return new AuthResponse(
                asesor.getId(),
                asesor.getNombre(),
                asesor.getEmail(),
                asesor.getTelefono(),
                asesor.getFotoPerfil(),
                "ASESOR"
        );
    }

    private boolean passwordCoincide(String passwordGuardada, String passwordIngresada) {
        // Se utiliza passwordEncoder.matches para comparar texto plano vs hash
        return passwordGuardada != null && passwordEncoder.matches(passwordIngresada, passwordGuardada);
    }

    private boolean estaVacio(String valor) {
        return valor == null || valor.isBlank();
    }
}
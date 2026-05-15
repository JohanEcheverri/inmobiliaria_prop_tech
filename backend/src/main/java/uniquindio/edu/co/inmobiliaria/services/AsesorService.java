package uniquindio.edu.co.inmobiliaria.services;

import org.springframework.stereotype.Service;
import uniquindio.edu.co.inmobiliaria.models.dto.AuthResponse;
import uniquindio.edu.co.inmobiliaria.models.entities.Asesor;
import uniquindio.edu.co.inmobiliaria.repositories.AsesorRepository;

import java.util.Optional;

@Service
public class AsesorService {

    private final AsesorRepository asesorRepository;

    public AsesorService(AsesorRepository asesorRepository) {
        this.asesorRepository = asesorRepository;
    }

    public Optional<AuthResponse> autenticar(String credencial, String password) {
        if (estaVacio(credencial) || estaVacio(password)) {
            return Optional.empty();
        }
        return asesorRepository.findByEmail(credencial)
                .or(() -> Optional.ofNullable(asesorRepository.findById(credencial)))
                .filter(asesor -> passwordCoincide(asesor.getPassword(), password))
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
        return passwordGuardada != null && passwordGuardada.equals(passwordIngresada);
    }

    private boolean estaVacio(String valor) {
        return valor == null || valor.isBlank();
    }
}

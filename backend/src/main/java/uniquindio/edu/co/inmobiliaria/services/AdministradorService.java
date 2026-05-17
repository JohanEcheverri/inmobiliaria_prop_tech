package uniquindio.edu.co.inmobiliaria.services;

import org.springframework.stereotype.Service;
import uniquindio.edu.co.inmobiliaria.models.dto.AuthResponse;
import uniquindio.edu.co.inmobiliaria.models.entities.Administrador;
import uniquindio.edu.co.inmobiliaria.repositories.AdministradorRepository;

import java.util.Optional;

@Service
public class AdministradorService {

    private final AdministradorRepository administradorRepository;

    public AdministradorService(AdministradorRepository administradorRepository) {
        this.administradorRepository = administradorRepository;
    }

    public Optional<AuthResponse> autenticar(String id, String password) {
        if (estaVacio(id) || estaVacio(password)) {
            return Optional.empty();
        }
        return administradorRepository.findById(id)
                .filter(administrador -> passwordCoincide(administrador.getPassword(), password))
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
        return passwordGuardada != null && passwordGuardada.equals(passwordIngresada);
    }

    private boolean estaVacio(String valor) {
        return valor == null || valor.isBlank();
    }
}

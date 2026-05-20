package uniquindio.edu.co.inmobiliaria.services;

import org.springframework.stereotype.Service;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import uniquindio.edu.co.inmobiliaria.models.dto.AuthResponse;
import uniquindio.edu.co.inmobiliaria.models.entities.Asesor;
import uniquindio.edu.co.inmobiliaria.repositories.AsesorRepository;
import uniquindio.edu.co.inmobiliaria.structures.DynamicArrayList;

import java.util.Optional;

@Service
public class AsesorService {

    private final AsesorRepository asesorRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public AsesorService(AsesorRepository asesorRepository, BCryptPasswordEncoder passwordEncoder) {
        this.asesorRepository = asesorRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Optional<AuthResponse> autenticar(String id, String password) {
        if (estaVacio(id) || estaVacio(password)) {
            return Optional.empty();
        }
        return asesorRepository.findById(id)
                .filter(asesor -> passwordCoincide(asesor.getContrasenia(), password))
                .map(this::mapearAuth);
    }

    public Asesor registrarAsesor(Asesor asesor) {
        if (asesor == null) {
            throw new IllegalArgumentException("El asesor no puede ser nulo");
        }
        if (estaVacio(asesor.getId())) {
            throw new IllegalArgumentException("El id del asesor no puede ser vacío");
        }
        if (estaVacio(asesor.getContrasenia())) {
            throw new IllegalArgumentException("La contraseña del asesor no puede ser vacía");
        }
        asesor.setContrasenia(passwordEncoder.encode(asesor.getContrasenia()));
        asesorRepository.save(asesor);
        return asesor;
    }

    public Asesor modificarAsesor(Asesor asesorActualizado) {
        if (asesorActualizado == null) {
            throw new IllegalArgumentException("El asesor no puede ser nulo");
        }
        if (estaVacio(asesorActualizado.getId())) {
            throw new IllegalArgumentException("El id del asesor no puede ser vacío");
        }
        if (!estaVacio(asesorActualizado.getContrasenia())) {
            asesorActualizado.setContrasenia(passwordEncoder.encode(asesorActualizado.getContrasenia()));
        }
        asesorRepository.update(asesorActualizado);
        return asesorActualizado;
    }

    public Optional<Asesor> consultarAsesorPorId(String id) {
        if (estaVacio(id)) {
            return Optional.empty();
        }
        return asesorRepository.findById(id);
    }

    public DynamicArrayList<Asesor> listarAsesores() {
        return asesorRepository.findAll();
    }

    private AuthResponse mapearAuth(Asesor asesor) {
        return new AuthResponse(
                asesor.getId(),
                asesor.getNombre(),
                asesor.getEmail(),
                asesor.getTelefono(),
                asesor.getFotoPerfil(),
                "ASESOR");
    }

    private boolean passwordCoincide(String passwordGuardada, String passwordIngresada) {
        return passwordGuardada != null && passwordEncoder.matches(passwordIngresada, passwordGuardada);
    }

    private boolean estaVacio(String valor) {
        return valor == null || valor.isBlank();
    }
}
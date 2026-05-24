package uniquindio.edu.co.inmobiliaria.services;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import uniquindio.edu.co.inmobiliaria.models.dto.AsesorRequest;
import uniquindio.edu.co.inmobiliaria.models.dto.AsesorResponse;
import uniquindio.edu.co.inmobiliaria.models.dto.AuthResponse;
import uniquindio.edu.co.inmobiliaria.models.entities.Asesor;
import uniquindio.edu.co.inmobiliaria.models.enums.TipoInmueble;
import uniquindio.edu.co.inmobiliaria.models.enums.Zona;
import uniquindio.edu.co.inmobiliaria.repositories.AsesorRepository;
import uniquindio.edu.co.inmobiliaria.structures.DynamicArrayList;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class AsesorService {

    private final AsesorRepository asesorRepository;
    private final PasswordEncoder passwordEncoder;

    public AsesorService(AsesorRepository asesorRepository, PasswordEncoder passwordEncoder) {
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
        if (asesor.getNumeroDeCierres() == null) {
            asesor.setNumeroDeCierres(0);
        }
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

        Asesor asesorExistente = asesorRepository.findById(asesorActualizado.getId())
                .orElseThrow(() -> new IllegalArgumentException("No se encontró un asesor con el id: " + asesorActualizado.getId()));

        if (!estaVacio(asesorActualizado.getContrasenia())) {
            asesorActualizado.setContrasenia(passwordEncoder.encode(asesorActualizado.getContrasenia()));
        } else {
            asesorActualizado.setContrasenia(asesorExistente.getContrasenia());
        }
        if (asesorActualizado.getNumeroDeCierres() == null) {
            asesorActualizado.setNumeroDeCierres(asesorExistente.getNumeroDeCierres());
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

    public DynamicArrayList<Asesor> listarAsesoresEntidades() {
        return asesorRepository.findAll();
    }

    public List<AsesorResponse> listarAsesores() {
        List<AsesorResponse> respuesta = new ArrayList<>();
        DynamicArrayList<Asesor> asesores = asesorRepository.findAll();
        for (int i = 0; i < asesores.size(); i++) {
            respuesta.add(convertirAResponse(asesores.get(i)));
        }
        return respuesta;
    }

    public AsesorResponse obtenerAsesor(String id) {
        Asesor asesor = asesorRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró el asesor con ID: " + id));
        return convertirAResponse(asesor);
    }

    public AsesorResponse registrarAsesor(AsesorRequest request) {
        validarAsesorRequest(request, true);
        if (asesorRepository.findById(request.getId()).isPresent()) {
            throw new IllegalArgumentException("Ya existe un asesor con la identificación: " + request.getId());
        }
        if (!estaVacio(request.getEmail()) && asesorRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Ya existe un asesor con el email: " + request.getEmail());
        }

        Asesor nuevoAsesor = new Asesor(
                request.getNombre(),
                request.getId(),
                request.getEmail(),
                request.getTelefono(),
                passwordEncoder.encode(request.getPassword()),
                request.getFotoPerfil(),
                request.getZonaAsignada(),
                request.getEspecialidad()
        );
        nuevoAsesor.setNumeroDeCierres(0);

        asesorRepository.save(nuevoAsesor);
        return convertirAResponse(nuevoAsesor);
    }

    public AsesorResponse actualizarAsesor(String id, AsesorRequest request) {
        if (estaVacio(id)) {
            throw new IllegalArgumentException("El id del asesor es obligatorio");
        }
        validarAsesorRequest(request, false);

        Asesor asesor = asesorRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró el asesor con ID: " + id));

        if (!estaVacio(request.getEmail())) {
            asesorRepository.findByEmail(request.getEmail())
                    .filter(otro -> !otro.getId().equals(id))
                    .ifPresent(otro -> {
                        throw new IllegalArgumentException("Ya existe un asesor con el email: " + request.getEmail());
                    });
        }

        asesor.setNombre(request.getNombre());
        asesor.setEmail(request.getEmail());
        asesor.setTelefono(request.getTelefono());
        asesor.setZonaAsignada(request.getZonaAsignada());
        asesor.setEspecialidad(request.getEspecialidad());

        if (!estaVacio(request.getFotoPerfil())) {
            asesor.setFotoPerfil(request.getFotoPerfil());
        }
        if (!estaVacio(request.getPassword())) {
            asesor.setContrasenia(passwordEncoder.encode(request.getPassword()));
        }

        asesorRepository.update(asesor);
        return convertirAResponse(asesor);
    }

    public void eliminarAsesor(String id) {
        if (!asesorRepository.deleteById(id)) {
            throw new IllegalArgumentException("No se encontró el asesor con ID: " + id);
        }
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

    private AsesorResponse convertirAResponse(Asesor asesor) {
        return AsesorResponse.builder()
                .id(asesor.getId())
                .nombre(asesor.getNombre() != null ? asesor.getNombre() : "Sin Nombre")
                .email(asesor.getEmail())
                .telefono(asesor.getTelefono() != null ? asesor.getTelefono() : "N/A")
                .fotoPerfil(asesor.getFotoPerfil())
                .zonaAsignada(asesor.getZonaAsignada() != null ? asesor.getZonaAsignada() : Zona.CENTRO)
                .especialidad(asesor.getEspecialidad() != null ? asesor.getEspecialidad() : TipoInmueble.CASA)
                .numeroDeCierres(asesor.getNumeroDeCierres() != null ? asesor.getNumeroDeCierres() : 0)
                .build();
    }

    private void validarAsesorRequest(AsesorRequest request, boolean validarPassword) {
        if (request == null) {
            throw new IllegalArgumentException("Los datos del asesor son obligatorios");
        }
        if (estaVacio(request.getNombre())) {
            throw new IllegalArgumentException("El nombre del asesor es obligatorio");
        }
        if (estaVacio(request.getEmail())) {
            throw new IllegalArgumentException("El email del asesor es obligatorio");
        }
        if (validarPassword) {
            if (estaVacio(request.getId())) {
                throw new IllegalArgumentException("El id del asesor es obligatorio");
            }
            if (estaVacio(request.getPassword())) {
                throw new IllegalArgumentException("La contraseña del asesor es obligatoria");
            }
        }
    }

    private boolean passwordCoincide(String passwordGuardada, String passwordIngresada) {
        return passwordGuardada != null && passwordEncoder.matches(passwordIngresada, passwordGuardada);
    }

    private boolean estaVacio(String valor) {
        return valor == null || valor.isBlank();
    }
}

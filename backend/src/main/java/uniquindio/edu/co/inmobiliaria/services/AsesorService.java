package uniquindio.edu.co.inmobiliaria.services;

import org.springframework.stereotype.Service;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import uniquindio.edu.co.inmobiliaria.models.dto.AsesorRequest;
import uniquindio.edu.co.inmobiliaria.models.dto.AsesorResponse;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
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
    private final BCryptPasswordEncoder passwordEncoder;

    public AsesorService(AsesorRepository asesorRepository, BCryptPasswordEncoder passwordEncoder) {
        this.asesorRepository = asesorRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // ==========================================
    //   MÉTODOS DE AUTENTICACIÓN
    // ==========================================

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

    // ==========================================
    //    MÉTODOS CRUD - CONTROL DE ASESORES
    // ==========================================

    public List<AsesorResponse> listarAsesores() {
        List<AsesorResponse> listaResponses = new ArrayList<>();

        var asesoresLista = asesorRepository.getAsesoresPorId().values();

        for (int i = 0; i < asesoresLista.size(); i++) {
            Asesor asesor = asesoresLista.get(i);
            if (asesor != null) {
                listaResponses.add(convertirAResponse(asesor));
            }
        }

        return listaResponses;
    }

    public AsesorResponse obtenerAsesor(String id) {
        Asesor asesor = asesorRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró el asesor con ID: " + id));
        return convertirAResponse(asesor);
    }

    public AsesorResponse registrarAsesor(AsesorRequest request) {
        // Validar si ya existe previamente
        if (asesorRepository.findById(request.getId()).isPresent()) {
            throw new IllegalArgumentException("Ya existe un asesor con la identificación: " + request.getId());
        }

        String passwordEncriptada = passwordEncoder.encode(request.getPassword());

        Asesor nuevoAsesor = new Asesor(
                request.getNombre(),
                request.getId(),
                request.getEmail(),
                request.getTelefono(),
                passwordEncriptada,
                request.getFotoPerfil(),
                request.getZonaAsignada(),
                request.getEspecialidad()
        );

        // Pone por defecto 0 cierres si es nuevo
        nuevoAsesor.setNumeroDeCierres(0);

        asesorRepository.save(nuevoAsesor);
        return convertirAResponse(nuevoAsesor);
    }

    public AsesorResponse actualizarAsesor(String id, AsesorRequest request) {
        Asesor asesor = asesorRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró el asesor con ID: " + id));

        // Actualizamos los datos del objeto gestionado
        asesor.setNombre(request.getNombre());
        asesor.setEmail(request.getEmail());
        asesor.setTelefono(request.getTelefono());
        asesor.setZonaAsignada(request.getZonaAsignada());
        asesor.setEspecialidad(request.getEspecialidad());

        if (request.getFotoPerfil() != null && !request.getFotoPerfil().isBlank()) {
            asesor.setFotoPerfil(request.getFotoPerfil());
        }

        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            asesor.setContrasenia(passwordEncoder.encode(request.getPassword()));
        }

        // Persistimos los cambios concurrentemente en memoria e Hibernate MySQL
        asesorRepository.save(asesor);

        return convertirAResponse(asesor);
    }

    public void eliminarAsesor(String id) {
        asesorRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró el asesor con ID: " + id));

        // Borra de las HashTables y ejecuta el delete de CrudRepository
        asesorRepository.deleteById(id);
    }

    private AsesorResponse convertirAResponse(Asesor a) {
        return AsesorResponse.builder()
                .id(a.getId())
                .nombre(a.getNombre() != null ? a.getNombre() : "Sin Nombre")
                .email(a.getEmail())
                .telefono(a.getTelefono() != null ? a.getTelefono() : "N/A")
                .fotoPerfil(a.getFotoPerfil())

                .zonaAsignada(a.getZonaAsignada() != null ? a.getZonaAsignada() : Zona.CENTRO)
                .especialidad(a.getEspecialidad() != null ? a.getEspecialidad() : TipoInmueble.CASA)

                .numeroDeCierres(a.getNumeroDeCierres())
                .build();
    }
}
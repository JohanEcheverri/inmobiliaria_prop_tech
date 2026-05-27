package uniquindio.edu.co.inmobiliaria.services;

import org.springframework.stereotype.Service;
import uniquindio.edu.co.inmobiliaria.models.dto.AuthResponse;
import uniquindio.edu.co.inmobiliaria.models.entities.Administrador;
import uniquindio.edu.co.inmobiliaria.repositories.AdministradorRepository;
import org.springframework.security.crypto.password.PasswordEncoder;


import java.util.Map;
import java.util.Optional;
@Service
/**
 * Servicio encargado de la gestión de Administradores del sistema.
 * Proporciona métodos para la autenticación, consulta y actualización de perfiles
 * administrativos, garantizando la seguridad en el manejo de contraseñas.
 */
public class AdministradorService {

    private final AdministradorRepository administradorRepository;
    private final PasswordEncoder passwordEncoder;

    public AdministradorService(AdministradorRepository administradorRepository, PasswordEncoder passwordEncoder) {
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

    /**
     * Obtiene los datos de un administrador por su identificador.
     *
     * @param id identificador del administrador
     * @return mapa con los campos del perfil
     */
    public Map<String, Object> obtenerAdmin(String id) {
        Administrador admin = administradorRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró un administrador con el id: " + id));
        return mapearPerfil(admin);
    }

    /**
     * Actualiza parcialmente los datos de un administrador existente.
     * Solo modifica los campos que llegan con valor no vacío; la contraseña
     * solo se actualiza si se proporciona explícitamente.
     *
     * @param id identificador del administrador
     * @param datos mapa con los campos a actualizar (nombre, email, telefono, password, fotoPerfil)
     * @return mapa con los datos actualizados
     */
    public Map<String, Object> actualizarAdmin(String id, Map<String, String> datos) {
        if (estaVacio(id)) {
            throw new IllegalArgumentException("El id del administrador es obligatorio");
        }

        Administrador admin = administradorRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró un administrador con el id: " + id));

        String nuevoEmail = datos.get("email");
        if (!estaVacio(nuevoEmail)) {
            administradorRepository.findByEmail(nuevoEmail)
                    .filter(otro -> !otro.getId().equals(id))
                    .ifPresent(otro -> {
                        throw new IllegalArgumentException("Ya existe un usuario con el email: " + nuevoEmail);
                    });
        }

        Administrador actualizado = Administrador.builder()
                .id(id)
                .nombre(!estaVacio(datos.get("nombre")) ? datos.get("nombre") : admin.getNombre())
                .email(!estaVacio(nuevoEmail) ? nuevoEmail : admin.getEmail())
                .telefono(!estaVacio(datos.get("telefono")) ? datos.get("telefono") : admin.getTelefono())
                .contrasenia(!estaVacio(datos.get("password")) ? passwordEncoder.encode(datos.get("password")) : admin.getContrasenia())
                .fotoPerfil(datos.containsKey("fotoPerfil") ? datos.get("fotoPerfil") : admin.getFotoPerfil())
                .build();

        administradorRepository.update(actualizado);
        return mapearPerfil(actualizado);
    }

    private Map<String, Object> mapearPerfil(Administrador admin) {
        return Map.of(
                "id", admin.getId() != null ? admin.getId() : "",
                "nombre", admin.getNombre() != null ? admin.getNombre() : "",
                "email", admin.getEmail() != null ? admin.getEmail() : "",
                "telefono", admin.getTelefono() != null ? admin.getTelefono() : "",
                "fotoPerfil", admin.getFotoPerfil() != null ? admin.getFotoPerfil() : ""
        );
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
        return passwordGuardada != null && passwordEncoder.matches(passwordIngresada, passwordGuardada);
    }

    private boolean estaVacio(String valor) {
        return valor == null || valor.isBlank();
    }
}

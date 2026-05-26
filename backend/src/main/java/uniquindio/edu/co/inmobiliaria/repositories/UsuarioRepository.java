package uniquindio.edu.co.inmobiliaria.repositories;

import uniquindio.edu.co.inmobiliaria.models.entities.Usuario;
import uniquindio.edu.co.inmobiliaria.repositories.jpa.UsuarioJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
/**
 * Repositorio ligero que delega consultas básicas a UsuarioJpaRepository.
 * Proporciona utilidades de búsqueda por id o por identificador/email combinado.
 */
public class UsuarioRepository {

    private final UsuarioJpaRepository jpaRepository;

    public Optional<Usuario> buscarPorId(String id) {
        return jpaRepository.findById(id);
    }

    public Optional<Usuario> buscarPorIdentificacionOEmail(String identificacionOEmail) {
        if (identificacionOEmail == null || identificacionOEmail.isBlank()) {
            return Optional.empty();
        }

        String valor = identificacionOEmail.trim();
        return jpaRepository.findById(valor)
                .or(() -> jpaRepository.findByEmail(valor));
    }

}

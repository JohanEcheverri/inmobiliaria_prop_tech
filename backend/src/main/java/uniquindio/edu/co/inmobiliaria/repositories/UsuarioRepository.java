package uniquindio.edu.co.inmobiliaria.repositories;

import uniquindio.edu.co.inmobiliaria.models.entities.Usuario;
import uniquindio.edu.co.inmobiliaria.repositories.jpa.UsuarioJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UsuarioRepository {

    private final UsuarioJpaRepository jpaRepository;

    public Optional<Usuario> buscarPorId(String id) {
        return jpaRepository.findById(id);
    }

}
package uniquindio.edu.co.inmobiliaria.repositories.jpa;
import uniquindio.edu.co.inmobiliaria.models.entities.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UsuarioJpaRepository extends JpaRepository<Usuario, String> {
    Optional<Usuario> findByEmail(String email);
}

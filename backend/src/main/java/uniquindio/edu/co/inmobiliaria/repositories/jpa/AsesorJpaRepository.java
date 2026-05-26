package uniquindio.edu.co.inmobiliaria.repositories.jpa;

import org.springframework.data.repository.CrudRepository;
import uniquindio.edu.co.inmobiliaria.models.entities.Asesor;

/**
 * Repositorio JPA para Asesor.
 */
public interface AsesorJpaRepository extends CrudRepository<Asesor, String> {
}

package uniquindio.edu.co.inmobiliaria.repositories.jpa;

import org.springframework.data.repository.CrudRepository;
import uniquindio.edu.co.inmobiliaria.models.entities.Cliente;

/**
 * Repositorio JPA para Cliente. Operaciones CRUD básicas delegadas a Spring Data.
 */
public interface ClienteJpaRepository extends CrudRepository<Cliente, String> {
}

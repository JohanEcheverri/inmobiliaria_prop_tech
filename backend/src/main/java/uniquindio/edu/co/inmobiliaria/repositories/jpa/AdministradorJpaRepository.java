package uniquindio.edu.co.inmobiliaria.repositories.jpa;

import org.springframework.data.repository.CrudRepository;
import uniquindio.edu.co.inmobiliaria.models.entities.Administrador;

/**
 * Repositorio JPA para Administrador con operaciones CRUD estándar.
 */
public interface AdministradorJpaRepository extends CrudRepository<Administrador, String> {
}

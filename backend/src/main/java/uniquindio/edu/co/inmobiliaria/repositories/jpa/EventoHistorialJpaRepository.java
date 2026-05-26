package uniquindio.edu.co.inmobiliaria.repositories.jpa;

import org.springframework.data.repository.CrudRepository;
import uniquindio.edu.co.inmobiliaria.models.entities.EventoHistorial;

/**
 * Repositorio JPA para EventoHistorial (CRUD estándar).
 */
public interface EventoHistorialJpaRepository extends CrudRepository<EventoHistorial, Long> {
}

package uniquindio.edu.co.inmobiliaria.repositories.jpa;

import org.springframework.data.repository.CrudRepository;
import uniquindio.edu.co.inmobiliaria.models.entities.Contrato;

/**
 * Repositorio JPA para Contrato (CRUD estándar).
 */
public interface ContratoJpaRepository extends CrudRepository<Contrato, String> {
}

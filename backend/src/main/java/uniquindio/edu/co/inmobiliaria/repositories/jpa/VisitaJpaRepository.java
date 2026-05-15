package uniquindio.edu.co.inmobiliaria.repositories.jpa;

import org.springframework.data.repository.CrudRepository;
import uniquindio.edu.co.inmobiliaria.models.entities.Visita;

public interface VisitaJpaRepository extends CrudRepository<Visita, String> {
}

package uniquindio.edu.co.inmobiliaria.repositories.jpa;

import org.springframework.data.repository.CrudRepository;
import uniquindio.edu.co.inmobiliaria.models.entities.EventoHistorial;

public interface EventoHistorialJpaRepository extends CrudRepository<EventoHistorial, Long> {
}

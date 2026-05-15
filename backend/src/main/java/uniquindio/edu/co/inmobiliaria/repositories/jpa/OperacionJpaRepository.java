package uniquindio.edu.co.inmobiliaria.repositories.jpa;

import org.springframework.data.repository.CrudRepository;
import uniquindio.edu.co.inmobiliaria.models.entities.Operacion;

public interface OperacionJpaRepository extends CrudRepository<Operacion, String> {
}

package uniquindio.edu.co.inmobiliaria.repositories.jpa;

import org.springframework.data.repository.CrudRepository;
import uniquindio.edu.co.inmobiliaria.models.entities.Inmueble;

public interface InmuebleJpaRepository extends CrudRepository<Inmueble, String> {
}

package uniquindio.edu.co.inmobiliaria.repositories.jpa;

import org.springframework.data.repository.CrudRepository;
import uniquindio.edu.co.inmobiliaria.models.entities.Cliente;

public interface ClienteJpaRepository extends CrudRepository<Cliente, String> {
}

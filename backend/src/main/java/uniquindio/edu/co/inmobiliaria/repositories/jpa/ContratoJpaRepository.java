package uniquindio.edu.co.inmobiliaria.repositories.jpa;

import org.springframework.data.repository.CrudRepository;
import uniquindio.edu.co.inmobiliaria.models.entities.Contrato;

public interface ContratoJpaRepository extends CrudRepository<Contrato, String> {
}

package uniquindio.edu.co.inmobiliaria.repositories.jpa;

import org.springframework.data.repository.CrudRepository;
import uniquindio.edu.co.inmobiliaria.models.entities.Administrador;

public interface AdministradorJpaRepository extends CrudRepository<Administrador, String> {
}

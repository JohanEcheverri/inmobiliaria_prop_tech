package uniquindio.edu.co.inmobiliaria.repositories.jpa;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import uniquindio.edu.co.inmobiliaria.models.entities.Inmueble;
import java.util.List;

public interface InmuebleJpaRepository extends CrudRepository<Inmueble, String> {

    @Query("SELECT i FROM Inmueble i LEFT JOIN FETCH i.asesor")
    List<Inmueble> findAllConAsesor();
}

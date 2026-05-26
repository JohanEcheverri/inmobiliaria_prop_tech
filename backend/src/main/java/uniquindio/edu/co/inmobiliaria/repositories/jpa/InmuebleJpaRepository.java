package uniquindio.edu.co.inmobiliaria.repositories.jpa;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import uniquindio.edu.co.inmobiliaria.models.entities.Inmueble;
import java.util.List;

/**
 * Repositorio JPA para Inmueble. Provee un método con join-fetch para traer
 * el asesor asociado y evitar LazyInitializationException al usar la entidad
 * fuera del contexto de persistencia.
 */
public interface InmuebleJpaRepository extends CrudRepository<Inmueble, String> {

    @Query("SELECT i FROM Inmueble i LEFT JOIN FETCH i.asesor")
    List<Inmueble> findAllConAsesor();
}

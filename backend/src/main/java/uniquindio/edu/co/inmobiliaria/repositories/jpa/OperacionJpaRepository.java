package uniquindio.edu.co.inmobiliaria.repositories.jpa;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import uniquindio.edu.co.inmobiliaria.models.entities.Operacion;

import java.util.List;

/**
 * Repositorio JPA para operaciones. Contiene una query con JOIN FETCH para
 * evitar problemas de carga perezosa al traer relaciones necesarias.
 */
public interface OperacionJpaRepository extends CrudRepository<Operacion, String> {

    @Query("""
            SELECT o FROM Operacion o
            LEFT JOIN FETCH o.inmueble
            LEFT JOIN FETCH o.cliente
            LEFT JOIN FETCH o.asesor
            """)
    List<Operacion> findAllConRelaciones();
}
